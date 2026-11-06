# Stage 1 Handoff from A to E

Prepared on 3 November 2026. Member A's Stage 1 integration and development
checks are complete. E's independent acceptance is pending.

This handoff reuses the team's existing customers, seating, menu, production
Order, staff, StaffTeam and CLI. A provides the controller/domain coordination,
shared contracts/views, reproducible build/run/check commands, replay fixtures
and integration notes. The game contains Stage 1 features only.

## Review update on 6 November 2026

The original handoff was `codex/stage-1-a-handoff` commit `e595fa9`.
Lichen's current personal branch is `lichen/stage_1`. It contains the integrated
review `b64651d` (including Rishika's `542a689`) plus personal AI history and
updated documentation. The production source remains the reviewed Stage 1 code.
PR #4 still points to the old handoff; it is not the current personal branch.
Follow the [manual-based checklist](Stage_1_Manual_Checklist_ZH.md) for the
requirement map, current paths and remaining acceptance work.

## Version to test

Use branch `lichen/stage_1` and record its exact SHA using `git rev-parse HEAD`.
A branch name alone is not a permanent test identity. The consolidated history
includes integrated main `705fd37`, original A work and the newer review.

On a clean checkout of the repository, after this personal branch is published:

```bash
git fetch origin
git switch lichen/stage_1
git pull --ff-only origin lichen/stage_1
git rev-parse HEAD
java -version
javac -version
bash scripts/test-stage1.sh
```

If a candidate is subsequently merged, test and record the accepted merge SHA.
No message or automated output here is E's sign-off.

## Commands and expected results

Requirements: JDK 17 or later and Bash. The game and checks work offline with
no third-party Java dependencies. Run the commands from the repository root.

| Purpose | Command | Expected result |
| --- | --- | --- |
| Compile production source | bash scripts/build.sh | BUILD_STAGE1=PASS; classes in build/classes |
| Play interactively | bash scripts/run.sh | Main launches the base-setting CLI |
| Development regression checks | bash scripts/test-stage1.sh | 14 core scenarios/344 assertions with real Order; 5 smoke scenarios/62 assertions; all PASS |
| Isolated A core baseline | bash scripts/test-member-a.sh | 14 scenarios/344 assertions; orders=fake |
| Real victory replay | bash scripts/run.sh < test/replays/stage1-victory.txt | 18 turns, VICTORY, 5 paid, $66.00, average 79.20, 0 unhappy departures |
| Real defeat replay | bash scripts/run.sh < test/replays/stage1-defeat.txt | 18 turns, DEFEAT, 0 paid, $0.00, average N/A, 2 unhappy departures |

All builds enable --release 17, -Xlint:all and -Werror. A failed development
check exits nonzero. Test classes are temporary. Developer logs are kept in:

- build/stage1-checks/core-production-order.log
- build/stage1-checks/smoke-summary.log
- build/stage1-checks/cli-victory.log
- build/stage1-checks/cli-defeat.log
- build/stage1-checks/cli-invalid-input-victory.log

Build output is Git-ignored. Preserve the logs needed for E's report separately
and associate them with the recorded commit. A's existing PASS output is
development evidence; E should rerun checks and also test independently.

## What the automated checks cover

MemberACoreTest defaults to test doubles. The --production-orders mode used by
test-stage1.sh replaces the order substitute with orders.Order. RecordingStaff
remains in that suite to verify calls and ordering.

Stage1SmokeTest separately uses real Order, real Waiter/Chef/Cashier,
StaffTeam and Main/CLI. Its five scenarios check:

1. Single-customer service/eating/payment timing, rejected-action retries and table release.
2. Actual staff per-turn allowances and failed actions preserving the allowance.
3. The complete 18-turn victory through Main/CLI.
4. The complete 18-turn all-WAIT defeat through Main/CLI.
5. Invalid text and out-of-range menu choices followed by the same victory result.

The input files contain zero-based displayed menu indexes, not customer or order
identifiers. See [replay notes](../test/replays/README.md) and the
[human-readable action table](Member_A_Integration.md#victory-replay-using-the-current-menu-rule).

## E acceptance matrix

Mark each item PASS or FAIL from E's own execution and retain observed evidence.
Development tests support these checks but do not pre-fill E's decisions.

| ID | Requirement to verify | Expected behaviour |
| --- | --- | --- |
| S1-01 | Base configuration and arrivals | 18 turns, 2 tables, one staff member per role; six arrivals on turns 1/4/7/10/13/16; satisfaction starts at 100 |
| S1-02 | Menu and customer food choice | Salad $9/1 unit, Burger $15/1, Pasta $18/2; current deterministic order repeats by customer id; player selects service priority |
| S1-03 | Turn display | Turn, revenue, average satisfaction or N/A, queue, tables, customer satisfaction, orders and relevant staff choices are readable |
| S1-04 | Waiter actions and retries | At most one accepted seating/order/serve/WAIT action per turn; rejected choices explain why and allow retry without consuming the action |
| S1-05 | Chef preparation | At most one unit per turn; may work on an order taken this turn; READY/served/cancelled/paid orders cannot be prepared again |
| S1-06 | Serving and eating delay | A dish completed on turn t is served no earlier than t+1; a served customer finishes eating on a later turn |
| S1-07 | Payment priority and delay | A new READY_TO_PAY customer cannot pay this turn; oldest eligible first, at most one payment per turn |
| S1-08 | Table lifecycle | A table stays occupied until payment or abandonment; it is released exactly when required |
| S1-09 | Waiting and cancellation | Unserved Regular customers lose 8 at turn end, minimum 0; zero means LEFT; PLACED/PREPARING/READY orders cancel and contribute no revenue |
| S1-10 | Statistics and money | Integer cents; two-decimal display; revenue and average use paid orders and payment snapshots; no paid customers means N/A |
| S1-11 | Both endings and full length | All three targets are required; reaching them early does not end play; no turn 19; late C6 cannot pay within the limit |
| S1-12 | Queries and repeated calls | Displaying snapshots does not advance time; duplicate turn finishing or duplicate payment cannot double-count |
| S1-13 | Clean run and scope | Fresh checkout compiles and launches with documented commands, mandatory play works offline, no Stage 2/3 features in the Stage 1 snapshot |

For a defect, record the ID, commit SHA, exact menu/input sequence, expected
behaviour, actual behaviour and log excerpt. Route controller/domain issues
to A and coordinate module issues with the corresponding owner.

## Acceptance record to complete

- Tested commit SHA:
- Tester:
- Date and time:
- OS and JDK:
- Automated command and exit status:
- Matrix results and independent scenarios:
- Defects, fixes and retest commit:
- Decision: pending / accepted / rejected:
- Evidence location:

E may add broader edge cases, documentation evidence and recorded demonstrations
to their testing/report work. The existing UML and design PDF are retained as
earlier proposals; synchronize reporting with the accepted implementation.

## Release gate

After E records acceptance and any fixes have been retested, create stage-1
on the accepted runnable commit. Keep that snapshot free of Stage 2/3 additions.
Neither the scripts nor this handoff create a tag or declare E acceptance.
Stage 2/3 implementation, final diagrams/report, group video, declarations and
the submission archive remain separate group deliverables.
