# Stage 1 AI conversation 01

Member: Rishika Garg (Member E). AI assistant: Claude Code (Claude Opus 5.5), VS Code extension.

Topic: Stage 1 testing: code map, test plan, plain-Java rule tests and scripted victory/defeat runs.

Source conversation ID: `e1dc7c2f-1c3b-470b-b722-ffe8819cb7e3`.

Date: 2026-11-14.

The original user and assistant messages appear below in source order, including user-facing progress messages. Wording, tables and code are kept as originally written; only line endings and trailing whitespace are normalized. System/developer instructions, environment metadata, internal reasoning and tool calls/results are not part of this conversation transcript. Per-message timestamps are not included. See the [history index](README.md) for scope.

Historical statements describe the repository at the time they were made. File links in the assistant replies point to a separate `stage-1` worktree (`../SC2002_stage1_test`) that existed on the author's machine during the session; they are kept as written.

<!-- transcript-message-start:chat-01-m001 -->
## Message 001 - User

<!-- original-text-start:chat-01-m001 -->
I'm the testing lead for our SC2002 Restaurant Rush project. I want to check Stage 1 before we submit. Please work in phases and stop after each one so I can review.

Ground rules:
- Check out tag stage-1 in a separate worktree. Don't edit any game code. If you find a bug, report it to me and don't fix it.
- Put tests in a test folder. Use JUnit if the project already has a build setup. If not, tell me first and suggest the lightest option.
- Explain your reasoning in plain language, because I'll need to present this myself.

Phase 1: Understand the code
Read the stage-1 source and give me a short map: which classes hold the turn logic, satisfaction, orders, payment and the win/lose check, and how the CLI passes in the manager's choices. Tell me if any game rules live in the UI instead of the domain/controller classes. Also confirm there are no VIP, Critic, Host, ComboMeal or Happy Hour features at this tag. Then stop.

Phase 2: Test plan
Propose a list of tests mapped to these rules from the brief. Mark anything you can't test easily because of how the code is structured.
1. Base setting: 18 turns, 2 tables, 1 Waiter/Chef/Cashier, arrivals on turns 1, 4, 7, 10, 13, 16, menu prices and prep units (Salad $9/1, Burger $15/1, Pasta $18/2).
2. Waiter does at most one task per turn. An invalid choice explains why and doesn't use up the task.
3. Chef can work on an order the Waiter took this same turn.
4. A dish that becomes READY this turn can't be served until next turn.
5. Served customer eats for one turn, then READY_TO_PAY, then pays next turn at the earliest.
6. Cashier handles at most one payment per turn, oldest first. Payment adds revenue, marks PAID, frees the table.
7. Waiting or seated unserved customers lose 8 per turn, min 0. At 0 they leave, the order is cancelled, the table is freed. Served/eating/ready-to-pay customers lose nothing.
8. Revenue never includes unpaid or abandoned orders. Money is stored as integer cents.
9. Average satisfaction uses the value recorded at payment, and shows N/A if nobody has paid.
10. The game always runs all 18 turns. Victory needs at least 4 paid, at least $45 revenue and average satisfaction of at least 60. Anything less is Defeat.
11. The turn 16 customer can't pay within 18 turns.
12. The worked example in section 3.3: one Pasta customer should end turns 1 to 4 at 92, 84, 76, 76, then pay $18.00 on turn 6 with 76 recorded.
Then stop.

Phase 3: Reproducible runs
After I approve the plan, write the tests. Also create two scripted input files I can pipe into the game: one full victory run and one full defeat run. Use the simplest possible manager choices so a TA can follow them. Show me the final summary each run produces.

Phase 4: Report
Give me a table of each test, what it checks, and pass/fail. List any bugs with the file, the line and the steps to reproduce. Don't change the game code.
<!-- original-text-end:chat-01-m001 -->

<!-- transcript-message-end:chat-01-m001 -->

<!-- transcript-message-start:chat-01-m002 -->
## Message 002 - Assistant

<!-- original-text-start:chat-01-m002 -->
I'll start Phase 1: set up a separate worktree at the `stage-1` tag, then read the source.
<!-- original-text-end:chat-01-m002 -->

<!-- transcript-message-end:chat-01-m002 -->

<!-- transcript-message-start:chat-01-m003 -->
## Message 003 - Assistant

