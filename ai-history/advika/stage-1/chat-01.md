User: give a summary of the code and features etc wtv is there in the project tilll now

Copilot: I’ll inspect the repository structure, documentation, build/config files, and main source areas, then summarize the current features, architecture, and any obvious gaps.

Find files matching `**/*`

Search for `^(#|##|###)|README|TODO|FIXME|class |interface |public static void main|def |...`

Ran terminal command: git --no-pager status --short; git --no-pager log -5 --oneline

Read 
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\README.md"
}

Read 
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\src\\entities\\RegularCustomer.java"
}

Read 
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\docs\\README.md"
}

Read 
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\src\\entities\\Customer.java"
}

Read 
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\src\\entities\\CustomerState.java"
}

Read 
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\src\\entities\\Table.Java"
}

Read 
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\src\\entities\\WaitingQueue.java"
}

Read 
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\src\\entities\\SeatingArea.java"
}

Read 
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\src\\fooditems\\MenuItem.java"
}

Read 
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\src\\fooditems\\Menu.java"
}

Read 
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\src\\config\\GameConfig.java"
}

Read 
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\docs\\UML\\README.md"
}

Read 
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\test\\CustomerSeatingTest.java"
}

Read 
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\docs\\Stage_1_Design_Discussion_EN.md"
}

Read 
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\.vscode\\settings.json"
}

Search for `^#{1,4} `

Read , lines 181 to 380
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\docs\\Stage_1_Design_Discussion_EN.md",
  "view_range": [
    181,
    380
  ]
}

Read , lines 1 to 180
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\docs\\Stage_1_Design_Discussion_EN.md",
  "view_range": [
    1,
    180
  ]
}

Ran terminal command: Get-ChildItem -Recurse -File | Select-Object FullName,Length | Format-Table -AutoSize

The repository currently contains a Stage 1 design plus a partial Java domain implementation. I’m doing one final compile/check pass so the summary can distinguish implemented behavior from planned-but-missing features.

Search for `TODO|FIXME|main\(|class (GameController|RestaurantCLI|Restaurant|Order|Waiter...`

Ran terminal command: git --no-pager log --oneline --all --decorate -12

Ran terminal command: $out = Join-Path $env:TEMP 'sc2002-classes'; if (Test-Path $out) { Remove-Item -Recurse -Force $out }; New-Item -ItemType Directory -Path $out | Out-Null; javac -d $out (Get-ChildItem -Recurse -Filter *.java src | ForEach-Object FullName); if ($LASTEXITCODE -eq 0) { Write-Output 'javac: OK' }

Ran terminal command: $out = Join-Path $env:TEMP 'sc2002-classes'; if (Test-Path $out) { Remove-Item -Recurse -Force $out }; New-Item -ItemType Directory -Path $out | Out-Null; javac -d $out 'src\config\GameConfig.java' 'src\fooditems\MenuItem.java' 'src\fooditems\Menu.java' 'src\entities\CustomerState.java' 'src\entities\Customer.java' 'src\entities\RegularCustomer.java' 'src\entities\Table.Java' 'src\entities\WaitingQueue.java' 'src\entities\SeatingArea.java'; if ($LASTEXITCODE -eq 0) { Write-Output 'javac: OK' }

Ran terminal command: $out = Join-Path $env:TEMP 'sc2002-classes'; $src = Join-Path $env:TEMP 'sc2002-src'; if (Test-Path $out) { Remove-Item -Recurse -Force $out }; if (Test-Path $src) { Remove-Item -Recurse -Force $src }; New-Item -ItemType Directory -Path $out,$src | Out-Null; Get-ChildItem -Recurse -File src | ForEach-Object { $relative = $_.FullName.Substring((Get-Location).Path.Length + 1); $target = Join-Path $src $relative; $parent = Split-Path $target -Parent; New-Item -ItemType Directory -Force -Path $parent | Out-Null; Copy-Item $_.FullName $target }; Rename-Item (Join-Path $src 'entities\Table.Java') 'Table.java'; javac -d $out (Get-ChildItem -Recurse -Filter *.java $src | ForEach-Object { $_.FullName }); if ($LASTEXITCODE -eq 0) { Write-Output 'javac: OK (using temporary lowercase copy of Table.Java)' }

