# Restaurant Rush

SC2002 Object-Oriented Design & Programming · Group `<LAB>_Grp<N>` (TODO) ·
Repository: https://github.com/Cooolc327/SC2002_Group_Assignment

| Member | Role |
| --- | --- |
| Lichen Yin | A: game control and restaurant coordination (`control`, `domain`, `view`), design document and UML |
| Xavier Kee | B: customers, tables, waiting queue, seating, menu and game configuration |
| Bernice Koh | C: orders and pricing |
| Advika Bhambri | D: staff (Waiter, Chef, Cashier) and command-line interface |
| Rishika Garg | E: integration (`Main`, `Order`, `StaffTeam`), testing and report |

> TODO (team): confirm the role column before submission.

## Overview

You are the manager of a small restaurant in this turn-based Java command-line game. Every turn you decide what the
Waiter and the Chef do. After 18 turns the game checks whether you served enough paying,
satisfied customers to win.

| Stage | Adds | Status | Tag |
| --- | --- | --- | --- |
| 1 | Regular customers, menu, Waiter/Chef/Cashier, full turn sequence, victory/defeat | Done | `stage-1` |
| 2 | VIP and Critic customers | TODO | `stage-2` |
| 3 | Host, ComboMeal, Happy Hour | TODO | `stage-3` |

## Requirements

JDK 17 or later. No external libraries, no build tool, works offline.

## Build and run

From the repository root.

Bash (Linux, macOS, Git Bash):

    javac -d out $(find src -name "*.java")
    java -cp out Main

Windows PowerShell:

    javac -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
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
    test/           automated tests and replay inputs (test/runs/)
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
4. **Eating and payment:** a customer served on an earlier turn finishes eating and becomes READY_TO_PAY. The Cashier then collects from the oldest eligible customer.
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

**Automated tests** (Bash):

    bash scripts/test-member-a.sh

These compile everything with warnings treated as errors and run the integration tests in `test/integration/`.

**Reproducible runs:** each file holds one menu choice per line, the Waiter's choice then the Chef's, for 18 turns.

| Run | Input | Strategy | Expected final result |
| --- | --- | --- | --- |
| Victory | [test/runs/stage1-victory.txt](test/runs/stage1-victory.txt) | Waiter: serve, else take order, else seat. Chef: always prepare the oldest order | **VICTORY**: 5 paid, $66.00, avg 79.20, 0 departures |
| Defeat | [test/runs/stage1-defeat.txt](test/runs/stage1-defeat.txt) | Look after customer 1 only, then wait for the rest of the game | **DEFEAT**: 1 paid, $9.00, avg 84.00, 1 departure |

Run them, after building:

    java -cp out Main < test/runs/stage1-victory.txt                    # Bash
    Get-Content test\runs\stage1-victory.txt | java -cp out Main        # PowerShell

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

Class diagram: [docs/UML/Stage_1_Class_Diagram.svg](docs/UML/Stage_1_Class_Diagram.svg) ·
Design discussion: [docs/Stage_1_Design_Discussion_EN.md](docs/Stage_1_Design_Discussion_EN.md)

## Stage links

- Stage 1: [`stage-1` tag](https://github.com/Cooolc327/SC2002_Group_Assignment/tree/stage-1)
- Stage 1 → 2 comparison: TODO
- Stage 2 → 3 comparison: TODO

## AI history

| Member | AI history folder |
| --- | --- |
| Lichen Yin | [codex/member-a-stage-1](https://github.com/Cooolc327/SC2002_Group_Assignment/tree/codex/member-a-stage-1/ai-history) (TODO) |
| Xavier | [Xavier](https://github.com/Cooolc327/SC2002_Group_Assignment/tree/Xavier/ai-history) (TODO) |
| Bernice Koh | [bernice/stage-2-pricing](https://github.com/Cooolc327/SC2002_Group_Assignment/tree/bernice/stage-2-pricing/ai-history) (TODO) |
| Advika Bhambri | [Advika](https://github.com/Cooolc327/SC2002_Group_Assignment/tree/Advika/ai-history) (TODO) |
| Rishika Garg | [Rishika](https://github.com/Cooolc327/SC2002_Group_Assignment/tree/Rishika/ai-history) (TODO) |
