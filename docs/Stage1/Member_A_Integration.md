# Member A Stage 1 Implementation and Integration

Updated and verified on 31 October 2026.

This document describes the implemented member-A code, the interfaces required
by members C and D, and the executable checks for member E.

## Project location and verification

The local project used for this handoff is:

```text
/Users/edward/Desktop/NTU/Y2S1/SC2002/Group Assignment/SC2002_Group_Assignment
```

Open that folder as the project root. It contains src, test, scripts and docs.
Other group members should use their own checkout of the same repository.
Run from the project root:

```bash
bash scripts/test-member-a.sh
```

Requirements: JDK 17 or later and Bash. The script needs no network access or
external Java libraries. It compiles source and tests with Java 17 compatibility,
all compiler warnings enabled, and warnings treated as errors. Build files are
created temporarily outside the repository.

Expected final output:

```text
MEMBER_A_CORE=PASS scenarios=14 assertions=344
```

## Implementation status

Member A implements the game controller and restaurant coordination layer.
The existing B/C customer, seating and menu classes are reused. C's production
Order and D's production staff/CLI are pending integration. Only Stage 1
behaviour is implemented here.

| Member | Delivered or required next |
| --- | --- |
| A | Implemented controller phases, arrival scheduling, restaurant coordination, immutable views, statistics, final result and executable core tests |
| B | Existing customer/seating classes are reused; the integration includes the Table filename fix and seating validation/release fixes below |
| C | Connect the production Order through OrderPort.Factory and OrderPort |
| D | Connect production staff through StaffGateway; implement CLI and the game entry point |
| E | Run the tests again with production C/D adapters; synchronize the final diagrams and report |

The verified A core is ready for C/D integration. A complete interactive Stage 1
release still requires those production implementations and the CLI.

## Ownership and files

| Area | Files | Responsibility |
| --- | --- | --- |
| A control | src/control/GameController.java, TurnPhase.java, GameResult.java | Arrival schedule, action phases, automatic processing order, 18-turn limit, final result |
| A coordination | src/domain/Restaurant.java | Coordinate seating, orders, preparation, serving, eating, payment, abandonment and statistics |
| Shared action data | src/contracts/ActionResult.java, WaiterAction.java, ChefAction.java | Typed actions, acceptance flag and explanatory messages |
| C integration | src/contracts/OrderPort.java | Order access and state-transition contract, plus order factory |
| D integration | src/contracts/StaffGateway.java | Adapter to D's Waiter, Chef and Cashier |
| Shared read-only views | src/view/GameView.java, GameSummary.java | Detached immutable state for D's CLI and E's tests |
| Verification | test/integration/MemberACoreTest.java, scripts/test-member-a.sh | Executable member-A scenarios using test-only C/D substitutes |

The shared contracts make the currently missing implementations replaceable.
C/D may implement them directly or write small adapters around their own classes.
The test substitutes are private to the test class and are never production defaults.

## Two changes to B's integration surface

1. Renamed src/entities/Table.Java to Table.java so standard Java source discovery
   and compilation include the class. The Table implementation is unchanged.
2. Updated SeatingArea: removeCustomer checks queue membership before attempting
   removal, allowing seated customers to reach table release. Added seat(customer,
   tableId), which validates all conditions before mutation. The existing
   seat(customer) API remains and still chooses the first free table.

Customer state names and food choice are preserved. The implementation uses
ORDERED and SERVED where the earlier proposal used WAITING_FOR_FOOD and EATING.
The current deterministic menu choice is Salad, Burger, Pasta, repeated by
customer id. Earlier proposal replay totals based on Pasta first are superseded
by the tested replay below.

## Member D CLI contract

Create one Restaurant using C's OrderPort.Factory and one GameController using
that restaurant and D's StaffGateway adapter. The integration setup supplies
those dependencies; there is no default or fake production implementation.

Assembly uses these implemented constructors:

```java
Restaurant restaurant = new Restaurant(orderFactory);
GameController controller = new GameController(restaurant, staffGateway);
```