Ran terminal command: $out = Join-Path $env:TEMP 'sc2002-classes'; $src = Join-Path $env:TEMP 'sc2002-src'; if (Test-Path $out) { Remove-Item -Recurse -Force $out }; if (Test-Path $src) { Remove-Item -Recurse -Force $src }; New-Item -ItemType Directory -Path (Join-Path $src 'config'),(Join-Path $src 'fooditems'),(Join-Path $src 'entities'),$out | Out-Null; Copy-Item 'src\config\GameConfig.java' (Join-Path $src 'config'); Copy-Item 'src\fooditems\*.java' (Join-Path $src 'fooditems'); Copy-Item 'src\entities\*.java' (Join-Path $src 'entities'); Rename-Item (Join-Path $src 'entities\Table.Java') 'Table.java'; $files = Get-ChildItem -Recurse -Filter *.java $src | ForEach-Object { $_.FullName }; & javac -d $out $files; if ($LASTEXITCODE -eq 0) { Write-Output 'javac: OK (using temporary lowercase copy of Table.Java)' }

## Project summary

This is a **Java-based SC2002 Restaurant Rush game**, currently at an early **Stage 1 implementation**. The project has a design proposal/UML model and several core domain classes, but the complete playable game flow has not been implemented yet.

## Current implemented features

### 1. Customer lifecycle and state management

The abstract `Customer` class currently supports:

- Customer IDs
- Satisfaction tracking, starting at 100
- Satisfaction loss while waiting
- Patience checking
- Choosing a menu item based on customer ID
- State transitions:
  - `WAITING`
  - `SEATED`
  - `ORDERED`
  - `SERVED`
  - `READY_TO_PAY`
  - `PAID`
  - `LEFT`
- Serving and payment turn tracking
- Abandoning the restaurant when satisfaction reaches zero
- Human-readable descriptions and `toString()` output

The `CustomerState` enum determines whether a customer is still waiting for food.

### 2. Regular customers

`RegularCustomer` is the only customer type currently implemented.

- Regular customers lose **8 satisfaction points per turn**
- Their display type is `"Regular"`
- The class is designed so that other customer types such as VIP customers could be added later

### 3. Tables and seating

`Table` provides:

- Unique table IDs
- One customer per table
- Occupancy checks
- Seating customers
- Releasing customers
- Prevention of seating someone at an occupied table
- Table status display

`SeatingArea` provides:

- Creation of a configurable number of tables
- Detection of free tables
- Adding customers to the waiting queue
- Checking whether a customer is queued
- Seating queued customers at available tables
- Removing customers from either the queue or a table
- Read-only views of tables and queued customers

### 4. Waiting queue

`WaitingQueue` manages customers waiting for a table.

It supports:

- Adding customers
- Removing customers
- Checking whether a customer is present
- Checking whether the queue is empty
- Returning an unmodifiable customer list

The queue currently preserves insertion order.

### 5. Menu and food items

`MenuItem` stores:

- Food name
- Price in cents
- Preparation units
- Formatted dollar price

`Menu` currently contains three fixed menu items:

| Item | Price | Preparation |
|---|---:|---:|
| Salad | $9.00 | 1 unit |
| Burger | $15.00 | 1 unit |
| Pasta | $18.00 | 2 units |

Customer food selection is deterministic based on the customer ID:

- C1/C4 → Pasta
- C2/C5 → Burger
- C3/C6 → Salad

### 6. Game configuration

`GameConfig` defines:

- Maximum game length: **18 turns**
- Number of tables: **2**
- Customer arrival turns: **1, 4, 7, 10, 13, 16**
- Minimum paid customers to win: **4**
- Minimum revenue to win: **$45.00**
- Minimum average paid-customer satisfaction: **60**

## Design and documentation completed

