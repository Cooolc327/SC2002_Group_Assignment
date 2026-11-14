# Restaurant Rush

SC2002 Object-Oriented Design & Programming. Java 17+, offline command-line game.
Repository: https://github.com/Cooolc327/SC2002_Group_Assignment

## Stage 2 release

Runnable snapshot: [`stage-2`](https://github.com/Cooolc327/SC2002_Group_Assignment/tree/stage-2), published from personal branch
[`lichen/stage_2`](https://github.com/Cooolc327/SC2002_Group_Assignment/tree/lichen/stage_2). It extends
[`stage-1`](https://github.com/Cooolc327/SC2002_Group_Assignment/tree/stage-1) and preserves B/C source history.
The `main` branch now includes this Stage 2 implementation. The `stage-1` and
`stage-2` tags retain their original runnable snapshots.

The release includes VIP/Critic behaviour, customer-specific locked bills,
matching class/sequence diagrams, reproducible inputs, full bounded personal
AI history and verification evidence. The final annotated tag archive is
validated before branch/tag publication; no E signature is claimed.

| Stage | Features | Status | Tag |
| --- | --- | --- | --- |
| 1 | Regular customers, full turn sequence, both endings | Published runnable snapshot | [stage-1](https://github.com/Cooolc327/SC2002_Group_Assignment/tree/stage-1) |
| 2 | VIP/Critic behaviour and customer-specific billing | Available on main and tagged | [stage-2](https://github.com/Cooolc327/SC2002_Group_Assignment/tree/stage-2) |
| 3 | Host, ComboMeal, Happy Hour | Not implemented in this version | Pending |

- [Stage 2 release verification](docs/verification/Stage_2_Release_2026-11-08.md)
- [Acceptance checklist](docs/Stage2/Stage_2_Acceptance_Checklist_ZH.md)
- [Reproducible test notes](docs/Stage2/Stage_2_Test_Notes.md)
- [New/changed/deleted files and reasons](docs/Stage2/Stage_1_to_2_Changes.md)
- [Formal Stage 1 to Stage 2 tag comparison](https://github.com/Cooolc327/SC2002_Group_Assignment/compare/stage-1...stage-2)
- [Lichen's complete available Stage 2 AI history](ai-history/stage-2/README.md)

## Build and run

Requires JDK 17 or later; no external Java libraries or network services.
From the repository root (macOS, Linux or Git Bash):

```bash
bash scripts/build.sh
bash scripts/run.sh
```

The default game is Stage 2. It displays seed `24369` and produces this fixed
arrival schedule; the same seed gives the same types on every run.

| Customer ID | Arrival turn | Type | Waiting loss | Food rule / bill |
| --- | --- | --- | --- | --- |
| 1 | 1 | Regular | 8 | Salad; full price |
| 2 | 4 | VIP | 5 | Burger; 10% off |
| 3 | 7 | Regular | 8 | Pasta; full price |
| 4 | 10 | Regular | 8 | Salad; full price |
| 5 | 13 | Regular | 8 | Burger; full price |
| 6 | 16 | Critic | 12 | Longest preparation time: Pasta; full price |

Customer IDs are 1-6 in arrival order. They are not turn numbers. The manager
chooses seating, ordering, serving and preparation priority, never the food.
Regular and VIP food choice is `(id - 1) % menu.size()`. Critic overrides
`chooseItem(Menu)` and chooses the most preparation units; a tie uses the first
item in menu order. `BillCalculator` calls the customer's discount method and
locks the integer-cent bill when ordering.

The two tables, staff limits and complete 18-turn flow are unchanged. A dish
completed this turn can be served on a later turn. A served customer finishes
one full later turn of eating; payment occurs on a later turn again. Unserved
customers lose satisfaction, reaching zero cancels unfinished orders and frees
the table. Revenue and the satisfaction average include only paid orders.
Victory after turn 18 requires all of: at least 4 paid, at least $45.00 revenue,
and average paid satisfaction at least 60. The sixth customer cannot pay by
turn 18. There is no early finish.

To exercise Regular-only regression behaviour on the current implementation:

```bash
bash scripts/run.sh --regular
```

This flag does not restore the historical Stage 1 source. Use the immutable
`stage-1` tag for the actual Stage 1 snapshot. `new GameController(restaurant,
staff)` also retains Regular-only behaviour; the production entry point injects
`CustomerFactory::createCustomer` through the three-argument constructor.

PowerShell build (syntax provided, not executed on Windows):

```powershell
javac --release 17 -Xlint:all -Werror -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out Main
```

## Automated checks and reproducible inputs

```bash
bash scripts/test-stage1.sh
bash scripts/test-stage2.sh
bash scripts/run.sh < test/replays/stage2-victory.txt
bash scripts/run.sh < test/replays/stage2-defeat.txt
```

Both scripts compile all production and test sources with
`--release 17 -Xlint:all -Werror`. The Stage 1 smoke suite now invokes
`Main --regular`; the Stage 2 suite invokes real default `Main` with mixed
customers. Core call-order tests use a recording staff double; the smoke and
Stage 2 integration suites use production staff and orders.

| Check | Actual local result, 8 November 2026 |
| --- | --- |
| Regular core with production Order | 14 scenarios / 344 assertions |
| Regular production smoke | 9 scenarios / 94 assertions |
| Actual VIP pricing and rounding | 5 scenarios / 26 assertions |
| Menu and Order lifecycle | 7 scenarios / 65 assertions |
| Mixed customer integration and real CLI | 14 scenarios / 506 assertions |

| Default Stage 2 replay | Paid | Revenue | Average satisfaction | Departures | Result |
| --- | --- | --- | --- | --- | --- |
| `stage2-victory.txt` | 5 | $64.50 | 80.40 | 0 | VICTORY |
| `stage2-defeat.txt` (all WAIT) | 0 | $0.00 | N/A | 1 | DEFEAT |

Both run all 18 turns. The victory choices serve READY orders first, otherwise
take orders, otherwise seat; the Chef prepares the oldest eligible order.
The VIP Burger costs $13.50 and records satisfaction 90 at payment on turn 8.
The suite also uses a Critic-first setup to verify its distinct Pasta choice,
full-price payment and PLACED/PREPARING/READY cancellation. That setup is a test
fixture; it does not change the default seed or CLI behaviour.

Logs are Git-ignored under `build/stage1-checks/` and `build/stage2-checks/`.
These are agent-run checks, not an E signature or independent human acceptance.
Exact committed-archive provenance is in the release verification record;
the earlier pre-tag record is retained as historical evidence.

## Design and change evidence

- [Stage 2 integration notes](docs/Stage2/Integration_Notes.md): provenance,
  changes, constraints and remaining steps.
- [B customer changes](docs/Stage2/MemberBChanges.md).
- [Stage 1 class diagram](docs/UML/Stage1_UML_Diagram.pdf), matching the Stage 1
  snapshot, not the current Stage 2 working code.
- [Stage 1 implemented design](docs/Stage_1_Implemented_Design_EN.md).
- [Stage 1 release verification](docs/verification/Stage_1_Release_2026-11-08.md).
- [Stage 1 reproducible test notes](docs/Stage_1_Test_Notes.md).
- [Stage 2 class diagram](docs/UML/Stage2_UML_Diagram.pdf) and
  [VIP complex sequence diagram](docs/UML/Stage2_VIP_Sequence_Diagram.pdf).
  [Diagram notes](docs/Stage2/Diagram_Notes.md) link editable sources, source
  hashes and a replayed single-choice alternative (average 77.80).
- [Stage 1 to Stage 2 tag comparison](https://github.com/Cooolc327/SC2002_Group_Assignment/compare/stage-1...stage-2);
  [tested implementation f8a7025](https://github.com/Cooolc327/SC2002_Group_Assignment/commit/f8a70258fe3300318a00cb913d45abefa1a552b5).
- Stage 2 to 3 comparison: pending Stage 3.

The briefing requires three class diagrams (one per stage) and one complex
sequence diagram from Stage 2 or 3. Existing Stage 1 diagrams are retained as
Stage 1 evidence and have not been presented as current Stage 2 diagrams.

## Team and final submission

| Member | Role |
| --- | --- |
| Lichen Yin | A: game control and restaurant coordination (`control`, `domain`, `view`), design document and UML |
| Xavier Kee | B: customers, tables, waiting queue, seating, menu and game configuration |
| Bernice Koh | C: orders and pricing |
| Advika Bhambri | D: staff (Waiter, Chef, Cashier) and command-line interface |
| Rishika Garg | E: integration (`Main`, `Order`, `StaffTeam`), testing and report |

Confirm lab/group identifier and contribution details before final submission.
Each member must keep their own full ordered AI conversations on a personal
branch; summaries alone do not satisfy the briefing. [Lichen's Stage 1 archive](https://github.com/Cooolc327/SC2002_Group_Assignment/tree/lichen/stage_1/ai-history/stage-1)
is published. [Lichen's Stage 2 archive](ai-history/stage-2/README.md) preserves the
full available continuation with an explicit cutoff.
[Lichen's Stage 3 AI review](https://github.com/Cooolc327/SC2002_Group_Assignment/tree/lichen/stage_3/ai-history/stage-3)
records AI-assisted implementation/integration, UML and change-document work,
and release preparation. It provides ordered conversation exports, canonical
message files and export audits. Each part declares its cutoff; later messages
are outside that part's scope. Known team AI-history links are listed below;
TODO entries still require verification by their owners.

| Member | AI history folder |
| --- | --- |
| Lichen Yin | [Stage 1 archive](https://github.com/Cooolc327/SC2002_Group_Assignment/tree/lichen/stage_1/ai-history/stage-1), [Stage 2 archive](ai-history/stage-2/README.md), [Stage 3 AI review](https://github.com/Cooolc327/SC2002_Group_Assignment/tree/lichen/stage_3/ai-history/stage-3) |
| Xavier Kee | [Xavier](https://github.com/Cooolc327/SC2002_Group_Assignment/tree/Xavier/ai-history) (TODO) |
| Bernice Koh | [Stage 2 AI review](https://github.com/Cooolc327/SC2002_Group_Assignment/blob/bernice-ai-history/ai-history/stage-2/chat-01.md), [Stage 3 AI review](https://github.com/Cooolc327/SC2002_Group_Assignment/blob/bernice-ai-history/ai-history/stage-3/chat-01.md) |
| Advika Bhambri | [Advika](https://github.com/Cooolc327/SC2002_Group_Assignment/tree/Advika/ai-history) (TODO) |
| Rishika Garg | [Rishika](https://github.com/Cooolc327/SC2002_Group_Assignment/tree/Rishika/ai-history) (TODO) |

The final group ZIP is due **15 November 2026, 23:59 Singapore time** and also
needs the report with individual reflections, approximately 15-minute group
video with README timestamps, three stage diagrams, the selected sequence
diagram, accessible GitHub links and signed original-work/GAI declarations.
