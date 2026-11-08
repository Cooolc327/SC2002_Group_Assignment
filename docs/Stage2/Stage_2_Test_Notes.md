# Stage 2 reproducible test notes

Base setting, default seed 24369. Six arrivals on turns 1/4/7/10/13/16 with
customer types Regular, VIP, Regular, Regular, Regular, Critic. All runs last
18 turns. Money is integer cents; display averages are rounded only for output.

## Commands

Requires Java 17+; no external Java libraries. From the project root:

```bash
bash scripts/build.sh
bash scripts/test-stage1.sh
bash scripts/test-stage2.sh
bash scripts/run.sh < test/replays/stage2-victory.txt
bash scripts/run.sh < test/replays/stage2-defeat.txt
bash scripts/run.sh < test/replays/stage2-vip-wait-turn5.txt
```

Each replay contains 36 zero-based menu choices: Waiter then Chef on each turn.
The Regular regression smoke invokes Main with `--regular`; the Stage 2 tests
and replay commands invoke the actual default Main with mixed customers.
The `stage-1` tag, rather than that regression flag, retains Stage 1 source.

## Known expected answers

| Run | Strategy | Paid | Revenue | Average paid satisfaction | Departures | Result |
| --- | --- | --- | --- | --- | --- | --- |
| Victory | Waiter: serve, otherwise order, otherwise seat; Chef: oldest eligible order | 5 | $64.50 | 80.40 | 0 | VICTORY |
| All-WAIT defeat | Waiter and Chef choose 0 every turn | 0 | $0.00 | N/A | 1 | DEFEAT |
| VIP delayed once | Waiter waits on turn 5, then resumes the same priority rule | 5 | $64.50 | 77.80 | 0 | VICTORY |

Normal paid satisfaction snapshots are 84, 90, 76, 76, 76 (sum 402). The bills
are 900, 1350, 1800, 900, 1500 cents (sum 6450). VIP #2 buys Burger for $13.50,
is served on turn 6, becomes ready to pay on turn 7, and pays on turn 8 with
snapshot 90. The delayed variant pays on turn 9 at 85 and has whole-game sum
389. These are independently known expected values, not copied from rounded
CLI output as test assertions.

The default late Critic #6 orders Pasta on turn 18 and completes only its first
preparation unit. It cannot pay by the deadline. A Critic-first production
fixture separately verifies its distinct Pasta choice, READY turn 3, SERVED
turn 4, ready-to-pay turn 5 and payment turn 6 at $18.00 / satisfaction 64.
Regular-only waiting loss is 8, VIP 5 and Critic 12. All-WAIT final satisfactions
are 0,25,4,28,52,64; only Regular #1 has abandoned by the deadline.

## Automated evidence

| Suite | Expected marker |
| --- | --- |
| Regular core, production Order / recording staff | MEMBER_A_CORE=PASS scenarios=14 assertions=344 orders=production |
| Regular production Order/staff/Main/CLI smoke | STAGE1_SMOKE=PASS scenarios=9 assertions=94 |
| Actual VipCustomer pricing | BILL_CALCULATOR=PASS scenarios=5 assertions=26 |
| Menu and order lifecycle | ORDER_MENU=PASS scenarios=7 assertions=65 |
| Mixed customer integration and real Main/CLI | STAGE2_INTEGRATION=PASS scenarios=14 assertions=506 |

Coverage includes seeded schedules, factory bounds and atomic failure/retry,
polymorphic losses and choices, state/turn guards, bill locking, Cashier timing,
Critic PLACED/PREPARING/READY cancellation, invalid Waiter/Chef input and BOM
input. Exact result boundaries, payment quotas/FIFO/ties, immutable snapshots
and original timing are covered by the Regular core/smoke suites.

## Current execution provenance

Candidate execution is pending in the first preparation commit. The final
[pre-tag verification record](../verification/Stage_2_Pre_Tag_2026-11-08.md)
will give the exact tested commit, environment, commands, actual logs, offline
and clean-archive checks. Agent execution is not an E signature or an unperformed
human walk-through. Actual Windows and an actual JDK 17 runtime are not claimed
when checks run on macOS/JDK 21 using `--release 17`.
