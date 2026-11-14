# Stage 3 testing conversation

Member: Rishika Garg (Member E). AI assistant: Claude Code (Claude Opus 5.5), VS Code extension.

Canonical verbatim message strings are in messages.json. This readable view trims trailing line whitespace.
The request to export this conversation is excluded. System/developer content, private reasoning and tool payloads are not exported. Timestamps are not included.

## 001 | user

Testing lead again. Stage 1 and 2 testing is done. Now Stage 3. Same rules as before: separate worktree at tag stage-3, no game code or tag changes, report bugs without fixing them, stop after each phase.

Copy test/qa/ from the stage-2 worktree. Add Stage3RulesTest.java in the same style, and make run-qa.sh run all three classes in order.

Phase 1: Understand the changes
Compare stage-2 with stage-3: new, changed and deleted files, with a reason for each. Explain how Host, ComboMeal and Happy Hour fit the design, and whether any of them needed type checks in the controller. Tell me the documented rule for when a customer picks a combo. Then stop.

Phase 2: Test plan
Map tests to these rules from the brief:
1. Host seats at most one queued customer per turn, on top of the Waiter's task. The Waiter can still seat.
2. Combo prep units are the sum of its two items. Price is 90% of the combined price, rounded to the nearest cent (Salad + Pasta = 2430, 3 units). The customer's combo choice follows the documented rule.
3. Happy Hour can be switched on once per game, at the start of a turn. It lasts that turn and the next, and the remaining duration is displayed.
4. Orders taken during Happy Hour get 20% off. The price is locked when the order is taken. Orders placed earlier don't change.
5. During each active turn, customers waiting for a table or for food recover 15 at turn start, max 100. Customers who have left aren't affected.
6. VIP discount first, then Happy Hour (VIP Burger = 1080). Check a VIP combo during Happy Hour and show the rounding.
7. Win and lose criteria are unchanged.
8. Regression: run the Stage 1 and Stage 2 tests on stage-3 and sort failures into real regressions and expected changes. Don't edit any test until I approve.
Then stop.

Phase 3: Runs
Write the tests. Add a stage3-victory.txt and a stage3-defeat.txt that use the Host, a combo and Happy Hour, with a comment on each line. Run them and paste the final summaries. Then stop.

Phase 4: Report
Give me a pass/fail table, a list of bugs with steps to reproduce, a draft of the stage-2 to stage-3 README section, and a list of every class with its key attributes, operations and relationships so I can check the final class diagram matches the code.


## 002 | assistant

I'll start by finding the existing worktrees and the tags.


## 003 | assistant

Creating the stage-3 worktree and copying the QA folder.


## 004 | assistant

No deleted files. Now the source diff.


## 005 | assistant

Source diff read. Now the relevant full files and the Stage 3 design docs.


## 006 | assistant

Checking for type checks in the controller and elsewhere, and that the stage-3 build compiles.


## 007 | assistant

## Phase 1: Stage 2 → Stage 3 changes

**Setup:** I created a new worktree at `../SC2002_stage3_test`, detached at tag `stage-3` (`fa2d608`). I copied `test/qa/` (both test classes, `play.sh`, `run-qa.sh`, `runs/`) from the stage-2 worktree. That folder is the only difference from the tag (`?? test/qa/`). No game code or tags were changed, and `scripts/build.sh` gives `BUILD_STAGE3=PASS`.

### File changes (`git diff --name-status -M stage-2 stage-3`)

70 files changed: 52 added, 18 modified, **0 deleted**. No files were renamed either.

**Added: game code**

| File | Reason |
| --- | --- |
| `src/staff/Host.java` | New staff role. It has its own once-per-turn allowance (`lastActionTurn`) and can only seat or wait. A rejected action doesn't use up the allowance. |
| `src/contracts/HostAction.java` | A `SEAT`/`WAIT` record for the Host. It has no order or food action. |
| `src/domain/HappyHourPolicy.java` | Holds the single activation (`activationTurn`) and the constants `DURATION_TURNS=2` and `SATISFACTION_RECOVERY=15`. Time left is worked out from the turn number, not counted down. |
| `src/fooditems/ComboMeal.java` | A subclass of `MenuItem` that holds two parts. Prep units are added together, and the price is `Discount.apply(sum, 10)`. |
| `src/fooditems/Discount.java` | Rounding to the nearest cent, moved here from `BillCalculator` so combos and bills both use it. |

**Added: tests, scripts, docs**

| File | Reason |
| --- | --- |
| `test/unit/ComboMealTest.java` | Bernice's combo build, rounding and prep tests |
| `test/integration/Stage3IntegrationTest.java` | The developers' Stage 3 feature and CLI replay tests |
| `test/replays/stage3-victory.txt`, `stage3-defeat.txt` | The developers' replay inputs (separate from the QA runs I'll write) |
| `scripts/test-stage3.sh` | Runs the developer Stage 3 tests |
| `docs/Stage3/*` (6 files) | Design, interfaces, change report, test notes, diagram notes |
| `docs/UML/Stage3_*`, `generate_stage3_diagrams.py`, layout/model JSON | Class and Happy Hour sequence diagrams, their sources and the generator |
| `docs/verification/Stage_3_*`, `stage-3/release-baseline/*` | Release record and build and regression logs |
| `ai-history/stage-3/*` (10 files) | Lichen's Stage 3 AI conversation export |

**Modified**

| File | Change and reason |
| --- | --- |
| `src/Main.java` | Stage 3 is now the default. Adds `--stage3` and `--stage2`, and keeps `--regular`. Passes `stage3` to `new Restaurant(Order::new, stage3)`. |
| `src/domain/Restaurant.java` | Has a `stage3Enabled` flag and owns `HappyHourPolicy`. Adds `resolveTurnStart(bool)`, which handles activation and recovery once per turn, and `getHostOptions()`. Bills now pass a Happy Hour flag, so the price is fixed when the order is taken. Staff actions now check `isActionTurnOpen`, so nobody can act before the turn-start decision. |
| `src/control/GameController.java`, `TurnPhase.java` | New phases `AWAIT_TURN_START` and `AWAIT_HOST`. Adds `submitTurnStartDecision` and `submitHostAction`. The view now includes Host options and Happy Hour status. |
| `src/contracts/StaffGateway.java`, `src/staff/StaffTeam.java` | Adds `executeHost`. Its default rejects the action, so the old Stage 1/2 test staff still compile. `StaffTeam` overrides it and passes the call to `Host`. |
| `src/entities/Customer.java` | `chooseItem` now picks from `getOrderableItems()`. New `recoverWaitingSatisfaction(points)`, limited to WAITING/SEATED/ORDERED and capped at 100. |
| `src/entities/CriticCustomer.java` | Same switch to `getOrderableItems()` |
| `src/fooditems/Menu.java` | Three combos. `getOrderableItems()` returns 3 items in Stage 2 and 6 in Stage 3. Adds `contains()`. |
| `src/orders/BillCalculator.java` | Adds an overload `billFor(customer, item, happyHourActive)`: VIP discount first, then 20%, rounded at each step. `applyDiscount` was removed (moved to `Discount`). |
| `src/view/GameView.java` | New `stage3Enabled` and `HappyHourView(canActivate, used, remainingTurns)`. `StaffView` gains `hostAvailable` and `hostOptions`. |
| `src/cli/RestaurantCLI.java` | Happy Hour prompt (only while unused), Host prompt, "Happy Hour remaining: N turn(s)" line, Stage 3 intro text |
| `scripts/build.sh` | Build message says `BUILD_STAGE3` |
| `test/integration/Stage2IntegrationTest.java` | Now runs `Main --stage2`, because running with no flag is now Stage 3 |
| `test/unit/BillCalculatorTest.java` | Tests for the shared rounding and for the stacked discounts |
| `test/replays/README.md`, `README.md`, `docs/README.md`, `docs/UML/README.md`, `.gitignore` | Docs and index updates; `.gitignore` adds `__pycache__/` |

