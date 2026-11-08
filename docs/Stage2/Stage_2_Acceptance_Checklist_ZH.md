# Stage 2 验收清单（已发布）

依据：作业 manual §2–5；补充 briefing 第 3–8 页。核对日期：2026-11-08。
范围：个人分支 `lichen/stage_2`，从已发布的 `stage-1` 继续开发。

本清单记录代理实际核验，不冒充 E 的独立测试或人工签字。此前用户要求停在
打 tag 之前；随后已明确授权继续发布。本次创建 annotated `stage-2`，验证其
干净归档并与个人分支一起原子推送。其他成员的 AI 记录、最终报告/视频/签署
表仍由团队分别完成，不伪造缺失材料。

执行状态：**Stage 2 代码、设计、归档、验证与发布步骤已完成**。精确已测基线、
环境、日志、源码与发布快照的关系见[发布记录](../verification/Stage_2_Release_2026-11-08.md)。
标签对象及最终提交可用 `git show stage-2`、`git rev-parse stage-2^{commit}` 查看。

## A. 基础配置与顾客生成

| 编号 | 验收项目 | 已有实现 / 核验入口 |
| --- | --- | --- |
| S2-01 | 可独立编译；Java 17 API 目标；所有必需文件来自仓库 | `scripts/build.sh`；`--release 17 -Xlint:all -Werror` |
| S2-02 | 必做功能离线可用 | 干净副本禁止网络后执行构建、测试和 Main 回放 |
| S2-03 | 总计 18 回合；到达回合 1/4/7/10/13/16；仍是六位顾客 | `GameConfig`；`Stage2IntegrationTest.arrivalSchedule` |
| S2-04 | 两张桌；Waiter/Chef/Cashier 各一；容量不变 | `StaffTeam`、`SeatingArea`；Regular core / production smoke |
| S2-05 | 顾客 ID 1–6；初始 WAITING/100；工厂失败不产生半个回合 | `CustomerFactory`、`GameController.startTurn`；`factoryFailure` |
| S2-06 | 基础可复现运行含至少一名 VIP、一名 Critic，其他四名 Regular | 默认种子 24369；`factorySchedule`；实际 Main 输出 |
| S2-07 | 同一 seed 生成相同类型顺序；顾客自己选菜，经理不选食物 | Factory 固定顺序；Regular/VIP 的 ID 规则；Critic 的最长备餐规则 |

## B. Stage 2 行为与多态

| 编号 | 验收项目 | 已有实现 / 核验入口 |
| --- | --- | --- |
| S2-08 | Regular/VIP/Critic 等待时分别扣 8/5/12 | `customerBehaviour` 覆盖 WAITING、SEATED、ORDERED |
| S2-09 | 满意度最低为 0；SERVED、READY_TO_PAY、PAID 不再扣分 | 同一测试的下限和服务后免扣检查 |
| S2-10 | VIP 九折，以分计价，四舍五入；基础三菜 810/1350/1620 分 | `BillCalculatorTest` 使用真实 VipCustomer；大数中间运算不溢出 |
| S2-11 | Critic 全价，且有已文档化的不同偏好 | `CriticCustomer.chooseItem` 选最长备餐，平手取菜单先出现项；`criticTiming` |
| S2-12 | 行为通过 Customer 对象分派，controller 没有 VIP/Critic 类型判断链 | 检查 `GameController`；Factory 中的类型注册 switch 与回合行为分派分开 |
| S2-13 | 三种顾客使用同一套下单/备餐/上菜/进餐/付款状态流程 | Regular smoke、混合 VIP 完整流程、Critic-first fixture |
| S2-14 | 下单时锁定折后账单，付款只收已锁金额 | `Restaurant.placeOrder`、Order final billCents；计价与实际 Cashier 回放 |

## C. 原有回合规则不能退化