<!-- original-text-start:chat-01-m003 -->
Worktree is at `../SC2002_stage1_test` (commit `ac445a8`). No `pom.xml`/`build.gradle` — just shell scripts. Now reading the source.
<!-- original-text-end:chat-01-m003 -->

<!-- transcript-message-end:chat-01-m003 -->

<!-- transcript-message-start:chat-01-m004 -->
## Message 004 - Assistant

<!-- original-text-start:chat-01-m004 -->
Now checking for the excluded features and looking at what test infrastructure already exists.
<!-- original-text-end:chat-01-m004 -->

<!-- transcript-message-end:chat-01-m004 -->

<!-- transcript-message-start:chat-01-m005 -->
## Message 005 - Assistant

<!-- original-text-start:chat-01-m005 -->
## Phase 1: Code map for the `stage-1` tag

**Setup.** I checked out `stage-1` (commit `ac445a8`) in a separate worktree at `../SC2002_stage1_test`, detached from any branch. Your `Rishika` branch is untouched, and I haven't edited any game code.

### Where each rule lives

| Concern | Class / method | What it does |
|---|---|---|
| **Constants** | [GameConfig.java](../SC2002_stage1_test/src/config/GameConfig.java) | 18 turns, 2 tables, arrival turns {1,4,7,10,13,16}, win thresholds (4 paid, 4500 cents, avg 60) |
| **Turn logic** | [GameController.java](../SC2002_stage1_test/src/control/GameController.java) | Runs each turn as a fixed sequence of phases (`TurnPhase`): `startTurn()` (customer arrives) → `submitWaiterAction()` → `submitChefAction()` → `finishTurn()`, which runs eating, then payment, then the satisfaction penalty, then the win/lose check. It won't move to the next phase until the current action is accepted. |
| **Shared state** | [Restaurant.java](../SC2002_stage1_test/src/domain/Restaurant.java) | Holds customers, orders and tables. Contains the rules for seating, ordering, preparing, serving (`serveOrder` L120), eating (`advanceEating` L140), payment (`findOldestPayableCustomer` L148, `settlePayment` L160), the penalty and customers leaving (`applyWaitingPenalty` L180), and the totals (`getSummary` L203). It also builds the list of *legal* choices shown to the player (`getWaiterOptions`/`getChefOptions`). |
| **Satisfaction** | [Customer.java](../SC2002_stage1_test/src/entities/Customer.java), [RegularCustomer.java](../SC2002_stage1_test/src/entities/RegularCustomer.java), [CustomerState.java](../SC2002_stage1_test/src/entities/CustomerState.java) | Customers lose 8 per turn, never below 0. Only WAITING, SEATED and ORDERED customers lose points (`isWaitingForFood`). The customer's state changes are also here. |
| **Orders** | [Order.java](../SC2002_stage1_test/src/orders/Order.java) (behind the `OrderPort` interface) | Tracks prep units and the status PLACED → PREPARING → READY → SERVED → PAID / CANCELLED. Records the turn it became ready, the bill in cents, and satisfaction at payment. |
| **Menu** | [Menu.java](../SC2002_stage1_test/src/fooditems/Menu.java) | Salad 900¢ / 1 unit, Burger 1500¢ / 1, Pasta 1800¢ / 2 |
| **Staff limits** | [Waiter.java](../SC2002_stage1_test/src/staff/Waiter.java), [Chef.java](../SC2002_stage1_test/src/staff/Chef.java), [Cashier.java](../SC2002_stage1_test/src/staff/Cashier.java), grouped in `StaffTeam` | Each one remembers the last turn it acted (`lastActionTurn`). That turn is only recorded when an action is accepted, so a rejected choice doesn't use up the task. |
| **Win/lose** | [GameResult.java](../SC2002_stage1_test/src/control/GameResult.java) | Returns `IN_PROGRESS` before turn 18. At turn 18 it checks all three thresholds. For the average it compares `sum ≥ 60 × paid` using whole numbers, so there's no rounding error. |

### How the CLI passes in the manager's choices
[RestaurantCLI.java](../SC2002_stage1_test/src/cli/RestaurantCLI.java) asks the controller for a read-only snapshot (`GameView`), prints the legal options as a numbered list (0 is always "Wait"), and reads one whole number per line. It then sends the matching `WaiterAction`/`ChefAction` object to the controller. Each turn takes **exactly two input lines**: one for the Waiter, one for the Chef. The Cashier acts automatically. The CLI fetches a fresh snapshot before the Chef's choice (L39), so an order taken this turn already appears as an option.

