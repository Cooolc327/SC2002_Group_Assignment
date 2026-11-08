# Stage 1 Reproducible Test Notes

Date: 8 November 2026. Executor: OpenAI Codex agent, requested by Lichen.
Release: `stage-1`. Exact tested commit and environment are in the
[verification record](verification/Stage_1_Release_2026-11-08.md).

## Reproduction conditions

Use JDK 17+ and Bash at the project root. The base setting is 18 turns, two
tables, one Waiter/Chef/Cashier, six Regular arrivals on 1/4/7/10/13/16.
Food is deterministic: Salad, Burger, Pasta repeated by customer id. No seed,
network service, external Java library or saved game is needed.

Each input fixture has 36 zero-based CLI choices: Waiter then Chef for every
turn. These numbers are displayed menu indexes, not customer/order identifiers.
A fresh Main process is used for each run.

```bash
bash scripts/run.sh < test/replays/stage1-victory.txt
bash scripts/run.sh < test/replays/stage1-defeat.txt
bash scripts/run.sh < test/runs/stage1-defeat.txt
```

| Scenario | Strategy/input | Expected and observed outcome |
| --- | --- | --- |
| Victory | test/replays/stage1-victory.txt; the 18-turn action table is in Member_A_Integration.md | VICTORY after 18 turns; 5 served, 5 paid, $66.00, average 79.20, 0 departures |
| All-WAIT defeat | test/replays/stage1-defeat.txt; both staff wait throughout | DEFEAT after 18 turns; 0 served/paid, $0.00, average N/A, 2 departures |
| One-customer defeat | test/runs/stage1-defeat.txt; serve/charge C1, then wait | DEFEAT after 18 turns; 1 served/paid, $9.00, average 84.00, 1 departure |

The two victory fixtures in test/replays and test/runs are identical. The two
defeat fixtures are deliberately different; compare results with the correct
input path. Victory paid-satisfaction snapshots are 84, 84, 76, 76, 76, totaling
396/5 = 79.20; paid bills total 900+1500+1800+900+1500 = 6600 cents.

Pasta is prepared on turns 8 and 9, served on 10, ready to pay on 11 and paid
on 12. Targets are already met at turn 15, but play continues through 18. C6,
who arrives on 16, remains unpaid. The all-WAIT run loses C1 on turn 13 and C2
on turn 16. These observations follow the manual's full-length/timing rules.

## Regression command

```bash
bash scripts/test-stage1.sh
```

Expected: MEMBER_A_CORE=PASS scenarios=14 assertions=344 orders=production;
STAGE1_SMOKE=PASS scenarios=9 assertions=94; STAGE1_DEVELOPMENT_CHECKS=PASS.
Core includes the real Order and recording staff; production smoke separately
uses actual staff, StaffTeam and Main. See the release record for the scope and
actual output. This evidence is agent execution, not a fabricated human sign-off.
