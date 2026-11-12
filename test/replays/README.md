# Stage 1 CLI replay inputs

Each file contains 36 zero-based menu choices: one Waiter choice and one Chef
choice for each of the 18 turns. They are stdin for the real Main/CLI and use
the base-setting deterministic menu rule. They contain no test doubles.

Run from the repository root:

```bash
bash scripts/run.sh < test/replays/stage1-victory.txt
bash scripts/run.sh < test/replays/stage1-defeat.txt
```

| Input | Result | Paid | Revenue | Average paid satisfaction | Departures |
| --- | --- | --- | --- | --- | --- |
| stage1-victory.txt | VICTORY after turn 18 | 5 | $66.00 | 79.20 | 0 |
| stage1-defeat.txt | DEFEAT after turn 18 | 0 | $0.00 | N/A | 2 |

The human-readable action table is in
[the A integration notes](../../docs/Member_A_Integration.md).
Menu numbering is part of these fixtures: after a CLI menu change, update and
verify the inputs alongside the action table. Member E should also exercise
the interactive menu independently of these development checks.


## Stage 2 local implementation fixtures

The default Stage 2 Main uses seed 24369 (Regular, VIP, Regular, Regular,
Regular, Critic). Run from the repository root:

```bash
bash scripts/run.sh < test/replays/stage2-victory.txt
bash scripts/run.sh < test/replays/stage2-defeat.txt
```

Victory: 5 paid, $64.50, average 80.40, zero departures. All-WAIT defeat:
0 paid, $0.00, N/A, one departure. Both inputs have 36 choices and run all 18
turns. The victory prioritises serve, then take-order, then seat, and prepares
the oldest available order. The default seed leaves Critic #6 too late to pay.
The integration suite separately checks an early Critic's full service flow.

To reuse the Stage 1 fixtures with the current implementation, pass `--regular`
to scripts/run.sh. The historical Stage 1 source is still the stage-1 tag.


## Diagram: a single delayed VIP order

`stage2-vip-wait-turn5.txt` chooses Waiter WAIT only on turn 5, then resumes
the same serve / take-order / seat priority rule (Chef oldest first). In the
actual Main replay the VIP pays on turn 9 at satisfaction 85, and the full
18-turn result is VICTORY: 5 paid, $64.50, average 77.80, zero departures.
The normal victory input pays VIP on turn 8 at 90 and finishes at average 80.40.
Both inputs are fixed 36-choice files. This is the alternative in the
[Stage 2 sequence diagram](../../docs/Stage2/Diagram_Notes.md).

```bash
bash scripts/run.sh < test/replays/stage2-vip-wait-turn5.txt
```
