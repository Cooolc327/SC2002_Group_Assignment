# SC2002 Restaurant Rush

Java command-line, turn-based restaurant management coursework.
[GitHub repository](https://github.com/Cooolc327/SC2002_Group_Assignment)

## Stage 1 handoff status

Updated on 3 November 2026. The base-setting game connects the controller,
restaurant coordination, customers, seating, menu, production Order, staff and
CLI through Main. Member A's development checks pass; member E's independent
Stage 1 acceptance and release decision are pending.

This handoff contains Stage 1 only. Create the stage-1 tag after E has recorded
acceptance on the exact commit to be released. Stage 2/3 are separate work.

## Build and play

Requirements: JDK 17 or later and Bash. No network access, external libraries,
Maven or Gradle are needed. Open the repository root containing src, test,
scripts and docs.

```bash
bash scripts/run.sh
```

The command builds production source into build/classes and launches Main.
Choose a zero-based option for the Waiter and then the Chef on each turn.
The game always completes 18 turns. Customers choose Salad, Burger and Pasta
cyclically by id; the manager sets service and kitchen priorities.

To compile without launching:

```bash
bash scripts/build.sh
```

## Developer checks before E acceptance

```bash
bash scripts/test-stage1.sh
```

This compiles all source/tests with Java 17 compatibility, -Xlint:all and -Werror.
It runs:

- 14 core scenarios and 344 assertions using the production Order. RecordingStaff
  remains a test double for call-order assertions.
- 5 smoke scenarios and 62 assertions using real Order, staff, StaffTeam and
  Main/CLI, including victory, defeat and invalid-input replays.

Expected markers:

```text
MEMBER_A_CORE=PASS scenarios=14 assertions=344 orders=production
STAGE1_SMOKE=PASS scenarios=5 assertions=62
STAGE1_DEVELOPMENT_CHECKS=PASS
```

The last marker is followed by the local log-directory path. Logs are written to
build/stage1-checks; build output and logs are ignored by Git. A failed check
returns a nonzero exit code. These development checks do not constitute E's
independent acceptance.

The original isolated A test command is also available:

```bash
bash scripts/test-member-a.sh
```

It uses test-only order/staff substitutes and labels the order mode as fake.

## Reproduce both endings with the real CLI

```bash
bash scripts/run.sh < test/replays/stage1-victory.txt
bash scripts/run.sh < test/replays/stage1-defeat.txt
```

| Run | Turns | Paid | Revenue | Average paid satisfaction | Unhappy departures |
| --- | --- | --- | --- | --- | --- |
| Victory | 18 | 5 | $66.00 | 79.20 | 0 |
| All-WAIT defeat | 18 | 0 | $0.00 | N/A | 2 |

## Team documentation

- [E acceptance handoff](docs/Stage_1_E_Handoff.md): exact test steps, expected
  results, acceptance matrix and release gate.
- [A integration notes](docs/Member_A_Integration.md): ownership, implemented
  interfaces, turn sequence, state boundaries and human-readable replay.
- [Replay inputs](test/replays/README.md).
- [Earlier Stage 1 design proposal](docs/Stage_1_Design_Discussion_EN.md): retained
  design history. E should synchronize diagrams and final reporting with the
  accepted implementation.
