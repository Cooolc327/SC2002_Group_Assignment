# Restaurant Rush

SC2002 Object-Oriented Design & Programming · Group `<LAB>_Grp<N>` (TODO) ·
Repository: https://github.com/Cooolc327/SC2002_Group_Assignment

| Member | Role |
| --- | --- |
| Lichen Yin | A: game control and restaurant coordination (`control`, `domain`, `view`), design document and UML |
| Xavier | B: customers, tables, waiting queue, seating, menu and game configuration |
| Bernice Koh | C: orders and pricing |
| Advika Bhambri | D: staff (Waiter, Chef, Cashier) and command-line interface |
| Rishika Garg | E: integration (`Main`, `Order`, `StaffTeam`), testing and report |

> TODO (team): confirm the role column before submission.

## Stage 1 verified release

The runnable Stage 1 snapshot is [`stage-1`](https://github.com/Cooolc327/SC2002_Group_Assignment/tree/stage-1).
It is published from Lichen's personal branch `lichen/stage_1`; merging into main
is not required. This branch incorporates main commit `f24c186` (the new class
diagram) while preserving the existing build/run scripts, production tests,
replays and personal AI history. PR #4 is closed; PR #5 is a separate optional
main-integration review and does not control this tag.

On 8 November 2026 the user requested that Codex complete the remaining tests
rather than wait for an absent E acceptance record. The release verification is
therefore **agent-run**, not a claimed E signature or independent human review.
See the [verification record](docs/verification/Stage_1_Release_2026-11-08.md),
[reproducible test notes](docs/Stage_1_Test_Notes.md),
[current design](docs/Stage_1_Implemented_Design_EN.md) and
[manual checklist](docs/Stage_1_Manual_Checklist_ZH.md).

## Overview

You are the manager of a small restaurant in this turn-based Java command-line game. Every turn you decide what the
Waiter and the Chef do. After 18 turns the game checks whether you served enough paying,
satisfied customers to win.

| Stage | Adds | Status | Tag |
| --- | --- | --- | --- |
| 1 | Regular customers, menu, Waiter/Chef/Cashier, full turn sequence, victory/defeat | Agent-verified runnable snapshot | [stage-1](https://github.com/Cooolc327/SC2002_Group_Assignment/tree/stage-1) |
| 2 | VIP and Critic customers | Separate remote work branches; not integrated into this baseline | Pending |
| 3 | Host, ComboMeal, Happy Hour | Outside this Stage 1 review | Pending |

## Requirements

JDK 17 or later. No external libraries, no build tool, works offline.

## Build and run

From the repository root.

Bash (Linux, macOS, Git Bash):

    bash scripts/build.sh
    bash scripts/run.sh

Windows PowerShell:

    javac --release 17 -Xlint:all -Werror -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
    java -cp out Main

## Project structure

    src/
      Main.java     entry point: wires Restaurant, StaffTeam, GameController and the CLI
      cli/          boundary: RestaurantCLI reads choices and prints the game state
      control/      control: GameController (turn sequence), TurnPhase, GameResult
      domain/       Restaurant: coordinates changes involving customers, tables and orders
      entities/     Customer, RegularCustomer, CustomerState, Table, WaitingQueue, SeatingArea
      fooditems/    Menu, MenuItem
      orders/       Order (preparation progress, status, locked bill)
      staff/        Waiter, Chef, Cashier, StaffTeam (per-turn allowances)
      contracts/    action records and interfaces between modules (WaiterAction, ChefAction, OrderPort, StaffGateway, ActionResult)
      view/         read-only snapshots for the CLI (GameView, GameSummary)
      config/       GameConfig: base-setting constants
    test/           automated tests and replay inputs (test/replays/ and test/runs/)
    scripts/        portable Bash build, run and development-check commands
    docs/           design discussion and UML

## How to play

**Base setting:**
- 18 turns, 2 tables
- Staff: one Waiter (one task per turn), one Chef (one preparation unit per turn), one Cashier (one payment per turn, automatic)
- One Regular customer arrives at the start of turns 1, 4, 7, 10, 13 and 16
- **Win** if, after turn 18, you have at least 4 paid customers, revenue of at least $45.00 and an average paid satisfaction of at least 60. Otherwise you lose.

| Menu item | Price | Prep units |
| --- | --- | --- |
| Salad | $9.00 | 1 |
| Burger | $15.00 | 1 |
| Pasta | $18.00 | 2 |

**Each turn:**
1. **Arrivals:** the scheduled customer joins the queue at satisfaction 100.
2. **Waiter:** seat a queued customer, take a seated customer's order, serve a READY order, or wait.
3. **Chef:** advance one order by one unit, or wait. The Waiter acts first. A dish that becomes READY this turn can be served next turn at the earliest.
4. **Eating and payment:** a customer served on an earlier turn finishes eating and becomes READY_TO_PAY. The Cashier then collects from the oldest eligible customer. A customer who became READY_TO_PAY this turn cannot pay until a later turn.
5. **Waiting penalty:** every customer not yet served loses 8 satisfaction. A customer who reaches 0 leaves without paying.
6. **Result:** shown after turn 18. There is no early ending.

Each prompt lists the valid options by number. Type a number and press Enter, for example:

    Waiter tasks (one per turn):
      0) Wait
      1) Seat customer #2 at Table 2
    Choice: 1
      OK: Seated customer 2 at table 2

