# Stage 1 Release Verification 8 November 2026

Executor: OpenAI Codex agent, requested by Lichen Yin. The user explicitly
instructed the agent to complete the tests when no E record was found. This is
actual agent execution, not an E signature or independent human review.

## Immutable version identity

- Tested implementation candidate commit: `df7cf0a113e729c8c134bb661cf474e1b59efbfb`.
- Source unchanged from main `f24c18679dca2d5f20c124427fd685f787ca5e33`
  and personal baseline `8abd4804de442486984ac080aaf67dbb27640b87`.
- Release: annotated Git tag `stage-1`; resolve its exact commit using
  `git rev-parse stage-1^{commit}`. The release commit adds this completed record
  and logs to the tested candidate; source, tests, scripts and class diagram are
  unchanged. Tag/archive checks are recorded by the release annotation and logs.
- Existing main was incorporated into `lichen/stage_1`; no main merge was needed
  to publish this snapshot. No Stage 2/3 classes or behavior are included.

## Environment and commands

macOS, Temurin OpenJDK/java/javac 21.0.12, Bash. Compile flags:
`--release 17 -Xlint:all -Werror`. Java 17 compatibility is compiled; execution
on an actual JDK 17 runtime and Windows PowerShell was not performed.

```bash
bash scripts/build.sh
bash scripts/test-stage1.sh
bash scripts/run.sh < test/replays/stage1-victory.txt
bash scripts/run.sh < test/replays/stage1-defeat.txt
bash scripts/run.sh < test/runs/stage1-defeat.txt
```

| Check | Actual result |
| --- | --- |
| Strict production build | PASS |
| Real Order core (recording staff) | 14 scenarios, 344 assertions PASS |
| Real Order/staff/StaffTeam/Main CLI smoke | 9 scenarios, 94 assertions PASS |
| Victory | 18 turns; 5 served/paid; $66.00; average 79.20; 0 departures |
| All-WAIT defeat | 18 turns; 0 served/paid; $0.00; average N/A; 2 departures |
| One-customer defeat | 18 turns; 1 served/paid; $9.00; average 84.00; 1 departure |
| Waiter invalid text/range retry | Same final victory after three rejected inputs |
| Chef invalid text/empty/overflow/range retry | Same final victory after five rejected inputs at the turn-2 Chef prompt |
| UTF-8 BOM before CLI input | Same victory, no spurious invalid-input prompt |
| Real CLI PLACED order abandonment | C1 LEFT at 0, unprepared Order CANCELLED, table released, no revenue |
| Real CLI READY order abandonment | C1 LEFT at 0, completed Order CANCELLED, table released, no revenue |
| Clean source export | Final candidate build/checks and complete replays pass without untracked files |
| Network-denied check | PASS: all core/smoke tests completed inside macOS sandbox-exec with deny network* |
| Class diagram | One-page vector PDF rendered and visually reviewed; type/encapsulation notes corrected |
| Transcript integrity | 161 original messages and 8 PNG attachments checked against the three source logs |
| Documentation links | Local links resolve; tag/branch links are verified at publication |

The exact committed candidate was exported to a clean path containing spaces, then
built/tested from an external working directory with network access denied:

```bash
/usr/bin/sandbox-exec -p '(version 1) (allow default) (deny network*)' python run_clean_verification.py
```

The wrapper invokes the repository scripts and three actual Main replays; the
[execution log](stage-1/clean-verification.log) records their complete output.
[Timeline checks](stage-1/timeline-verification.log) also inspected the Pasta
transition turns, early target and actual payment ids in the real CLI trace.

Full test output and selected process logs are committed in [stage-1/](stage-1/).
[Replay notes](../Stage_1_Test_Notes.md) define exact input, strategy and expected
results. Errors/timeouts/nonzero process exits fail the test command; successful
CLI replays require exactly 18 turn headers and one final result.

## Requirement matrix and observed evidence

