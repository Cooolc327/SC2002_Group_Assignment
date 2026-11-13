# Stage 3 implementation and interface handoff

This document describes the runnable implementation on `lichen/stage_3`, built
from Stage 2 main `b02de7bef0fa237a693f5c84a9b0757d43dfb023`. Bernice's
`bernice/stage-3-combo` tip `c013d2b29c78905f7431f564dc5048aafaab9ce0` is merged
with its original ancestry, including commits `877dfd6`, `0464559`, and `c013d2b`.
The remaining implementation and integration tests were added with AI assistance
on the integration branch. Module review assignments below do not attribute
these new edits to teammates who did not author them.

## Run modes

```bash
bash scripts/run.sh                 # Stage 3, required mixed-customer base setting
bash scripts/run.sh --stage3        # explicit equivalent
bash scripts/run.sh --stage2        # Stage 2 gameplay and existing replay inputs
bash scripts/run.sh --regular       # Regular-only regression mode
```

The historical `stage-1` and `stage-2` tags remain the independently runnable
stage snapshots. Regression flags exercise the current shared implementation;
they do not restore historical source code. All modes work offline with JDK 17+.

## Responsibility and review map

| Module | Responsibility | Existing team review owner |
| --- | --- | --- |
| `GameController` | Legal phases, turn-start decision, Host/Waiter/Chef sequencing, immutable snapshots, result | A / Lichen |
| `Restaurant` | Atomic seating/order/payment coordination, once-per-turn policy application, locked prices | A / Lichen |
| `HappyHourPolicy` | One activation; derives current/next-turn validity without UI-driven decrements | A / Lichen |
| `Customer`, `CriticCustomer`, seating | Guarded recovery, deterministic polymorphic food choices, queue/table consistency | B / Xavier |
| `ComboMeal`, `Discount`, `Menu`, `BillCalculator` | Composed menu items, integer-cent rounding, VIP then Happy Hour pricing | C / Bernice |
| `Host`, `StaffGateway`, `StaffTeam`, CLI | Independent Host allowance, staff adaptation, input validation and state display | B / Xavier and D / Advika |
| Tests, replays, diagrams and report integration | Requirements evidence, regression and final code/document consistency | E / Rishika, with module owners |

## Turn protocol

Stage 3 uses these phases:

`NOT_STARTED / TURN_ENDED -> AWAIT_TURN_START -> AWAIT_HOST -> AWAIT_WAITER -> AWAIT_CHEF -> AUTO -> TURN_ENDED / FINISHED`

1. `startTurn()` registers a scheduled arrival at satisfaction 100.
2. `submitTurnStartDecision(boolean)` decides whether to activate Happy Hour.
   An unused policy prompts the manager; a used policy continues automatically.
   Recovery is applied exactly once here, after arrivals and before staff act.
3. The manager chooses one Host seating task or Wait.
4. The manager chooses one Waiter task or Wait. Seating remains available to the
   Waiter. A customer seated by the Host may have their order taken this turn.
5. The Chef advances one preparation unit or waits, including on a new order.
6. Earlier-served customers finish eating; the Cashier collects one payment from
   the oldest customer who became ready on an earlier turn. Waiting penalties,
   abandonment, statistics and result evaluation follow.

A dish completed this turn can only be served on a later turn. A customer
becoming ready to pay this turn cannot pay until a later turn. The game always
finishes all 18 turns, with two tables and arrivals on 1/4/7/10/13/16.

## Interface contracts