The project contains a detailed Stage 1 design proposal in `Stage_1_Design_Discussion_EN.md`, together with a PDF version.

The design specifies planned classes such as:

- `RestaurantCLI`
- `GameController`
- `Restaurant`
- `Customer`
- `Table`
- `Order`
- `Waiter`
- `Chef`
- `Cashier`
- `MenuItem`

It also defines:

- Staff responsibilities
- Turn order and action rules
- Customer and order states
- Satisfaction calculations
- Payment rules
- Victory and defeat conditions
- Statistics and invariants
- Acceptance scenarios
- Boundary cases and invalid-action handling

The UML files are available in `docs/UML/`, including editable Mermaid and draw.io versions as well as PNG/SVG exports.

## What is not implemented yet

The design document describes a much larger game system than the current source code. The following major components are still missing:

- No `main()` method
- No command-line interface
- No `GameController`
- No central `Restaurant` domain class
- No `Order` class
- No `Waiter` class
- No `Chef` class
- No `Cashier` class
- No full 18-turn game loop
- No user input handling
- No preparation progress management
- No serving workflow tied to orders
- No eating-time logic integrated with tables
- No payment processing
- No revenue tracking
- No average satisfaction calculation
- No victory/defeat result handling
- No automatic arrival scheduling
- No turn-by-turn summaries
- No complete implementation of the documented acceptance scenarios

## Testing status

The only test file is `CustomerSeatingTest.java`, but it is currently empty.

The Java source compiles successfully after temporarily renaming `Table.Java` to the conventional lowercase-extension filename `Table.java`.

However, compiling the repository directly currently fails because `Table.Java` uses an uppercase `.Java` extension, which standard `javac` does not recognize as a Java source file. This should likely be renamed to `Table.java`.

## Current project maturity

### Completed

- Basic entity/domain model
- Customer state transitions
- Satisfaction logic
- Seating and table occupancy
- Waiting queue
- Fixed menu
- Game configuration constants
- Stage 1 design documentation
- UML diagrams

### In progress / incomplete

- Full restaurant simulation
- Staff actions
- Orders and food preparation
- Payment and statistics
- Game controller and CLI
- Automated testing

Overall, the repository currently represents a **core domain foundation plus a detailed design specification**, rather than a complete playable restaurant game.

User: Review the code I've written to all the classes in staff, mention the errors in each

Copilot: I’ll inspect every class under `src\staff`, then trace their dependencies against the existing domain classes and report concrete errors per file, separating compile-time issues from behavioral/design issues.

Find files matching `src/staff/**/*.java`

Search for `package staff|import .*staff|class |interface |enum |record `

Ran terminal command: git --no-pager status --short; git --no-pager diff -- src/staff; Get-ChildItem -Recurse -File src\staff | Select-Object FullName,Length

Read 
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\src\\staff\\ChefAction.java"
}

Read 
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\src\\staff\\Cashier.java"
}

Read 
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\src\\staff\\Chef.java"
}

Read 
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\src\\staff\\ActionResult.java"
}

Read 
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\src\\entities\\SeatingArea.java"
}

Read 
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\src\\staff\\Waiter.java"
}

Read 
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\src\\entities\\Customer.java"
}

Read 
Completed with input: {
  "path": "C:\\Users\\Payal Bhambri\\SC2002_Group_Assignment\\src\\staff\\WaiterAction.java"
}

Search for `Restaurant|Order|nextCustomerReadyToPay|settlePayment|advancePreparation|seat...`

Ran terminal command: Get-ChildItem src\staff\*.java | ForEach-Object { Write-Output "--- $($_.Name)"; $n=0; Get-Content $_.FullName | ForEach-Object { $n++; '{0,3}: {1}' -f $n,$_ } }

Ran terminal command: $files = Get-ChildItem -Recurse -Filter *.java src | ForEach-Object FullName; & javac -d (Join-Path $env:TEMP 'sc2002-check') $files 2>&1

## Overall result

The `staff` classes are currently **not integrated or compilable as part of the project**.

