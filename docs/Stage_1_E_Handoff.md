# Stage 1 Verification and Review Handoff

Updated 8 November 2026. Stage 1 is released as `stage-1` from `lichen/stage_1`.
Main integration is optional and separate. The source includes main `f24c186`
and the existing A development, replay and test tools.

## Verification provenance

No completed E acceptance entry was found in the repository or PR #5 reviews/
comments at the start of this work. On 8 November the user instructed Codex to
perform the remaining tests. The current record is an **agent-run verification**,
not an E signature or independent human acceptance. The old requirement to wait
for E before creating a tag is superseded by that instruction. E may still use
the matrix below for an optional independent review.

The [release record](verification/Stage_1_Release_2026-11-08.md) identifies the
tested commit, environment, commands, actual results and limits. The tag resolves
to the release commit with `git rev-parse stage-1^{commit}`. Do not use a moving
branch name alone as permanent test identity.

## Build and reproduce

Use a clean checkout or archive of `stage-1`. JDK 17+ and Bash are sufficient;
there are no external Java libraries or network services.

```bash
git fetch origin --tags
git switch --detach stage-1
git rev-parse HEAD
java -version
javac -version
bash scripts/build.sh
bash scripts/test-stage1.sh
bash scripts/run.sh < test/replays/stage1-victory.txt
bash scripts/run.sh < test/replays/stage1-defeat.txt
```

Return to `lichen/stage_1` before editing. Developer checks compile all Java
with `--release 17 -Xlint:all -Werror`. Expected markers are 14 core scenarios/
344 assertions with the real Order, and 9 production smoke scenarios/94
assertions. The core uses recording staff; the smoke suite separately uses real
staff/StaffTeam and child Main/CLI processes.

CLI coverage includes victory, all-WAIT defeat, invalid Waiter input, invalid
Chef input (text, empty line, integer overflow and out-of-range values), BOM
input, and PLACED/READY abandonment. Domain fixtures additionally cover two-unit
Pasta, PREPARING cancellation, FIFO payment/ties, atomic rejection, immutable
snapshots and exact victory boundaries. The natural six-arrival schedule is
verified separately from artificial domain edge-case fixtures.

Full generated logs are under `build/stage1-checks/` (ignored by Git).
[Committed results and source/test hashes](verification/Stage_1_Release_2026-11-08.md)
provide permanent evidence. See [test notes](Stage_1_Test_Notes.md) for all three
replay outcomes; the two defeat fixtures intentionally differ.

## Requirement matrix for optional independent review

The release record contains the agent's observed results. An independent tester
should record their own SHA, environment, input, expected/actual result, defects
and retest details rather than copy another tester's sign-off.

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


## Optional independent review record

- Tested commit SHA:
- Tester:
- Date/time/timezone:
- OS and JDK:
- Commands, inputs and exit status:
- Matrix results and evidence:
- Defects/fixes/retest SHA:
- Review conclusion:

This blank optional-review form does not indicate an unmet E gate for the user-
authorized agent-verified release. Final report, other members' AI histories,
video and signed declarations remain separate group-submission work.
