# Stage 2 to Stage 3 changes

Baseline: main `b02de7b`, containing the published `stage-2` implementation.
Integration branch: `lichen/stage_3`. No historical stage tag is moved.

## Added files

| Files | Reason |
| --- | --- |
| `src/fooditems/ComboMeal.java`, `Discount.java` | Bernice's composed menu item and shared integer-cent discount calculation |
| `src/contracts/HostAction.java`, `src/staff/Host.java` | A separate seating-only action and per-turn allowance |
| `src/domain/HappyHourPolicy.java` | Encapsulates one-time activation and derives remaining duration from the turn |
| `test/unit/ComboMealTest.java` | Bernice's construction, rounding, containment and preparation tests |
| `test/integration/Stage3IntegrationTest.java` | Production-flow feature boundaries, fixed outcomes and real CLI replays |
| `scripts/test-stage3.sh`, `test/replays/stage3-*.txt` | Offline checks and reproducible victory/defeat inputs |
| `docs/Stage3/*`, `docs/UML/Stage3_UML_Diagram.mmd` | Current interfaces, decisions, change analysis, test evidence and editable class model |

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
- Responsibility/DIP: Controller owns phase sequencing and uses StaffGateway
  and OrderPort.Factory boundaries. It does not inspect concrete customer types
  or calculate bills. Restaurant coordinates cross-entity effects.
- Stable extension still requires small setup/UI changes. No claim is made that
  every class is closed to modification or that all SOLID principles follow
  automatically from adding an interface.

The retained Stage 2 VIP sequence diagram documents its tagged historical
scenario. For a final Stage 3 sequence, opening and Host messages must be added;
do not relabel the old diagram as matching the new default flow.
