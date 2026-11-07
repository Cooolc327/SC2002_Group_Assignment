# Stage 1 Design Proposal

This proposal gives our five-member team a shared starting point for discussing class responsibilities, relationships, method contracts, and turn rules before implementation. It covers Stage 1 only. No game implementation has been written or executed as part of this design work.

The gameplay requirements come from the *SC2002 Restaurant Rush Group Assignment*, particularly Sections 2, 3, 4.1, 4.4, and 5. The class structure, method names, state names, storage choices, ownership allocation, and fixed food-selection rule below are proposed team decisions, subject to agreement.

## 1 Scope and Base Configuration

| Item | Stage 1 Specification |
| --- | --- |
| Game length | Run all 18 turns, with no early ending. |
| Tables | Two tables, labelled T1 and T2. |
| Staff | One Waiter, one Chef, and one Cashier. Per turn, they may respectively complete at most one task, advance one preparation unit, and collect one payment. |
| Arrivals | One Regular customer arrives on turns 1, 4, 7, 10, 13, and 16, labelled C1 to C6. |
| Satisfaction | Each customer starts at 100. At turn end, customers who are waiting or seated and have not been served lose 8 points, down to a minimum of 0. A customer who reaches 0 leaves without paying. |
| Menu | Salad: 900 cents, 1 preparation unit. Burger: 1500 cents, 1 preparation unit. Pasta: 1800 cents, 2 preparation units. |
| Proposed food-selection rule | C1 and C4 choose Pasta; C2 and C5 choose Burger; C3 and C6 choose Salad. Document this deterministic rule in the README. The player decides when to take an order; the customer chooses the food. |
| Victory | At the end of turn 18, all three conditions must hold: at least 4 paid customers, revenue of at least 4500 cents, and average satisfaction of paid customers of at least 60. |
| Stage 1 scope | Regular customers, the base menu, the three staff roles, and the complete game flow. Later-stage features are added in their respective stages. |

## 2 Class Responsibilities and Ownership

We propose 10 main classes, supported by a small number of enums and read-only data objects. Fields are private by default, and entity state changes go through controlled methods.

| Class | Layer / Owner | Main Data | Responsibilities and Boundaries |
| --- | --- | --- | --- |
| RestaurantCLI | Boundary / D | GameController reference; input/output objects | Display state, collect choices, show rejection reasons, and prompt again. The control and domain layers validate game rules. |
| GameController | Control / A | Current turn; current phase; Restaurant and the three staff objects | Schedule arrivals, Waiter and Chef actions, eating and payment, satisfaction updates, and result checks. Control when each type of action is accepted. |
| Restaurant | Domain coordination / A | All customers; waiting queue; two tables; all orders; three menu items | Find domain objects and coordinate changes involving multiple entities. Keep seating, ordering, serving, payment, and abandonment consistent. Produce statistics. |
| Customer | Entity / B | id; satisfaction; status; readyToPayTurn | Represent a Regular customer's state. Determine food choice, waiting penalty, and the Regular customer's bill. Protect legal customer state transitions. |
| Table | Entity / B | id; occupant | Manage occupancy and release. Prevent an occupied table from being assigned again. |
| MenuItem | Immutable domain object / C | name; priceCents; preparationUnits | Describe one base menu item. Its data remains unchanged after creation. |
| Order | Entity / C | id; customer; item; billCents; preparedUnits; status; readyTurn; servedTurn; paidTurn; satisfactionAtPayment | Manage preparation progress and order state. Store the price fixed at ordering, relevant turn numbers, and satisfaction recorded at payment. |
| Waiter | Domain role / D | lastActionTurn | Accept one seating, order-taking, serving, or WAIT action per turn. Use Restaurant to coordinate related changes. |
| Chef | Domain role / D | lastActionTurn | Advance one valid order by one preparation unit per turn, or WAIT. |
| Cashier | Domain role / D | lastActionTurn | Automatically process the customer who became eligible for payment earliest, with at most one payment per turn. |

Member E leads test and replay planning, read-only data-object definitions, class diagram preparation, and report integration. Every member contributes the design explanation and test records for their own module.

### Responsibility Boundaries

- **GameController** determines the current turn phase and whose action comes next.
- **Staff objects** enforce each role's per-turn allowance.
- **Restaurant** coordinates the entities that must change together during an operation.
- **Customer, Table, and Order** protect their internal state and legal transitions.

For example, Cashier initiates a payment. Restaurant coordinates marking the Order and Customer as PAID and releasing the Table. Revenue is derived from paid orders, and the CLI displays the result.

## 3 Class Relationships

