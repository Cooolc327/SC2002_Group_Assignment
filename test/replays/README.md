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
