# Stage 2 local code integration

Status: Stage 2 released at annotated tag `stage-2`, 8 November 2026.
The user requested code and necessary tests first, then diagrams and release
work in separate steps. No E/human acceptance is claimed.

## Source and ownership

Base: published Stage 1 commit `ac445a8f956232232188fd765eed98a5e8b49c46`.
B source: `Xavier-Stage2`, `7d45e18943d6fc521772a11b86cdd4ed2badbcc8`.
C source: `bernice/stage-2-pricing`, `8e4f6a36c8b85f41b1b5a2f2bf747ef946f16283`.
C is already an ancestor of B; its changes are included once. B supplies customer
subclasses/factory and C supplies bill calculation and order/menu tests. A
connects them and adds integration checks. Existing Stage 1 document paths are
kept so their relative links continue to work.

## Small changes from Stage 1

| File / responsibility | Reason |
| --- | --- |
| `entities/VipCustomer.java` | B: waiting loss 5, bill discount 10% |
| `entities/CriticCustomer.java` | B: waiting loss 12, longest-preparation food choice, no discount |
| `entities/CustomerFactory.java` | B schedule; A separates IDs 1-6 from turns/indexes and rejects invalid IDs |
| `entities/Customer.java` | C default zero-discount hook; retain existing `chooseItem(Menu)` interface |
| `fooditems/MenuItem.java` | B getter used by Critic; existing `prepUnits()` remains compatible |
| `orders/BillCalculator.java` | C integer-cent pricing; A uses a long intermediate to avoid int multiplication overflow |
| `domain/Restaurant.java` | C calculates and locks the customer-specific bill at order creation |
| `control/GameController.java` | A injects `IntFunction<? extends Customer>`; validates arrival before starting a turn |
| `Main.java` | Wire the mixed factory; `--regular` enables explicit regression mode |
| `cli/RestaurantCLI.java` | Stage 2 title, setup description and behaviour help; no domain type checks |
| `test/unit/BillCalculatorTest.java` | Replace placeholder discounted customer with actual VipCustomer; check large valid amount |
| `test/integration/Stage1SmokeTest.java` | Run real Main with `--regular` so old expected outcomes remain meaningful |
| `test/integration/Stage2IntegrationTest.java` | Production customers/staff/orders, factory validation, both endings, input retries and cancellations |
| `scripts/build.sh`, `scripts/test-stage2.sh` | Accurate build marker and reproducible test command |
| `test/replays/stage2-*.txt` | Fixed 36-choice victory and all-WAIT defeat inputs |

No control-layer VIP/Critic type chain was added. The controller knows only
Customer; waiting loss, food choice and discount are dispatched through the
customer object. The factory contains the registration switch. Using the
standard Java IntFunction avoids adding an otherwise unnecessary interface.

`startTurn()` creates and validates a scheduled arrival before mutating the
restaurant turn. A bad factory cannot leave a partially opened turn or consume
the next customer ID. The original two-argument controller constructor retains
Regular-only behaviour for baseline regression fixtures. Production Main uses
the explicit mixed-customer constructor.

## Reproducibility and verified outcomes

Default seed 24369 gives Regular, VIP, Regular, Regular, Regular, Critic for IDs
1-6 arriving on turns 1, 4, 7, 10, 13, 16. Same seed, same types on each run.
Changing the constructor seed can change their positions but not the required
four Regular / one VIP / one Critic count. Critic chooses Pasta in the base menu
and the first menu item breaks equal-preparation ties.

Both complete 18-turn default CLI runs were executed locally:

- Victory: 5 paid, $64.50, paid satisfaction average 80.40, no departures.
- All-WAIT defeat: 0 paid, $0.00, average N/A, one departure.
- Actual VIP #2: Burger $13.50, served turn 6, ready-to-pay turn 7, paid turn 8,
  recorded satisfaction 90. The Regular baseline is $66.00 / 79.20; the new
  outcome reflects the VIP discount and lower waiting loss.
- Actual default Critic #6: arrives turn 16, Pasta placed turn 18, only one
  preparation unit by the deadline, not paid. This is the intended late-arrival
  timing rule, not a missing flow.
- A Critic-first integration fixture separately verifies Pasta READY turn 3,
  SERVED turn 4, ready-to-pay turn 5, paid turn 6 at $18.00 / satisfaction 64.
- Other checks: repeated seeded schedules; factory bounds/failed-arrival retry;
  waiting losses 8/5/12; served/payment immunity; Critic cancellation from each
  unfinished status; CLI invalid Waiter/Chef choices and BOM input.

Commands:

```bash
bash scripts/build.sh
bash scripts/test-stage1.sh
bash scripts/test-stage2.sh
bash scripts/run.sh < test/replays/stage2-victory.txt
bash scripts/run.sh < test/replays/stage2-defeat.txt
```

Expected markers: Regular core 14/344, Regular smoke 9/94, billing 5/26,
order/menu 7/65, Stage 2 integration 14/506. Logs are under the ignored build
folder. macOS Temurin JDK 21.0.12 was used with `--release 17 -Xlint:all -Werror`;
actual JDK 17 and Windows execution are not claimed.

## Subsequent diagram step and remaining work

The customer factory / Main / controller walkthrough has been provided. The
[Stage 2 class and VIP sequence diagrams](Diagram_Notes.md) are now drawn with
editable sources, source hashes and a separately replayed manager-choice
alternative. No production code changed during that drawing step.

The diagram/source audit has been performed and linked in Diagram_Notes.md.
[Acceptance checklist](Stage_2_Acceptance_Checklist_ZH.md),
[test notes](Stage_2_Test_Notes.md), [transition analysis](Stage_1_to_2_Changes.md)
and [full AI continuation](../../ai-history/stage-2/README.md) are now prepared.
The exact tested commit and clean-archive result are recorded separately in
[the release verification record](../verification/Stage_2_Release_2026-11-08.md).
After the earlier hold, the user authorized publication. The final annotated
tag archive is verified and branch/tag are published together.

The existing stage-1 tag and main branch are not changed. No Host, ComboMeal or
Happy Hour implementation has been added.