| Relationship | Multiplicity and Meaning |
| --- | --- |
| RestaurantCLI → GameController | The CLI operates the game through one controller. It does not receive mutable domain collections. |
| GameController ◆— Restaurant | One game controller owns one Restaurant. |
| GameController ◆— Waiter / Chef / Cashier | Each game has one object for each staff role. |
| Restaurant ◆— Customer | Between 0 and 6 customers are created during a game. Records remain available after payment or abandonment. |
| Restaurant ◆— Table | The base configuration contains exactly two tables. |
| Restaurant ◆— Order | A game has between 0 and 6 orders. Paid and cancelled orders remain available as records. |
| Restaurant ◆— MenuItem | The game owns three immutable base menu items. |
| Table → Customer | A table has 0 or 1 occupant at a time. A customer occupies 0 or 1 table at a time. |
| Order → Customer | Each order belongs to exactly one customer. A customer has 0 or 1 order in Stage 1. |
| Order → MenuItem | Each order references one base menu item. A menu item may be referenced by multiple orders. |
| Waiter / Chef / Cashier → Restaurant | Staff use Restaurant to coordinate domain operations. |

The diamond symbol denotes composition. Ordinary arrows indicate references or calls.

The waiting queue can be an ordered collection inside Restaurant; a separate WaitingQueue class is unnecessary for this proposal. Display customers in arrival order, while allowing the player to choose any customer who is still waiting for seating.

### Storage Conventions

- Table stores its occupant.
- Order stores its customer and menu item.
- Restaurant finds a customer's table or order through queries.
- Customer does not duplicate table or order references, reducing the risk of inconsistent references.
- The queue refers to the same Customer objects held in the customer registry; it does not contain separate customer copies.

Encapsulation and composition provide the main structure in Stage 1. This proposal introduces neither a Staff superclass nor additional Java interfaces solely to demonstrate a pattern. Customer exposes meaningful behaviour methods that can be overridden by customer subclasses in Stage 2.

## 4 States and Ownership of State Changes

### 4.1 CustomerStatus

| State | Meaning and Normal Next State |
| --- | --- |
| WAITING | In the queue. Moves to SEATED when assigned a table. |
| SEATED | Seated, with no order yet. Moves to WAITING_FOR_FOOD when the order is taken. |
| WAITING_FOR_FOOD | An order exists; the customer is waiting for preparation or serving. Moves to EATING when served. |
| EATING | Food has been served. Moves to READY_TO_PAY during the eating step of the following turn. |
| READY_TO_PAY | Eating is complete, but payment is outstanding. Moves to PAID when payment is processed on a later turn. |
| PAID | Payment is complete. Terminal state. |
| LEFT | Satisfaction reached 0 and the customer left without paying. Terminal state. |

WAITING, SEATED, and WAITING_FOR_FOOD customers lose satisfaction at turn end and may enter LEFT. EATING, READY_TO_PAY, and PAID customers lose no further satisfaction.

### 4.2 OrderStatus

**PLACED → PREPARING → READY → SERVED → PAID**

- A one-unit dish can move directly from PLACED to READY. PREPARING means preparation has started but is incomplete.
- A PLACED, PREPARING, or READY order becomes CANCELLED if its customer leaves.
- The order remains SERVED while the customer is READY_TO_PAY. Customer represents eating and payment eligibility.
- readyTurn, servedTurn, and paidTurn are absent until their corresponding events occur. satisfactionAtPayment is absent until payment.

### 4.3 Coordinated Changes

| Operation | Entry Point and Coordinator | Changes That Must Complete Together |
| --- | --- | --- |
| Arrival | GameController → Restaurant | Register the customer and add them to the queue with satisfaction 100. |
| Seating | Waiter → Restaurant | Remove the customer from the queue, occupy a free table, and move the Customer to SEATED. |
| Taking an order | Waiter → Restaurant | Let Customer choose food; create an Order with a fixed bill; move Customer to WAITING_FOR_FOOD. |
| Preparation | Chef → Restaurant → Order | Advance one preparation unit. On completion, set READY and record readyTurn. |
| Serving | Waiter → Restaurant | Set Order to SERVED and record servedTurn; move Customer to EATING. |
| Finishing eating | GameController → Restaurant | Move eligible EATING customers to READY_TO_PAY and record readyToPayTurn. |
| Payment | Cashier → Restaurant | Set Order and Customer to PAID, record paidTurn and satisfaction at payment, and release the table. |
| Satisfaction loss and abandonment | GameController → Restaurant | Apply the penalty. At 0, remove the customer from the queue or release their table, cancel any unserved order, and mark the customer LEFT. |