## Rules and assumptions

- **Food choice:** customers are numbered 1–6 in arrival order. Customer *n* orders menu item `(n − 1) mod 3`: Salad, Burger, Pasta, Salad, Burger, Pasta. The manager never chooses food.
- **Money:** stored as integer cents (`int`/`long`) and shown as dollars with two decimals. The bill is fixed when the order is taken.
- **Satisfaction:** Regular customers lose 8 per turn while WAITING, SEATED or ORDERED (including while their dish is READY but not yet served), with a minimum of 0. Served, eating and ready-to-pay customers lose nothing.
- **Abandonment:** a customer who reaches 0 leaves. Their order is cancelled, even if it is READY, and their table is freed. A customer who is never seated leaves at the end of their 13th turn.
- **Payment:**
  - The Cashier picks the customer who has been READY_TO_PAY longest. If two became ready on the same turn, the lower customer number pays first.
  - The satisfaction at the moment of payment is recorded and used for the average. With no payments, the average shows N/A.
  - The victory check uses the exact average, with no rounding.
- **Invalid input:**
  - A non-number or an out-of-range number gets an explanation and the prompt repeats, without using the staff task.
  - An action the game rejects explains why and does not use the task.
  - If input ends, the game exits cleanly.
- **Turn-16 customer:** this customer cannot pay within 18 turns, so at most 5 customers can pay (intended by the brief).

## Test notes

**Development regression checks** (Bash):

    bash scripts/test-stage1.sh

All production and test Java sources compile with `--release 17 -Xlint:all -Werror`.
Expected results:

    MEMBER_A_CORE=PASS scenarios=14 assertions=344 orders=production
    STAGE1_SMOKE=PASS scenarios=9 assertions=94
    STAGE1_DEVELOPMENT_CHECKS=PASS

The core suite uses the production Order and a recording staff test double.
The smoke suite separately uses real Order, StaffTeam, staff and Main/CLI.
Logs are in `build/stage1-checks/` (Git-ignored). The release execution and
results are recorded separately; no E sign-off is claimed. Extra CLI scenarios
cover Chef invalid-input retries, UTF-8 BOM input, and PLACED/READY order
cancellation with real staff and orders.

`bash scripts/test-member-a.sh` still runs the isolated core suite with fake
orders/staff. `test/CustomerSeatingTest.java` is an empty placeholder and does
not supply additional executed tests.

**Reproducible runs:** each file holds one zero-based menu choice per line, the Waiter's choice then the Chef's, for 18 turns. Both replay directories are retained: A's all-WAIT defeat and Rishika's one-customer defeat intentionally produce different results.

| Run | Input | Strategy | Expected final result |
| --- | --- | --- | --- |
| Victory | [test/runs/stage1-victory.txt](test/runs/stage1-victory.txt) | Waiter: serve, else take order, else seat. Chef: always prepare the oldest order | **VICTORY**: 5 paid, $66.00, avg 79.20, 0 departures |
| One-customer defeat | [test/runs/stage1-defeat.txt](test/runs/stage1-defeat.txt) | Look after customer 1 only, then wait for the rest of the game | **DEFEAT**: 1 paid, $9.00, avg 84.00, 1 departure |