### How the three features fit the design

**Host.** It's a separate staff object owned by `StaffTeam`, next to Waiter, Chef and Cashier, and it's reached through `StaffGateway.executeHost`. Turn order is: turn-start decision → Host → Waiter → Chef → automatic steps. The Host can't take orders. Its seat options come from the SEAT entries of `getWaiterOptions`, and it calls the same `Restaurant.seatCustomer`. So the Waiter can still seat someone after the Host, as long as there's another free table and another customer in the queue. A customer the Host seats can also have their order taken by the Waiter in the same turn.

**ComboMeal.** It extends `MenuItem`, so `Order`, Chef preparation, Waiter serving and Cashier payment all handle it as an ordinary menu item. Only `Menu` knows about combos, and Stage 2 mode hides them through `getOrderableItems()`.

**Happy Hour.** `Restaurant` owns `HappyHourPolicy`. The controller only enforces when it can be switched on (the `AWAIT_TURN_START` phase). `Restaurant.placeOrder` reads `isActive(currentTurn)` and stores the final bill in the `Order` at that point, so orders placed earlier keep their price. Recovery happens once, in `resolveTurnStart`, after arrivals and before any staff action.

**Type checks:** none were needed. `grep` finds no `instanceof` or `getClass()` anywhere in `src/`. Critic food choice is still handled by `CriticCustomer` overriding `chooseItem`, and VIP pricing by overriding `getBillDiscountPercent`. Combos go through the `MenuItem` methods. The controller never looks at the concrete customer type or the item type. `getTypeName()` is only used for display.

### Documented rule for picking a combo

The rule is in `docs/Stage3/Stage_3_Design_and_Interfaces_EN.md`, the CLI intro and `Customer.chooseItem`.

* **Regular and VIP customers** pick item `(id − 1) % 6` from the Stage 3 list:

  | Index | Item | Price | Prep units |
  | --- | --- | --- | --- |
  | 0 | Salad | 900 | 1 |
  | 1 | Burger | 1500 | 1 |
  | 2 | Pasta | 1800 | 2 |
  | 3 | Salad + Burger Combo | 2160 | 2 |
  | 4 | Salad + Pasta Combo | 2430 | 3 |
  | 5 | Burger + Pasta Combo | 2970 | 3 |

  This means customers #4, #5 and #6 would pick combos 3, 4 and 5.
* **Critics** pick the item with the most prep units, and the first one in menu order wins a tie (strict `>`). In Stage 3 that's always the **Salad + Pasta Combo** (3 units, 2430). It's no longer Pasta.
* With the default seed 24369 the customers are Regular, VIP, Regular, Regular, Regular, Critic. So #4 (Regular) gets Salad+Burger, #5 (Regular) gets Salad+Pasta, and #6 (Critic) gets Salad+Pasta.

### Things to look at in Phase 2

None of these are confirmed bugs yet:
* Recovery uses `isWaitingForFood()` (WAITING, SEATED, ORDERED), which covers both "waiting for a table" and "waiting for food". SERVED, READY_TO_PAY, PAID and LEFT are excluded.
* If Happy Hour is switched on at turn 18, it gets only one active turn.
* The remaining-turns display includes the current turn: 2 on the turn it's switched on, then 1.
* Expected regression change: running `Main` with no flag is now Stage 3. The Stage 2 QA tests that run the CLI without `--stage2` may fail for that reason, not because of a real regression. `--regular` now runs Stage 2 rules with only Regular customers.

I've stopped here. Phase 2 (the test plan) is next when you're ready.


## 008 | user

Thanks. Decisions on your open points:

1. Recovery covering WAITING, SEATED and ORDERED matches the brief ("waiting for a table or food"). Not a bug.
2. Happy Hour switched on at turn 18 getting only one active turn is fine, since the game ends there. Note it as an assumption.
3. The remaining-turns display counting the current turn (2, then 1) is fine. Check the CLI shows it clearly on both turns.
4. Regression: run the Stage 2 QA tests with --stage2 and the Stage 1 QA tests with --regular. Confirm that --regular at stage-3 still gives exactly Stage 1 behaviour, meaning no combos, no Host and no Happy Hour. Sort any failures into real regressions and expected changes. Don't edit tests until I approve.

Please also cover these in the Phase 2 plan:
- Rounding at each step vs once at the end. Check every real menu item and combo for Regular and VIP, with and without Happy Hour. Tell me if any case gives a different cent value. The brief's example is VIP Burger = 1080.
- Happy Hour can't be switched on a second time, or after the turn-start step.
- Price locking: an order taken during Happy Hour keeps its discount after Happy Hour ends, and an order taken just before it starts stays at full price.
- Recovery happens on both active turns. Customers who are SERVED, READY_TO_PAY, PAID or LEFT don't recover. A customer close to 0 can be saved by recovery.
- Host: a second Host seat in the same turn is rejected. Host and Waiter can both seat in one turn when two tables are free. A rejected Host action doesn't use up its turn.
- Combos go through the full flow (prep units, READY, SERVED, paid at the combo price), including the Critic picking the Salad + Pasta Combo.

Go ahead with Phase 2 and stop after the plan.


## 009 | assistant

I'll start by checking how the existing QA tests drive the game, since that decides how the regression runs work.


## 010 | assistant

The Stage 2 CLI tests run plain `Main`, which is now Stage 3. I'll run both QA classes unchanged first to get the raw result.


## 011 | assistant

Stage2RulesTest doesn't compile because `BillCalculator.applyDiscount` was removed (it moved to `fooditems.Discount.apply`). To get results without touching the tests, I'll run Stage 1 unchanged and a Stage 2 copy in the scratchpad.


## 012 | assistant

Stage 1 passes unchanged: 29/29 with `--regular`. Next, the Stage 2 copy in two versions: (a) only the `applyDiscount` → `Discount.apply` rename, running plain `Main`; (b) the same plus `--stage2`.


## 013 | assistant

Variant (b), with `--stage2`, leaves one failure, S2-4c. Its test menu overrides `getItems()`, but the Critic now reads `getOrderableItems()`, which uses the private list directly. Next I'll check that this is only the test hook moving, not a broken tie-break, and that `--regular` on stage-3 matches stage-2 output exactly.


## 014 | assistant

`--regular` and `--stage2` output on stage-3 is byte-for-byte the same as the stage-2 tag. The Critic tie-break still works once the test hooks the new method. I'll also diff `--regular` against the stage-1 tag itself and work out the rounding table.


## 015 | assistant

The only difference from the stage-1 tag is the intro banner (title, mode line and two Stage 2 rule lines). Gameplay is identical. Every real item gives the same cents whether rounded at each step or once at the end. I'll find a synthetic price where the two methods differ, so the test can prove which method the code uses.


## 016 | assistant

## Phase 2: Stage 3 test plan

All runs used the stage-3 worktree. No test files in it were edited: the Stage 2 variants are copies in the scratchpad.

### Rule 8 first: regression results (needs your approval)