The biggest project-level issue is that:

- There is no `Restaurant` class yet.
- There is no `Order` class yet.
- All staff classes are missing `package staff;`.
- `Cashier` refers to `Customer` without importing `entities.Customer`.
- The staff classes call methods that do not exist anywhere in the current repository.

I reviewed these files:

- `ActionResult.java`
- `WaiterAction.java`
- `Waiter.java`
- `ChefAction.java`
- `Chef.java`
- `Cashier.java`

---

# 1. `ActionResult.java`

`ActionResult.java`

### Errors/issues

#### 1. Missing package declaration

The file begins directly with:

```java
public class ActionResult {
```

It should probably begin with:

```java
package staff;
```

All files under `src/staff` should consistently belong to the `staff` package.

Currently, these classes are in Java’s **default package**, while the rest of the project uses packages such as:

- `entities`
- `fooditems`
- `config`

This will make imports and project organization inconsistent.

#### 2. No null validation for messages

These methods accept `null`:

```java
ActionResult.ok(null);
ActionResult.fail(null);
```

That can cause confusing output later when the CLI tries to display the message.

This is not necessarily a compilation error, but it weakens the error-reporting contract.

#### 3. No `toString()`

For debugging and CLI output, it may be useful for `ActionResult` to provide a meaningful string representation. Currently, printing an `ActionResult` directly produces an object reference string.

### What is correct

- The constructor is private, enforcing factory-method usage.
- The immutable fields are appropriate.
- Separating success and failure results is a good approach.

---

# 2. `WaiterAction.java`

`WaiterAction.java`

### Errors/issues

#### 1. Missing package declaration

Like the other staff files, it should likely include:

```java
package staff;
```

#### 2. No validation of IDs

These factories accept invalid values:

```java
WaiterAction.seat(-1, -5);
WaiterAction.takeOrder(0);
WaiterAction.serve(-10);
```

The action object can therefore contain invalid customer, table, or order IDs.

Validation should happen either:

- in the action factory methods, or
- in the `Restaurant` domain layer.

The important point is that invalid identifiers should be rejected with a clear error rather than passed deeper into the system.

#### 3. Ambiguous `targetId`

The field is described as:

```java
private final int targetId;   // customer id, or order id for SERVE
```

This creates an ambiguous API:

- For `SEAT`, `targetId` means customer ID.
- For `TAKE_ORDER`, it means customer ID.
- For `SERVE`, it means order ID.

This is easy to misuse. Separate fields or separate action types/records would make the API safer.

#### 4. `tableId` is silently set to zero for non-seat actions

For example:

```java
WaiterAction.takeOrder(customerId)
```

sets `tableId` to `0`.

That is not necessarily wrong, but it means the class permits meaningless combinations of fields. A caller could also accidentally use `getTableId()` for a non-seat action.

#### 5. No `equals`, `hashCode`, or `toString`

This is not a functional blocker, but action objects will be harder to test and debug.

### What is correct

- The factory methods make action creation readable.
- The enum prevents arbitrary action strings.
- The action type is immutable.

---

# 3. `Waiter.java`

`Waiter.java`

### Compilation errors

#### 1. `Restaurant` cannot be resolved

This method uses:

```java
public ActionResult execute(WaiterAction action, Restaurant restaurant, int turn)
```

There is currently no `Restaurant` class in the repository.

Therefore, the file cannot compile.

#### 2. Called `Restaurant` methods do not exist

These methods are called:

```java
restaurant.seatCustomer(...)
restaurant.placeOrder(...)
restaurant.serveOrder(...)
```

No such class or methods currently exist in the source tree.

The expected methods would need to be implemented in a future `Restaurant` class.

### Runtime/logic issues

#### 3. `action` can be null

This line will throw a `NullPointerException` if a caller passes `null`:

```java
action.getType()
```

The method should reject null input explicitly with a clear result or exception.

#### 4. `restaurant` can be null

A null `restaurant` causes a `NullPointerException` when the action is executed.

There is no validation for this dependency.

