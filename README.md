# Restaurant Rush

SC2002 Object-Oriented Design & Programming. Java 17+, offline command-line game.
Repository: https://github.com/Cooolc327/SC2002_Group_Assignment

## Stages

`main` runs Stage 3, adding Host, ComboMeal and Happy Hour to Stage 2.
Each tag below preserves the runnable source for that stage.

| Stage | Features | Snapshot |
| --- | --- | --- |
| 1 | Regular customers, full turn sequence, both endings | [stage-1](https://github.com/Cooolc327/SC2002_Group_Assignment/tree/stage-1) |
| 2 | VIP/Critic behaviour and customer-specific billing | [stage-2](https://github.com/Cooolc327/SC2002_Group_Assignment/tree/stage-2) |
| 3 | Independent Host, combos and two-turn Happy Hour | [stage-3](https://github.com/Cooolc327/SC2002_Group_Assignment/tree/stage-3) |

## Build, play and replay

Requires JDK 17 or later. Mandatory gameplay and checks need no external Java
libraries or network connection. From the project root:

```bash
bash scripts/build.sh
bash scripts/run.sh                 # Stage 3, default mixed-customer base setting
bash scripts/run.sh --stage2        # Stage 2 regression mode
bash scripts/run.sh --regular       # Regular-only regression mode
bash scripts/run.sh < test/replays/stage3-victory.txt
bash scripts/run.sh < test/replays/stage3-defeat.txt
```

`--stage3` explicitly selects the default. The historical tags preserve each
stage's original source; regression flags run the current shared implementation.
Older Stage 2 inputs require `--stage2`; older Stage 1 inputs require `--regular`.

PowerShell build (syntax provided, not executed on Windows):

```powershell
javac --release 17 -Xlint:all -Werror -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out Main
```

## Base setting and turn choices

There are 18 turns, two tables and six arrivals on turns 1, 4, 7, 10, 13 and 16.
Default seed 24369 produces Regular, VIP, Regular, Regular, Regular, Critic.
The win criteria remain: at least four paid customers, at least $45.00 revenue,
and average satisfaction of paid customers at least 60 after turn 18.

Every Stage 3 turn: arrivals -> Happy Hour opening decision -> Host -> Waiter
-> Chef -> eating -> automatic Cashier payment -> waiting loss/abandonment.
The Host can seat one queued customer or wait. The Waiter independently keeps
one task: seat, take an order, serve a previously READY order, or wait. Chef
advances one preparation unit or waits. Cashier handles at most one eligible
payment automatically. Invalid input repeats the prompt without using capacity.

A dish completed this turn cannot be served until a later turn. A customer
served on turn t finishes eating on t+1 and can pay on t+2 at the earliest.
A table remains occupied until payment or departure. The game always completes
all 18 turns. Revenue and satisfaction averages include paid orders only.

| Item | Price | Preparation units |
| --- | --- | --- |
| Salad | $9.00 | 1 |
| Burger | $15.00 | 1 |
| Pasta | $18.00 | 2 |
| Salad + Burger Combo | $21.60 | 2 |
| Salad + Pasta Combo | $24.30 | 3 |
| Burger + Pasta Combo | $29.70 | 3 |

Regular and VIP choices cycle through this ordered six-item list using
`(id - 1) % menu.size()`. Critic chooses the most preparation units, using the
first item to break ties: Salad + Pasta Combo. Customers choose food, never the
manager. Stage 2 mode retains the original three-item list and Pasta-preferring
Critic. Waiting losses are Regular 8, VIP 5 and Critic 12.

Happy Hour can be activated once, at turn start, for the current and next turn.
The CLI displays remaining active turns, including the current turn. At each
active opening, WAITING/SEATED/ORDERED customers recover 15 satisfaction, capped
at 100. Normal end-of-turn losses still apply. New orders get 20% off after the
VIP discount; each step rounds to cents. Existing orders and customers who
have left are unaffected. Prices are locked when ordering and remain fixed at
payment, even after expiration. VIP Burger example: $15.00 -> $13.50 -> $10.80.

## Verification

```bash
bash scripts/test-stage1.sh
bash scripts/test-stage2.sh
bash scripts/test-stage3.sh
```

All scripts compile production and test sources using
`--release 17 -Xlint:all -Werror`. Stage 2 CLI tests explicitly use `--stage2`;
Stage 3 tests execute real default Main. Test logs are ignored under `build/`.

| Suite | Scenarios / assertions |
| --- | --- |
| Regular core / production smoke | 14 / 344; 9 / 94 |
| Billing / base menu and Order | 8 / 48; 7 / 65 |
| Stage 2 integration | 14 / 506 |
| ComboMeal / Stage 3 integration | 6 / 42; 18 / 550 |

| Default Stage 3 replay | Paid | Revenue | Average | Departures | Result |
| --- | --- | --- | --- | --- | --- |
| stage3-victory.txt | 5 | $83.70 | 87.20 | 0 | VICTORY |
| stage3-defeat.txt | 0 | $0.00 | N/A | 1 | DEFEAT |

The victory activates Happy Hour on turn 4 and includes real VIP/combo payment.
Both runs complete all 18 turns. Tests also cover phase guards, independent
Host/Waiter allowances, recovery eligibility/caps, locked prices, combo
abandonment, immutable views, invalid input, BOM and EOF.

## Design and stage evidence

### Stage 1

- [Implemented design](docs/Stage_1_Implemented_Design_EN.md)
- [Class diagram PDF](docs/UML/Stage1_UML_Diagram.pdf)
- [Release verification](docs/verification/Stage_1_Release_2026-11-08.md)

### Stage 2

Changes from Stage 1:

- **Added:** `VipCustomer`, `CriticCustomer`, `CustomerFactory` and
  `BillCalculator` for customer behaviour, reproducible arrivals and pricing.
- **Updated:** `Customer` and `MenuItem` expose the behaviour and preparation
  information used by the new types. `Restaurant` calculates and locks bills;
  `GameController`, `Main` and `RestaurantCLI` connect and display mixed customers.

Documentation and comparisons:

- [Class diagram PDF](docs/UML/Stage2_UML_Diagram.pdf)
- [VIP sequence diagram PDF](docs/UML/Stage2_VIP_Sequence_Diagram.pdf)
- [Release verification](docs/verification/Stage_2_Release_2026-11-08.md)
- [Stage 1 to 2 file changes and reasons](docs/Stage2/Stage_1_to_2_Changes.md)
- [Stage 1 to 2 tag comparison](https://github.com/Cooolc327/SC2002_Group_Assignment/compare/stage-1...stage-2)

### Stage 3

Changes from Stage 2:

- **Added:** `ComboMeal` and `Discount` for composed items and shared pricing;
  `Host` and `HostAction` for independent seating; `HappyHourPolicy` for activation
  and duration.
- **Updated:** `Menu`, `BillCalculator`, `Customer` and `CriticCustomer` support
  combo choices, discounts and satisfaction recovery. `Restaurant`,
  `GameController` and `TurnPhase` coordinate the new actions. `StaffGateway`
  and `StaffTeam` connect the Host; `GameView`, `RestaurantCLI` and `Main` expose
  the new choices, policy status and stage modes.

Documentation and comparisons:

- [Design and interfaces](docs/Stage3/Stage_3_Design_and_Interfaces_EN.md)
- [Class diagram PDF](docs/UML/Stage3_UML_Diagram.pdf) and
  [Happy Hour sequence PDF](docs/UML/Stage3_Happy_Hour_Sequence.pdf)
- [Standalone class image](docs/UML/Stage3_UML_Diagram.png),
  [sequence image](docs/UML/Stage3_Happy_Hour_Sequence.png), and
  [editable diagram sources and SVGs](docs/UML/README.md)
- [Stage 2 to 3 file changes and OO impact](docs/Stage3/Stage_2_to_3_Changes.md),
  [PDF](docs/Stage3/Stage_2_to_3_Changes.pdf)
- [Test notes and exact timeline](docs/Stage3/Stage_3_Test_Notes.md)
- [Release verification and repeatable results](docs/verification/Stage_3_Release_2026-11-14.md)
- [Stage 3 AI history](ai-history/stage-3/README.md)
- [Exact Stage 2 to Stage 3 implementation comparison](https://github.com/Cooolc327/SC2002_Group_Assignment/compare/stage-2...3f5e682f775734a6a305fc159abd89aaf76257c3),
  [implementation commit](https://github.com/Cooolc327/SC2002_Group_Assignment/commit/3f5e682f775734a6a305fc159abd89aaf76257c3)
- [Formal Stage 2 to Stage 3 tag comparison](https://github.com/Cooolc327/SC2002_Group_Assignment/compare/stage-2...stage-3)
  includes documentation, diagram exports and release evidence added after the
  implementation commit; the file analysis explains the code changes.

Stage 3 diagrams are available as PDF, SVG and PNG, with editable drawio
sources. Diagram notes identify the corresponding code version and replay
values. The VIP sequence describes Stage 2; the Happy Hour sequence describes
Stage 3.

No production files were deleted in either transition. The change records
include the complete source, test and documentation file lists.

## Team and final submission

| Member | Role |
| --- | --- |
| Lichen Yin | A: game control and restaurant coordination (`control`, `domain`, `view`), design document and UML |
| Xavier Kee | B: customers, tables, waiting queue, seating, menu and game configuration |
| Bernice Koh | C: orders and pricing |
| Advika Bhambri | D: staff (Waiter, Chef, Cashier) and command-line interface |
| Rishika Garg | E: integration (`Main`, `Order`, `StaffTeam`), testing and report |

Each member's AI history is linked below.

| Member | AI history |
| --- | --- |
| Lichen Yin | [Stage 1 archive](https://github.com/Cooolc327/SC2002_Group_Assignment/tree/lichen/stage_1/ai-history/stage-1), [Stage 2 archive](ai-history/stage-2/README.md), [Stage 3 AI review](https://github.com/Cooolc327/SC2002_Group_Assignment/tree/lichen/stage_3/ai-history/stage-3) |
| Xavier Kee | Pending |
| Bernice Koh | [Stage 2 AI review](https://github.com/Cooolc327/SC2002_Group_Assignment/blob/bernice-ai-history/ai-history/stage-2/chat-01.md), [Stage 3 AI review](https://github.com/Cooolc327/SC2002_Group_Assignment/blob/bernice-ai-history/ai-history/stage-3/chat-01.md) |
| Advika Bhambri | Pending |
| Rishika Garg | [AI history (branch `Rishika`)](https://github.com/Cooolc327/SC2002_Group_Assignment/tree/Rishika/ai-history/rishika) |

## Video

[Watch the video](LINK)

### Required segments

| # | Segment | Timestamp | Speaker |
|---|---|---|---|
| 1 | Game launching from the submitted project (`build.sh`, `run.sh`) | 01:40 | Li Chen |
| 2 | Meaningful manager choices: turn flow and input validation | 04:46 | Xavier |
|   | Decision where staff are constrained (turn 16, waiter has one task) | 06:03 | Xavier |
| 3 | A dish going READY → SERVED → payment (turns 1 to 4) | 05:17 | Xavier |
| 4 | Critic behaviour (orders the longest-cooking combo) | 06:23 | Xavier |
|   | VIP behaviour (10% discount with Happy Hour pricing) | 07:30 | Bernice |
| 5 | Stage 3 features: Host, Happy Hour and Combo Meals | 06:57 | Bernice |
| 6 | Victory run (replayed from a saved input file) | 10:19 | Advika |
|   | Defeat run (same seed, every staff member waits) | 10:40 | Advika |
| 7 | Stage tag comparisons (stage-1 → stage-2 → stage-3) | 10:55 | Advika |
|   | Class diagrams across the three stages | 13:02 | Rishika |

### Design explanations: UML class and sequence diagrams

| Topic | Timestamp | Speaker |
|---|---|---|
| Layered architecture (CLI, view, control, domain) | 02:14 | Li Chen |
| Simplified Stage 3 class diagram overview | 02:38 | Li Chen |
| Dependency inversion: GameController and StaffGateway | 03:02 | Li Chen |
| Encapsulation and the template method in Customer | 03:39 | Xavier |
| Composition, aggregation and association | 04:19 | Xavier |
| Polymorphism: VIP and Critic subclasses | 06:30 | Xavier |
| Liskov substitution: ComboMeal as a MenuItem | 08:18 | Bernice |
| Single responsibility: pricing classes | 08:40 | Bernice |
| Sequence diagram walkthrough | 09:15 | Bernice |
| Sequence diagram: alt fragment for the Happy Hour choice (turn 4) | 09:36 | Bernice |
| Sequence diagram: order and locked price | 09:44 | Bernice |
| Sequence diagram: payment and table release (turn 7) | 10:06 | Bernice |
| How the class diagram grew across stages | 13:02 | Rishika |
| Real Stage 3 class diagram: interface realisation (DIP) | 13:40 | Rishika |
| Visibility, abstract class, enum and static constants | 13:55 | Rishika |
| Notation legend | 14:37 | Rishika |
| Design trade-offs | 14:57 | Rishika |
| Alternative design we rejected | 15:30 | Rishika |
| Summary | 15:36 | Rishika |

## Submission materials

| Item | Status or link |
| --- | --- |
| Lab and group identifier | Pending |
| Final PDF report, including individual reflections | Pending |
| Presentation video | [Watch the video](LINK) |
| Video timestamps for the required demonstration segments | [Video section](#video) |
| Signed original-work and GAI declarations | Pending |

Submission deadline: **15 November 2026, 23:59 Singapore time**.