| 编号 | 验收项目 | 已有实现 / 核验入口 |
| --- | --- | --- |
| S2-15 | Waiter 先于 Chef；Chef 可备本回合刚点的订单 | Controller 两个操作阶段；生产流程测试 |
| S2-16 | 员工每回合至多一个任务/备餐单位/付款 | `staffAllowances`；core payment priority/quotas |
| S2-17 | READY 当回合不能上菜 | `readyTurn < turn`；Pasta / Critic 时间线 |
| S2-18 | 上菜后一个完整后续回合才能吃完；刚 READY_TO_PAY 的回合不能付款 | `servedTurn`、`readyToPayTurn` 严格小于当前回合；VIP 第 6/7/8 回合 |
| S2-19 | Cashier 自动收取最早可付款者，至多一人；同时间按 ID | core FIFO / tie / quota；生产 Cashier |
| S2-20 | 非整数/空白/越界选择可重试；不消耗员工任务或推进阶段 | Regular smoke；混合 CLI Waiter/Chef retry；BOM 输入 |
| S2-21 | 顾客耗尽满意度离开，取消 PLACED/PREPARING/READY 订单并释放桌位 | `criticAbandonment(0/1/2)`；原 Regular abandonment 测试 |
| S2-22 | 未付款/离店订单不计收入；平均满意度用付款快照；无人付款显示 N/A | Summary / Order；全等待失败回放和付款测试 |
| S2-23 | 第 16 回合到达者不能在 18 回合内付款，最多五位付款 | 默认 Critic #6 最终仍在准备 Pasta；`mixedVictory` |
| S2-24 | 胜负门槛不变；精确比较 ≥4 人、≥4500 分、平均 ≥60；不提前结束 | `GameResult`；core 边界；所有 Main 完整回放 |

## D. 测试、设计和版本证据

| 编号 | 验收项目 | 已有实现 / 核验入口 |
| --- | --- | --- |
| S2-25 | 胜利可复现：18 回合、5 付款、$64.50、80.40、0 离店 | `test/replays/stage2-victory.txt`；`scripts/run.sh` |
| S2-26 | 失败可复现：18 回合、0 付款、$0.00、N/A、1 离店 | `test/replays/stage2-defeat.txt` |
| S2-27 | 顺序图的单次选择效果有真实回放支持 | 第 5 回合 Wait；VIP 第 9 回合付款/85；最终平均 77.80，仍然胜利 |
| S2-28 | Regular 模式回归通过，不能把它冒充旧 tag 的源码 | Core 14/344；production smoke 9/94；Main `--regular` |
| S2-29 | Stage 2 类图对应源码；复杂顺序图覆盖要求；原 Stage 1 图保留 | `docs/UML/Stage2_*`；`Diagram_Notes.md`；图源码 SHA 表 |
| S2-30 | Stage 1→2 新增/修改/删除及原因、设计影响、commit/compare 链接 | `Stage_1_to_2_Changes.md`；README |
| S2-31 | Lichen 完整 AI 对话按原顺序保存在个人分支，有范围/截止时间和 README 链接 | `ai-history/stage-2/`；原始文本逐条核对，非重写总结 |
| S2-32 | 精确候选 SHA 从干净副本通过；路径含空格，从外部 cwd 运行，禁网 | 发布前验证记录及其日志 |
| S2-33 | stage-1 tag、main、其他组员分支不被改写；仅推送自己的候选分支 | 发布前后的远端 SHA 核对 |
| S2-34 | 创建 annotated stage-2 并推送/核对 | **完成：annotated tag；干净 tag 归档验证；原子推送并核对 peeled commit** |

## 实际测试与人工演示的边界

默认运行的 Critic 是第六位，受题目时间限制不能付款。早到 Critic 的完整
付款、双单位备餐和离店取消，由生产对象的测试 fixture 单独验证；不是通过
改变默认 CLI 或偷加 Stage 3 功能来演示。VIP 在默认 Main 中实际付款。

自动化日志和图核对属于代理执行证据。用户此前参与过 Stage 1 手动检查；
本轮不把代理运行写成用户/E 的手动走查。可以之后安排真人复核，但不伪造
签名，也不把它重新解释成必须等待 E 才能完成当前已授权准备工作的阻塞。
