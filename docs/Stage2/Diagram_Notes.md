# Stage 2 diagrams and their code evidence

Prepared on 8 November 2026 from the `lichen/stage_2` working code.
Signatures and behaviour are unchanged; pre-tag preparation later removed
trailing whitespace from three new customer files. Both hash sets are recorded.
These are reviewable design artifacts, not a release/tag or a human acceptance
record. The user requested drawing after the local implementation walkthrough.

## Files and editing

| Diagram | Vector PDF | SVG | Editable diagrams.net source |
| --- | --- | --- | --- |
| Stage 2 class diagram | [PDF](../UML/Stage2_UML_Diagram.pdf) | [SVG](../UML/Stage2_UML_Diagram.svg) | [drawio](../UML/Stage2_UML_Diagram.drawio) |
| VIP complex use case | [PDF](../UML/Stage2_VIP_Sequence_Diagram.pdf) | [SVG](../UML/Stage2_VIP_Sequence_Diagram.svg) | [drawio](../UML/Stage2_VIP_Sequence_Diagram.drawio) |

Both PDFs are one continuous vector page, so text and lines remain sharp when
zoomed. The sequence is a tall diagram; use its turn bands to navigate the
scenario. Import the `.drawio` files into diagrams.net for manual editing. The
[optional generator](../UML/generate_stage2_diagrams.py) rebuilds PDF/SVG/drawio
and exports layout/event JSON with Python and ReportLab. Those documentation
tools are not used by the offline Java game. Regenerating overwrites the
exports; incorporate manual edits into the generator before regenerating.

```bash
python3 docs/UML/generate_stage2_diagrams.py
```

The [source identity](Diagram_Source_Identity.json) records SHA-256 hashes of
all 31 production Java files, the unchanged Stage 1 reference PDF, generated
artifacts, scenario outcomes and the verification scope. The identity file preserves both drawing-time and candidate source hashes,
including the whitespace-only cleanup. The pre-tag record verifies their
normalized equivalence and the actual tested source.

## Reference and class-diagram interpretation

The reference is [the existing Stage 1 diagram](../UML/Stage1_UML_Diagram.pdf).
Its black/white three-compartment classes, visibility symbols, underlined static
members, inheritance, realization, multiplicities and whole/part notation are
retained. Blue headers identify added/modified project types for Stage 2. The
layout was expanded/rearranged for clarity rather than overwriting Stage 1.

The diagram contains 37 classifiers and 52 selected relationships. It includes
boundary/control/domain objects, concrete staff and their gateway, action
records/enums, seating/queue/tables, customer inheritance, menu/order contracts,
actual pricing, GameView and summary. Key operations are shown; routine getters
and private presentation helpers are selectively omitted. Java record fields
are implicitly private/final and have public accessors of the same name. The
five nested GameView record types are listed in a note; their typed list fields
appear on GameView. Reference fields can also be shown as association roles.

Important Stage 2 relationships:

- `GameController` holds an `IntFunction<? extends Customer>`; Main binds it to
  `customers::createCustomer`. CustomerFactory does **not** implement IntFunction.
- CustomerFactory creates Customer subtypes, with a six-entry private Kind
  schedule. IDs 1-6 are separate from arrival turns 1/4/7/10/13/16.
- RegularCustomer, VipCustomer and CriticCustomer inherit Customer. Loss,
  discount and food preference dispatch through the customer object.
- Restaurant uses BillCalculator when creating an order through OrderPort.Factory;
  Order realizes OrderPort and retains the fixed bill in integer cents.
- Restaurant composes Menu, SeatingArea and its orders; SeatingArea composes two
  Tables and a WaitingQueue. Queue/table links to customers do not imply that
  they create those customers. Menu composes the three base MenuItems.
- There is no Host, ComboMeal or Happy Hour implementation in this diagram.

`getSatistfactionLossPerTurn` keeps the current code's spelling. `chooseItem`
keeps the existing polymorphic interface. Names are not silently corrected in
UML to signatures absent from the source.

## Complex use case: VIP #2 from arrival to result

Stage: 2. Seed: 24369. Starting point: turn 4 after customer #1 was served on
turn 3; Table 1 is occupied, Table 2 is free. Main has already bound the customer
creation function and `Order::new` factory. The manager follows the committed
victory input's priority rule: serve, otherwise take order, otherwise seat;
Chef prepares the oldest available order.

| Turn | VIP / Order 2 state after the turn | Payment effect |
| --- | --- | --- |
| 4 | Arrives, seats at Table 2, SEATED, satisfaction 95 | None |
| 5 | Chooses Burger; VIP discount locks 1350 cents; READY, customer ORDERED, satisfaction 90 | Customer #1 pays $9.00 |
| 6 | SERVED; satisfaction stays 90 | VIP cannot finish eating on its service turn |
| 7 | READY_TO_PAY with readyToPayTurn 7; satisfaction 90 | No VIP payment on the same turn |
| 8 | Customer and order PAID; snapshot 90; Table 2 released | VIP pays $13.50; total revenue $22.50, paid 2, average 87.00 |
| 18 | Full game finishes | VICTORY: paid 5, $64.50, satisfaction sum 402, average 80.40 |

The sequence has 100 numbered messages and uses real method names, dynamic
object creation, guards, a remaining-turn loop and alternative fragments. It
shows CLI, controller, the bound customer function, factory, StaffTeam and its
three staff objects, Restaurant, VIP, billing, the order factory, Order,
SeatingArea and result evaluation. `ref` frames abbreviate identified existing
interactions and non-VIP customers; they do not invent new Java methods.

## One changed manager choice: verified alternative

The outer `alt` compares taking VIP's order on turn 5 with choosing Wait on that
turn and resuming the same priority rule on turn 6. These are separate runs,
not two events in one game. The late order is served on turn 7, finishes eating
on turn 8 and pays on turn 9 at satisfaction 85. The whole-game result is still
VICTORY with 5 paid and $64.50, but paid satisfaction sum is 389 and the average
is 77.80. The diagram does not claim this one delay causes defeat.

Both traces were executed using real CustomerFactory, GameController, StaffTeam
and Order, then replayed through actual `Main`. The normal generated input
matches the existing victory file byte for byte. Each alternative input contains
36 choices. Reproduce from the repository root:

```bash
bash scripts/run.sh < test/replays/stage2-victory.txt
bash scripts/run.sh < test/replays/stage2-vip-wait-turn5.txt
```

This provides a concrete manager-choice effect on timing and paid satisfaction.
The independent all-WAIT defeat replay remains available in
`test/replays/stage2-defeat.txt`; it is a different scenario.

## Diagram verification and remaining work

Class names, selected signatures, visibility, static members and relationships
were checked against the current source. Both exported PDFs were rendered and
inspected in sections. Text bounds, SVG/XML syntax, diagram cell references and
pure-vector PDF content were also checked. No production Java file was changed
for the drawing step; the previously executed implementation tests remain the
code evidence recorded in [integration notes](Integration_Notes.md).

The drawing step itself did not commit or publish. Subsequent full AI-history
archival and candidate execution are recorded in the
[pre-tag verification record](../verification/Stage_2_Pre_Tag_2026-11-08.md).
The stage-2 tag remains held; the drawing-time source identity is retained.
