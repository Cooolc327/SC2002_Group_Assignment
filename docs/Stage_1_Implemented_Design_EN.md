# Stage 1 Implemented Design

This document describes the runnable `stage-1` snapshot, not the earlier design
proposal. The source behavior is unchanged from the reviewed production game;
release work integrates the team's current class diagram, stronger CLI tests,
reproducible evidence and accurate branch/tag instructions.

## Responsibilities and object relationships

| Component | Responsibility and relevant relationships |
| --- | --- |
| Main | Creates Restaurant(Order::new), StaffTeam, GameController and RestaurantCLI |
| RestaurantCLI | Displays GameView and collects zero-based choices; formats input errors; does not enforce game rules |
| GameController | Owns turn/phase sequencing, scheduled Regular arrivals, automatic-step order and final result check; depends on StaffGateway |
| Restaurant | Coordinates customers, seating and OrderPort objects; validates cross-entity changes before mutation; derives paid statistics |
| Customer / RegularCustomer | Protect customer state and satisfaction; choose food by id; waiting loss is polymorphic through the protected abstract behavior |
| SeatingArea / WaitingQueue / Table | Create/manage two tables and the ordered queue; track occupancy until payment or abandonment |
| Menu / MenuItem | Create three immutable base dishes with integer-cent prices and prep units |
| Order / OrderPort / Factory | Order implements the contract; tracks preparation, status, milestone turns, locked bill and payment-satisfaction snapshot; factory supplied by Main |
| StaffTeam / StaffGateway | Team implements the gateway and creates one Waiter, Chef and Cashier |
| Waiter / Chef / Cashier | Enforce role-specific per-turn allowances; accepted WAIT consumes a choice; rejected actions preserve allowance |
| GameView and nested records | Detached read-only snapshots; collection components are copied, so the CLI receives no mutable entity collections |
| GameSummary / GameResult | Paid-only statistics and exact inclusive three-condition victory at turn 18 |

[Class diagram](UML/Stage1_UML_Diagram.pdf). Reference fields and associations
include one Controller-to-Restaurant and Controller-to-StaffGateway, one
Restaurant-to-Menu/SeatingArea/Factory, retained customers/orders, two tables,
three menu items and optional table occupancy. StaffTeam owns its three staff
objects; SeatingArea owns its tables/queue; Menu owns its items. Constructor
injection alone is not treated as ownership of an injected object.

## Temporal rules and state ownership

One turn runs arrival, Waiter, Chef, eating, automatic payment, waiting penalty,
abandonment and completion/result in that order. A new order may be prepared in
the same turn because the Chef sees a fresh view. Serving requires readyTurn <
currentTurn; finishing eating requires servedTurn < currentTurn; payment requires
readyToPayTurn < currentTurn. Thus cooking, serving, eating and charging cannot
collapse into one turn.

Customer state is WAITING -> SEATED -> ORDERED -> SERVED -> READY_TO_PAY -> PAID,
with LEFT possible while waiting for food/table. Order state is PLACED ->
PREPARING -> READY -> SERVED -> PAID, with CANCELLED for unfinished orders. A
one-unit dish goes directly PLACED -> READY. The Order stays SERVED while its
customer becomes READY_TO_PAY. Integer cents and a payment-time satisfaction
snapshot avoid premature revenue and averages over unpaid customers.

## Design choices and trade-offs

SRP separates boundary formatting, control sequencing, domain coordination and
entity state protection. DIP is visible in StaffGateway and OrderPort.Factory;
Main selects concrete implementations. Small action records and narrow gateway/
order contracts define actual integration needs rather than a generic framework.
Customer waiting behavior is dispatched through the abstract customer method;
Stage 1 implements only RegularCustomer and does not pre-implement VIP/Critic.

A considered alternative was letting CLI directly mutate Customer/Table/Order
and compute outcomes. The chosen coordinator adds a domain object and snapshots
but centralizes invariants, retries and testability. Two interface boundaries
add adapters/records, with the benefit that A orchestration can be tested before
other production modules arrive. Those recording/fake tests alone are not
runtime proof, so separate real staff/Main/CLI regression checks are included.

Player choices affect seating/order/serving and kitchen priorities. Waiting
instead of serving can lower satisfaction to zero and cancel an otherwise READY
order; prompt service permits the five paid customers in the victory fixture.
See [test notes](Stage_1_Test_Notes.md) for measured outcomes and the comparison.

The original proposal used different state names, a different food ordering and
a smaller set of classes. It is kept as development history rather than silently
rewritten as if it had always described the final implementation. Stage 2/3
changes, final group reflections/video and declarations are separate work.