The A fixtures additionally cover no payments and the required N/A average:

| Run | Input | Expected final result |
| --- | --- | --- |
| Victory (same choices) | [test/replays/stage1-victory.txt](test/replays/stage1-victory.txt) | VICTORY; 5 paid, $66.00, average 79.20, 0 departures |
| All-WAIT defeat | [test/replays/stage1-defeat.txt](test/replays/stage1-defeat.txt) | DEFEAT; 0 paid, $0.00, average N/A, 2 departures |

Run the Bash launcher directly:

    bash scripts/run.sh < test/runs/stage1-victory.txt
    bash scripts/run.sh < test/runs/stage1-defeat.txt
    bash scripts/run.sh < test/replays/stage1-defeat.txt

After the PowerShell build above:

    Get-Content test\runs\stage1-victory.txt | java -cp out Main

See [A replay notes](test/replays/README.md), [Rishika replay notes](test/runs/README.md)
and [the 18-turn action table](docs/Member_A_Integration.md#victory-replay-using-the-current-menu-rule).

## Stage 1 design summary

| Class | Responsibility |
| --- | --- |
| `GameController` | Runs the six turn steps in order, accepts staff actions only in the right phase, evaluates the result |
| `Restaurant` | Keeps customers, tables and orders consistent during seating, ordering, serving, payment and abandonment, and computes statistics |
| `Customer` (abstract) / `RegularCustomer` | Satisfaction, food choice, legal state transitions. Subclasses supply the per-turn satisfaction loss |
| `Table`, `WaitingQueue`, `SeatingArea` | Table occupancy and the arrival-order queue |
| `Order` | Preparation progress, status (PLACED → PREPARING → READY → SERVED → PAID / CANCELLED), locked bill, satisfaction at payment |
| `Waiter`, `Chef`, `Cashier`, `StaffTeam` | Each enforces its own one-action-per-turn allowance. A rejected action does not use it up |
| `RestaurantCLI` | Shows `GameView` snapshots and collects choices. It contains no game rules |

The [Stage 1 class diagram](docs/UML/Stage1_UML_Diagram.pdf) matches the
implemented structure, including StaffGateway/StaffTeam, SeatingArea/WaitingQueue,
RegularCustomer, OrderPort and the actual enums/views. Its field type and
encapsulation notes were checked against source. See the
[current design discussion](docs/Stage_1_Implemented_Design_EN.md).

The older `Stage_1_Design_Discussion_EN` Markdown/PDF are retained explicitly as
historical proposals, not current implementation evidence. The briefing requires
one matching class diagram per stage and one Stage 2 or Stage 3 sequence diagram.

## Stage links

- Stage 1: [runnable stage-1 tag](https://github.com/Cooolc327/SC2002_Group_Assignment/tree/stage-1).
- [Verification and optional independent-review handoff](docs/Stage_1_E_Handoff.md).
- Resolve the immutable release commit with `git rev-parse stage-1^{commit}`.
- Stage 1 → 2 comparison: TODO
- Stage 2 → 3 comparison: TODO

## AI history and submission evidence

The briefing requires each member's **full ordered project conversations**
(prompts, replies and follow-ups), stored on that member's own repository branch.
A summary alone is insufficient. Lichen's full available history is linked
below: three related conversations, 161 messages and eight screenshots, with
the export scope/cutoff documented. Other members' links remain pending.
Do not replace full conversations with reconstructed summaries or link to
nonexistent folders.

| Member | Full history link |
| --- | --- |
| Lichen Yin | [Stage 1 full AI history](https://github.com/Cooolc327/SC2002_Group_Assignment/tree/lichen/stage_1/ai-history/stage-1) |
| Xavier | Pending |
| Bernice Koh | Pending |
| Advika Bhambri | Pending |
| Rishika Garg | Pending |

Final submission is due **15 November 2026, 23:59 Singapore time**. The group ZIP
must include code/build instructions, README stage comparisons and video
timestamps, PDF report (three class diagrams, one later-stage sequence diagram,
design/results and individual reflections), an approximately 15-minute video
with every member explaining a part, and the signed original-work/GAI forms.