| Run | Result |
| --- | --- |
| Stage 1 QA, unchanged (`Main --regular`) | **29/29 pass** |
| Stage 2 QA, unchanged | **Doesn't compile**: `BillCalculator.applyDiscount` no longer exists (lines 198–201) |
| Stage 2 copy, rename only, plain `Main` | 23 pass, 5 fail |
| Stage 2 copy, rename + `--stage2` | 27 pass, 1 fail (S2-4c) |

**Sorting the failures: no real regressions.**

| Failure | Type | Evidence |
| --- | --- | --- |
| Compile error, `applyDiscount` | Expected change | The method moved to `fooditems.Discount.apply`. The same four rounding checks pass against the new method, and `billFor` didn't change. |
| S2-R4, S2-R5, RUN2-V, RUN2-D (plain `Main`) | Expected change | Running with no flag is now Stage 3, so the Happy Hour and Host prompts shift the input numbers. With `--stage2` all four pass, and `--stage2` output is **byte-for-byte identical** to the stage-2 tag for both stage-2 runs. |
| S2-4c Critic tie-break (test menu) | Expected change (test hook) | The test overrides `Menu.getItems()`, but customers now read `getOrderableItems()`, which uses the private list, so the override is ignored. Overriding `getOrderableItems()` instead gives the correct first-item result (`A`). Real menus: Stage 3 → Salad + Pasta Combo, Stage 2 → Pasta. |

**`--regular` at stage-3 gives Stage 1 behaviour.** I ran `victory.txt` and `defeat.txt` through stage-3 `Main --regular` and through the stage-1 tag `Main`. Every turn, the menu and both final summaries are identical (VICTORY $66.00 / 79.20; DEFEAT 0 paid, 2 departures). The only differences are 4 intro lines: the "Stage 2" title, "Regular-only regression mode", and the waiting-loss and Critic rule text. Those runs printed no "Happy Hour", "Host" or "Combo" lines. The menu has 3 items and no Host prompt appears.

**Proposed test edits.** I'll make these only after you approve. Each one keeps the QA folder runnable on all three tags:

* **E1** (`Stage2RulesTest` lines 198–201): check the same four rounding cases through `BillCalculator.billFor(new VipCustomer(1), new MenuItem("T", 901, 1))` and so on. `billFor` exists at both tags. Using `Discount.apply` directly would break the stage-2 tag build.
* **E2** (`Stage2RulesTest.runMain`): add `--stage2` only when `domain.HappyHourPolicy` exists (a `Class.forName` check), because stage-2 `Main` rejects `--stage2`. Rename "through plain Main" to "through Stage 2 Main".
* **E3** (S2-4c test menu): also override `getOrderableItems()`, without `@Override`, so it still compiles at stage-2.
* **run-qa.sh**: detect Stage 3 via `src/domain/HappyHourPolicy.java`, run Stage1 → Stage2 → Stage3, and print three summary lines. Until E1 is in, `run-qa.sh` can't compile on stage-3.

### Rounding: each step vs once at the end

Real bills worked out with the game's `BillCalculator.billFor`. "Once" means rounding price × 72% a single time.

| Item | Price | Regular | Regular + HH | VIP | VIP + HH (each step) | VIP + HH (once) |
| --- | --- | --- | --- | --- | --- | --- |
| Salad | 900 | 900 | 720 | 810 | 648 | 648 |
| Burger | 1500 | 1500 | 1200 | 1350 | **1080** | 1080 |
| Pasta | 1800 | 1800 | 1440 | 1620 | 1296 | 1296 |
| Salad + Burger | 2160 | 2160 | 1728 | 1944 | 1555 (1555.2) | 1555 |
| Salad + Pasta | 2430 | 2430 | 1944 | 2187 | **1750** (1749.6) | 1750 |
| Burger + Pasta | 2970 | 2970 | 2376 | 2673 | 2138 (2138.4) | 2138 |

**No real menu case gives a different cent value.** All menu and combo prices are multiples of 10 cents, so the VIP step is always exact, and only the last step rounds. Rounding from the raw component sum also gives the same values. To prove the code really rounds at each step, the test will also use synthetic prices where the two methods differ:

* 13 → 12 → **10** at each step, vs 9.36 → 9 once
* 16 → 14 → **11** at each step, vs 11.52 → 12 once

### Rule tests (`Stage3RulesTest`, same style as Stage 1/2)

These use only real classes: `new Restaurant(Order::new, true)` and `StaffTeam`, with a fixed customer factory where I need to control customer types. The runs go through real `Main`.

**R1: Host**

| ID | Check |
| --- | --- |
| S3-1a | Host options are only Wait plus SEAT for queued customer × free table. No order or serve options. |
| S3-1b | Host seats #1, then a second Host action the same turn is rejected (controller is in the Waiter phase). Calling `StaffTeam.executeHost` directly twice gives "already acted". |
| S3-1c | With two queued customers and two free tables, the Host seats one and the Waiter seats the other in the same turn. The queue empties and both tables are full. |
| S3-1d | A rejected Host action (taken table, unknown customer, bad table) leaves the phase at `AWAIT_HOST` and the state unchanged, and a valid seat then succeeds the same turn. |
| S3-1e | Host seats #1, then the Waiter takes #1's order in the same turn. |
| S3-1f | The Waiter alone can still seat when the Host waits. Host actions are rejected during `AWAIT_TURN_START`. |

**R2: Combos**

