# Stage 1 核对与完成流程

核对日期：2026 年 11 月 6 日。本文结合 assignment manual、project briefing 和实际代码，供 Member A 与 E 逐步复核。程序开发检查已通过；人工复核、E 的独立验收、远端整合与 stage-1 tag 尚未完成。每一步都列出操作、预期结果、证据和后续动作，不把自动测试当作任何成员的签字。

本次个人分支整理更新：当前分支为 `lichen/stage_1`，承接复核提交 `b64651d`，保留 A 的实现/交接以及 Rishika 更新的历史；`ai-history/stage-1/` 已保存三段真实相关会话，共 131 条消息和 8 张截图，覆盖范围与截止点见该目录 README。其他成员记录、E 验收和 stage-1 tag 仍需分别完成。下方远端状态表保留前一次核对时的事实，并更新当前使用入口。

人工走查已经通过截图确认到第 6 回合开始：C1/Order1 PAID、收到 $9、满意度快照 84、T1 释放；C2 Burger READY，未计入收入。用户明确未检查 Chef 错误输入，之后退出游戏；Pasta 和完整胜负的人工走查尚未完成，不能把之前的操作建议当成已执行证据。

## 0 当前版本与本次同步

唯一工作仓库：

```text
/Users/edward/Desktop/NTU/Y2S1/SC2002/Group Assignment/SC2002_Group_Assignment
```

| 对象 | 11 月 6 日核对结果 |
| --- | --- |
| 远端 main、本地 main | 均为 `705fd37cddbb21e8d11b4f112e0f8d2a3dd5123b`，没有新的 main 提交 |
| A 原交接版本 | `codex/stage-1-a-handoff`，`e595fa94b4a69a560f7d1b1007bcd3bdb0fc5514` |
| PR #4 | 仍 open，未合并；GitHub 上未见 review 或评论中的验收记录 |
| Rishika 新提交 | `542a6891a1544dd211a3b779d41a3fe6ee342bf1`，README、BOM 输入兼容、out/ 忽略和两组回放 |
| 当前个人分支 | `lichen/stage_1`，承接 `b64651d` 的整合内容和历史，并增加完整个人 AI 对话记录；远端身份以发布后的 SHA 核验为准 |
| Stage 2 工作 | `origin/Xavier-Stage2` 与 `origin/bernice/stage-2-pricing` 已 fetch 到本地，保留在各自分支，未混入 Stage 1 |
| stage-1 tag | 本地和远端均没有 |
| AI history | Lichen 的三段相关原始会话已整理到 ai-history/stage-1；其他成员的记录和 README 链接仍待各自补齐/核实 |

本次没有修改 main，也没有合并远端 PR 或创建/推送 tag。Rishika 的 README 原来写了 Stage 1 Done 并链接一个不存在的 tag；复核版本改为候选版本、验收和 tag 待完成，保留 A 的构建/测试脚本。

两组失败回放都有效，但结果不同：`test/replays` 中是全程 WAIT；`test/runs` 中是只照顾 C1。不要混用两者的预期结果。

## 1 先确认两份要求文件

原始文件：

- `SC2002_Restaurant_Rush_Group_Assignment(1).docx`，作业说明，尤其第 2、3、4.1、5、6 节。
- `SC2002_Group_Project_Briefing.pdf`，12 页的补充 briefing。

| 要求来源 | 对本次 Stage 1 的影响 |
| --- | --- |
| Manual 2、3、4.1 | 完整、离线、18 回合基础游戏；能独立构建；有胜负回放；不能提前加入 Stage 2/3 功能 |
| Manual 4、5；briefing 第 3 页 | 保存真实、可运行的 stage-1、stage-2、stage-3 Git tag |
| Briefing 第 5 页 | **三个阶段各一张类图，共三张**；还需要一张 Stage 2 或 Stage 3 的复杂用例顺序图。不能只准备最终类图 |
| Briefing 第 7、8 页 | 每人完整且按顺序的项目 AI 对话放在个人分支；摘要不足；README 链接所有阶段和每个人的 AI history |
| Briefing 第 9、10 页 | 一份组报告，每人独立署名写 reflection：学到什么、怎样应用、难点和解决过程，并举自己的工作实例 |
| Manual 6；briefing 第 11、12 页 | 约 15 分钟视频，每人讲解；README 时间戳；报告、声明、有效链接和最终 ZIP；2026-11-15 23:59 SGT 截止 |