Here orderFactory is the OrderPort.Factory supplied by C, and staffGateway is
the StaffGateway supplied by D. Import domain.Restaurant, control.GameController
and the relevant contracts. This assembly is performed once per game.

| Controller method | Behaviour |
| --- | --- |
| startTurn() | Start the next turn, add a scheduled RegularCustomer, enter AWAIT_WAITER, and return GameView |
| getView() | Return an immutable snapshot without advancing time |
| submitWaiterAction(action) | Accept only in AWAIT_WAITER; on success enter AWAIT_CHEF; on rejection retain the phase |
| submitChefAction(action) | Accept only in AWAIT_CHEF; on success enter AUTO; on rejection retain the phase |
| finishTurn() | In AUTO, process eating, cashier, waiting penalties/abandonment, statistics and result; return the end-turn view |
| getLastPaymentResult() | Return the latest automatic cashier result for optional display |

CLI loop: startTurn, display the snapshot, retry Waiter input until accepted,
display the new snapshot, retry Chef input until accepted, finishTurn, and display
the summary. Repeat until the returned phase is FINISHED. Always complete all
18 turns, even when targets have already been met.

Use WaiterAction.seat(customerId, tableId), takeOrder(customerId), serve(orderId),
or waitTurn(). Use ChefAction.prepare(orderId) or waitTurn(). The manager never
passes a menu item. A null or invalid choice is rejected without phase progress.
Incorrect startTurn/finishTurn ordering is a programming error and throws
IllegalStateException.

Record accessors use names such as view.turn(), result.accepted(), and
view.summary().revenueCents(). Display cents as dollars with two decimal places.
GameSummary.averagePaidSatisfaction() returns OptionalDouble.empty() until a
customer pays; display N/A in that case.

GameView contains customer satisfaction/state, ordered waiting ids, table
occupancy, order status/progress/timing, menu, available staff actions, statistics
and game result. No mutable Customer, Table or Order reference is exposed.

## Member D staff contract

StaffGateway has three methods:

- executeWaiter(WaiterAction action, Restaurant restaurant, int turn)
- executeChef(ChefAction action, Restaurant restaurant, int turn)
- processPayment(Restaurant restaurant, int turn)

Each returns ActionResult. The adapter delegates to D's actual staff objects.
Record each staff member's last action turn only after success or WAIT.
Business rejections must leave staff capacity and domain state unchanged.
The controller also prevents a second accepted Waiter/Chef submission.

Waiter delegates to seatCustomer, placeOrder or serveOrder. Chef delegates to
advancePreparation. These domain methods return ActionResult for invalid
business choices; show the reason and allow another choice.

Cashier obtains findOldestPayableCustomer(turn), then calls
settlePayment(customerId, turn) when a customer qualifies. With no candidate,
return a successful no-op result. Eligibility requires readyToPayTurn < turn.
Oldest means earliest readyToPayTurn, then lowest customer id. Restaurant also
guards against two payments in one turn and an out-of-order payment.

Do not apply eating updates or waiting penalties in staff/CLI: the controller
already runs these once in their required sequence.

## Member C order contract

Implement OrderPort or adapt the production Order to it. Supply a factory with
create(orderId, customer, item, billCents). It must return a fresh PLACED order
with zero prepared units and null unset timestamps/payment satisfaction.
It must not mutate customers, tables or restaurant collections.

The order provides id, customer id, menu item, fixed bill cents, preparation
progress, status, readyTurn, servedTurn, paidTurn and satisfactionAtPayment.

- advanceOneUnit(turn): advance exactly one unit; set READY and readyTurn on completion.
- markServed(turn): require READY and readyTurn < turn; set SERVED and servedTurn.
- markPaid(turn, satisfaction): require SERVED; set PAID and retain the payment snapshot.
- cancel(): allow PLACED, PREPARING or READY to become CANCELLED.

Legal transitions must complete atomically. Failed transitions must not partially
change the order. Preserve the chosen item's reference and locked bill.
Stage 1 uses the full item price; future discount policies belong to later stages.