| ID | Check |
| --- | --- |
| S3-2a | The three combos have the right parts, prep units 2/3/3 and prices 2160/2430/2970 (Salad + Pasta = 2430, 3 units). |
| S3-2b | The six-item list order matches the docs. Regular/VIP #1–#6 pick `(id − 1) % 6`, so #4 → Salad + Burger, #5 → Salad + Pasta, #6 → Burger + Pasta. |
| S3-2c | A Critic picks the Salad + Pasta Combo; the tie with Burger + Pasta goes to the first one in menu order. |
| S3-2d | Full flow for Salad + Pasta (#5 Regular): the Chef needs 3 prep actions. It isn't READY after 2. Once READY it can't be served that turn; it's served next turn and paid at 2430, and revenue goes up by 2430. |
| S3-2e | The same full flow for a Critic, paid at 2430. |
| S3-2f | `--stage2` and `--regular` show no combos (3-item menu). |

**R3: Happy Hour activation and display**

| ID | Check |
| --- | --- |
| S3-3a | Switch on at turn T: remaining is 2 at T and 1 at T+1, then 0 and inactive at T+2. |
| S3-3b | A second activation on a later turn is rejected ("already been used"), with no recovery and no phase change. The view's `canActivate` is false from then on. |
| S3-3c | `submitTurnStartDecision` is rejected in `AWAIT_HOST`, `AWAIT_WAITER` and `AWAIT_CHEF`, and nothing changes (still unused). A second decision in the same turn is rejected. |
| S3-3d | Staff actions before the turn-start decision are rejected. |
| S3-3e | CLI: the prompt appears only while Happy Hour is unused. "Happy Hour remaining: 2 turn(s) (activation used)" shows on the activation turn and "1 turn(s)" on the next, then 0. |
| S3-3f | Assumption: switching on at turn 18 gives one active turn and the game ends normally. |

**R4: Price locking**

| ID | Check |
| --- | --- |
| S3-4a | An order taken on turn T (activation) and one taken on T+1 are both billed at 80%, and still are at T+2 and later. They're paid at the discounted amount. |
| S3-4b | An order taken on T−1 (before activation) stays at full price through Happy Hour and is paid at full price. |
| S3-4c | An order taken on T+2 (just expired) is full price. |

**R5: Recovery**

| ID | Check |
| --- | --- |
| S3-5a | WAITING, SEATED and ORDERED customers each get +15 at the start of both active turns, capped at 100. A brand-new arrival stays at 100. |
| S3-5b | SERVED, READY_TO_PAY, PAID and LEFT customers don't change at an active turn start. |
| S3-5c | Waiting losses still apply at end of turn. Expected values over T and T+1, then no recovery at T+2. |
| S3-5d | Rescue: a queued Regular left alone leaves at the end of turn 13 without Happy Hour. Switching on at turn 12 (12 → 27 → 19 → 34 → 26) means they're still there at the end of turn 13 and don't leave in the game. |

**R6: Discount order**

| ID | Check |
| --- | --- |
| S3-6a | VIP Burger during Happy Hour = 1080 (1500 → 1350 → 1080), through a real order. |
| S3-6b | VIP Salad + Pasta during Happy Hour = 1750 (2430 → 2187 → 1749.6 → 1750), through a real order. |
| S3-6c | Every cell of the rounding table above, plus the synthetic 13 and 16 cases. |

**R7: Win/lose**

| ID | Check |
| --- | --- |
| S3-7a | `GameConfig` didn't change between tags. The Stage 3 game is still 18 turns, 2 tables, arrivals on turns 1/4/7/10/13/16, and the thresholds 4 paid / $45.00 / average 60. |
| S3-7b | A Stage 3 game that ends just past a threshold. Using Happy Hour doesn't change how the result is decided. |

**R8: Regression**

| ID | Check |
| --- | --- |
| S3-8a | After E1–E3, Stage 1 and Stage 2 QA pass on stage-3 (and still on their own tags). |

**Phase 3 runs:** `runs/stage3-victory.txt` and `runs/stage3-defeat.txt`, played through plain stage-3 `Main`. Each line will be commented as Happy Hour / Host / Waiter / Chef. Both runs will use the Host seating someone, at least one combo order and a Happy Hour activation. The checks will assert the final summary and that no "Invalid" line appears.

**Assumptions noted:** Happy Hour at turn 18 gives one turn; the remaining count includes the current turn; the Host acts before the Waiter (the documented design choice); and the Stage 2 `getItems()` override not affecting customers is a test-hook change, not a bug.

**What I need from you:** approval for E1–E3 and the `run-qa.sh` change. After that I'll write the tests and runs in Phase 3.


## 017 | user

Approved: E1, E2, E3 and the run-qa.sh change, since they only touch files in test/qa/.

One correction to S3-5d. After turn 13 the customer is at 26 and still loses 8 per turn (18, 10, 2, 0), so they leave at the end of turn 17, not never. The test should check that Happy Hour moves the departure from turn 13 to turn 17.

Go ahead with Phase 3 and stop after the runs and test results.


## 018 | assistant

You're right about S3-5d: from 26 the customer drops 18, 10, 2, 0 and leaves at the end of turn 17. I'll test that the departure moves from turn 13 to turn 17. First I'll read the existing test style and the remaining API I need.


## 019 | assistant

Applying the approved edits E1–E3 to `Stage2RulesTest.java`.


## 020 | assistant

Edits applied. Now the CLI and controller, so the Stage 3 runs and tests use exact prompts and option numbers.


## 021 | assistant

I'll generate the two CLI runs from a scripted strategy played through the real controller. A scratch generator records each choice's menu index and writes a commented line for it, so the option numbers can't drift.


## 022 | assistant

Two strategies work:
* **Victory:** fast service, Happy Hour at turn 13, so #5's Salad + Pasta combo is locked at $19.44. Result: 5 paid, $81.54, average 87.80.
* **Defeat:** Happy Hour wasted at turn 1, Host idle until turn 10. Result: 3 paid, $40.50, average 37.00.

Next I'll write the run files with a header and check them through the real CLI.


## 023 | assistant

Both runs replay cleanly through `Main`: no "Invalid" lines, and the line counts match (13 Happy Hour prompts + 54 = 67; 1 + 54 = 55). I'll make the comments explain the deliberate waits in the defeat run, then regenerate.


## 024 | assistant

The run files are done. Now I'll write `Stage3RulesTest.java` in the same style as the Stage 1/2 classes.


## 025 | assistant

I'm fixing three weak spots in the file before running it:
* The RUN3-V summary comparison was circular, so it always passed. It now compares against the real controller result.
* The `canActivate` expectations before each decision were wrong for turn 3.
* The "no 4th unit" check read a view where Chef options are always empty, so it passed trivially.


## 026 | assistant

All three classes pass on stage-3: 29 + 28 + 33. Before trusting a fully green first run, I'll check two things:
* The tests actually catch faults: I'll plant known bugs (mutants) in a scratch copy of `src`.
* The edited QA folder still runs on the stage-2 tag.


## 027 | assistant

All 10 seeded mutants were caught by at least one test. Next, portability: the edited QA folder against the stage-2 and stage-1 sources.


## 028 | assistant

The QA folder runs on all three tags. Last, the final CLI summaries and confirmation that nothing outside `test/qa/` changed.


## 029 | assistant

## Phase 3: Stage 3 tests and scripted runs

All 90 checks pass on stage-3, and both Stage 3 runs give the expected results through the real CLI. No bugs found. Everything I changed is inside `test/qa/` in the stage-3 worktree; game code, tags and the tag commit are unchanged.

### What I changed in `test/qa/`

* **[Stage2RulesTest.java](../SC2002_stage3_test/test/qa/Stage2RulesTest.java):** your approved E1–E3.
  * E1: the rounding checks now go through `billFor`, using a new `vipBill(cents)` helper.
  * E2: `runMain` adds `--stage2` only when `domain.HappyHourPolicy` is found, and "plain Main" is renamed "Stage 2 Main" in the labels.
  * E3: the test menu also overrides `getOrderableItems()`.
* **[run-qa.sh](../SC2002_stage3_test/test/qa/run-qa.sh):** runs Stage 1 → 2 → 3 and prints three summary lines. Each stage is skipped if its code isn't at the tag.
* **[Stage3RulesTest.java](../SC2002_stage3_test/test/qa/Stage3RulesTest.java)** (new): the 33 tests from the plan.
  * S3-5d now checks the departure moving from turn 13 to turn 17, as you corrected: 27/34 after recovery, then 19, 26, 18, 10, 2 at the end of turns 12–16, and LEFT at the end of turn 17.
  * I renumbered slightly: the CLI display check is S3-3d, and the turn-18 assumption is S3-3e.
* **[runs/stage3-victory.txt](../SC2002_stage3_test/test/qa/runs/stage3-victory.txt) and [runs/stage3-defeat.txt](../SC2002_stage3_test/test/qa/runs/stage3-defeat.txt)** (new): each has a header with the strategy and expected result, and a comment on every input line. I generated them by playing the strategy through `GameController` and recording each choice's menu number, so the comments match the real menus.

### Results (`bash test/qa/run-qa.sh` on stage-3)

```
================ QA SUMMARY ================
Stage 1 rules (all-Regular regression): PASS (passed=29 failed=0)
Stage 2 rules (VIP / Critic): PASS (passed=28 failed=0)
Stage 3 rules (Host / Combo / Happy Hour): PASS (passed=33 failed=0)
```

**Same folder on the other tags:**
* stage-1: Stage 1 PASS 29/29; Stages 2 and 3 skipped.
* stage-2: Stage 1 PASS 29/29, Stage 2 PASS 28/28; Stage 3 skipped.

**Seeded faults:** I ran Stage3RulesTest against 10 deliberately broken copies of `src` in the scratchpad, and every one made at least one test fail:

| Fault planted | Caught by |
| --- | --- |
| Happy Hour applied before the VIP discount | S3-6 |
| Recovery for every customer state | S3-5, RUN3-V |
| No 100 cap on recovery | S3-5, both runs |
| Host can seat any number per turn | S3-1 |
| Happy Hour lasts 3 turns | S3-3, S3-4, S3-5, both runs |
| Recovery only on the activation turn | S3-5, both runs |
| Happy Hour can be switched on again | S3-3 |
| Combo prep = larger part instead of sum | S3-2, S3-3, both runs |
| No combo discount | S3-2, S3-6, both runs |
| Critic tie goes to the last item | S3-2, RUN3-V |

### Final summaries of the runs (plain stage-3 `Main`)

**stage3-victory.txt** (Host seats every arrival, combos for #4–#6, Happy Hour on turn 13, so #5's Salad + Pasta Combo is $19.44):
```
================ VICTORY ================
Dishes served 5 | Paid 5 | Unhappy departures 0 | Revenue $81.54 | Avg satisfaction 87.80 | Turns used 18/18
  Paid customers 5 (need 4)
  Revenue $81.54 (need $45.00)
  Avg satisfaction 87.80 (need 60)
```

**stage3-defeat.txt** (Happy Hour wasted on turn 1, Host waits until turn 10, #4's Salad + Burger Combo is cooked but never served):
```
================ DEFEAT ================
Dishes served 3 | Paid 3 | Unhappy departures 0 | Revenue $40.50 | Avg satisfaction 37.00 | Turns used 18/18
  Paid customers 3 (need 4)
  Revenue $40.50 (need $45.00)
  Avg satisfaction 37.00 (need 60)
```

Neither run printed an "Invalid" line, and both ran to turn 18. The victory result matches the same strategy played through `GameController`.

### Two observations to decide on in Phase 4 (not counted as bugs)

* **Turn 18:** if Happy Hour is switched on at turn 18, the display shows "2 turn(s)" remaining, though only one turn is left. That follows the "counts the current turn" assumption you accepted; S3-3e records it.
* **Menu hook:** `Menu.getOrderableItems()` reads its private list instead of calling `getItems()`, so a subclass that overrides `getItems()` no longer changes what customers pick. Real gameplay isn't affected; it only mattered for the S2-4c test menu, now fixed by E3.

Logs are in `../SC2002_stage3_test/build/qa/` (gitignored). I've stopped here; Phase 4 (the report) is next when you're ready.


## 030 | user

Both observations accepted as documented assumptions, no action.

Go ahead with Phase 4. Give me:
1. One pass/fail table covering all three stages, with a short line on the seeded-fault check.
2. Any bugs found (none so far) and the list of assumptions we accepted across all stages.
3. Drafts of the stage-1 to stage-2 and stage-2 to stage-3 README sections: new files, changed files with reasons, and compare links in the format github.com/<org>/<repo>/compare/stage-1...stage-2.
4. Every class in stage-3 with its key attributes and operations (with visibility), inheritance, associations and multiplicities, and enums, so I can check the class diagram against the code.
5. Exact commands for a TA to run run-qa.sh and the victory and defeat runs for each stage.


## 031 | assistant

I'll collect the facts for the report: the remote, `play.sh`, earlier-stage assumptions, the stage-1→2 diff and the full stage-3 source.


## 032 | assistant

Now the full stage-3 source, for the class list.


## 033 | assistant

Interface methods and one-line getters were missed by that filter, so I'll read those files in full.


## 034 | assistant

Before writing the TA commands, I'll check that `play.sh` really reproduces every stage's runs at its own tag.


## 035 | assistant

All six runs reproduce at their own tags. One more check: I'll compare the class list against the team's Stage 3 class diagram source (`.mmd`) so I can flag any mismatches.


## 036 | assistant

Written for: the README drafts (section 3) and TA commands (section 5) are for markers and TAs reading the repo. The rest is for you as testing lead.

# Phase 4: Stage 1–3 QA report

No bugs were found in any stage: all 90 QA checks pass on every tag where their code exists, and every planted fault was caught. Two gaps before this goes out:

* **`test/qa/` isn't committed anywhere yet.** It only exists, untracked, in my local stage-3 worktree. The TA commands in section 5 assume it's on `main`. Should I commit it to your `Rishika` branch?
* **I compared my class list against the team's diagram source** (`docs/UML/Stage3_UML_Diagram.mmd`) and found some omissions. I've listed them at the end of section 4.

## 1. Pass/fail across all three stages

| Suite | at `stage-1` | at `stage-2` | at `stage-3` |
| --- | --- | --- | --- |
| Stage 1 rules (29 tests) | **29/29 PASS** | **29/29 PASS** (`--regular`) | **29/29 PASS** (`--regular`) |
| Stage 2 rules (28 tests) | not applicable | **28/28 PASS** | **28/28 PASS** (`--stage2`) |
| Stage 3 rules (33 tests) | not applicable | not applicable | **33/33 PASS** |
| Victory run | `victory.txt`: VICTORY, 5 paid, $66.00, 79.20 | `stage2-victory.txt`: VICTORY, 5 paid, $64.50, 80.40 | `stage3-victory.txt`: VICTORY, 5 paid, $81.54, 87.80 |
| Defeat run | `defeat.txt`: DEFEAT, 0 paid, 2 departures | `stage2-defeat.txt`: DEFEAT, 4 paid, $49.50, average 59.75 | `stage3-defeat.txt`: DEFEAT, 3 paid, $40.50, 37.00 |

**Stage 3 rule coverage:**

| Brief rule | Tests | Result |
| --- | --- | --- |
| 1. Host | S3-1a to 1f | PASS |
| 2. Combos | S3-2a to 2f | PASS |
| 3. Happy Hour activation and display | S3-3a to 3e | PASS |
| 4. Price locking | S3-4a to 4d | PASS |
| 5. Recovery | S3-5a to 5d | PASS |
| 6. Discount order | S3-6a to 6c | PASS |
| 7. Win/lose | S3-7a, 7b | PASS |
| Design check (no type checks) | S3-D | PASS |
| CLI runs | RUN3-V, RUN3-D | PASS |
| 8. Regression | the Stage 1 and Stage 2 rows above | PASS |

**Seeded faults:** I planted known bugs in throwaway copies of the code and checked that the tests fail.
* Stage 1: 2 faults, 9 failures.
* Stage 2: 3 of 3 faults caught.
* Stage 3: 10 of 10 caught: discount order, recovery states, recovery cap, Host limit, Happy Hour duration, recovery on the second turn, second activation, combo prep, combo discount, Critic tie-break.

**Regression summary:**
* At stage-3, `--regular` gameplay is identical to the stage-1 tag. Only 4 intro lines differ.
* At stage-3, `--stage2` output is byte-for-byte identical to the stage-2 tag.
* The only test changes were the three you approved: E1–E3 in `Stage2RulesTest`.

## 2. Bugs and accepted assumptions

**Bugs: none in any stage.**

**Stage 1 testing limits (noted, not bugs):**
* R4b: serving a dish on the same turn it becomes READY can't happen in play, because the Waiter acts before the Chef. The guard was tested directly.
* R6b: two customers waiting to pay at once can't happen in play, because only one dish is served per turn. Tested directly.
* R1d: the number of staff is checked through behaviour (one task each per turn), because `StaffTeam`'s fields are private.
* R8b: money being whole cents is guaranteed by the `int`/`long` types, so tests can only check the values.

**Stage 2:**
* Exactly half a cent rounds up.
* The Critic arrives on turn 16 and can't pay within 18 turns in the default game. Its rules were tested with a factory that makes the first customer a Critic.
* The seed can't be chosen on the command line; it's always 24369.
* Notes only, no action:
  * `CustomerFactory` uses a `switch` on customer type.
  * The method name `getSatistfactionLossPerTurn` has a typo, still present at stage-3.
  * The leftover "(Regular,VIP,Host)" comment in `Customer.java` was fixed at stage-3.

**Stage 3:**
* Recovery covers WAITING, SEATED and ORDERED customers.
* Happy Hour switched on at turn 18 gets one active turn, and the display shows "2 turn(s)".
* The remaining count includes the current turn: 2, then 1.
* The Host acts before the Waiter. This is the team's documented design choice.
* `Menu.getOrderableItems()` doesn't call `getItems()`, which only affected a test hook.
* Plain `Main` is now Stage 3, so the regressions run Stage 1 with `--regular` and Stage 2 with `--stage2`.
* On the real menu, rounding at each step and rounding once give the same cents. The code was shown to round at each step, VIP first, using made-up prices 13 → 10 and 16 → 11.

## 3. README section drafts

### Stage 1 → Stage 2

````markdown
## Changes from Stage 1 to Stage 2

Compare: https://github.com/Cooolc327/SC2002_Group_Assignment/compare/stage-1...stage-2

Stage 2 adds VIP and Critic customers. The turn order, staff limits, tables, arrivals and win/lose
targets are unchanged. `Main --regular` still plays the Stage 1 all-Regular game.

### New files
| File | Reason |
| --- | --- |
| `src/entities/VipCustomer.java` | Loses 5 satisfaction per waiting turn; 10% bill discount |
| `src/entities/CriticCustomer.java` | Loses 12 per waiting turn; picks the dish with the most prep units (first on the menu wins a tie) |
| `src/entities/CustomerFactory.java` | Seeded schedule (default 24369): exactly 1 VIP, 1 Critic and 4 Regular customers |
| `src/orders/BillCalculator.java` | Bills in whole cents, rounded to the nearest cent, using the customer's discount |
| `test/unit/BillCalculatorTest.java`, `test/unit/OrderMenuTest.java` | Billing, order and menu tests |
| `test/integration/Stage2IntegrationTest.java`, `scripts/test-stage2.sh` | Stage 2 game and CLI tests |
| `test/replays/stage2-*.txt` | Victory, defeat and delayed-VIP CLI inputs |
| `docs/Stage2/*`, `docs/UML/Stage2_*`, `docs/verification/*stage-2*` | Change notes, class and VIP sequence diagrams, release evidence |

### Changed files
| File | Change and reason |
| --- | --- |
| `src/entities/Customer.java` | Adds `getBillDiscountPercent()` (0 by default) so each customer type supplies its own discount |
| `src/fooditems/MenuItem.java` | Adds `getPrepUnits()`, used by the Critic's choice |
| `src/domain/Restaurant.java` | The bill is worked out by `BillCalculator` and fixed when the order is taken |
| `src/control/GameController.java` | Takes a customer factory; the old constructor keeps all-Regular customers |
| `src/Main.java` | Uses the seeded mixed factory; `--regular` gives the Stage 1 customers |
| `src/cli/RestaurantCLI.java` | Shows the stage, seed and the VIP/Critic rules |
| `scripts/build.sh`, `test/integration/Stage1SmokeTest.java` | Build message; the Stage 1 smoke test now runs `Main --regular` |

No files were deleted. Staff, orders, seating, `GameConfig`, `TurnPhase` and `GameResult` are unchanged.
````

### Stage 2 → Stage 3

````markdown
## Changes from Stage 2 to Stage 3

Compare: https://github.com/Cooolc327/SC2002_Group_Assignment/compare/stage-2...stage-3

Stage 3 adds a Host, combo meals and a once-per-game Happy Hour. Each turn is now:
arrival -> Happy Hour decision -> Host -> Waiter -> Chef -> eating, payment and waiting losses.
Plain `Main` is Stage 3. `--stage2` and `--regular` replay the earlier rules.
Tables, arrivals, Waiter/Chef/Cashier limits and win/lose targets are unchanged.

Rules:
- The Host seats at most one queued customer per turn, on top of the Waiter's task.
- A combo's prep units are the sum of its two items, and its price is 90% of their combined price
  (Salad + Pasta = $24.30, 3 units).
- Regular and VIP customers pick item `(id - 1) % 6` from Salad, Burger, Pasta and then the three combos.
  The Critic picks the Salad + Pasta Combo.
- Happy Hour can be switched on once, at the start of a turn, and lasts that turn and the next.
  Orders taken during it are 20% off, applied after the VIP discount and rounded at each step
  (VIP Burger $10.80). The price is fixed when the order is taken.
- On each Happy Hour turn, customers waiting for a table or for food recover 15 satisfaction (maximum 100).

### New files
| File | Reason |
| --- | --- |
| `src/staff/Host.java`, `src/contracts/HostAction.java` | Seating-only staff member with its own once-per-turn limit |
| `src/fooditems/ComboMeal.java` | A `MenuItem` made of two items: prep units added, price 90% |
| `src/fooditems/Discount.java` | Percentage discount rounded to the nearest cent, used by combos and bills |
| `src/domain/HappyHourPolicy.java` | Single activation; works out how many turns remain from the turn number |
| `test/unit/ComboMealTest.java`, `test/integration/Stage3IntegrationTest.java`, `scripts/test-stage3.sh` | Stage 3 tests |
| `test/replays/stage3-*.txt` | Victory and defeat CLI inputs |
| `docs/Stage3/*`, `docs/UML/Stage3_*`, `docs/verification/Stage_3_*` | Design notes, class and Happy Hour sequence diagrams, release evidence |

### Changed files
| File | Change and reason |
| --- | --- |
| `src/domain/Restaurant.java` | Owns the Happy Hour policy; turn-start step with recovery; Host options; Happy Hour price fixed at ordering; staff can't act before the turn-start step |
| `src/control/GameController.java`, `src/control/TurnPhase.java` | New `AWAIT_TURN_START` and `AWAIT_HOST` phases; `submitTurnStartDecision`, `submitHostAction` |
| `src/contracts/StaffGateway.java`, `src/staff/StaffTeam.java` | `executeHost`; the default rejects, so older test staff still work |
| `src/entities/Customer.java`, `src/entities/CriticCustomer.java` | Choose from `getOrderableItems()`; recovery limited to waiting customers |
| `src/fooditems/Menu.java` | Three combos; `getOrderableItems()` returns 3 items in Stage 2 and 6 in Stage 3 |
| `src/orders/BillCalculator.java` | `billFor(customer, item, happyHourActive)`: VIP discount, then 20%; rounding moved to `Discount` |
| `src/view/GameView.java` | Host options, Happy Hour status (remaining turns), Stage 3 flag |
| `src/cli/RestaurantCLI.java`, `src/Main.java` | Happy Hour and Host prompts, remaining-turns line, six-item menu; `--stage2` and `--regular` modes |
| `scripts/build.sh`, `test/integration/Stage2IntegrationTest.java`, `test/unit/BillCalculatorTest.java` | Build message; the Stage 2 test runs `Main --stage2`; tests for the shared rounding |

No files were deleted. The controller has no customer or combo type checks: combos go through the normal
`MenuItem` and `Order` flow.
````

## 4. Stage 3 classes (for checking the class diagram)

Notation: `+` public, `-` private, `#` protected, `$` static. Record fields are private final with public accessors.

**Entry point and UI**
* **`Main`**
  * Operations: `+main(String[])$`.
  * Creates `CustomerFactory`, `Restaurant(Order::new, stage3)`, `StaffTeam`, `GameController` and `RestaurantCLI`.
* **`RestaurantCLI`**
  * Attributes: `-controller: GameController`, `-in: Scanner`, `-setupDescription: String`.
  * Operations: `+run()`, plus private `chooseTurnStart`, `chooseHostAction`, `chooseWaiterAction`, `chooseChefAction`, `pick`, `report`, `printIntro`, `printState`, `printResult`, `describe…`, `readInt`.
  * Holds 1 `GameController` and reads only `GameView`.

**Control**
* **`GameController`** (final)
  * Attributes: `-restaurant`, `-staff: StaffGateway`, `-customerFactory: IntFunction<? extends Customer>`, `-currentTurn`, `-nextCustomerId`, `-phase: TurnPhase`, `-result: GameResult`, `-lastPaymentResult: ActionResult`.
  * Operations: `+startTurn(): GameView`, `+submitTurnStartDecision(boolean)`, `+submitHostAction(HostAction)`, `+submitWaiterAction(WaiterAction)`, `+submitChefAction(ChefAction)`, `+finishTurn(): GameView`, `+getView(): GameView`, `+getLastPaymentResult()`.
  * Holds 1 `Restaurant` and 1 `StaffGateway`; builds `GameView` snapshots.
* **`TurnPhase`** (enum): `NOT_STARTED`, `AWAIT_TURN_START`, `AWAIT_HOST`, `AWAIT_WAITER`, `AWAIT_CHEF`, `AUTO`, `TURN_ENDED`, `FINISHED`.
* **`GameResult`** (enum): `IN_PROGRESS`, `VICTORY`, `DEFEAT`, with `+evaluate(GameSummary)$`.
* **`GameConfig`** (final)
  * Constants (`+…$`): `MAX_TURNS = 18`, `TABLE_COUNT = 2`, `ARRIVAL_TURNS = {1, 4, 7, 10, 13, 16}`, `MIN_PAID_CUSTOMERS = 4`, `MIN_REVENUE_CENTS = 4500`, `MIN_AVG_SATISFACTION = 60`.

**Domain**
* **`Restaurant`** (final)
  * Attributes: `-stage3Enabled`, `-happyHour: HappyHourPolicy`, `-menu: Menu`, `-seatingArea: SeatingArea`, `-orderFactory: OrderPort.Factory`, `-customers: Map<Integer, Customer>`, `-orders: Map<Integer, OrderPort>`, `-currentTurn`, `-completedTurns`, `-lastPenaltyTurn`, `-lastPaymentTurn`, `-nextOrderId`, `-lastTurnStartPolicyTurn`.
  * Turn flow: `+beginTurn`, `+registerArrival`, `+resolveTurnStart(boolean)`, `+advanceEating`, `+applyWaitingPenalty`, `+completeTurn`.
  * Staff actions: `+seatCustomer`, `+placeOrder`, `+advancePreparation`, `+serveOrder`, `+findOldestPayableCustomer`, `+settlePayment`.
  * Views and options: `+getSummary`, `+get…Views`, `+getWaitingCustomerIds`, `+getWaiterOptions`, `+getHostOptions`, `+getChefOptions`.
  * Stage 3 queries: `+isStage3Enabled`, `+hasUsedHappyHour`, `+getHappyHourRemainingTurns`, `+isActionTurnOpen`.
  * Relationships:
    * Composition, 1 each: `HappyHourPolicy`, `Menu`, `SeatingArea`.
    * Aggregation: 0..* `Customer`.
    * Composition: 0..* `OrderPort`.
    * Uses 1 `OrderPort.Factory`.
    * Depends on `BillCalculator`.
* **`HappyHourPolicy`** (final)
  * Attributes: `+DURATION_TURNS = 2$`, `+SATISFACTION_RECOVERY = 15$`, `-activationTurn: Integer`.
  * Operations: `+hasBeenUsed()`, `+activate(int)`, `+remainingTurns(int)`, `+isActive(int)`.

**Customers**
* **`Customer`** (abstract)
  * Attributes: `+MAX_SATISFACTION = 100$`, `-id`, `-satisfaction`, `-state: CustomerState`, `-servedTurn`, `-readyToPayTurn`.
  * Abstract operations: `#getSatistfactionLossPerTurn(): int {abstract}`, `+getTypeName(): String {abstract}`.
  * Choice, billing and satisfaction: `+chooseItem(Menu): MenuItem`, `+getBillDiscountPercent()`, `+recoverWaitingSatisfaction(int)`, `+applyWaitingPenalty()`, `+isOutOfPatience()`.
  * State changes: `+seat()`, `+placeOrder()`, `+serve(int)`, `+finishedEating(int)`, `+pay()`, `+abandon()`.
  * Getters and helpers: `+getId`, `+getSatisfaction`, `+getState`, `+getReadyToPayTurn`, `+describe`, `+toString`, `-require`.
* **`RegularCustomer`**: extends `Customer`; `+SATISFACTION_LOSS_PER_TURN = 8$`; overrides the loss method and `getTypeName`.
* **`VipCustomer`**: extends `Customer`; `-LOSS_PER_TURN = 5$`, `-BILL_DISCOUNT_PERCENT = 10$`; also overrides `getBillDiscountPercent`.
* **`CriticCustomer`**: extends `Customer`; `-LOSS_PER_TURN = 12$`; also overrides `chooseItem(Menu)`.
* **`CustomerFactory`** (final)
  * Attributes: `+DEFAULT_SEED = 24369$`, `-seed`, `-schedule: Kind[]`, and the private enum `Kind {Regular, VIP, Critic}`.
  * Operations: `+createCustomer(int): Customer`, `+getSeed()`.
  * Creates the three subtypes.
* **`CustomerState`** (enum): `WAITING`, `SEATED`, `ORDERED`, `SERVED`, `READY_TO_PAY`, `PAID`, `LEFT`, with `+isWaitingForFood()`.

**Seating**
* **`SeatingArea`**
  * Attributes: `-tables: List<Table>`, `-queue: WaitingQueue`.
  * Operations: `+addToQueue`, `+isQueued`, `+hasFreeTable`, `+seat(Customer)`, `+seat(Customer, int)`, `+removeCustomer`, `+getTables`, `+getQueuedCustomers`.
  * Composition: 2 `Table` (`TABLE_COUNT`) and 1 `WaitingQueue`.
* **`Table`**
  * Attributes: `-id`, `-occupant: Customer`.
  * Operations: `+isFree`, `+seat`, `+release`, `+getId`, `+getOccupant`.
  * Refers to 0..1 `Customer`.
* **`WaitingQueue`**
  * Attributes: `-customers: List<Customer>`.
  * Operations: `+add`, `+remove`, `+contains`, `+isEmpty`, `+getCustomers`.
  * Aggregation: 0..* `Customer`.

**Food items**
* **`MenuItem`**
  * Attributes: `-name`, `-priceCents`, `-prepUnits`.
  * Operations: `+getName`, `+getPriceCents`, `+prepUnits()`, `+getPrepUnits()`, `+getFormattedPrice`.
* **`ComboMeal`**
  * Extends `MenuItem`.
  * Attributes: `+COMBO_DISCOUNT_PERCENT = 10$`, `-components: List<MenuItem>`.
  * Operations: `+getComponents()`.
  * Holds 2 shared `MenuItem` parts; depends on `Discount`.
* **`Menu`**
  * Attributes: `-combosEnabled`, `-salad`, `-burger`, `-pasta`, `-items: List<MenuItem>`, `-combos: List<ComboMeal>`.
  * Operations: `+getItems`, `+getCombos`, `+getOrderableItems`, `+contains(MenuItem)`.
  * Composition: 3 `MenuItem` and 3 `ComboMeal`.
* **`Discount`** (final, private constructor): `-PERCENT_BASE = 100$`, `+apply(int, int)$`.

**Orders**
* **`BillCalculator`** (final, private constructor)
  * Attributes: `+HAPPY_HOUR_DISCOUNT_PERCENT = 20$`.
  * Operations: `+billFor(Customer, MenuItem)$`, `+billFor(Customer, MenuItem, boolean)$`.
  * Depends on `Customer`, `MenuItem` and `Discount`.
* **`OrderPort`** (interface)
  * Operations: getters for id, customerId, item, billCents, preparedUnits, status, readyTurn, servedTurn, paidTurn and satisfactionAtPayment; `advanceOneUnit`, `markServed`, `markPaid`, `cancel`.
  * Nested enum `Status`: `PLACED`, `PREPARING`, `READY`, `SERVED`, `PAID`, `CANCELLED`.
  * Nested functional interface `Factory`: `create(int, Customer, MenuItem, int): OrderPort`.
* **`Order`**
  * Implements `OrderPort`.
  * Attributes: `-id`, `-customerId: int` (an id, not a `Customer` reference), `-item: MenuItem`, `-billCents` (final), `-preparedUnits`, `-status`, `-readyTurn`, `-servedTurn`, `-paidTurn`, `-satisfactionAtPayment`.
  * Refers to 1 `MenuItem`.

**Staff and contracts**
* **`StaffGateway`** (interface): `+executeHost(…)` (a default method that rejects), `+executeWaiter`, `+executeChef`, `+processPayment`.
* **`StaffTeam`**: implements `StaffGateway`; composition of 1 each `-host`, `-waiter`, `-chef`, `-cashier`.
* **`Host`** (final): `-lastActionTurn`; `+execute(HostAction, Restaurant, int)`.
* **`Waiter`**: `-lastActionTurn`; `+execute(WaiterAction, Restaurant, int)`, `+getLastActionTurn`.
* **`Chef`**: `-lastActionTurn`; `+execute(ChefAction, Restaurant, int)`, `+getLastActionTurn`.
* **`Cashier`**: `-lastActionTurn`; `+processPayment(Restaurant, int)`, `+getLastActionTurn`.
* All four staff depend on `Restaurant`, which is passed in as a parameter.
* **Action records** (`contracts`):
  * `ActionResult(accepted, message)`, with `success`$ and `rejected`$.
  * `HostAction(type, customerId, tableId)`, enum `Type {SEAT, WAIT}`, with `seat`$ and `waitTurn`$.
  * `WaiterAction(type, customerId, tableId, orderId)`, enum `Type {SEAT, TAKE_ORDER, SERVE, WAIT}`.
  * `ChefAction(type, orderId)`, enum `Type {PREPARE, WAIT}`.

**View records** (`view`)
* **`GameView`**: `turn`, `phase`, `customers`, `waitingCustomerIds`, `tables`, `orders`, `menu`, `staff: StaffView`, `summary: GameSummary`, `result`, `stage3Enabled`, `happyHour: HappyHourView`.
* **Nested in `GameView`:**
  * `CustomerView`
  * `TableView`
  * `HappyHourView(canActivate, used, remainingTurns)`
  * `MenuItemView`
  * `OrderView`
  * `StaffView(hostAvailable, hostOptions, waiterAvailable, chefAvailable, cashierAvailable, waiterOptions, chefOptions)`
* **`GameSummary`**: `servedCount`, `paidCount`, `unhappyDepartures`, `revenueCents`, `paidSatisfactionSum`, `turnsUsed`, with `+averagePaidSatisfaction(): OptionalDouble`.

**What the code has but `Stage3_UML_Diagram.mmd` leaves out** (missing elements, not wrong ones):
* **`Customer`:** its abstract methods `#getSatistfactionLossPerTurn()` and `+getTypeName()`, its `-id` and `MAX_SATISFACTION`, and the subclasses' overrides of the loss method. The diagram's note says loss is polymorphic, but that operation isn't shown.
* **Constants:** `COMBO_DISCOUNT_PERCENT` and `HAPPY_HOUR_DISCOUNT_PERCENT`.
* **`Restaurant`:** `getHostOptions()` and `isActionTurnOpen()`, and the `orderFactory` association.
* **`GameController`:** the `customerFactory` association and its link to `GameResult`.
* **Whole classes:** `ActionResult`, `WaiterAction`, `ChefAction`, `GameResult` and `GameConfig`. This may be a deliberate simplification.
* **`Menu.contains()`:** nothing in the game calls it.

Everything else I checked lines up: inheritance, multiplicities, the four staff compositions, `ComboMeal` → 2 `MenuItem`, `Menu` 3 + 3, the Happy Hour members and the new phases.

## 5. TA commands

You need JDK 17+ and Bash (Git Bash on Windows). This assumes `test/qa/` is committed to `main`. The tags don't contain it, so it's copied into each tag's worktree.

```bash
git clone https://github.com/Cooolc327/SC2002_Group_Assignment.git
cd SC2002_Group_Assignment
for s in 1 2 3; do git worktree add ../rr-stage-$s stage-$s && cp -r test/qa ../rr-stage-$s/test/; done

# Stage 1
cd ../rr-stage-1
bash test/qa/run-qa.sh                              # Stage 1 rules: PASS (passed=29 failed=0)
bash test/qa/play.sh test/qa/runs/victory.txt       # VICTORY: 5 paid, $66.00, 79.20
bash test/qa/play.sh test/qa/runs/defeat.txt        # DEFEAT: 0 paid, 2 departures

# Stage 2
cd ../rr-stage-2
bash test/qa/run-qa.sh                              # Stage 1 29/29, Stage 2 28/28
bash test/qa/play.sh test/qa/runs/stage2-victory.txt   # VICTORY: 5 paid, $64.50, 80.40
bash test/qa/play.sh test/qa/runs/stage2-defeat.txt    # DEFEAT: 4 paid, $49.50, 59.75

# Stage 3
cd ../rr-stage-3
bash test/qa/run-qa.sh                              # Stage 1 29/29, Stage 2 28/28, Stage 3 33/33
bash test/qa/play.sh test/qa/runs/stage3-victory.txt   # VICTORY: 5 paid, $81.54, 87.80
bash test/qa/play.sh test/qa/runs/stage3-defeat.txt    # DEFEAT: 3 paid, $40.50, 37.00
```

I ran exactly this layout (each tag's `src` and `scripts` plus this `test/qa`) in the scratchpad today. All three `run-qa.sh` runs and all six `play.sh` runs gave the results in the comments.
