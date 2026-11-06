# Rishika Stage 1 replay inputs

Imported unchanged from Rishika commit `542a6891a1544dd211a3b779d41a3fe6ee342bf1`.
Each file contains 36 zero-based CLI choices: Waiter then Chef for all 18 turns.
Run from the repository root:

```bash
bash scripts/run.sh < test/runs/stage1-victory.txt
bash scripts/run.sh < test/runs/stage1-defeat.txt
```

| Replay | Result | Paid | Revenue | Average paid satisfaction | Unhappy departures |
| --- | --- | --- | --- | --- | --- |
| Victory | VICTORY after turn 18 | 5 | $66.00 | 79.20 | 0 |
| One-customer defeat | DEFEAT after turn 18 | 1 | $9.00 | 84.00 | 1 |

The defeat input serves and charges C1, then waits. It differs deliberately from
[A's all-WAIT defeat](../replays/stage1-defeat.txt), whose result is 0 paid,
$0.00, average N/A and 2 departures. The two victory input files are identical.
Checks on 6 November 2026 reproduced all three outcomes using the real Main/CLI.