| IDs | Requirement | Observed evidence |
| --- | --- | --- |
| M01-M04 | Base setup, arrival schedule, menu, deterministic customer choice | Source constants/actual objects, core arrival tests and real CLI fixtures |
| M05-M06 | Offline operation, boundary-only UI and complete turn display | Dependency/source review, clean Java build, CLI traces; network check above |
| M07-M09 | Staff allowances, rejected actions preserve choice, Waiter before Chef | Core phase tests, real staff allowances and actual Chef retry CLI |
| M10-M14 | READY/serve/eat/payment delays, automatic FIFO charging and coherent updates | Real single-customer flow, Pasta timeline, payment priority/tie fixtures and CLI trace |
| M15-M19 | -8 waiting loss, no served loss, zero/abandonment, cancellation and table occupancy | Three core cancellation states plus real PLACED/READY abandonment processes |
| M20-M23 | Robust input, integer cents, paid-only revenue and payment-snapshot average | Invalid CLI cases, zero-payment N/A and real replay totals |
| M24-M29 | Inclusive win thresholds, no early finish/turn 19, C6 timing, immutable queries and reproducible endings | Core boundary/snapshot tests and complete child Main processes |
| M30 | Runnable release identity | Annotated stage-1; remote peeled SHA checked and archive validation repeated |
| M31 | Matching Stage 1 diagram | Current classes/contracts/enums/views checked; PDF render inspected |
| M32 | Personal AI history | Lichen's available original history is linked; other members' evidence remains separate |

## Limits and remaining group deliverables

The core uses recording staff to observe order of calls; it is complemented by
actual staff/StaffTeam/Main processes. Some payment tie and PREPARING-cancellation
fixtures deliberately create domain states outside the natural arrival schedule;
they do not introduce a cheat mode or alter the production base setting.

The earlier human walk-through reached C1 payment at turn 6 only. The remaining
checks here were executed by the agent; no unperformed human interactions are
claimed. No completed E review was found at preparation time; optional independent
review can be recorded later without falsifying the executor of this release.

Group id/member contributions still need confirmation for final submission.
Other members' full AI histories, individual reflections and signed declarations,
final report/video, and Stage 2/3 evidence are final-submission work rather than
preconditions for this runnable Stage 1 snapshot. Original design proposals and
older dated verification files are clearly retained as history.

## Production source and test identity

SHA-256 values below identify the compiled implementation and test inputs.