Validate all conditions before applying any changes. A rejected action must leave entities, identifier allocation, turn phase, and staff allowance unchanged.

## 5 Interface Contracts

Here, an interface means a method-level agreement between modules. It does not require the Java `interface` keyword. The following entries specify signatures and behaviour only; they contain no method implementations.

### 5.1 CLI and Controller

| Proposed Method | Preconditions and Outcome |
| --- | --- |
| GameController.startTurn() → GameView | Allowed before the first turn or after the previous turn ends. Increment the turn, add scheduled arrivals, enter AWAIT_WAITER, and return the current view. |
| GameController.getView() → GameView | Read-only query. Calling it never advances the turn. |
| GameController.submitWaiterAction(action) → ActionResult | Accepted only in AWAIT_WAITER. A successful action or WAIT moves the phase to AWAIT_CHEF. A rejected action keeps the current phase. |
| GameController.submitChefAction(action) → ActionResult | Accepted only in AWAIT_CHEF. A successful action or WAIT moves the phase to AUTO. A rejected action keeps the current phase. |
| GameController.finishTurn() → GameView | Accepted only in AUTO. Process eating, automatic payment, satisfaction loss and abandonment, statistics, and the result check. Enter TURN_ENDED or FINISHED. |
| RestaurantCLI.run() | Using its supplied controller reference, repeatedly display GameView, collect Waiter and Chef choices, show ActionResult, and finish the turn. Display the final outcome when the game ends. |

TurnPhase values are **NOT_STARTED, AWAIT_WAITER, AWAIT_CHEF, AUTO, TURN_ENDED, and FINISHED**. No further startTurn call is allowed after turn 18 is completed.

The CLI submits and displays the Waiter's action result before collecting the Chef's choice. This allows the Chef to work on an order created in the same turn. The turn's action screen includes customers who arrived at the start of that turn.

### 5.2 Action Requests and Read-Only Data Objects

| Object or Action | Fields or Values |
| --- | --- |
| WaiterAction | type: SEAT, TAKE_ORDER, SERVE, or WAIT. |
| SEAT parameters | customerId and tableId. |
| TAKE_ORDER parameters | customerId. The player supplies no menu-item choice. |
| SERVE parameters | orderId. |
| WAIT parameters | None. |
| ChefAction | type: PREPARE or WAIT. PREPARE includes orderId. |
| ActionResult | accepted: whether the action was accepted; message: success information or a specific rejection reason. |
| GameView | Immutable snapshot containing the turn, phase, queue, tables, customer satisfaction and states, orders, menu, available staff tasks and targets, statistics, and GameResult. |
| GameResult | IN_PROGRESS, VICTORY, or DEFEAT. |

These objects may be simple immutable classes or records; the team should agree on a consistent form. GameView must not expose mutable Customer, Table, or Order objects or their backing collections. The CLI may handle input-format errors, such as a non-numeric identifier, while the control and domain layers validate game rules.

### 5.3 Controller and Staff

| Proposed Method | Contract |
| --- | --- |
| Waiter.execute(action, restaurant, turn) → ActionResult | Check the per-turn allowance, then delegate seating, order-taking, or serving to Restaurant. Record lastActionTurn only after success or WAIT. |
| Chef.execute(action, restaurant, turn) → ActionResult | Check the allowance. Only PLACED or PREPARING orders can advance, by exactly one unit. Record lastActionTurn only after success or WAIT. |
| Cashier.processPayment(restaurant, turn) → ActionResult | Automatically choose the customer who became eligible for payment earliest. Process at most one payment. With no eligible customer, make no domain changes. After a successful payment, reject further payments in the same turn. |

### 5.4 Staff and Controller Calls to Restaurant

| Proposed Method | Validation and Coordinated Changes |
| --- | --- |
| registerArrival(customer) | Require a unique id and initial WAITING state. Register and enqueue the customer. Called only during the arrival step. |
| seatCustomer(customerId, tableId) → ActionResult | Require a customer still in the queue and a free table. Update the queue, table, and customer state together. |
| placeOrder(customerId) → ActionResult | Require a seated customer with no existing order. Call Customer's food-choice and quotation methods, create the order, and update the customer state. |
| advancePreparation(orderId, turn) → ActionResult | Require an active order with incomplete preparation. Advance one unit and record readyTurn if preparation completes. |
| serveOrder(orderId, turn) → ActionResult | Require READY, readyTurn < turn, and a customer still occupying a table. Set SERVED and EATING. |
| advanceEating(turn) | Process only EATING customers whose servedTurn < turn. Set READY_TO_PAY and readyToPayTurn. |
| findOldestPayableCustomer(turn) → Optional customer identifier | Require READY_TO_PAY and readyToPayTurn < turn. Select by ascending readyToPayTurn, then ascending customerId as the tie-breaker. |
| settlePayment(customerId, turn) → ActionResult | Require payment eligibility, a consistent order/table assignment, and no previous payment. Record the payment snapshot and release the table. |
| applyWaitingPenalty(turn) | Apply the penalty to the three unserved customer states. Coordinate abandonment at 0. Call exactly once per turn. |
| getSummary() → Read-only statistics | Return servedCount, paidCount, unhappyDepartures, revenueCents, and averagePaidSatisfaction without changing state. |