#### 5. `turn` is not validated

The method accepts:

- negative turns
- turn zero
- turns beyond the configured maximum
- turns that move backwards

There is no validation against `GameConfig`.

#### 6. `SKIP` does not use or update `lastActionTurn`

This code returns immediately:

```java
if (action.getType() == WaiterAction.Type.SKIP) {
    return ActionResult.ok("Waiter waits this turn.");
}
```

As a result, calling `SKIP` multiple times in the same turn is always accepted. Calling `SKIP` followed by a real action in the same turn is also accepted.

If the rule is that the Waiter gets exactly one action opportunity per turn, this is incorrect. `SKIP` should either:

- consume the Waiter’s turn, or
- be handled by the controller rather than as an executable action.

#### 7. Staff state cannot be reset

`lastActionTurn` is retained forever:

```java
private int lastActionTurn = 0;
```

If the same `Waiter` object is reused for a second game, its previous turn state may interfere with the new game.

A reset method or a new staff instance per game is needed.

#### 8. Possible state mismatch with `Customer`

The existing `Customer` class only supports:

```java
customer.placeOrder();
customer.serve(turn);
```

But there is currently no `Order` entity to verify whether an order is actually prepared and ready to serve. The `Restaurant` layer must enforce this correctly.

### What is correct

- Failed actions do not consume the Waiter’s action allowance.
- Successful actions update `lastActionTurn`.
- The switch structure matches the intended Waiter responsibilities.

---

# 4. `ChefAction.java`

`ChefAction.java`

### Errors/issues

#### 1. Missing package declaration

It should likely include:

```java
package staff;
```

#### 2. No order validation

This is allowed:

```java
ChefAction.prepare(-1);
ChefAction.prepare(0);
```

Invalid order IDs should be rejected or handled explicitly.

#### 3. `orderId` is meaningless for `SKIP`

`skip()` stores:

```java
new ChefAction(Type.SKIP, 0)
```

This is acceptable internally, but the model allows callers to access an order ID even when the action is not `PREPARE`.

#### 4. No `toString`, equality, or validation helpers

This makes the class less convenient to test and debug.

### What is correct

- The Chef action types are clear.
- The factory methods make action creation readable.
- The action is immutable.

---

# 5. `Chef.java`

`Chef.java`

### Compilation errors

#### 1. `Restaurant` cannot be resolved

This method references a class that does not currently exist:

```java
public ActionResult execute(ChefAction action, Restaurant restaurant, int turn)
```

#### 2. `advancePreparation` does not exist

This call cannot currently compile:

```java
restaurant.advancePreparation(action.getOrderId(), turn);
```

A future `Restaurant` implementation must expose this operation, or the Chef should depend on a more appropriate domain service.

### Runtime/logic issues

#### 3. Null action and restaurant are not checked

These can cause `NullPointerException`:

```java
action.getType()
restaurant.advancePreparation(...)
```

#### 4. `SKIP` does not consume the Chef’s turn

As with `Waiter`, this code returns before checking or updating `lastActionTurn`:

```java
if (action.getType() == ChefAction.Type.SKIP) {
    return ActionResult.ok("Chef waits this turn.");
}
```

Therefore, multiple skips or a skip followed by preparation are allowed in the same turn.

#### 5. Turn values are not validated

The Chef can be called with invalid or out-of-order turns.

#### 6. Preparation eligibility is entirely delegated to an absent class

The Chef itself does not verify:

- Whether the order exists
- Whether the order belongs to a customer
- Whether it is already ready
- Whether it is cancelled
- Whether it is paid
- Whether one preparation unit is still available

This can be acceptable if `Restaurant` is the domain coordinator, but that contract needs to be implemented clearly.

#### 7. No reset mechanism

The same stale `lastActionTurn` problem exists as in `Waiter`.

### What is correct

- A failed preparation does not consume the Chef’s action.
- A successful preparation updates the Chef’s action turn.
- The Chef correctly models one preparation unit per action.

---

# 6. `Cashier.java`

`Cashier.java`

