# Bernice Koh Stage 3 requirements and implementation review

Date: 14 November 2026.
Reviewers: OpenAI Codex agents.

Reviewed versions:

- Bernice's implementation: `bernice/stage-3-combo`, commit `c013d2b29c78905f7431f564dc5048aafaab9ce0`.
- Integrated Stage 3: `stage-3`, commit `fa2d608f50c43f3ca3aabf8ddfb58063ff95af74`.
- Stage 2 baseline: `stage-2`, commit `ad6f39ba20102ef3fa20778cb19e70ab3cb69c21`.

Repository references describe these reviewed versions. The three reviews cover
requirements, implementation and verification evidence.

## Prompt 1

### Prompt

Identify the requirements and acceptance criteria for Bernice’s Stage 3 menu, combo and billing contribution. Cover combo composition, preparation units, integer-cent pricing, VIP and Happy Hour discount order, locked bills and preservation of earlier behaviour. Give concrete expected outcomes and distinguish module responsibilities from shared game integration.

### Agent response

Bernice’s Stage 3 contribution covers **combo menu items, preparation requirements, shared discount arithmetic and customer billing**, together with the corresponding unit tests. The review should compare her implementation at `c013d2b` with the integrated `stage-3` release at `fa2d608`.

The following criteria define the expected outcomes.

| ID | Acceptance criterion |
| --- | --- |
| **B3-01 Combo composition** | A `ComboMeal` contains two existing menu items and supports the `MenuItem` price and preparation contract. Its components remain available for inspection. The three ordinary dishes retain their existing prices and preparation requirements. |
| **B3-02 Combo preparation** | Preparation units equal the sum of the components: Salad + Burger requires **2 units**; Salad + Pasta and Burger + Pasta require **3 units**. An order becomes READY only after all units are completed and can be served on a later turn. Existing order, serving, payment and cancellation rules apply to combos. |
| **B3-03 Combo prices** | The combo price is **90% of the combined component prices**, rounded to the nearest cent. Store the resulting amount as integer cents and display dollars with two decimal places. Salad + Pasta must produce **2430 cents, displayed as $24.30**. |
| **B3-04 Discount sequence** | Start with the item’s established price, including the combo reduction where applicable. Apply the customer discount first, then Happy Hour’s **20%** reduction when active. VIP customers receive **10%** off; Regular and Critic customers have no customer-specific reduction. The implementation’s shared rounding operation produces consistent integer-cent results at each step. |
| **B3-05 Customer choice and menu integration** | Customers select their own dishes through the documented deterministic rule. Stage 3’s orderable menu includes ordinary dishes and combos; selected combos enter the same order flow as ordinary items. The integrated rule cycles Regular/VIP choices through six items, while the Critic chooses the greatest preparation requirement, with the first item breaking ties. |
| **B3-06 Locked bills and revenue** | Calculate and store the final bill when the order is taken. Happy Hour activation or expiry does not change an existing order’s bill. Payment collects the stored amount once. Unpaid and abandoned orders contribute no revenue. |
| **B3-07 Earlier behaviour** | Without Happy Hour, ordinary-item billing retains Stage 2 results. The two-argument `billFor(customer, item)` continues to mean no Happy Hour. The shared game retains its arrival schedule, staff allowances, deferred serving/payment, abandonment rules, 18-turn limit and victory thresholds. |
| **B3-08 Design and verification evidence** | Explain how `ComboMeal`, `Discount`, `Menu` and `BillCalculator` divide responsibilities. Connect composition, inheritance, shared arithmetic and order compatibility to the relevant source changes and tests. Use commit comparisons and regression evidence to support preservation of earlier behaviour. |

Expected combo amounts are:

| Combo | Preparation units | Combo price | VIP price | Happy Hour price | VIP + Happy Hour |
| --- | ---: | ---: | ---: | ---: | ---: |
| Salad + Burger | 2 | $21.60 | $19.44 | $17.28 | $15.55 |
| Salad + Pasta | 3 | $24.30 | $21.87 | $19.44 | $17.50 |
| Burger + Pasta | 3 | $29.70 | $26.73 | $23.76 | $21.38 |

The ordinary-item reference example is **VIP Burger during Happy Hour: $15.00 → $13.50 → $10.80**.

Bernice’s modules provide combo construction, menu membership, discount calculation and billing APIs. Shared game integration connects these modules to customer selection, order placement and the active Happy Hour state. Host actions, activation input, the two-turn duration and waiting-customer satisfaction recovery are integration requirements; their connection to pricing should be reviewed alongside the module behaviour.

