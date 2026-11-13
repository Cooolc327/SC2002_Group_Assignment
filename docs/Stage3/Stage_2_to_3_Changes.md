# Stage 2 to Stage 3 changes

Stage 2 source baseline: tag `stage-2`, commit `ad6f39b`.
Stage 3 implementation: `3f5e682` on `lichen/stage_3`, based on the Stage 2 merge
into main (`b02de7b`). The difference between those Stage 2 baselines is the
merge and README integration, not a different gameplay implementation.

- [Exact Stage 2 to Stage 3 implementation comparison](https://github.com/Cooolc327/SC2002_Group_Assignment/compare/stage-2...3f5e682f775734a6a305fc159abd89aaf76257c3)
- [Stage 3 implementation commit](https://github.com/Cooolc327/SC2002_Group_Assignment/commit/3f5e682f775734a6a305fc159abd89aaf76257c3)

Documentation exports may have later commits. The diagrams and gameplay values
below describe the identified implementation. Historical tags are preserved;
`stage-3` publication remains a separate approved release step.

## Behaviour before and after

| Area | Stage 2 | Stage 3 | Why it changed |
| --- | --- | --- | --- |
| Staff capacity | Waiter seats, orders or serves once; Chef prepares once | A separate Host can seat once, while Waiter keeps its task | Adds an independent role and permits same-turn Host seating then Waiter ordering |
| Turn protocol | Arrival, Waiter, Chef, automatic steps | Arrival, opening policy, Host, Waiter, Chef, automatic steps | Makes activation timing and independent staff phases explicit |
| Available menu | Salad, Burger, Pasta | The three dishes plus all three two-item combos | Adds composed menu choices with summed preparation units |
| Regular/VIP choice | ID cycles through three dishes | ID cycles through six orderable items | Documented deterministic rule can select combos |
| Critic preference | Longest prep time: Pasta | Longest prep time: Salad + Pasta Combo; first menu item breaks ties | The same polymorphic preference applies to the extended menu |
| Pricing | Customer-specific discount, locked at ordering | Combo price first; VIP discount; then active Happy Hour 20% off | New menu and pricing policy compose without changing the Order lifecycle |
| Satisfaction | End-of-turn waiting loss only | Each active opening restores 15 for waiting customers, cap 100; losses still apply | Adds the required Happy Hour recovery without affecting served or departed customers |
| Display and input | Waiter and Chef prompts | Opening choice, Host prompt, six-item menu and remaining duration | Makes the new decisions and state visible; invalid input still retries |

## Price examples and unchanged base rules

- Salad + Pasta Combo: (900 + 1800) x 90% = 2430 cents; prep units 1 + 2 = 3.
- VIP Burger during Happy Hour: 1500 -> 1350 -> 1080 cents.
- VIP Salad + Pasta Combo during Happy Hour: 2430 -> 2187 -> 1750 cents,
  rounding each discount step to the nearest cent.
- Existing orders keep the amount stored when placed; expiration does not
  reprice unpaid orders. Recovery excludes SERVED, READY_TO_PAY, PAID and LEFT.
- Both stages retain 18 turns, two tables, six scheduled arrivals, independent
  Waiter/Chef/Cashier limits, deferred serving/payment, abandonment rules and
  the same three victory thresholds. Required gameplay remains offline.

## Effect of the manager's choice

| Reproducible run | Paid | Revenue | Average paid satisfaction | Result |
| --- | --- | --- | --- | --- |
| Stage 2 stored victory | 5 | $64.50 | 80.40 | VICTORY |
| Stage 3 service priorities, Happy Hour unused | 5 | $86.40 | 86.20 | VICTORY |
| Same Stage 3 priorities, activate on turn 4 | 5 | $83.70 | 87.20 | VICTORY |

The Stage 2 and Stage 3 rows use their documented stage-specific strategies;
this is not a controlled measure of a single feature. The two Stage 3 rows are
a controlled manager-choice comparison: the same service priorities, with only
activation changed. Turn-4 activation sacrifices $2.70 and raises average paid
satisfaction by 1.00. Both runs win; the diagram does not claim that this choice
causes defeat.

## Added files

| Files | Reason |
| --- | --- |
| `src/fooditems/ComboMeal.java`, `Discount.java` | Bernice's composed menu item and shared integer-cent discount calculation |
| `src/contracts/HostAction.java`, `src/staff/Host.java` | A separate seating-only action and per-turn allowance |
| `src/domain/HappyHourPolicy.java` | Encapsulates one-time activation and derives remaining duration from the turn |
| `test/unit/ComboMealTest.java` | Bernice's construction, rounding, containment and preparation tests |
| `test/integration/Stage3IntegrationTest.java` | Production-flow feature boundaries, fixed outcomes and real CLI replays |
| `scripts/test-stage3.sh`, `test/replays/stage3-*.txt` | Offline checks and reproducible victory/defeat inputs |
| `docs/Stage3/*`, Stage 3 diagram models and exports | Interfaces, change analysis, test evidence, vector diagrams and editable source |
| `ai-history/stage-3/*` | Available ordered AI conversation, verbatim message export and explicit cutoff audit |

## Modified files

| Files | Change and reason |
| --- | --- |
| `src/fooditems/Menu.java` | Bernice's three combos plus an explicit immutable orderable list; default constructor keeps the three-item baseline |
| `src/orders/BillCalculator.java` | Bernice's VIP-then-Happy-Hour overload; two-argument billing remains the non-Happy-Hour API; rounding moved to Discount |
| `src/entities/Customer.java`, `CriticCustomer.java` | Select from the current orderable list through polymorphism; guarded waiting-only satisfaction recovery |
| `src/domain/Restaurant.java` | Stage 3 setup, once-per-turn recovery, Host options and real locked billing using current policy |
| `src/control/GameController.java`, `TurnPhase.java` | Explicit opening/Host phases and guarded decision/action entry points |
| `src/contracts/StaffGateway.java`, `src/staff/StaffTeam.java` | Production Host adaptation; default rejection preserves earlier test doubles |
| `src/view/GameView.java` | Immutable Host choices and Happy Hour status, plus current mode |
| `src/cli/RestaurantCLI.java`, `src/Main.java` | Stage 3 default, opening/Host input, menu/policy display and explicit regression modes |
| `scripts/build.sh` | Build marker reflects the current default stage |
| `test/unit/BillCalculatorTest.java` | Bernice's shared rounding and composed discount tests |
| `test/integration/Stage2IntegrationTest.java` | Runs the historical CLI input format with explicit `--stage2`; fixed Stage 2 expectations are retained |
| README/index files | Current commands, stage status, interfaces and evidence links |

No production files are deleted. Order, Waiter, Chef, Cashier, SeatingArea,
Table, queue, arrival factory, base configuration and victory criteria retain
their existing implementations. Their behaviour is exercised by the earlier
regressions and new Stage 3 tests.

## OO impact and trade-offs

- Encapsulation: HappyHourPolicy protects activation history; Customer protects
  satisfaction/state; Restaurant applies recovery once per opening and creates
  an Order with a final locked amount. CLI receives only detached records.
- Polymorphism/LSP: ComboMeal supplies the MenuItem price/preparation contract,
  so existing Order and staff flow handles it. Critic continues to override
  customer choice; VIP supplies its discount through the customer contract.
- Composition: StaffTeam owns its independent staff, Restaurant owns its policy,
  and ComboMeal references the two component menu items. Components remain
  shared MenuItems, so the UML component association is not exclusive ownership.
- SRP/DIP: Controller owns phase sequencing and depends on StaffGateway and
  the customer-factory function. Restaurant uses OrderPort.Factory for fresh
  orders and coordinates cross-entity effects. Controller does not inspect
  concrete customer types or calculate bills.
- OCP is partial: ComboMeal extends MenuItem without a new Order lifecycle, but
  setup, policy sequencing and UI still require targeted edits to existing
  classes. Broadly claiming all code is closed to modification would be wrong.
- No new ISP claim is made: adding executeHost broadens StaffGateway, while its
  default rejection preserves old test doubles. Compatibility is distinct from
  segregating client-specific interfaces.
- Alternative considered: an automatic FIFO Host. A manager-selected seating
  action was retained so seating priority remains a player decision. It acts
  before Waiter, allowing same-turn ordering while keeping separate allowances.

## Diagrams and validation evidence

The Stage 3 class diagram adds Host/HostAction, HappyHourPolicy,
ComboMeal/Discount and policy/staff snapshot records. It updates Menu,
BillCalculator, Customer, Controller, Restaurant, CLI and staff contracts.
Blue headers identify new or modified Stage 3 abstractions. Visibility,
static members, inheritance, realization, ownership, shared references and
multiplicities are shown. Reference-valued fields may appear as association
roles instead of duplicate attribute rows.

The Stage 3 Happy Hour sequence includes arrival, Host seating, Waiter order,
Chef preparation, READY then later SERVED, eating, deferred payment/table
release, and the turn-18 result. Its activation alternative changes both the
locked bill and recorded satisfaction. Factory method references and omitted
subinteractions are identified explicitly. The old Stage 2 VIP diagram remains
historical evidence for its own tag.

- [Stage 3 class diagram](../UML/Stage3_UML_Diagram.pdf)
- [Stage 3 Happy Hour sequence](../UML/Stage3_Happy_Hour_Sequence.pdf)
- [Detailed replay timeline and checks](Stage_3_Test_Notes.md)
- [Source identity and verification](../verification/Stage_3_Implementation_2026-11-13.md)

All three stages' current regression/feature suites passed: 76 unique scenarios
and 1,649 assertions, with the repeated billing suite counted once. These are
agent-run checks, not a teammate acceptance signature. A runnable candidate is
not the final group report, video, declarations or authorized release tag.
