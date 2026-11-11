# Bernice Koh Stage 2 requirements and implementation review

Date: 11 November 2026.
Reviewer: OpenAI Codex agent, `stage2_history_review`.

Reviewed versions:

- Bernice's implementation: `bernice/stage-2-pricing`, commit `8e4f6a36c8b85f41b1b5a2f2bf747ef946f16283`.
- Integrated Stage 2: `stage-2`, commit `ad6f39ba20102ef3fa20778cb19e70ab3cb69c21`.
- Stage 1 baseline: `stage-1`, commit `ac445a8f956232232188fd765eed98a5e8b49c46`.

Repository paths below refer to the integrated Stage 2 commit unless stated
otherwise. The review examined source, tests, commit history and recorded
execution evidence. It did not execute builds or tests.

## Prompt 1

### Prompt

Identify the requirements and acceptance criteria for Bernice's Stage 2 menu,
order and billing contribution. Include concrete expected outcomes for pricing,
order behaviour and preservation of Stage 1 functionality. Keep Stage 3 features
outside the review.

### Agent response

Bernice's menu and order responsibilities cover preparation progress, order
status, billing, price-calculation tests and explanation of the order design.
Her Stage 2 extension supports customer-specific billing while retaining the
established order lifecycle and interfaces used by the rest of the game.

| ID | Acceptance criterion and expected outcome |
| --- | --- |
| **B2-01 Pricing** | Regular and Critic customers pay full price; VIP customers receive 10% off. Salad, Burger and Pasta cost **900/1500/1800 cents** at full price and **810/1350/1620 cents** for VIPs. Store amounts as integer cents and display dollars with two decimal places. |
| **B2-02 Billing integration** | Calculate the customer-specific bill when taking the order and store the locked amount. Revenue includes each paid bill once. Unpaid and cancelled orders contribute no revenue. Dispatch customer-specific behaviour through customer objects. |
| **B2-03 Choice and menu** | Retain Salad, Burger and Pasta with preparation costs **1/1/2**. Customers choose dishes through the documented rule; the manager controls service priority. Display the chosen dish when placing an order and support the Critic's distinct preference within the common order flow. |
| **B2-04 Order lifecycle** | Start orders in PLACED with zero preparation. Complete the required units before READY; serve only on a later turn. Preserve the served, eating and payment sequence. Cancel unfinished orders on abandonment, including READY orders. Reject invalid transitions without changing order state. |
| **B2-05 Stage 1 preservation** | Retain Waiter-before-Chef sequencing, staff allowances, serving and payment timing, table release rules and retry behaviour. Verify that pricing and order changes preserve these shared game rules. |
| **B2-06 Stage 2 compatibility** | Retain 18 turns, two tables and six arrivals at turns **1/4/7/10/13/16**, including VIP and Critic customers. Waiting losses remain **8/5/12** for Regular/VIP/Critic. Victory requires **at least 4 paid customers, $45 revenue and average paid satisfaction of 60**. |
| **B2-07 Review evidence** | Identify the contribution through commits, pricing/order tests and the Stage 1-to-2 change record. Connect code changes to their responsibilities and regression evidence. |

Host, ComboMeal and Happy Hour belong to Stage 3 and are outside this review.

## Prompt 2

### Prompt

Review Bernice's Stage 2 contribution and its integration into the published
`stage-2` version against B2-01 through B2-07. Identify the relevant source and
tests, explain how the implementation meets the criteria, and distinguish the
original contribution from integration work. Keep the review read-only.

### Agent response

**The integrated Stage 2 implementation meets the reviewed gameplay criteria
on code inspection. No mandatory gameplay defect was found.**

Bernice's four commits are `3007499`, `4eff9ce`, `322da51` and `8e4f6a3`.
They introduce the customer discount hook, bill calculation and order-entry
integration, together with pricing and order/menu tests. All four commits are
preserved in the integrated Stage 2 history.