Repository references for the review are `src/fooditems/{ComboMeal,Discount,Menu}.java`, `src/orders/BillCalculator.java`, `test/unit/{ComboMealTest,BillCalculatorTest,OrderMenuTest}.java`, and the Stage 3 integration and change records under `test/integration/` and `docs/Stage3/`.

## Prompt 2

### Prompt

Review Bernice’s three Stage 3 commits and their integration into the published `stage-3` version against those criteria. Explain the responsibilities of `ComboMeal`, `Discount`, `Menu` and `BillCalculator`, their use of composition and inheritance, and how customer selection and order placement connect to them. Support findings with repository code and commit evidence. Keep the review read-only.

### Agent response

**Bernice’s Stage 3 combo and billing implementation satisfies the reviewed criteria on source inspection.** Her three commits remain in the ancestry of the published `stage-3` release, `fa2d608`. Comparing her branch tip, `c013d2b`, with the release confirms that `ComboMeal`, `Discount`, `BillCalculator` and her two affected unit-test files are retained unchanged.

| Commit | Contribution and design purpose |
| --- | --- |
| `877dfd6` | Moves percentage arithmetic into `fooditems.Discount`, allowing combo prices and customer bills to share the same rounding operation. This keeps combo construction independent of the orders package. |
| `0464559` | Adds `ComboMeal`, registers the three component pairs in `Menu`, and adds construction, pricing, containment and preparation tests. |
| `c013d2b` | Adds the Happy Hour billing overload and tests VIP-then-Happy-Hour calculations while preserving the existing two-argument billing method. |

The four classes divide their responsibilities clearly.

| Class | Responsibility and acceptance evidence |
| --- | --- |
| **`ComboMeal`** | Extends `MenuItem`, so existing orders and preparation logic can use a combo through the established item contract. It stores an immutable list referencing its two component items, adds their preparation units and applies the 10% combo reduction. The three combos cost **2160/2430/2970 cents** and require **2/3/3 units**. This supports **B3-01–B3-03**. |
| **`Discount`** | Performs validated percentage arithmetic on integer cents, using a `long` multiplication intermediate and nearest-cent, half-up rounding. Both combo construction and bill calculation call this shared operation, supporting consistent pricing under **B3-03–B3-04**. |
| **`Menu`** | Keeps the original three dishes and the three combo objects in separate immutable lists. Bernice’s `getItems()`, `getCombos()` and membership support establish the menu structure. Integration adds `Menu(boolean combosEnabled)` and `getOrderableItems()`: the default menu retains three choices, while Stage 3 exposes six. This supports **B3-01, B3-05 and B3-07**. |
| **`BillCalculator`** | Applies the customer’s polymorphic discount first, then Happy Hour’s 20% reduction when active. The two-argument method delegates with Happy Hour disabled. It calculates the amount without altering menu prices, supporting **B3-04 and B3-07**. |

The integrated order flow connects those contracts as follows:

1. `src/entities/Customer.java` selects from `Menu.getOrderableItems()` using the documented ID cycle. `CriticCustomer.chooseItem()` selects the greatest preparation requirement and retains the first item on ties.
2. `src/domain/Restaurant.java` asks the customer to choose the dish and validates it against the current orderable list.
3. Order placement passes the active Happy Hour state to `BillCalculator.billFor(customer, item, active)`.
4. The returned amount is stored in `src/orders/Order.java` as `final billCents`.
5. The existing order lifecycle uses the item’s preparation requirement. A three-unit combo becomes READY after its third preparation action and can be served on a later turn.
6. Payment preserves the stored amount; revenue totals include PAID orders only. Later activation or expiry therefore leaves previously placed bills unchanged.

For example, a VIP Salad + Pasta Combo follows **2430 → 2187 → 1750 cents**. A VIP Burger during Happy Hour follows **1500 → 1350 → 1080 cents**. These flows satisfy **B3-04–B3-06**.

The implementation demonstrates inheritance through `ComboMeal extends MenuItem`, component composition through its stored menu-item references, and encapsulation through immutable component lists and locked order amounts. Existing `Order` preparation, serving, payment and cancellation implementations are retained. Customer choice and discount behaviour remain dispatched through customer objects.

Supporting tests include `ComboMealTest.orderPreparation()`, `BillCalculatorTest.comboBills()` and `roundingOrder()`, plus `Stage3IntegrationTest.menuChoices()`, `lockedBills()`, `comboPayment()` and `vipComboPayment()`. The responsibilities and integration changes are also recorded in `docs/Stage3/Stage_3_Design_and_Interfaces_EN.md` and `Stage_2_to_3_Changes.md`, supporting **B3-08**.

This review inspected Git history, source and test definitions. It did not execute builds or tests or modify files.

## Prompt 3

### Prompt

