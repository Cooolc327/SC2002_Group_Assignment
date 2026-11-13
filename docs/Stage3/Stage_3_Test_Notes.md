# Stage 3 reproducible checks

Verification date: 13 November 2026 (UTC+8). JDK 17 or later, no network services
or third-party Java libraries. Checks use production customers, Restaurant,
orders, staff, controller and CLI. Special domain fixtures are identified in
test comments; the default base-setting schedule is unchanged.

```bash
bash scripts/build.sh
bash scripts/test-stage1.sh
bash scripts/test-stage2.sh
bash scripts/test-stage3.sh
bash scripts/run.sh < test/replays/stage3-victory.txt
bash scripts/run.sh < test/replays/stage3-defeat.txt
```

All test scripts compile production and test sources using
`javac --release 17 -Xlint:all -Werror`. Logs are ignored under
`build/stage1-checks`, `build/stage2-checks`, and `build/stage3-checks`.

| Suite | Result |
| --- | --- |
| Stage 1 core, real Order | 14 scenarios / 344 assertions |
| Stage 1 production smoke and CLI | 9 / 94 |
| Billing, including VIP and Happy Hour | 8 / 48 |
| Base menu and Order lifecycle | 7 / 65 |
| Stage 2 integration and explicit `Main --stage2` | 14 / 506 |
| ComboMeal | 6 / 42 |
| Stage 3 integration and real default Main/CLI | 18 / 550 |

Billing runs in both Stage 2 and Stage 3 scripts; do not count it twice when
reporting unique scenario totals.

## Stored inputs and expected results

| Replay | Paid | Revenue | Paid satisfaction sum / average | Departures | Result |
| --- | --- | --- | --- | --- | --- |
| `stage3-victory.txt` | 5 | $83.70 | 436 / 87.20 | 0 | VICTORY |
| `stage3-defeat.txt` | 0 | $0.00 | 0 / N/A | 1 | DEFEAT |

Both complete all 18 turns and use seed 24369. The victory strategy activates
Happy Hour on turn 4. Host seats the first queued customer at the first free
table. Waiter prioritises serve, then take order, then seat; Chef works on the
first eligible order. The defeat strategy never activates Happy Hour and
chooses Wait for Host, Waiter and Chef throughout.

Inputs contain one integer per prompt. Before activation there is a Happy Hour
choice (0 keep unused, 1 activate); after activation this prompt disappears.
Every turn then has Host, Waiter and Chef choices, even when only Wait is
available. Existing Stage 2 input files must be run with `--stage2`.

## Victory timeline for the default mixed customers

| Customer | Item | Order | READY | SERVED | Ready to pay | Paid | Bill | Paid satisfaction |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 Regular | Salad | 1 | 1 | 2 | 3 | 4 | $9.00 | 92 |
| 2 VIP | Burger | 4 | 4 | 5 | 6 | 7 | $10.80 | 100 |
| 3 Regular | Pasta | 7 | 8 | 9 | 10 | 11 | $18.00 | 84 |
| 4 Regular | Salad + Burger Combo | 10 | 11 | 12 | 13 | 14 | $21.60 | 84 |
| 5 Regular | Salad + Pasta Combo | 13 | 15 | 16 | 17 | 18 | $24.30 | 76 |
| 6 Critic | Salad + Pasta Combo | 17 | unfinished | — | — | — | unpaid | — |

Turn 4 displays two active turns remaining; turn 5 displays one; turn 6 displays
zero and activation used. The VIP's turn-4 waiting loss is recovered at the
start of turn 5, while its $10.80 order remains locked when paid after expiry.
Customer 1's pre-existing $9.00 order is unchanged by activation.

With the same service priorities and no activation, five customers pay $86.40
at average satisfaction 86.20. Activating on turn 4 reduces revenue by $2.70
and increases average satisfaction by 1.00. Both strategies meet the goals;
this is a reproducible pricing/satisfaction trade-off.

## Feature and boundary coverage

- Host and Waiter can each seat a different customer in one turn. Invalid table
  selection retains capacity; a second successful Host action is rejected.
- Opening decisions precede staff. Invalid activation, wrong-phase actions,
  invalid Host selections and recovery retries leave snapshots unchanged.
- Recovery covers WAITING/SEATED/ORDERED, caps at 100, and excludes SERVED,
  READY_TO_PAY, PAID and LEFT. Negative recovery is rejected.
- Happy Hour covers activation and next turn only, cannot restart, and never
  changes existing order prices. Views do not consume duration.
- VIP combo pricing is checked through actual order creation and payment after
  expiry in an explicitly identified all-VIP fixture.
- PLACED, PREPARING and READY combos cancel on abandonment; tables are released
  and unpaid orders contribute no revenue.
- Three-unit combos use three separate Chef actions; same-turn serving and
  same-turn payment remain prohibited.
- Real default CLI checks cover victory, defeat, BOM input, invalid input in
  all four initial prompts, activation prompt suppression and EOF.
- Six arrivals, two tables, all 18 turns, final unfinished Critic order, and
  immutable customer/menu/Host/policy views are checked.

The optional `bash scripts/test-stage3.sh --write-replays` explicitly regenerates
the two stored input files from the documented strategy. Normal checks only
read those files and assert the fixed expected outcomes above.