E 独立验收是你们已经选定的团队分工流程，并非 manual 指定必须由字母 E 的成员签字。Manual 的硬性要求是可运行、符合规则的版本与相应证据。保留原交接文件的验收栏，是为了记录谁在什么版本上检查过什么。

## 2 锁定本次检查的目录和版本

在 Terminal 执行：

```bash
cd "/Users/edward/Desktop/NTU/Y2S1/SC2002/Group Assignment/SC2002_Group_Assignment"
pwd
git status --short --branch
git branch --show-current
git rev-parse HEAD
java -version
javac -version
```

预期：目录正确、当前分支为 `lichen/stage_1`，没有未提交的文件。把完整 SHA、系统和 JDK 版本写到复核记录。只记录分支名不够，因为分支以后会移动。

本次运行使用 Temurin JDK 21.0.12，通过 `--release 17` 构建。README 要求 JDK 17 或以上；本次没有声称在真实 JDK 17 运行时执行过。如果 E 用另一台机器复测，应记录那台机器实际的版本。

通过标准：身份清楚且工作区干净。若你改过文件，先查看差异并保留自己的改动，不用 reset 或 checkout 覆盖。

## 3 构建和自动检查

按顺序执行：

```bash
bash scripts/build.sh
bash scripts/test-stage1.sh
```

预期标记：

```text
BUILD_STAGE1=PASS
MEMBER_A_CORE=PASS scenarios=14 assertions=344 orders=production
STAGE1_SMOKE=PASS scenarios=5 assertions=62
STAGE1_DEVELOPMENT_CHECKS=PASS
```

标记后可能跟本地 classes/logs 路径。编译打开 Java 17 兼容、全部 lint 与 warnings-as-errors，失败应返回非零。

| 检查 | 实际覆盖 | 解释边界 |
| --- | --- | --- |
| MemberACoreTest --production-orders | 14 个场景、344 个断言，真实 Order；回合、时序、取消、支付顺序、快照、原子校验、胜负边界 | Staff 用 recording test double，不能单独证明真实 Staff/CLI 正常 |
| Stage1SmokeTest | 5 个场景、62 个断言，真实 Order、StaffTeam、Waiter/Chef/Cashier、Main/CLI | 包含时序、员工额度、胜利、失败和错误输入；不是 E 已签字的证明 |
| 本次额外回放 | Rishika 胜利、单顾客失败、UTF-8 BOM 前缀胜利，三项通过 | Linux/macOS 上注入 BOM 验证了输入修复；没有声称在 Windows PowerShell 上实机运行 |
| CustomerSeatingTest.java | 空类，无可执行测试内容 | 不计入已执行场景数 |

完整开发日志在 `build/stage1-checks/`，该目录被 Git 忽略。把要交给 E 的日志连同被测 SHA 另行留存；不要只发一张 PASS 截图而丢失版本信息。

通过标准：上面四个标记均出现，且没有编译错误或异常。失败时先记下完整命令、SHA、错误和退出码，再根据下面的责任表定位。

## 4 手动启动和回合显示

执行：

```bash
bash scripts/run.sh
```

先停在第一个 `Choice:`，逐项观察：

| 检查项 | 第 1 回合预期 |
| --- | --- |
| 标题与回合 | Restaurant Rush (Stage 1)，Turn 1 / 18 |
| 初始统计 | Paid 0、Revenue $0.00、Avg satisfaction N/A、Turns used 0/18 |
| 队列 | 顾客 #1 |
| 桌位 | Table 1、Table 2 均 free |
| 顾客 | #1 Regular，satisfaction 100，WAITING |
| 菜单 | Salad $9.00/1 unit；Burger $15.00/1；Pasta $18.00/2 |
| Waiter 选项 | 0 为 Wait，可让 C1 坐 T1 或 T2 |
| Chef 选项 | 选完 Waiter 后显示；没有订单时只有 Wait |
| 没有后续阶段功能 | 没有 VIP/Critic、Host、ComboMeal 或 Happy Hour 操作 |

注意：回合开始时 `Turns used` 统计的是**已经完成**的回合，所以第 1 回合行动前是 0/18。每个新回合都应该显示 revenue、paid average、队列、桌子、所有顾客满意度、订单状态和员工可做的任务。

通过标准：以上信息都能看到；不是只看到一个静态欢迎画面。

## 5 亲自走一次 C1 的完整服务链