### Do any game rules live in the UI?
**No real game rules are in the UI.** The CLI only:
- checks that the typed number is in range (input checking, not a game rule)
- formats money and shows "N/A" for the average. The decision that there *is* no average comes from the domain (`GameSummary.averagePaidSatisfaction()` returns empty).
- works out the "Cashier collected $X" line by looking for the order paid this turn (display only)

Three things matter for testing:
1. **Rejections never come from the domain in normal play.** The CLI only lists options the domain already considers legal, so a wrong choice can only be an out-of-range number, which the CLI itself rejects with "choose a number from 0 to N". The domain's explanations (for example "A dish completed this turn can be served next turn") only show up if you call the controller directly. So I'll test rule 2 at the controller level.
2. **If input runs out, the game exits with code 0** (`System.exit(0)` at L220) and prints "Input ended". A scripted run that's too short would stop silently without showing a summary. My scripts in Phase 3 will check for this.
3. **Customers' food is fixed by their ID.** `(id-1) % 3` gives #1 Salad, #2 Burger, #3 Pasta, #4 Salad, #5 Burger, #6 Pasta. The manager doesn't pick the dish. This affects rule 12: in the real game, customer #1 orders **Salad**, not Pasta. Reproducing the "one Pasta customer" example needs either a test customer that always picks Pasta, or following customer #3 (who arrives on turn 7) instead. I'll cover this in the plan.