Staff objects enforce their own action allowances. GameController routes player tasks through the appropriate staff object. Restaurant calls controlled state-changing methods on Customer, Table, and Order. The CLI receives no unrestricted state or revenue setters.

### 5.5 Key Customer and Order Behaviours

| Proposed Method | Stage 1 Meaning |
| --- | --- |
| Customer.chooseMenuItem(items) → MenuItem | Apply the fixed rule in Section 1 when a valid order is taken. Return an item from the supplied menu. |
| Customer.waitingPenalty() → int | Return 8. Customer subclasses may override this in Stage 2. |
| Customer.quoteBillCents(item) → int | Return the menu item's full price for a Regular customer. Store it in Order.billCents when the order is taken. |
| Order.advanceOneUnit(turn) | Controlled internal change. Never exceed the required preparation units. Set READY when complete. |
| Order.markServed(turn) | Controlled internal change. Require READY and readyTurn < turn. |
| Order.markPaid(turn, satisfaction) | Controlled internal change. Record the payment snapshot exactly once; retain it unchanged thereafter. |
| Order.cancel() | Controlled internal change. Allow cancellation only for an active, unserved order. |

Customer transitions and Table occupancy/release are also controlled by the domain layer. Final Java visibility is determined when the package structure is agreed. Expose only the necessary queries and business operations.

## 6 Exact Turn Sequence

1. **Start the turn.** Increment the turn number, add scheduled arrivals, and display an action snapshot including the new arrivals and available tasks.
2. **Waiter action.** Choose seating, order-taking, serving, or WAIT. Explain rejected choices and prompt again.
3. **Chef action.** Choose one order to advance by one preparation unit, or WAIT. An order taken this turn is eligible. Prompt again after rejected choices.
4. **Eating and payment.** Move EATING customers with servedTurn < current turn to READY_TO_PAY. Cashier then selects the earliest customer with readyToPayTurn < current turn. A customer who has just finished eating cannot pay in the same turn.
5. **Satisfaction and abandonment.** Deduct 8 from WAITING, SEATED, and WAITING_FOR_FOOD customers. At 0, process departure, order cancellation, and table release as applicable.
6. **Turn summary and result.** Display statistics. Determine VICTORY or DEFEAT only after turn 18 ends.

Both execution order and eligibility checks enforce the timing rules: a dish finished this turn can be served next turn at the earliest; eating finishes one turn after serving; payment occurs on a later turn after eating finishes.

## 7 Statistics and Invariants

- Store money as integer cents. Display dollar amounts with two decimal places.
- revenueCents is the sum of billCents across PAID orders. paidCount is the number of PAID orders, which also equals the number of paid customers because each customer has at most one order.
- averagePaidSatisfaction is the sum of recorded satisfactionAtPayment values divided by paidCount. When paidCount is 0, return an absent value and display N/A.
- To avoid floating-point comparisons in the victory check, compare satisfactionSum against 60 × paidCount. The separate requirement paidCount ≥ 4 must still hold.
- servedCount counts orders with a recorded servedTurn, including orders subsequently paid. Keep it separate from paidCount.
- GameController supplies turnsUsed from the number of completed turns. Display it with the other end-of-turn statistics.
- unhappyDepartures counts customers in LEFT. Retain all customer and order records until the game ends.
- Each WAITING customer appears exactly once in the queue and occupies no table. Each SEATED, WAITING_FOR_FOOD, EATING, or READY_TO_PAY customer occupies one table. PAID and LEFT customers occupy no table and are absent from the queue.
- An Order exists only after an order is taken. Each successful order-taking action creates exactly one order. Unpaid and cancelled orders contribute no revenue.
- Served customers lose no further satisfaction. Save satisfaction when payment is collected.
- Rejected choices consume no staff allowance, cause no satisfaction loss, advance no turn, and leave no partially applied domain changes.

## 8 UML Class Diagram Draft

The sketch shows the main classes, relationships, and selected operations. Turn phases, customer/order states, action requests, and read-only snapshots are defined in Sections 4 and 5. Their separate boxes and routine query methods are omitted for readability.