当前代码的食物规则是 `(customerId - 1) % 3`，所以 C1 是 Salad。Manual 的 Pasta 示例是规则示例，不要求 C1 必须吃 Pasta；Pasta 两单位时序在自动测试和胜利回放中的 C3 验证。

在刚启动的游戏中，先完成下面六回合。数字是**显示的菜单索引**，不是顾客 id；以屏幕上动作文字为准。

| 回合 | Waiter 选择 | Chef 选择 | 本回合结束时观察 |
| --- | --- | --- | --- |
| 1 | 输入 1：C1 坐 T1 | 输入 0：Wait | C1 SEATED，满意度 92；revenue 0 |
| 2 | 输入 1：给 C1 点单 | 输入 1：准备 Order 1 | Salad 同回合准备完成；Order READY，C1 满意度 84；未收费 |
| 3 | 输入 1：Serve Order 1 | 输入 0 | C1 SERVED；不扣满意度；没有立即吃完或付款 |
| 4 | 输入 1：C2 坐 T2 | 输入 0 | C1 到 READY_TO_PAY；仍未付款、T1 仍占用 |
| 5 | 输入 1：给 C2 点单 | 输入 1：准备 Order 2 | 自动向 C1 收 $9.00；C1/Order1 PAID；记录满意度 84；释放 T1 |
| 6 | 输入 1：Serve Order 2 | 输入 0 | C2 SERVED，C1 不会重复计费 |

每个新回合开始屏幕会显示上一个回合留下的状态。Order 在 C1 吃完时仍为 SERVED；READY_TO_PAY 是**顾客状态**，不是必须新增的 Order 状态。

这六回合要理解的重点：Waiter 在 Chef 之前；新订单可以当天准备；新 READY 菜不能当天上；上菜后再过一回合吃完；新 READY_TO_PAY 顾客再等后续回合付款。桌子直到支付步骤才释放，不能在同回合较早的 Waiter 步骤提前使用。

通过标准：你能根据日志解释上述三个时间间隔，并指出控制判断所在的方法。对应 `Restaurant.serveOrder`、`Customer.finishedEating`、`Restaurant.findOldestPayableCustomer`。

## 6 完整胜利和两种失败

分别启动新游戏执行：

```bash
bash scripts/run.sh < test/replays/stage1-victory.txt
bash scripts/run.sh < test/replays/stage1-defeat.txt
bash scripts/run.sh < test/runs/stage1-defeat.txt
```

| 场景 | 结束回合 | Served | Paid | Revenue | Paid average | Unhappy departures |
| --- | --- | --- | --- | --- | --- | --- |
| 胜利 | 18 | 5 | 5 | $66.00 | 79.20 | 0 |
| A：一直 WAIT 的失败 | 18 | 0 | 0 | $0.00 | N/A | 2 |
| Rishika：只照顾 C1 的失败 | 18 | 1 | 1 | $9.00 | 84.00 | 1 |