| Criterion | Implementation and supporting evidence |
| --- | --- |
| **B2-01 Pricing** | `src/orders/BillCalculator.java` obtains the discount through `Customer.getBillDiscountPercent()`. Regular and Critic inherit zero; `VipCustomer` returns 10. The integrated calculator uses a `long` intermediate for percentage arithmetic and rounds to the nearest cent. `test/unit/BillCalculatorTest.java` checks full-price and VIP menu amounts, rounding, input validation and large amounts. |
| **B2-02 Billing integration** | `src/domain/Restaurant.java` asks the customer to choose a dish, calculates the bill during order placement, passes it to the order factory and displays it. `src/orders/Order.java` stores the amount in `final billCents`. Revenue totals include only PAID orders. `orderLocksBill()` checks the stored amount and order message; `Stage2IntegrationTest.mixedVictory()` verifies the VIP Burger payment of **1350 cents** on turn 8. |
| **B2-03 Choice and menu** | `src/fooditems/Menu.java` retains the three base items and preparation costs. `OrderMenuTest.baseMenu()` verifies them. `Customer.chooseItem(Menu)` supplies the deterministic choice, while `CriticCustomer.chooseItem()` selects the item with the greatest preparation requirement. The CLI documents this preference and order placement displays the chosen dish. |
| **B2-04 Order lifecycle** | The established `Order` and `OrderPort` implementations are retained. Bernice's `test/unit/OrderMenuTest.java` checks initial state, preparation, later-turn serving, payment snapshots, cancellation and unchanged state after rejected operations. Integrated Critic scenarios verify service timing, cancellation, table release and exclusion of unpaid orders from revenue. `Restaurant` coordinates customer readiness and payment eligibility. |
| **B2-05 Stage 1 preservation** | Staff, order, configuration and result rules retain the baseline behaviour. Controller extensions supply customer construction while preserving turn sequencing. `MemberACoreTest` and `Stage1SmokeTest` cover timing, allowances, retries, payment ordering and complete game outcomes. `Main --regular` supports Regular-only regression runs on the integrated code. |
| **B2-06 Stage 2 compatibility** | `CustomerFactory`, customer subclasses, `Main` and `GameController` connect the mixed customer schedule to the existing game flow. `Stage2IntegrationTest` checks arrival turns, seeded schedules, satisfaction behaviour, both endings, 18-turn completion and CLI retries. |
| **B2-07 Review evidence** | Bernice's commits identify her billing and test contribution. `docs/Stage2/Stage_1_to_2_Changes.md` and `docs/Stage2/Integration_Notes.md` explain the combined stage transition and its source history. Recorded regression results support preservation of earlier behaviour. |

Integration uses the production `VipCustomer` in the pricing fixtures and the
calculator's `long` intermediate. These changes connect the pricing module to
the completed customer implementation and support the recorded regression
checks.

## Prompt 3

### Prompt

Assess how the existing unit, integration and regression tests verify Bernice's
Stage 2 responsibilities. Explain the expected outcomes and distinguish test
coverage confirmed through inspection from previously recorded execution
results. Keep the review read-only.

### Agent response

**The combined Stage 2 tests adequately cover the reviewed mandatory menu,
order and billing responsibilities. No blocking coverage gap was found.**

| Area | Verification provided by the existing tests |
| --- | --- |
| **Pricing** | `BillCalculatorTest` checks Regular prices, VIP prices of **810/1350/1620 cents**, rounding, invalid inputs, unchanged menu prices and large amounts. `Stage2IntegrationTest.customerBehaviour()` verifies the Critic's zero discount and **1800-cent** Pasta bill. |
| **Order-entry integration** | `BillCalculatorTest.orderLocksBill()` exercises `Restaurant` and `Order` together, checking the selected Salad, displayed **$8.10** bill and stored **810 cents**. The mixed victory scenario verifies a VIP Burger payment of **1350 cents**, satisfaction **90** and final revenue of **6450 cents**. |
| **Menu and preparation** | `OrderMenuTest` checks all three items, prices, preparation requirements and the immutable menu list. Critic integration scenarios exercise Pasta choice and its two-unit preparation flow. |
| **Lifecycle and cancellation** | `OrderMenuTest` checks fresh state, one- and two-unit preparation, same-turn serving rejection, payment snapshots, duplicate payment, unfinished-order cancellation and unchanged snapshots after rejected transitions. Production integration scenarios verify cancellation of PLACED, PREPARING and READY orders, table release and zero revenue. |
| **Stage 1 regression** | `MemberACoreTest --production-orders` covers timing, payment priority, staff allowances, retries, abandonment and result boundaries. `Stage1SmokeTest` separately exercises production staff, orders and CLI. Regular regression retains the **$66.00 revenue / 79.20 average satisfaction** victory result and N/A satisfaction when no customer pays. |
| **Complete Stage 2 behaviour** | `Stage2IntegrationTest` checks the seeded six-customer schedule, exact arrival turns, waiting losses, satisfaction limits and immunity after serving, mixed victory and defeat, 18-turn completion and invalid-input retries. |

The locked bill is supported by `final billCents`, the order-entry check,
payment unit checks and the integrated VIP payment scenario.

`scripts/test-stage2.sh` runs the pricing, order/menu and Stage 2 integration
suites. `scripts/test-stage1.sh` provides the Regular regression checks.

The committed records in `docs/verification/stage-2/release-baseline/`, including
`execution.json`, `regular-regression.log` and `stage2-regression.log`, report:

| Suite | Recorded scenarios | Recorded assertions |
| --- | --- | --- |
| Billing | 5 | 26 |
| Order and menu | 7 | 65 |
| Mixed Stage 2 integration | 14 | 506 |
| Regular core | 14 | 344 |
| Regular smoke | 9 | 94 |

These results were recorded at commit `5402f4a` using macOS/JDK 21 with Java 17
targeting. Git inspection confirms that the integrated release retains the
same source, tests and scripts as that verified baseline. This review assessed
those test definitions and execution records; it did not rerun the suites.
