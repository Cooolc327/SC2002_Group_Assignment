# Stage 1 to Stage 2 change analysis

Baseline: [`stage-1`](https://github.com/Cooolc327/SC2002_Group_Assignment/tree/stage-1), commit
`ac445a8f956232232188fd765eed98a5e8b49c46`.
Candidate: personal branch [`lichen/stage_2`](https://github.com/Cooolc327/SC2002_Group_Assignment/tree/lichen/stage_2).
[Branch comparison](https://github.com/Cooolc327/SC2002_Group_Assignment/compare/stage-1...lichen/stage_2).
An immutable tested-implementation compare link is recorded after its commit
exists in the [verification record](../verification/Stage_2_Pre_Tag_2026-11-08.md).
No stage-2 tag is implied by a personal branch.

## New production files

| File | Responsibility / justification |
| --- | --- |
| `src/entities/VipCustomer.java` | B: waiting loss 5, type name and 10% discount |
| `src/entities/CriticCustomer.java` | B: waiting loss 12, longest-preparation preference, inherited full-price bill |
| `src/entities/CustomerFactory.java` | B: seeded six-type schedule; A: positive arrival-order IDs / bounds, separate from turns |
| `src/orders/BillCalculator.java` | C: integer-cent price calculation and rounding through Customer's discount hook; A: long intermediate |

## Existing production files modified

| File | Modification and reason |
| --- | --- |
| `src/entities/Customer.java` | Add a default zero-discount hook; keep existing chooseItem(Menu) and state guards |
| `src/fooditems/MenuItem.java` | B prep-unit getter used by Critic; existing prepUnits() remains compatible |
| `src/domain/Restaurant.java` | Call BillCalculator at order creation; pass and display the locked bill; service/payment/abandonment rules retained |
| `src/control/GameController.java` | Inject IntFunction<Customer subtype>; validate a created arrival before beginning the turn; old constructor retains Regular fixtures |
| `src/Main.java` | Wire the seeded mixed factory; explicit --regular regression mode |
| `src/cli/RestaurantCLI.java` | Display stage/setup/behaviour help; no customer-type domain decisions in UI |

No production files were deleted. `StaffTeam`, Waiter/Chef/Cashier, OrderPort,
Order, queue/table/seating state machinery, GameConfig, TurnPhase, GameResult
and immutable views retain their Stage 1 implementation. Food choice and billing
remain domain work, not new UI decisions.

## Test, tooling, design and history files

- New `test/unit/BillCalculatorTest.java` and `OrderMenuTest.java` come from C;
  the pricing fixture now uses real VipCustomer instead of a placeholder.
- New `test/integration/Stage2IntegrationTest.java` and `scripts/test-stage2.sh`
  exercise real mixed customers, staff, orders and Main.
- `test/integration/Stage1SmokeTest.java` now explicitly runs Main --regular;
  it retains all previous expected outcomes. `scripts/build.sh` identifies the
  current build as Stage 2. Existing run/test scripts remain portable.
- New Stage 2 victory/defeat and delayed-VIP inputs live under test/replays/;
  its README distinguishes the current mode from the historical Stage 1 tag.
- New Stage 2 class and complex VIP sequence diagrams include PDF, SVG, editable
  drawio, optional documentation generator and source/layout/event evidence.
  Stage1_UML_Diagram.pdf remains unchanged. The Python documentation generator
  is not a mandatory runtime/build dependency of the Java game.
- New Stage2 acceptance/test/change notes and execution logs document the
  transition. Root/docs/UML indices link them rather than leaving stale status.
- ai-history/stage-2 contains the full original main-session continuation with
  an explicit cutoff and hashes. Stage 1's full transcripts remain unchanged;
  the continuation also covers their closing gap. No other member is impersonated.
- `.gitattributes` marks PDFs as binary, preventing line-ending conversion and
  meaningless text-whitespace diffs. Trailing whitespace was removed from the
  three new customer classes without changing normalized lines or Java tokens.
- B's proposed move of existing Stage 1 document paths is not applied: retaining
  those paths preserves existing links. B/C source commits remain in ancestry.

## Effect on the OO design

Encapsulation: state still changes through guarded entity/domain methods, and
Order retains a final billCents. Boundary views remain detached immutable records.
Polymorphism: Customer.applyWaitingPenalty calls the subtype loss method;
Restaurant calls chooseItem and BillCalculator calls getBillDiscountPercent.
CustomerFactory contains the registration switch, not a turn-controller type
chain. Factory is not an implementation of Java IntFunction; Main binds its
method to the injected function.

SRP: customer subclasses own their variations; Factory selects types; billing
calculates money; Controller sequences; Restaurant coordinates; CLI displays.
OCP: variations are localized, but adding a type still requires explicit factory
registration, which is an acknowledged setup change. LSP: all types follow the
same state and service contracts. ISP/DIP: the existing StaffGateway and OrderPort
boundaries remain in use; no new custom interface is invented just for a pattern.

Trade-off: a standard IntFunction keeps the new wiring small but is less named
than a project-specific customer-source interface. A controller switch was
considered and rejected because it would put per-type behaviour into sequencing.
The seeded shuffle is reproducible and always has both new types, but the
mandatory six-arrival demonstration is deliberately small, not a general random
simulation. There are no speculative Stage 3 features in this snapshot.

## What still works and what changed for the player

Regular core/smoke expected results remain 14/344 and 9/94; baseline victory is
5 paid / $66.00 / 79.20. Mixed Stage 2 victory is 5 / $64.50 / 80.40: the VIP
Burger discount reduces revenue by $1.50 while lower waiting loss improves its
snapshot from 84 to 90. A single Waiter WAIT on turn 5 delays VIP payment to
turn 9 and lowers the final average to 77.80. Both runs still win; the separate
all-WAIT replay demonstrates Defeat. Staff constraints and all three original
win criteria remain the same. See [test notes](Stage_2_Test_Notes.md) and the
[sequence explanation](Diagram_Notes.md) for reproducible choices and outcomes.