胜利逐回合动作见 [A 整合说明](Member_A_Integration.md#victory-replay-using-the-current-menu-rule)。C3 的 Pasta 在第 8、9 回合各做一单位，第 10 回合上菜，第 11 回合吃完，第 12 回合付款。

特别检查：第 15 回合已经达到目标，程序仍继续到第 18 回合；没有第 19 回合。第 16 回合到达的 C6 最后仍未付款，这是要求设定的结果。只准备、上菜或吃完都不能计为 paid revenue。

一直 WAIT 时 C1 在第 13 回合、C2 在第 16 回合因满意度耗尽离开。不是六个顾客都会在 18 回合内离开。

通过标准：三个结果完全一致；若使用不同回放，先核对路径，再判断是否是程序错误。

## 7 错误输入和边界检查

另开一局，在第 1 个 Waiter 提示依次输入 `hello`、`999`、`-1`，再输入合法的 `1`。预期：每次说明错误且仍停留在该 Waiter 选择；之后可以正常坐下，Chef 还可行动。数字之外也可检查空行和超出 int 范围的数字，它们都应提示重新输入。

以下矩阵把 Manual 的 Stage 1 规则逐条映射到代码和现有测试。状态“开发检查通过”代表程序/测试证据；E 应在自己的记录中另写结果。

| ID | Manual 要求及预期 | 核对位置或场景 | 当前开发检查 |
| --- | --- | --- | --- |
| M01 | 18 回合、2 桌、三个员工各一人 | GameConfig；StaffTeam；core idleDefeat；真实 CLI | 通过 |
| M02 | 1/4/7/10/13/16 到店，共 6 人，初始 100 | GameController.startTurn；core idleDefeat | 通过 |
| M03 | 三菜单价格和工时准确 | Menu；MenuItem；CLI 菜单；serviceTiming/pastaTimeline | 通过 |
| M04 | 顾客选餐、规则有文档、下单显示菜品 | Customer.chooseItem；Restaurant.placeOrder；README | 通过 |
| M05 | 游戏规则不放 UI；离线运行 | Main/CLI 调用链；构建零外部依赖；源代码检查 | 通过；可由 E 断网复测 |
| M06 | 每回合开始完整状态显示 | RestaurantCLI.printState/describeSummary；CLI 日志 | 通过；人工阅读见第 4 步 |
| M07 | Waiter 座位/点单/上菜/WAIT，每回合最多一个 | Waiter.execute；controller phase；invalidRetries；staffAllowances | 通过 |
| M08 | Chef 每回合最多一单位，可处理当回合新订单 | Chef.execute；Restaurant.advancePreparation；真实 staff smoke | 通过 |
| M09 | Waiter 先于 Chef，新增订单在 Chef 视图中出现 | Controller phase；CLI fresh getView；serviceTiming | 通过 |
| M10 | 最后一单位后 READY；最早下一回合上菜 | Order.advanceOneUnit/markServed；Restaurant.serveOrder；pastaTimeline | 通过 |
| M11 | 上菜后有一整回合进食 | Customer.serve/finishedEating；真实 timing smoke | 通过 |
| M12 | 新 READY_TO_PAY 最早下一回合付费 | Restaurant.findOldestPayableCustomer；serviceTiming/pastaTimeline | 通过 |
| M13 | Cashier 自动、最久等待优先、每回合最多一单 | Cashier；Restaurant 过滤与排序；paymentPriority 两个场景 | 通过；同回合 tie 用顾客 id 是团队选择 |
| M14 | 支付时 Customer/Order PAID、计收入、释放桌 | Restaurant.settlePayment；Order.markPaid；真实 timing smoke | 通过 |
| M15 | 未上菜 Regular 每回合末 -8，最低 0 | Customer.applyWaitingPenalty；RegularCustomer；idle/abandonment | 通过 |
| M16 | SERVED/进食/待付款不再扣分 | CustomerState.isWaitingForFood；serviceTiming；真实 timing smoke | 通过 |
| M17 | 达到 0 离开，取消未完成订单、释放队列/桌子 | Restaurant.applyWaitingPenalty；三种 abandonment 场景 | 通过 |
| M18 | PLACED/PREPARING/READY 均可因离店取消，不计收入 | Order.cancel；abandonment(0/1/2) | 通过 |
| M19 | 桌子一直占用到付款或离店；占用桌不能再坐人 | SeatingArea/Table；seatingValidation；真实 timing smoke | 通过 |
| M20 | 错误选择说明原因，可重试且不消耗任务 | CLI readInt/pick；controller；真实 staff；invalid CLI replay | 通过 |
| M21 | 只有已支付收入，金额整数分，显示两位小数 | Restaurant.getSummary；Order.billCents；CLI money | 通过 |
| M22 | 平均分只用付款快照；无人付款 N/A | Order.markPaid；GameSummary；两类失败与 timing tests | 通过 |
| M23 | 末尾有 served/departures/revenue/average/turns | CLI end-of-turn 和 printResult；replay 日志 | 通过 |
| M24 | 最终 paid>=4 AND revenue>=4500 AND avg>=60 | GameResult.evaluate；resultBoundaries 各条件单独不达标 | 通过；等于阈值为胜利 |
| M25 | 不提前结束，第 18 回合才判结果 | GameResult.evaluate；victoryReplay；真实 CLI 18 回合 | 通过 |
| M26 | C6 不能在 18 回合内付款，最多 5 人支付 | 时序链与 victoryReplay 的最终 C6 状态 | 通过 |
| M27 | 无隐藏的查询副作用、重复结算/结束不能重复计数 | immutableSnapshots；invalidRetries；paymentPriority | 通过 |
| M28 | Stage 1 独立可构建，排除后续阶段功能 | src 范围检查；完整 javac；实际 Main；无后续功能类 | 通过；注释里的未来类名不构成功能 |
| M29 | 可复现胜利和失败测试笔记 | 两组 stdin 文件、README 与本文件 | 通过 |
| M30 | GitHub 上真实可运行的 stage-1 tag | 远端 tags 与本地 git tag | **待完成** |
| M31 | Stage 1 类图匹配对应代码 | 现有 UML 与实现对照 | **待修正**，见第 9 步 |
| M32 | 完整个人 AI history 及 README 有效链接 | ai-history/stage-1 与 root README | **Lichen 已整理三个可恢复会话；其他成员及可能的其他聊天仍待确认** |

“最久等待优先”和一次多位 READY_TO_PAY 等边界不容易只靠正常 CLI 准备；已有 domain fixtures 会直接构建这些状态。不能为了演示而给生产游戏加作弊入口，也不要把 fixture 中的人数/时序当成基础配置。

## 8 Member A 应能讲清楚的调用链

```text
Main: Restaurant(Order::new) + StaffTeam -> GameController -> RestaurantCLI
CLI.run
  -> GameController.startTurn -> Restaurant.beginTurn / registerArrival
  -> submitWaiterAction -> StaffGateway / StaffTeam -> Waiter -> Restaurant
  -> submitChefAction  -> StaffGateway / StaffTeam -> Chef   -> Restaurant -> Order
  -> finishTurn
       -> Restaurant.advanceEating -> Customer.finishedEating
       -> StaffTeam / Cashier -> findOldestPayableCustomer / settlePayment
       -> Restaurant.applyWaitingPenalty
       -> Restaurant.completeTurn -> GameSummary -> GameResult.evaluate
  -> getView -> GameView -> CLI displays
```

| 谁负责 | 核心边界 | 与你 A 的关系 |
| --- | --- | --- |
| A：GameController | 决定回合和行动阶段；拒绝错误阶段；安排自动步骤 | 完整流程入口；不能把 staff 额度和所有实体规则都塞进它 |
| A：Restaurant | 跨 Customer/Table/Order 的一致更新；支付和取消；统计 | B/C/D 都通过这里协作；先验证再修改 |
| A：GameView/GameSummary/GameResult/contracts | 只读快照、统计、结果和模块约定 | UI 不拿可变实体；真实实现通过接口连接 |
| B | 顾客状态/满意度/选餐、队列、桌位 | A 使用 B 的行为；不在 controller 手写顾客子类型分支 |
| C | Order 状态、准备进度、锁定金额、支付记录 | A 工厂创建真实 Order；不能把 FakeOrder 当生产实现 |
| D | 员工额度和 CLI | 真实 StaffTeam 已接上；CLI 收集输入、不计算胜负或修改实体 |
| E | 独立测试、证据与报告整合 | 从精确 SHA 复测，记录缺陷、修复 SHA 和复测结果 |

A 的自查问题：为什么无效动作不推进 phase？为什么 getView 不推进回合？为什么 prepared/served 不算收入？为什么刚空出的桌子不能倒流给较早的 Waiter？为什么用分和付款时的满意度快照？能对照代码回答后，再准备视频讲解。

## 9 修正 Stage 1 类图和设计说明

现有 `docs/UML/Stage_1_Class_Diagram.*` 和 `Stage_1_Design_Discussion_EN.*` 明确是早期 proposal，不可直接标为最终实现。

| 旧方案 | 当前代码应表示的内容 |
| --- | --- |
| GameController 直接连接三个 staff | GameController 持有 StaffGateway；StaffTeam implements StaffGateway 并创建/持有 Waiter、Chef、Cashier |
| Restaurant 直接拥有一个 List waitingQueue 和两张 Table | Restaurant 创建并持有 SeatingArea；SeatingArea 管理 WaitingQueue 与 tables |
| 只画 Customer | Customer 是 abstract，RegularCustomer extends Customer；补必要的继承关系 |
| CustomerStatus、WAITING_FOR_FOOD、EATING | 实际为 CustomerState，包含 ORDERED 和 SERVED；不要混用旧枚举 |
| chooseMenuItem/waitingPenalty/quoteBillCents | 实际 chooseItem(Menu)、getSatistfactionLossPerTurn() 等；Stage 1 Restaurant 取完整菜单价 |
| OrderStatus | 当前为 OrderPort.Status；Order implements OrderPort；Restaurant 使用 OrderPort.Factory 创建订单 |
| Order 直接持有 Customer | 实际保存 customerId，并持有 MenuItem；可以标识业务关联，但不能画成不存在的 Customer 引用字段 |
| Table.isAvailable、包可见 seat/release | 实际方法名、可见性以 Table.java 为准逐个校对 |
| C1/C4 Pasta，C3/C6 Salad | 实际按 Salad/Burger/Pasta 循环；更新相应演示表和设计解释 |

操作顺序：先确认代码版本；逐个抄对关键字段、方法和可见性；补主体 entity/control/boundary、关键接口/枚举/快照；画正确的关联、继承/实现、多重性与确实存在的 composition；用真实调用链复核；导出一致 SVG/PNG 和编辑源，再更新报告。

图可以省略琐碎 getter，但不能把已经使用的核心类/关系漏掉。标注真实的封装、Customer 的多态行为、SeatingArea/StaffTeam 等组合例子。不要把构造器注入的外部引用一概画成 composition。

Manual 5 要求最终类图包含关键属性/操作与可见性、相关枚举、继承和关联多重性。Briefing 又要求每阶段一张，因此要保留与 Stage 1 快照相符的图；不能后续把 Stage 3 图覆盖后只剩一张。

通过标准：任何图中展示的类名、字段、方法、可见性和关系都能在对应代码中找到依据；不同导出格式语义一致。此项在本次仍待做。

## 10 README、个人 AI history 和 reflection

本次已修正并合并 README 的构建命令、测试边界、两种失败回放、Stage 1 当前状态和待办。还要由团队确认实际 lab/group 编号、成员姓名/贡献表，并在真正发布后补有效 stage 链接。

每位成员在自己的分支准备完整 AI 记录，例如：

```text
ai-history/stage-1/chat-01.md
ai-history/stage-2/chat-01.md
ai-history/stage-3/chat-01.md
```

这是 briefing 给的目录例子，可以采用等价清晰结构。内容包括该项目相关的用户 prompts、AI replies 和后续交流，保持顺序。提交摘要、只贴生成的代码、或用本文件代替聊天记录均不满足第 7 页要求。先取得真实完整导出，再上传并验证 README 的个人分支链接；不要从压缩的上下文或记忆反向编造缺失原话。

Member A 的 reflection 可先收集事实素材，再用自己的话写：你实际负责哪些提交/方法；怎样决定 controller 与 domain 的边界；怎样处理三处时序延迟、错误重试、模块接口不一致；怎样用真实 Order/Staff/Main 取代仅有 fake 的验证。具体“学到了什么”“最难的是什么”应由你根据亲身经历写，不把建议冒充经历。

通过标准：成员个人分支中的全文记录可读；README 链接可访问；每人署名的 reflection 内容具体且真实。报告、视频和 signed declarations 的最终整合属于整个作业后续交付，不是要求现在就给 Stage 1 增加 Stage 2 功能。

## 11 E 独立复核与缺陷关闭

当前候选个人分支是 `lichen/stage_1`，包含之前 `b64651d` 的复核代码与本次 AI history/文档。PR #4 仍指向旧候选 `e595fa9`；E 应通过当前个人分支获取新版本，并记录精确 SHA。

发布个人分支后，可以从 `lichen/stage_1` 准备团队整合 PR，并协调原 PR #4，避免重复合并同一份 A 改动。个人分支发布不代表 main 已整合，也不代表 stage-1 tag 已创建。

E 应拿到最终候选版本的精确 SHA，执行第 2 至 7 步和独立场景，并填 [原交接表](Stage_1_E_Handoff.md) 的实际验收栏。交接文件的当前分支指令已更新为 `lichen/stage_1`，仍需记录 E 实际测试的 SHA。

记录模板：

```text
被测 commit SHA：
测试人：
日期时间和时区：
OS / java -version / javac -version：
构建、自动测试的命令和退出码：
手动场景与输入：
逐项实际结果、日志位置：
缺陷 ID / 预期 / 实际 / 负责人：
修复 commit SHA 与复测结果：
验收结论：
```

有缺陷先修复并在新 SHA 上复测相关场景，必要时跑完整回归。没有远端记录只能说“未见验收记录”，不能推断某位同学线下没有测试过。

通过标准：真实记录能回答“谁，在什么版本上，以什么输入，得到什么结果”；不能让自动化脚本填 E 的人名和 accepted。

## 12 保存并验证 stage-1

本次未执行这一发布步骤。功能验证、团队复核完成后，由团队合并经过检查的纯 Stage 1 候选版本，再在确定的 runnable commit 上创建 tag。Manual 不强制 tag 必须位于 main；用已验证的 main merge commit 是更容易解释的团队做法。

发布前重新检查：工作区干净；选中的 SHA 是刚验收的代码或仅有无行为变化的合并；src 中没有 Stage 2/3 功能；胜负回放在该版本仍可运行。若 merge 改了代码，复测 merge 后的版本。

下面是后续操作模板，先明确被测版本再执行，不照抄成对旧版本的发布：

```bash
git switch main
git pull --ff-only origin main
git status --short
git rev-parse HEAD
bash scripts/test-stage1.sh
git tag -a stage-1 -m "Runnable Stage 1 baseline"
git push origin stage-1
git ls-remote --tags origin 'refs/tags/stage-1*'
```

tag 已存在时先核实目标 SHA，不覆盖、不强推。Annotated tag 的 `refs/tags/stage-1^{}` 应解析到选定的 commit。随后更新 README 的真实 tag 链接。

最后从 tag 的文件做一次独立构建，避免依赖当前目录的未跟踪文件：

```bash
stage1_check_dir="$(mktemp -d /tmp/sc2002-stage1-tag.XXXXXX)"
git archive stage-1 | tar -x -C "$stage1_check_dir"
bash "$stage1_check_dir/scripts/test-stage1.sh"
bash "$stage1_check_dir/scripts/run.sh" < "$stage1_check_dir/test/replays/stage1-victory.txt"
bash "$stage1_check_dir/scripts/run.sh" < "$stage1_check_dir/test/replays/stage1-defeat.txt"
```

通过标准：远端 tag 真实存在、解析到正确 SHA，干净副本能离线构建和复现两种结局。之后 Stage 2/3 继续扩展，而 stage-1 保留原样。

## 13 需要保留但不应混入 Stage 1 的最终作业事项

以下不是本次通过 Stage 1 就自动完成的内容；保留为后续待办，防止只做代码而遗漏分数。

- [ ] Stage 2：VIP、Critic 的差异行为通过对象多态；六次到店含两种新类型；独立可运行并有 stage-2。
- [ ] Stage 3：Host、ComboMeal、Happy Hour 完整规则；独立可运行并有 stage-3。
- [ ] README 两次阶段变化表：新增文件、改/删文件与原因、commit/compare 链接；既有行为的回归证据，不能只写行数。
- [ ] 三阶段类图分别匹配代码；一张 Stage 2 或 3 的完整复杂用例顺序图，含 manager choice、到店/点单、员工分配、READY/SERVED/进食/支付/结果和后续阶段行为、guards/loops。
- [ ] 报告中的职责表、实际适用的 SOLID、trade-offs、考虑过的 alternative、玩家选择怎样改变结果；每人 reflection。
- [ ] 约 15 分钟视频：启动；受员工约束的有意义选择；READY→SERVED→payment；VIP/Critic；Stage 3 功能；胜负各一；tag comparisons 与 diagrams。每人讲解，README 列时间戳。
- [ ] 每人完整 AI history 和个人分支有效链接；original work 与每人的 GAI 声明按课程表格签署。
- [ ] 一个 `LABGroup_AssignmentGrp.zip`，含源码/构建说明、README、PDF 报告、视频或可下载链接、声明；所有链接对评分者可访问。
- [ ] 截止：2026 年 11 月 15 日 23:59，新加坡时间；建议预留打包和链接验证时间。

评分为 class diagram 25、sequence diagram 20、design 15、implementation 20、thinking/development process 20。GUI 可选，单独做 GUI 没有额外分；保持所有必做功能离线可用。

## 14 当前结论与下一次会话入口

**Stage 1 的生产调用链已接通，现有开发检查与新回放通过。** 当前个人分支为 `lichen/stage_1`；Lichen 的三个相关 Codex 会话已按原文整理并链接，人工走查确认了 C1 的完整服务链。尚待：后续人工场景、E 独立记录、Stage 1 图/设计对齐、其他个人 AI 对话与成员链接的完整性确认、团队整合与 runnable stage-1 tag。

下一次按第 2 步确认版本，再按第 4、5 步从第一个 Waiter 提示亲自走六回合；遇到某一行不符，就用回合数、输入和实际输出定位，而不跳过。自动检查不需在代码未改变时反复重跑；把时间用于尚未完成的人工证据和图文对齐。