Assess how `ComboMealTest`, `BillCalculatorTest`, `OrderMenuTest` and the Stage 3 integration tests verify Bernice’s contribution. Cover combo preparation, discount calculations and rounding, customer choice, bill locking through Happy Hour expiry, payment and preservation of earlier behaviour. Distinguish inspected test coverage from previously recorded execution results. Keep findings tied to mandatory requirements and existing evidence.

### Agent response

**The existing unit, integration and regression tests provide direct coverage of Bernice’s mandatory Stage 3 menu, combo and billing behaviour.** The unit definitions were inspected at her branch tip, `c013d2b`, and the integrated checks and execution records at the published `stage-3` release, `fa2d608`.

| Area | Inspected test coverage and expected outcome |
| --- | --- |
| **Combo construction and preparation** | `test/unit/ComboMealTest.java` checks all three component pairs, their **2160/2430/2970-cent** prices and **2/3/3-unit** preparation requirements. It verifies component identity, immutable lists and use through the `MenuItem` contract. `orderPreparation()` exercises a three-unit combo: PREPARING after units one and two, READY after unit three, serving on a later turn and payment retaining the original bill. |
| **Discount arithmetic and rounding** | `test/unit/BillCalculatorTest.java` checks ordinary and VIP prices, percentage validation, zero/full discounts, half-cent rounding and large valid amounts. `happyHour()` checks the **1080-cent VIP Burger** example and verifies that the two-argument billing method retains non-Happy-Hour behaviour. `comboBills()` checks all three combos with VIP, Happy Hour and combined reductions. Expected VIP-plus-Happy-Hour bills are **1555/1750/2138 cents**. `roundingOrder()` verifies that per-step rounding agrees with rounding once for the supplied menu. |
| **Customer choice** | `Stage3IntegrationTest.menuChoices()` checks that the ordinary menu offers three choices and Stage 3 offers six. Regular customer 4 selects Salad + Burger Combo; VIP customer 5 selects Salad + Pasta Combo. The Critic selects the greatest preparation requirement, choosing Salad + Pasta on the first-item tie, while retaining Pasta choice in the earlier menu mode. |
| **Bill capture and Happy Hour expiry** | `BillCalculatorTest.orderLocksBill()` exercises actual `Restaurant` order creation and verifies the displayed and stored **810-cent VIP Salad** bill. `Stage3IntegrationTest.lockedBills()` verifies that activation leaves an existing **900-cent** order unchanged, creates a **1080-cent VIP Burger** order and retains that discounted amount when payment occurs after expiry. |
| **Combo payment** | `Stage3IntegrationTest.comboPayment()` follows actual combo orders through preparation, service and payment. It checks the two-unit combo’s payment on turn 14 and the three-unit combo’s payment on turn 18. `vipComboPayment()` uses an explicitly identified all-VIP fixture to verify **2430 → 2187 → 1750 cents**, three preparation units and payment after Happy Hour has expired. |
| **Cancellation and revenue** | `Stage3IntegrationTest.comboAbandonment()` exercises PLACED, PREPARING and READY combo orders. Each abandoned order becomes CANCELLED, its customer leaves, its table is released and its revenue contribution remains zero. |
| **Earlier order behaviour** | `test/unit/OrderMenuTest.java` preserves checks for the original menu, initial order state, one- and two-unit preparation, later-turn serving, payment snapshots and cancellation. Rejected operations must leave the order snapshot unchanged. Integrated service-timing tests retain the eating and payment delays. |

The full-game checks also verify six arrivals, two tables, all 18 turns and unchanged victory thresholds. The stored Stage 3 victory expects **five paid customers, $83.70 revenue and 87.20 average paid satisfaction**. Its defeat replay expects **zero payments, $0.00 revenue and N/A paid satisfaction**.

Previously recorded execution evidence is available in `docs/verification/stage-3/release-baseline/execution.json` and the regression logs:

| Suite | Recorded scenarios | Recorded assertions |
| --- | ---: | ---: |
| Billing | 8 | 48 |
| ComboMeal | 6 | 42 |
| Base menu and Order | 7 | 65 |
| Stage 3 integration | 18 | 550 |
| Regular core | 14 | 344 |
| Regular production smoke | 9 | 94 |
| Stage 2 integration | 14 | 506 |

The manifest records successful build and Stage 1–3 regression commands for candidate `c322b0f`. The unique total is **76 scenarios and 1,649 assertions**, counting the billing suite once. `scripts/test-stage3.sh` compiles production and test sources with `--release 17 -Xlint:all -Werror`, then invokes the ComboMeal, billing and Stage 3 integration suites.

This review inspected test definitions and committed execution records. It did not execute builds or tests or modify files.