Restaurant coordinates the related Customer and Table transitions. Order methods
must not also move customers or release tables. A customer becoming READY_TO_PAY
does not change Order from SERVED until payment.

## Exact turn sequence

1. Begin turn and add the scheduled arrival on turns 1, 4, 7, 10, 13 or 16.
2. Waiter action or WAIT. Invalid choices retain this phase.
3. Chef action or WAIT. Newly taken orders are eligible for preparation.
4. Advance eating for customers served on an earlier turn.
5. Cashier handles at most one customer ready to pay on an earlier turn.
6. Apply waiting penalties and coordinate abandonment, order cancellation and table release.
7. Record the completed turn; decide VICTORY or DEFEAT only after turn 18.

Preparation completed this turn can be served next turn at the earliest.
Serving is followed by one full turn of eating and payment on a later turn.
Only PAID orders contribute revenue and average satisfaction. Paid and abandoned
records remain available for statistics. Satisfaction has a lower bound of zero.

## Verification and reproducible runs

Run bash scripts/test-member-a.sh from the repository root. Java 17 compatibility,
all compiler warnings and warnings-as-errors are enabled. Build output is temporary.

The suite contains 14 scenarios and 344 assertions: arrival schedule, full-length
defeat, retries and phase limits, same-turn preparation, delayed serving/eating/
payment, two-unit Pasta, full-length victory, cancellation in all three unserved
order states, FIFO/tied payments, snapshot immutability, failed factory atomicity,
seating atomicity and victory thresholds.

These are executed A-core results with test doubles, not a final production-game
demonstration. E should rerun equivalent scenarios using C/D production adapters.

### Victory replay using the current menu rule

All unlisted Chef choices are WAIT. Customer order ids are 1 through 6.

| Turn | Waiter action | Chef preparation | Automatic payment |
| --- | --- | --- | --- |
| 1 | Seat C1 at T1 | WAIT | None |
| 2 | Take C1's Salad order | Order 1 | None |
| 3 | Serve order 1 | WAIT | None |
| 4 | Seat C2 at T2 | WAIT | None |
| 5 | Take C2's Burger order | Order 2 | C1 $9 |
| 6 | Serve order 2 | WAIT | None |
| 7 | Seat C3 at T1 | WAIT | None |
| 8 | Take C3's Pasta order | Order 3, unit 1 | C2 $15 |
| 9 | WAIT | Order 3, unit 2 | None |
| 10 | Serve order 3 | WAIT | None |
| 11 | Seat C4 at T2 | WAIT | None |
| 12 | Take C4's Salad order | Order 4 | C3 $18 |
| 13 | Serve order 4 | WAIT | None |
| 14 | Seat C5 at T1 | WAIT | None |
| 15 | Take C5's Burger order | Order 5 | C4 $9 |
| 16 | Serve order 5 | WAIT | None |
| 17 | Seat C6 at T2 | WAIT | None |
| 18 | Take C6's Pasta order | Order 6, unit 1 | C5 $15 |

Verified result: VICTORY; 5 paid, 5 served, $66.00 revenue, 79.20 average paid
satisfaction, 0 unhappy departures, 18 turns. The paid satisfaction values are
84, 84, 76, 76 and 76. Targets are already met at turn 15; play still continues.
C6 remains unpaid with one Pasta preparation unit completed.

### Defeat replay

Choose WAIT for Waiter and Chef on all 18 turns. Verified result: DEFEAT; 0 paid,
$0.00 revenue, average N/A, and 2 unhappy departures. C1 leaves on turn 13
and C2 on turn 16.

## Remaining group integration

C supplies the production Order factory/adapter. D supplies production staff,
their adapter, the CLI and the game entry point. E runs production integration
tests and updates diagrams/report to match actual packages and contracts.
The earlier UML remains a proposal until synchronized with the integrated code.
Create stage-1 only after the real playable version independently compiles and
reproduces both endings. No Stage 2/3 features are included in this implementation.