| File | SHA-256 |
| --- | --- |
| `src/Main.java` | `33e480ff0499548dbd28921b6ebfb8845eed7fb358123b8f10151562632dfa5b` |
| `src/cli/RestaurantCLI.java` | `e49f5e46f398684c728825b33445b1c6fa33805f76e664e353e37f4b7ca6411b` |
| `src/config/GameConfig.java` | `62e512607354445e3c69eeb5ef2cdea5b5cf064a5865f8c55bb11f3c964ac5d7` |
| `src/contracts/ActionResult.java` | `277eb2c4eada8c8f5dd832896e03709c82b69d44914bb3c3f632f853a07b8f2a` |
| `src/contracts/ChefAction.java` | `a85e09b8380a5740d7197c8d16199c1c39eea865515b031ba7f106d447d73f0e` |
| `src/contracts/OrderPort.java` | `5010436bf2883b9fa6431005e653e4b4693fe6fa04ca6a2f7ad1aa4bcb541b10` |
| `src/contracts/StaffGateway.java` | `d7b84b25411f77ce58271cfbf929dc90fb6534710c75118b30fad1205e56efb4` |
| `src/contracts/WaiterAction.java` | `870f5cabadfbbb6cc016d7bfeac13d2ea71fc90788b9d3700516182638615bb8` |
| `src/control/GameController.java` | `cfa380e35a62b0b24565ea96fcd4d94718cf640074707f2a80dce267672d3cef` |
| `src/control/GameResult.java` | `079c93c9b1adc62bfb78754d4be45c463db913a4797f27b936fd1b00d5002c7f` |
| `src/control/TurnPhase.java` | `a8c490a27dfea8c473d3a317d19f43af1970cae75366bbef23c3baad8d06217b` |
| `src/domain/Restaurant.java` | `af64957fb9bfd0af1bc24e6d6960240d78adc437344b59c2b09d4f35e65e9db9` |
| `src/entities/Customer.java` | `0304c5ad6ab861ed3e574a4f324befa03762bcf6332264ad0d1e59ddfc993eb3` |
| `src/entities/CustomerState.java` | `58503a66d8be6ee3677984dbe3f262611fb57215e66135726f75a68dec81a19e` |
| `src/entities/RegularCustomer.java` | `43ba5f75293a0996e4bc9684cdaab2673640bee5d47abd67ec1dee0425805fa8` |
| `src/entities/SeatingArea.java` | `539d228a4b5e76271c9016d92dde745570844d109caf23cb6fe85de0017af694` |
| `src/entities/Table.java` | `24eb24dc141fb6994c3880a3f4d754c435f60d821091411ca81877315f9eb4e1` |
| `src/entities/WaitingQueue.java` | `8f4ae9fef01278dcecc63734f8c0d9ea0e2462a16b28beadbf27bb06ec312928` |
| `src/fooditems/Menu.java` | `b7adaf92fc650c52043141fda6d1039177226745eea8e2805658f2c658a4dcf1` |
| `src/fooditems/MenuItem.java` | `f48a22d99905303743405f99b8929797be65f3d81b13ad43ce3aa5776289161a` |
| `src/orders/Order.java` | `d33c52fe5da71cfb05cd3ed8964ae524f11ce5d4bcd997ce035cc5e8703ca6eb` |
| `src/staff/Cashier.java` | `b5f3cf5775633b3a79a4d6b63c95361fea1048f87cc572973b9ebace43ef541f` |
| `src/staff/Chef.java` | `80973246ad60fe84007e7d749bc19a6388b5559b31f9df6adb5df09ae2a29aaf` |
| `src/staff/StaffTeam.java` | `b43c2c23a3086167d9bcf96757755b07cdacd48a602867a6a1ea1a2f345557b2` |
| `src/staff/Waiter.java` | `e822919696555df403078b907ebc0f333f266fec831da13a6992d59bba2dbeee` |
| `src/view/GameSummary.java` | `7a1871dc6220fb95f149c547df416c29801dd1148bee8368f13f31d73776e0f8` |
| `src/view/GameView.java` | `1222c201b80ab175663a7217a6c8defc0783fc4b8e95180b672652da2c416518` |
| `scripts/build.sh` | `fee03b2badeeb3326437d49627226ee5190223481c6dedc1c7cf4d78c35e82c2` |
| `scripts/run.sh` | `4bb077e336325802c6e21bbb7a1b11cbcd58e8ee970ef920b422be0a4c854b7d` |
| `scripts/test-member-a.sh` | `d9ff38020cdce3d3aae9c37e6e36f9572a6e08994df8996cae4342337b18abff` |
| `scripts/test-stage1.sh` | `dc0fc3dd58f034f50ec7670e8398eb9ac68d2e9e39d302e68464f6d667370824` |
| `test/CustomerSeatingTest.java` | `0251e9357e69a13481c63b0aa9106458e3abd304a7641d151b2ccb98efcc0ceb` |
| `test/integration/MemberACoreTest.java` | `a9b91c4237335e0275ba55d64a3f2897329d67d1ad0c86fb3b0f9f3b519e59eb` |
| `test/integration/Stage1SmokeTest.java` | `98cc044c0c9df9d5b7743d684da22c9e8198832d4cc07df9434bb76be1398aba` |
| `test/replays/README.md` | `64fcdce2a5f185ff8b5dbf63a2262383b4b1b6c9164ccb7af1c68b8fab914335` |
| `test/replays/stage1-defeat.txt` | `271c746e8d7d486f51b741785090e7adc29dc4e2fe84aaadb2e1a8429e9fcda2` |
| `test/replays/stage1-victory.txt` | `b5f1effca836960440b9118e73f5e99361d519645c3a60a236913b79da50925d` |
| `test/runs/README.md` | `7297259a8df5843cef43bbc449ea49a931e79778bef5cc8eed22134050ede75a` |
| `test/runs/stage1-defeat.txt` | `995d7bfecc1a9348aa006adcbad9815b57adf7685468673e2c062800d13d09c7` |
| `test/runs/stage1-victory.txt` | `b5f1effca836960440b9118e73f5e99361d519645c3a60a236913b79da50925d` |
| `docs/UML/Stage1_UML_Diagram.pdf` | `40343bb150dfd58d32239cf272e02c18cbcca272bc46ed3234012aa49dc32a42` |