| API | Contract |
| --- | --- |
| `new Restaurant(orderFactory, true)` | Enables Stage 3 menu and opening policy. The original one-argument constructor preserves Stage 2 rules. |
| `GameController.startTurn()` | Opens one turn; returns `AWAIT_TURN_START` in Stage 3. Invalid arrival factories are rejected before mutation. |
| `submitTurnStartDecision(activate)` | Only accepted in `AWAIT_TURN_START`. Successful decision applies recovery once and moves to `AWAIT_HOST`. Used activation is rejected without mutation. |
| `submitHostAction(HostAction)` | Only accepted in `AWAIT_HOST`; success moves to `AWAIT_WAITER`. Invalid actions retain the same phase and allowance. |
| `HostAction.seat(customerId, tableId)` / `waitTurn()` | Host has no order or food-choice action. Seating delegates to Restaurant's shared queue/table validation. |
| `StaffGateway.executeHost(action, restaurant, turn)` | Production StaffTeam delegates to its Host. The default rejection retains compatibility with older Stage 1/2 test doubles. |
| `Customer.recoverWaitingSatisfaction(points)` | Rejects negative points. Only WAITING/SEATED/ORDERED recover; clamps to 100. Other states are unchanged. |
| `Menu.getItems()` / `getCombos()` | Original three dishes and three immutable combo objects respectively. |
| `Menu.getOrderableItems()` | Three choices in the default/Stage 2 menu, six in `new Menu(true)`. Returns an immutable list. |
| `BillCalculator.billFor(customer, item, active)` | Applies customer discount first, then 20% Happy Hour discount, rounding each step to cents. Restaurant stores this value in the Order at placement. |
| `GameView.happyHour()` | Detached record of activation availability, used status, and remaining turns. Remaining duration includes the current active turn. |
| `GameView.staff().hostOptions()` | Immutable valid Host options, supplied only during the Host phase. UI cannot mutate entities. |

## Customer choice and price rules

Regular and VIP customers use `(id - 1) % orderableItems.size()` with this list:

| Index | Item | Price | Prep units |
| --- | --- | --- | --- |
| 0 | Salad | $9.00 | 1 |
| 1 | Burger | $15.00 | 1 |
| 2 | Pasta | $18.00 | 2 |
| 3 | Salad + Burger Combo | $21.60 | 2 |
| 4 | Salad + Pasta Combo | $24.30 | 3 |
| 5 | Burger + Pasta Combo | $29.70 | 3 |

Critic overrides `chooseItem(Menu)` and chooses the longest preparation time,
breaking ties by menu order: Salad + Pasta Combo in Stage 3, Pasta in Stage 2.
No controller type-check chain selects customer behaviour. The default seed
24369 still supplies Regular, VIP, Regular, Regular, Regular, Critic.

Happy Hour can be activated once at turn start and covers that turn plus the
next. Each active opening restores 15 satisfaction to waiting customers, up to
100. End-of-turn waiting losses still apply normally. Existing orders retain
their prices; expiration never recalculates a bill. Example: VIP Burger during
Happy Hour is 1500 -> 1350 -> 1080 cents. A VIP Salad + Pasta Combo is
2430 -> 2187 -> 1750 cents.

## Design choices and alternatives

- The Host is a separate staff object, rather than a second Waiter allowance.
  It can only seat, making its independent capacity explicit.
- Restaurant owns the policy, so order creation and recovery use the same
  activation state. Controller owns the legal activation phase; CLI only asks
  for input and displays immutable views.
- Remaining duration is calculated from activation turn rather than decremented
  by view reads or repeated input attempts.
- ComboMeal inherits the MenuItem contract and holds two component references.
  Existing Order/Chef/Waiter/Cashier operations therefore handle combos without
  type checks or a parallel order lifecycle.
- Explicit earlier-stage modes preserve regression evidence while Stage 3 is
  the default. Immutable historical tags retain the actual earlier source.
- An automatic first-in-queue Host was considered. A manager-selected Host was
  used so that seating priority remains a meaningful player decision. Host
  precedes Waiter; this ordering is a documented design choice, not an extra
  timing requirement imposed by the brief.

## Verification and remaining submission work

See [test notes](Stage_3_Test_Notes.md), [Stage 2 to 3 changes](Stage_2_to_3_Changes.md),
and the [editable class model](../UML/Stage3_UML_Diagram.mmd).
Automated checks are agent-run evidence, not an independent teammate signature.
Before a final release, module owners should review the implementation and
diagrams. The group still supplies its report/reflections, recorded video with
timestamps, signed declarations, and explicit approval for publishing `stage-3`.