[Open the Stage 1 class diagram (PDF)](UML/Stage1_UML_Diagram.pdf)

Notation: **+** public query or business entry point; **−** private field; **~** operation internal to the domain package; a filled diamond denotes composition; dashed arrows denote call dependencies. MenuItem fields are immutable.

Keep this Stage 1 sketch synchronized with implementation as development proceeds. The final class diagram submitted for the assignment must match Stage 3.

## 9 Acceptance Scenarios and Expected Results

The scenarios below are manually derived test specifications. They do not demonstrate an implemented or executed game. After implementation, Member E should run them and retain the actual results.

### 9.1 Basic Timing Scenario

C1 chooses Pasta:

| Turn | Event | Satisfaction at Turn End |
| --- | --- | --- |
| 1 | Seat C1. | 92 |
| 2 | Take the Pasta order and prepare the first unit. | 84 |
| 3 | Complete preparation; the order becomes READY. | 76 |
| 4 | Serve C1. | 76 |
| 5 | C1 finishes eating and becomes READY_TO_PAY. | 76 |
| 6 | Collect payment and release the table. | 76 recorded at payment |

Revenue increases by 1800 cents only on turn 6.

### 9.2 Reproducible Victory Scenario

Perform the following Waiter and Chef actions. The automatic steps still run normally on every turn. Food names identify the customer's deterministic choice, not a choice made by the manager.

| Turn | Waiter Action | Chef Action | Payment This Turn |
| --- | --- | --- | --- |
| 1 | Seat C1 at T1. | WAIT | None |
| 2 | Take C1's order: Pasta. | C1's order, unit 1. | None |
| 3 | WAIT | C1's order, unit 2. | None |
| 4 | Serve C1. | WAIT | None |
| 5 | Seat C2 at T2. | WAIT | None |
| 6 | Take C2's order: Burger. | Complete C2's order. | C1: $18.00 |
| 7 | Serve C2. | WAIT | None |
| 8 | Seat C3 at T1. | WAIT | None |
| 9 | Take C3's order: Salad. | Complete C3's order. | C2: $15.00 |
| 10 | Serve C3. | WAIT | None |
| 11 | Seat C4 at T2. | WAIT | None |
| 12 | Take C4's order: Pasta. | C4's order, unit 1. | C3: $9.00 |
| 13 | Seat C5 at T1. | C4's order, unit 2. | None |
| 14 | Serve C4. | WAIT | None |
| 15 | Take C5's order: Burger. | Complete C5's order. | None |
| 16 | Serve C5. | WAIT | C4: $18.00 |
| 17 | Seat C6 at T2. | WAIT | None |
| 18 | Take C6's order: Salad. | Complete C6's order. | C5: $15.00 |

Expected results: **5 paid customers; revenue $75.00; paid-customer satisfaction values 76, 76, 76, 68, and 76; average satisfaction 74.40; servedCount 5; unhappyDepartures 0; VICTORY after turn 18.**

C6's dish becomes READY on turn 18. C6 has not been served and has not paid when the game ends.

### 9.3 Reproducible Defeat Scenario

Choose WAIT for both Waiter and Chef on all 18 turns.

Expected results: **0 paid customers; revenue $0.00; average satisfaction N/A; DEFEAT after turn 18.** C1 leaves at the end of turn 13, and C2 leaves at the end of turn 16.

### 9.4 Additional Boundary Checks

- A dish completed this turn cannot be served in that turn. A customer who finishes eating this turn cannot pay in that turn.
- Block a second successful Waiter task, a second Chef preparation unit, and a second Cashier payment in the same turn.
- Reject nonexistent identifiers, occupied tables, order-taking before seating, preparation of READY/CANCELLED/PAID orders, and serving of orders that are not eligible. Return a specific reason and preserve the opportunity to choose again.
- If a customer leaves with a PLACED, PREPARING, or READY order, cancel it, release the table, and leave revenue unchanged.
- Collect payment exactly once. Paid orders must remain available for revenue and satisfaction statistics.
- Satisfaction cannot fall below 0. Meeting the victory targets early does not end the game. Starting turn 19 is prohibited.

## 10 Decisions for Team Discussion

Before implementation, agree on:

1. The proposed classes, responsibility boundaries, and use of Restaurant to coordinate related state changes.
2. The fixed food-selection rule, identifiers, and state names.
3. The action-request, result, and read-only snapshot structures, including submitting the Waiter action before collecting the Chef action.
4. Module ownership for Members A–E, and how interface changes will be recorded and communicated.

Once the team agrees, implementation can begin with the Stage 1 modules. This proposal remains a design-only deliverable.