### Compilation errors

#### 1. `Restaurant` cannot be resolved

The method references a missing class:

```java
public ActionResult processPayment(Restaurant restaurant, int turn)
```

#### 2. `Customer` is not imported

This line uses `Customer`:

```java
Customer next = restaurant.nextCustomerReadyToPay();
```

But the file has no import:

```java
import entities.Customer;
```

Since `Customer` belongs to the `entities` package, the compiler cannot resolve it.

#### 3. `nextCustomerReadyToPay` does not exist

This method is called here:

```java
restaurant.nextCustomerReadyToPay();
```

There is currently no `Restaurant` implementation or method with this name.

#### 4. `settlePayment` does not exist

This call is also unresolved:

```java
restaurant.settlePayment(next.getId(), turn);
```

### Runtime/logic issues

#### 5. The Cashier does not receive a payment action

Unlike the Waiter and Chef, the Cashier has no action object. This may be intentional because payment is automatic, but it means the design does not allow the user/controller to explicitly choose or reject a customer.

The design document states that the Cashier should automatically process the earliest eligible customer, so this is probably acceptable if implemented consistently.

#### 6. No validation for null Restaurant

A null Restaurant causes a `NullPointerException`.

#### 7. No validation for invalid turns

The Cashier accepts any integer turn.

#### 8. “No customer ready” returns success

This code returns:

```java
ActionResult.ok("Cashier: no customer is ready to pay.");
```

Whether this is correct depends on the intended contract.

If “no eligible payment” is a normal automatic outcome, success is reasonable. If the result is meant to represent whether a payment actually happened, this should instead be a distinct status such as:

- `NO_ACTION`
- `NOTHING_TO_PROCESS`
- or a result containing a payment flag

Currently, `isSuccess() == true` does not necessarily mean a payment was collected.

#### 9. Payment ordering depends on an undefined method

The requirement is to pay the customer who became ready earliest. The correctness of this class depends entirely on `nextCustomerReadyToPay()` correctly sorting by `readyToPayTurn`.

The current `Customer` exposes `getReadyToPayTurn()`, but there is no implementation yet that uses it.

#### 10. No reset mechanism

The Cashier also retains `lastActionTurn` across games.

### What is correct

- It prevents a second successful payment in the same turn.
- It only updates its action turn when payment succeeds.
- It delegates payment coordination to the Restaurant, which is consistent with the proposed architecture.

---

# Most important fixes

## Required for compilation

1. Add `package staff;` to every class in `src/staff`.
2. Import domain classes where needed:

```java
import entities.Customer;
```

3. Implement a `Restaurant` class.
4. Implement an `Order` class.
5. Add the methods expected by the staff classes:
   - `seatCustomer`
   - `placeOrder`
   - `serveOrder`
   - `advancePreparation`
   - `nextCustomerReadyToPay`
   - `settlePayment`

## Required for correct turn behavior

6. Decide whether `SKIP` consumes the staff member’s action for that turn. Based on the game design, it should generally consume the phase/action opportunity.
7. Validate null actions, null Restaurant objects, and invalid turn values.
8. Add a reset strategy for staff objects between games.
9. Define and document whether “no customer ready to pay” is a successful result or a separate no-op result.
10. Add tests for:
   - successful actions
   - failed actions not consuming the turn
   - `SKIP` behavior
   - repeated actions in one turn
   - invalid IDs
   - Cashier payment ordering
   - reusing staff in a new game

## Summary by severity

| File | Main issue | Severity |
|---|---|---|
| `ActionResult.java` | Missing package; otherwise mostly functional | Medium |
| `WaiterAction.java` | Missing package and weak ID/action validation | Medium |
| `Waiter.java` | Missing `Restaurant`, missing methods, skip bypasses action limit | High |
| `ChefAction.java` | Missing package and weak order validation | Medium |
| `Chef.java` | Missing `Restaurant`, missing preparation method, skip bypasses limit | High |
| `Cashier.java` | Missing imports/classes/methods and ambiguous no-payment success | High |