### Excluded features
I searched `src/` for vip, critic, host, combo and happy (case-insensitive). The only matches are:
- the word "Unhappy" in the departure count
- one comment in [Customer.java:24](../SC2002_stage1_test/src/entities/Customer.java#L24) mentioning "(Regular,VIP,Host)" as future subclasses

There are **no** VIP, Critic, Host, ComboMeal or Happy Hour classes or logic. `RegularCustomer` is the only kind of customer, and the bill is always the full menu price ([Restaurant.java:97-98](../SC2002_stage1_test/src/domain/Restaurant.java#L97-L98)).

### Build setup: I need your decision before Phase 3
**There's no build tool:** no Maven, no Gradle, no JUnit jar. The project builds with plain `javac` via `scripts/build.sh`. The existing tests (`test/integration/*.java`) are hand-written programs with a `main` method, and `test/CustomerSeatingTest.java` is an empty class. Java 26 is installed.

The lightest options:
- **(A) Recommended: the JUnit 5 "standalone" jar.** Download one file, `junit-platform-console-standalone-1.x.jar`, into `test/lib/` and run it with `javac` + `java -jar`. No Maven or Gradle, and the game build stays as it is. It gives you proper JUnit output to show.
- **(B) No download.** Write the tests as plain Java programs like the existing ones. This needs no new files, but the output is less standard.

I'll stop here. When you're ready, tell me A or B, and I'll write the Phase 2 test plan.
<!-- original-text-end:chat-01-m005 -->

<!-- transcript-message-end:chat-01-m005 -->

<!-- transcript-message-start:chat-01-m006 -->
## Message 006 - User

<!-- original-text-start:chat-01-m006 -->
Write the tests as plain Java programs like the existing ones.
<!-- original-text-end:chat-01-m006 -->

<!-- transcript-message-end:chat-01-m006 -->

<!-- transcript-message-start:chat-01-m007 -->
## Message 007 - Assistant

<!-- original-text-start:chat-01-m007 -->
## Phase 2: Test plan

**Format (option B).** I'll write one plain Java program, `test/qa/Stage1RulesTest.java`, with a small `check(name, condition)` helper. It prints `PASS`/`FAIL` for each test and exits with a non-zero code if anything fails. A short script, `test/qa/run-qa.sh`, will compile `src/` and `test/qa/` with `javac` and run it. I'll only add files inside `test/qa/` and won't touch the existing tests or any game code.

**How I'll call the game in tests.** I'll use three levels and pick the highest one that can reach each rule:
- **Controller:** drives the real `GameController` with the real `StaffTeam` and `Order`, exactly as the CLI does. This is the most realistic level.
- **Restaurant:** calls `Restaurant` and the staff classes directly. I'll only use this when normal play can't reach a situation.
- **CLI:** runs `Main` as a separate process with piped input, then checks the text it prints. It has to be a separate process because the CLI calls `System.exit` when input runs out, which would stop the test program too.

### Tests mapped to the brief

| ID | Rule | What it checks | Level | Testability |
|---|---|---|---|---|
| **R1a** | 1 | `MAX_TURNS=18`, the game has exactly 2 tables, and the arrival turns are exactly {1,4,7,10,13,16} | Controller | ✅ |
| **R1b** | 1 | Play 18 turns choosing "Wait" every time. A new customer should appear on turns 1, 4, 7, 10, 13 and 16 only (6 in total). | Controller | ✅ |
| **R1c** | 1 | Menu: Salad 900¢/1, Burger 1500¢/1, Pasta 1800¢/2, in that order | Restaurant | ✅ |
| **R1d** | 1 | One each of Waiter, Chef and Cashier | Staff classes | ⚠️ **Partly testable.** `StaffTeam`'s staff fields are private, so I can't count them. I'll check the behaviour instead: there's one Waiter task, one Chef task and one payment per turn (R2, R6). |
| **R2a** | 2 | After the Waiter's action is accepted, a second Waiter action in the same turn is rejected | Controller + `Waiter` directly | ✅ |
| **R2b** | 2 | Invalid choices are rejected with a message saying why, for example seating at an occupied table, serving an order that doesn't exist, or taking an order from a customer who isn't seated. Nothing changes, the game is still waiting for the Waiter, and a valid action afterwards is accepted. | Controller | ✅ |
| **R2c** | 2 | Same as R2b for the Chef (preparing an order that doesn't exist or is already READY) | Controller | ✅ |
| **R2d** | 2 | Typing an out-of-range number at the CLI prints "Invalid: choose a number…", re-asks, and the game carries on | CLI | ⚠️ **Note.** The CLI only lists legal options, so in real play the domain's "why" messages never appear. The player only ever sees the out-of-range message. R2b tests the domain messages directly. |
| **R3** | 3 | The Waiter takes an order, and then in the **same turn** the Chef's option list includes it and preparing it is accepted (1 unit done) | Controller | ✅ |
| **R4a** | 4 | A Salad that becomes READY on turn T is not in the Waiter's options on turn T, but is on turn T+1 and can be served then | Controller | ✅ |
| **R4b** | 4 | Calling `serveOrder` in the same turn a dish became READY is rejected with "A dish completed this turn can be served next turn" | Restaurant | ⚠️ **Note.** In real play this can't even be attempted, because the Waiter always acts *before* the Chef in a turn. The game's turn order already prevents it. I'll test the guard directly anyway. |
| **R5** | 5 | Served on turn T: still SERVED at the end of T, READY_TO_PAY at the end of T+1, not paid on T+1, paid on T+2. Satisfaction doesn't change during this time. | Controller | ✅ |
| **R6a** | 6 | Payment adds the bill to revenue, marks the order and customer PAID, records satisfaction, and frees the table | Controller | ✅ |
| **R6b** | 6 | Two customers ready to pay at once: only one pays per turn, the one who became ready first. If they became ready on the same turn, the lower ID pays first. A second `processPayment` in the same turn is rejected. | Restaurant + `Cashier` | ⚠️ **Not reachable in normal play.** The Waiter serves at most one dish per turn, so at most one customer becomes ready to pay per turn, and the Cashier pays one per turn. A waiting line of payers never builds up. To test the rule, I have to set up the situation by calling `Restaurant` directly. |
| **R7a** | 7 | A customer waiting in the queue loses 8 per turn. Arriving on turn 1 means 4 at the end of turn 12, then 0 at the end of turn 13, and they leave (LEFT). Satisfaction never goes below 0. | Controller | ✅ |
| **R7b** | 7 | A seated customer with an order that's never served goes down to 0 and leaves. Their order becomes CANCELLED and their table is freed. | Controller | ✅ |
| **R7c** | 7 | A customer who is SERVED, eating or READY_TO_PAY loses nothing | Controller | ✅ |
| **R8a** | 8 | Revenue is $0 if a dish was served but not yet paid, and doesn't include a cancelled order. At the end of a run it equals exactly the sum of the paid bills. | Controller | ✅ |
| **R8b** | 8 | Money is stored as whole cents (`int`/`long`), never as decimals. The amounts are exact: 900, 1500, 1800. | Code check + Restaurant | ⚠️ **Partly testable.** The storage type is guaranteed when the code compiles. A test can only confirm the exact values. |
| **R9a** | 9 | With 0 paid, the average is empty, and the CLI prints `Avg satisfaction N/A` | Controller + CLI | ✅ |
| **R9b** | 9 | The average is calculated from the satisfaction saved on the order at payment, not the customer's current value | Restaurant + fake order | ⚠️ **Hard to tell apart.** In Stage 1, satisfaction can't change once a customer is served, so the saved value and the current value are always equal. To show the average really uses the saved value, I'd plug in a fake order (the `Restaurant` constructor accepts any order factory) that reports a different saved value. Otherwise I can only check that they match. |
| **R10a** | 10 | An all-"Wait" game reaches turn 18 and then stops. The result is `IN_PROGRESS` for turns 1–17 **even after the win targets are already met**, and trying to start turn 19 is refused. | Controller | ✅ |
| **R10b** | 10 | Edge cases for the win check (`GameResult.evaluate`): exactly 4 paid, $45.00 and an average of 60 is VICTORY. 3 paid, $44.99, or an average of 59.75 is DEFEAT. Turn 17 is IN_PROGRESS. | Pure function | ✅ |
| **R11** | 11 | Best possible play for customer #6 (arrives turn 16, orders Pasta): seat on 16, take the order and prepare on 17, prepare on 18. The dish only becomes ready on turn 18 and is never paid. Even a 1-unit dish would only be paid on turn 20 at the earliest. | Controller + reasoning | ✅ |
| **R12a** | 12 | The worked example on the real game. Customer #1 orders Salad, not Pasta, so I'll follow **customer #3** (Pasta, arrives turn 7) on turns 7–12. Expected satisfaction at the end of those turns: 92, 84, 76, 76, then $18.00 paid on turn 12 with 76 recorded. | Controller | ⚠️ Same timeline as the brief, shifted 6 turns later (see note 1). |
| **R12b** | 12 | The worked example on **literal turns 1–6**, using a test-only customer who always picks Pasta. Expected: 92, 84, 76, 76, then paid $18.00 on turn 6 with 76. | Restaurant + test customer | ⚠️ Needs a test customer, because the controller always creates the real customer type. |
| **E1** | extra | Customers' food is assigned by their ID (#1 Salad, #2 Burger, #3 Pasta, …) | Controller | ✅ |

### Things to flag before Phase 3
1. **The worked example doesn't match the real game as written.** In the actual game, the first customer orders Salad. The brief's "one Pasta customer on turns 1–6" can only be reproduced with a test customer (R12b). It's worth checking whether the brief says customers choose their own food or the manager picks it. If the manager is meant to pick, that's a design question for the team, not a test failure.
2. **Two rules are already guaranteed by the game's turn order:** R4 (the Waiter acts before the Chef) and R6 (a waiting line of payers never builds up). The tests prove the checks exist, but in your presentation you can explain that these cases can't happen in normal play anyway.
3. **The team already has scripted runs** in `test/runs/` and `test/replays/`. Mine will be separate and written from scratch, kept in `test/qa/runs/` with a short note explaining each line.

I'll stop here. Approve the plan (or add or cut tests), and in Phase 3 I'll write the tests and the two scripted victory and defeat runs.
<!-- original-text-end:chat-01-m007 -->

<!-- transcript-message-end:chat-01-m007 -->

<!-- transcript-message-start:chat-01-m008 -->
## Message 008 - User

<!-- original-text-start:chat-01-m008 -->
Plan approved. A few changes:

1. On your first flag: the brief says customers choose their own food using a documented deterministic or seeded rule, and the manager never chooses. So our ID-based rule is fine and isn't a design issue. Keep R12a (customer #3, turns 7 to 12). Drop R12b unless it takes very little code, since R12a already covers the timeline on the real game.

2. R2d: confirm the out-of-range message tells the player why it failed, not just "invalid", and that the staff task isn't used up. Quote the exact message in your report.

3. R9b: keep the fake order test, but keep it small. In the report, explain in one or two sentences why it's needed.

4. For the victory and defeat runs: run each one against the stage-1 tag and paste the final summary (paid count, revenue, average satisfaction, result). Each step in the input file should have a short comment so a TA can follow it.

5. Look at the team's existing runs in test/runs/ and test/replays/. If any of them give a different result from yours, or no longer work at stage-1, flag it. Don't edit them.

6. Before you write anything, confirm that test/qa/ compiles against stage-1 only.

Go ahead with Phase 3. Stop after the runs and test results so I can check them before Phase 4.
<!-- original-text-end:chat-01-m008 -->

<!-- transcript-message-end:chat-01-m008 -->

<!-- transcript-message-start:chat-01-m009 -->
## Message 009 - Assistant

<!-- original-text-start:chat-01-m009 -->
Starting Phase 3. First I'll make sure stage-1 builds cleanly by itself, and look at the team's existing runs.
<!-- original-text-end:chat-01-m009 -->

<!-- transcript-message-end:chat-01-m009 -->

<!-- transcript-message-start:chat-01-m010 -->
## Message 010 - Assistant

<!-- original-text-start:chat-01-m010 -->
Stage-1 builds cleanly with the project's own strict settings (`-Xlint:all -Werror`). Now I'll run the team's four existing input files against stage-1 and save the full logs to the scratchpad.
<!-- original-text-end:chat-01-m010 -->

<!-- transcript-message-end:chat-01-m010 -->

<!-- transcript-message-start:chat-01-m011 -->
## Message 011 - Assistant

<!-- original-text-start:chat-01-m011 -->
All four existing runs still work at stage-1 and match the results in their READMEs. Next, I'll confirm the worktree is exactly at the tag, then write the files.
<!-- original-text-end:chat-01-m011 -->

<!-- transcript-message-end:chat-01-m011 -->

<!-- transcript-message-start:chat-01-m012 -->
## Message 012 - Assistant

<!-- original-text-start:chat-01-m012 -->
The worktree HEAD is exactly `stage-1`, and the only extra folder is `build/`, which git ignores. Now I'll write the test program.
<!-- original-text-end:chat-01-m012 -->

<!-- transcript-message-end:chat-01-m012 -->

<!-- transcript-message-start:chat-01-m013 -->
## Message 013 - Assistant

<!-- original-text-start:chat-01-m013 -->
Now the two annotated input files, the runner for them, and the test runner script.
<!-- original-text-end:chat-01-m013 -->

<!-- transcript-message-end:chat-01-m013 -->

<!-- transcript-message-start:chat-01-m014 -->
## Message 014 - Assistant

<!-- original-text-start:chat-01-m014 -->
All 29 tests pass on the first run. Before trusting that, I want to make sure the tests can actually fail. In a throwaway copy of the game in the scratchpad, I'll plant two small bugs (penalty 8→7, and payment allowed one turn early), then run the same tests against it. The worktree isn't touched.
<!-- original-text-end:chat-01-m014 -->

<!-- transcript-message-end:chat-01-m014 -->

<!-- transcript-message-start:chat-01-m015 -->
## Message 015 - Assistant

<!-- original-text-start:chat-01-m015 -->
The planted bugs caused 9 failures, so the tests do catch broken rules. Now I'll play both scripted files through the real game, the same way a TA would.
<!-- original-text-end:chat-01-m015 -->

<!-- transcript-message-end:chat-01-m015 -->

<!-- transcript-message-start:chat-01-m016 -->
## Message 016 - Assistant

<!-- original-text-start:chat-01-m016 -->
Last check: I'll compare my two files' numbers with the team's existing files.
<!-- original-text-end:chat-01-m016 -->

<!-- transcript-message-end:chat-01-m016 -->

<!-- transcript-message-start:chat-01-m017 -->
## Message 017 - Assistant

<!-- original-text-start:chat-01-m017 -->
## Phase 3: Tests and scripted runs

All 29 tests pass, both scripted runs end with the expected result, and the game code is unchanged. `git status` in the worktree shows only the new, uncommitted `test/qa/` folder.

### Checks before writing (your point 6)
- The worktree HEAD is exactly the `stage-1` tag (`git describe --exact-match` prints `stage-1`).
- `src/` builds cleanly with the project's own strict settings (`-Xlint:all -Werror`).
- [run-qa.sh](../SC2002_stage1_test/test/qa/run-qa.sh) compiles only `src/` plus `test/qa/`, with the same strict settings. The team's other tests and other branches aren't part of it.

### What I added (all inside `test/qa/`)
| File | Purpose |
|---|---|
| [Stage1RulesTest.java](../SC2002_stage1_test/test/qa/Stage1RulesTest.java) | The tests, written as a plain Java program. Each test prints one PASS or FAIL line. |
| [run-qa.sh](../SC2002_stage1_test/test/qa/run-qa.sh) | `bash test/qa/run-qa.sh` compiles and runs everything. Logs go to `build/qa/`. |
| [runs/victory.txt](../SC2002_stage1_test/test/qa/runs/victory.txt), [runs/defeat.txt](../SC2002_stage1_test/test/qa/runs/defeat.txt) | The scripted runs, with a comment on every line. |
| [play.sh](../SC2002_stage1_test/test/qa/play.sh) | `bash test/qa/play.sh test/qa/runs/victory.txt` |

**Why `play.sh` is needed:** the game only accepts bare numbers. If you pipe the annotated file in directly, every line is rejected with "please enter a whole number" and the choices get out of step with the turns. `play.sh` removes the `#` comments first.

### Test results: 29 passed, 0 failed
R1a–d, R2a–d, R3, R4a–b, R5, R6a–b, R7a–c, R8a–b, R9a–b, R10a–b, R11, R12a, E1, E2, RUN-V and RUN-D all pass.

**Can the tests fail?** In a throwaway copy of the game in the scratchpad, I planted two bugs: satisfaction loss changed from 8 to 7, and payment allowed one turn early. 9 tests failed, including R5, R7a, R12a and RUN-V. So a passing result means something.

### Your requested changes
- **R12b is dropped.** R12a follows customer #3 on turns 7–12: satisfaction 92, 84, 76, 76, then $18.00 paid on turn 12 with 76 recorded. ✅
- **R2d:** the exact messages are:
  - out-of-range number: `Invalid: choose a number from 0 to 2.`
  - not a number: `Invalid: please enter a whole number.`

  After each one the game asks again, and the next valid choice is accepted in the same turn (`OK: Seated customer 1 at table 1`), so the task isn't used up. One wording point for your report: the out-of-range message tells the player what range is allowed, but it doesn't say their number wasn't on the list. It's clear enough, but not a full explanation.
- **R9b** is a small fake order: an `Order` that reports its saved satisfaction minus 34. The average comes out at 50.0, while the customer's own satisfaction is 84. This proves the average is read from the value saved at payment.

### Scripted runs against `stage-1`
Both runs played all 18 turns with exit code 0, no `Invalid` lines and no "Input ended".

| Run | Result | Paid | Revenue | Avg satisfaction | Departures |
|---|---|---|---|---|---|
| **victory.txt** | **VICTORY** | 5 (need 4) | $66.00 (need $45.00) | 79.20 (need 60) | 0 |
| **defeat.txt** | **DEFEAT** | 0 (need 4) | $0.00 (need $45.00) | N/A (need 60) | 2 |

The victory run collects $9 on turn 5, $15 on turn 8, $18 on turn 12, $9 on turn 15 and $15 on turn 18. The defeat run is "Wait" every turn: customer #1 leaves on turn 13 and #2 on turn 16.

### The team's existing runs (your point 5)
All four still work at stage-1 and give the results their READMEs state.

| File | Result at stage-1 | Same as its README? | Compared with mine |
|---|---|---|---|
| `test/runs/stage1-victory.txt` | VICTORY, 5 paid, $66.00, 79.20 | ✅ | Same result. The only difference is turns 17–18: they seat customer #6 and start cooking, I choose Wait. Customer #6 can't finish in time either way, so the result is the same. |
| `test/replays/stage1-victory.txt` | same | ✅ | Identical to the one above. |
| `test/replays/stage1-defeat.txt` | DEFEAT, 0 paid, $0.00, N/A, 2 departures | ✅ | Exactly the same numbers as my defeat file. |
| `test/runs/stage1-defeat.txt` | DEFEAT, 1 paid, $9.00, 84.00, 1 departure | ✅ | A different defeat: it serves one customer and then waits. This is intentional and documented. |

**No mismatches to flag.** One small note: the team has two victory files with identical contents in two folders. That's harmless but redundant.

### Bugs found so far
**None.** Every rule from the brief behaved as specified. Phase 4 will repeat the notes about rules that normal play can't reach (R4, R6b) and the parts that are only partly testable (R1d, R8b), but those are limits of what can be tested, not bugs.

I'll stop here. When you've checked these, I'll do Phase 4, the final report table.
<!-- original-text-end:chat-01-m017 -->

<!-- transcript-message-end:chat-01-m017 -->
