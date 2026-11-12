# Stage 1 AI conversation 02

Member: Lichen Yin (Member A). AI assistant: OpenAI Codex.

Topic: Stage 1 class responsibilities interfaces UML design and explanation.

Source conversation ID: `01a171ac-4ad0-7053-a39a-a46a3a3234c7`.

Coverage: 2026-10-25T14:09:51+08:00 through 2026-10-27T23:21:22+08:00 (Singapore time, UTC+08:00).

The original user and assistant messages appear below in source order, including user-facing progress messages. Original Chinese/English wording and code are retained; only line endings and trailing whitespace are normalized. Images are recovered from the original user-message attachments. System/developer instructions, environment/page metadata, internal reasoning and tool payloads are not part of this conversation transcript. See the [history index](README.md) for scope and requirements.

Historical statements describe the repository at the time they were made. They are preserved as original replies, including earlier design proposals and later corrections; do not interpret every old statement as the current implementation status.

<!-- transcript-message-start:chat-02-m001 -->
## Message 001 - User

Time: 2026-10-25T14:09:51+08:00.

<!-- original-text-start:chat-02-m001 -->
按照上面的那个分配计划 我们是不是要先整理出不同的类之间的关系图 然后绘制一个UML的草图 然后开始写代码
<!-- original-text-end:chat-02-m001 -->

<!-- transcript-message-end:chat-02-m001 -->

<!-- transcript-message-start:chat-02-m002 -->
## Message 002 - Assistant

Time: 2026-10-25T14:10:17+08:00.

<!-- original-text-start:chat-02-m002 -->
对，整体顺序是对的。建议你们按照 **“梳理流程 → 明确类的职责和关系 → 画 UML 草图 → 约定接口 → 分工写代码”** 来推进。

你说的“类之间的关系图”本身就是 UML 类图的主要内容，可以合并在一起做。

| 步骤 | 你们需要讨论什么 | 得到什么 |
|---|---|---|
| **1. 梳理游戏流程** | 一回合怎么执行？顾客、订单和桌子的状态什么时候变化？ | 一份全组认可的回合流程和状态变化说明 |
| **2. 确定类的职责** | 每个类负责什么、保存哪些数据、提供哪些操作？ | 类与职责清单 |
| **3. 画 UML 草图** | 哪些类相互调用、持有哪些对象、存在什么继承关系？ | 第一版类图，以及一个典型场景的顺序图草稿 |
| **4. 约定模块接口** | 调用需要什么参数、返回什么结果、失败如何反馈、由谁修改状态？ | 五个人共同遵守的接口约定 |
| **5. 并行实现与联调** | 各自实现负责的模块，再连接成完整游戏 | 可运行的 Stage 1，并同步修订 UML |

比如，第一版可以先围绕这些核心类讨论：

| 类或模块 | 主要职责 |
|---|---|
| `Customer` | 保存顾客状态和满意度，提供顾客行为 |
| `Table` | 管理桌子的占用与释放 |
| `MenuItem` | 描述菜品价格和备餐单位 |
| `Order` | 保存所点菜品、备餐进度、订单状态和锁定价格 |
| `Waiter`、`Chef`、`Cashier` | 执行各自允许的任务 |
| 游戏控制器 | 按规定顺序推进回合、协调对象、判断胜负 |
| CLI | 展示状态、读取玩家选择、显示操作结果 |

**最值得提前统一的是“谁负责修改状态”。** 例如，上菜时由谁把订单改为 `SERVED`，付款时由谁记录收入并释放桌子。如果没有约定，大家容易在不同模块里重复实现同一条规则。

你们可以用作业中的 Pasta 示例走一遍顺序图草稿：

**入座 → 点单并完成第一单位备餐 → 完成备餐变成 READY → 下一回合上菜 → 再下一回合吃完 → 再下一回合付款。**

这能帮助你们在写代码前发现接口和回合时序的问题。

开始时先把 **Stage 1 的设计确定到足以分工实现的程度**，同时讨论 Stage 2、3 会怎样扩展。之后每完成一个阶段，都更新图和设计说明；最终提交的类图需要与 Stage 3 代码一致。:codex-file-citation{path="/Users/edward/Desktop/NTU/Y2S1/SC2002/Group Assignment/SC2002_Restaurant_Rush_Group_Assignment(1).docx" purpose="source"}

所以，你们下一次组会最合适的目标就是：**共同整理出 Stage 1 的类职责表、UML 草图和接口约定，然后五个人开始并行实现。**
<!-- original-text-end:chat-02-m002 -->

<!-- transcript-message-end:chat-02-m002 -->

<!-- transcript-message-start:chat-02-m003 -->
## Message 003 - User

Time: 2026-10-25T14:12:29+08:00.

<!-- original-text-start:chat-02-m003 -->
那你先帮我们整理一下 Stage 1 的类职责表、类之间的关系和接口约定，再画一个 UML 草图，先不要写代码。
<!-- original-text-end:chat-02-m003 -->

<!-- transcript-message-end:chat-02-m003 -->

<!-- transcript-message-start:chat-02-m004 -->
## Message 004 - Assistant

Time: 2026-10-25T14:12:31+08:00.

<!-- original-text-start:chat-02-m004 -->
我会先核对 Stage 1 的规则，再整理类职责、关系、接口和 UML 草图，重点明确状态修改的归属与回合时序。这次只做设计，不写实现代码。
<!-- original-text-end:chat-02-m004 -->

<!-- transcript-message-end:chat-02-m004 -->

<!-- transcript-message-start:chat-02-m005 -->
## Message 005 - Assistant

Time: 2026-10-25T14:14:06+08:00.

<!-- original-text-start:chat-02-m005 -->
设计里最需要统一的是：顾客、订单和桌子的状态由谁修改，以及菜做好、吃完、付款之间的回合间隔。我会让一个 `Restaurant` 对象协调关联状态，控制器负责回合顺序，并把非法操作可重选的规则写进接口约定。
<!-- original-text-end:chat-02-m005 -->

<!-- transcript-message-end:chat-02-m005 -->

<!-- transcript-message-start:chat-02-m006 -->
## Message 006 - Assistant

Time: 2026-10-25T14:16:31+08:00.

<!-- original-text-start:chat-02-m006 -->
职责表和接口草案已整理好。Stage 1 建议使用 10 个主要类，暂时不引入额外的父类或框架；CLI 只收集选择和展示结果。我还补了一条手工推演的胜利路径，供实现后验收，目前没有生成或运行游戏代码。
<!-- original-text-end:chat-02-m006 -->

<!-- transcript-message-end:chat-02-m006 -->

<!-- transcript-message-start:chat-02-m007 -->
## Message 007 - Assistant

Time: 2026-10-25T14:19:01+08:00.

<!-- original-text-start:chat-02-m007 -->
已经整理成一份 **Stage 1 设计草案**，包含类职责、关系、接口约定、状态变化和验收场景，**没有编写 Java 实现代码**。

[查看完整设计文档](</Users/edward/Documents/ChatGPT/SC2002 Group Assignment/design/stage-1-design.md>) · [放大查看 UML 草图](</Users/edward/Documents/ChatGPT/SC2002 Group Assignment/design/stage-1-classes.svg>) · [Mermaid 图源文件](</Users/edward/Documents/ChatGPT/SC2002 Group Assignment/design/stage-1-classes.mmd>)

这套设计的核心分工是：**CLI 收集选择，GameController 安排回合，员工限制任务额度，Restaurant 协调关联状态，实体保护自身数据。** 类名和接口名是建议，可由你们小组共同调整。

**类职责建议如下：**

| 类 | 主要职责 | 负责人 |
|---|---|---|
| `RestaurantCLI` | 显示餐厅状态、读取玩家选择、显示操作结果和错误原因 | D |
| `GameController` | 推进回合，控制操作顺序，处理到达、自动步骤和最终胜负 | A |
| `Restaurant` | 保存顾客、队列、桌子、菜单和订单；协调入座、点单、付款等关联变化 | A |
| `Customer` | 保存满意度和顾客状态；决定菜品、等待扣分和账单 | B |
| `Table` | 管理桌子的占用和释放 | B |
| `MenuItem` | 保存菜名、价格和所需备餐单位，创建后保持不变 | C |
| `Order` | 管理备餐进度和订单状态；保存下单价格、关键回合与付款时满意度 | C |
| `Waiter` | 每回合执行一次入座、点单、上菜或等待操作 | D |
| `Chef` | 每回合推进一个订单的一个备餐单位，或等待 | D |
| `Cashier` | 自动处理最早符合付款条件的顾客，每回合最多一笔 | D |

E 负责只读数据对象、测试与回放规格，以及 UML、README 和报告整合；其他成员提供本人模块的设计说明和测试记录。

其中，`Restaurant` 与 `GameController` 的职责需要特别明确。例如，付款时：

**GameController 安排付款步骤 → Cashier 选择付款对象 → Restaurant 协调订单标为 PAID、顾客标为 PAID、桌子释放。**

这样，同一次业务操作涉及的多个状态就有统一的协调入口。

**类之间的主要关系是：**

- 一个 `GameController` 管理一个 `Restaurant`，以及三名员工。
- 一个 `Restaurant` 保存两张桌子、三种菜品，以及本局创建的顾客和订单。
- 一张桌子同时容纳 **0–1 名顾客**；一个顾客同时占用 **0–1 张桌子**。
- 一名顾客在 Stage 1 中拥有 **0–1 个订单**；每个订单属于一名顾客，并引用一种菜品。
- 等待队列保存同一批顾客对象的引用；顾客付款或离店后，历史记录继续保留，供统计使用。

Stage 1 暂时不增加 `Staff` 父类或额外的 Java `interface`。后续 Stage 2 可以通过顾客子类扩展等待扣分、选菜和价格行为。

UML 草图如下，`◆` 表示组合关系，虚线箭头表示调用依赖：

![Stage 1 UML 类图草稿](/Users/edward/Documents/ChatGPT/SC2002%20Group%20Assignment/design/stage-1-classes.png)

**接口首先要统一“谁调用谁、调用后发生什么”。** 这里的接口指方法契约，不要求使用 Java 的 `interface` 关键字。

| 调用入口 | 约定 |
|---|---|
| `GameController.startTurn()` | 开始下一回合，加入计划到达的顾客，进入 Waiter 操作阶段 |
| `GameController.getView()` | 返回只读状态快照，不推进回合 |
| `GameController.submitWaiterAction(action)` | 接收入座、点单、上菜或等待；成功后进入 Chef 操作阶段 |
| `GameController.submitChefAction(action)` | 接收备餐或等待；成功后进入自动处理阶段 |
| `GameController.finishTurn()` | 处理用餐、付款、满意度、离店和统计；第 18 回合结束后判断胜负 |
| `Waiter.execute(...)` | 检查本回合任务额度，再委托 Restaurant 执行业务操作 |
| `Chef.execute(...)` | 检查额度，每次最多推进一个备餐单位 |
| `Cashier.processPayment(...)` | 自动选择最早符合付款资格的顾客，最多处理一笔 |

玩家操作统一使用以下参数：

| 操作 | 必需参数 |
|---|---|
| 入座 `SEAT` | 顾客编号、桌子编号 |
| 点单 `TAKE_ORDER` | 顾客编号；菜品由顾客对象决定 |
| 上菜 `SERVE` | 订单编号 |
| 备餐 `PREPARE` | 订单编号 |
| 等待 `WAIT` | 无 |

操作返回 `ActionResult`，至少包含：

- `accepted`：是否接受操作。
- `message`：成功信息，或具体失败原因。

**无效操作必须保持当前状态和任务额度，让玩家重新选择。** 例如桌子已被占用时，不能先把顾客移出队列，再发现操作失败。

**还需要统一两套状态：**

| 对象 | 正常状态变化 |
|---|---|
| 顾客 | `WAITING → SEATED → WAITING_FOR_FOOD → EATING → READY_TO_PAY → PAID` |
| 订单 | `PLACED → PREPARING → READY → SERVED → PAID` |

补充约定：

- 一单位菜品可以直接从 `PLACED` 进入 `READY`。
- 尚未上菜的顾客满意度降到零后进入 `LEFT`，对应订单进入 `CANCELLED`。
- 顾客吃完后，订单仍保持 `SERVED`；付款资格由顾客的 `READY_TO_PAY` 状态表达。
- 金额统一存整数 cents；收入只汇总已付款订单，满意度平均值使用付款时保存的数值。

**回合时序按下面的顺序固定下来：**

**到达 → Waiter 操作 → Chef 操作 → 用餐与付款 → 扣满意度与离店 → 统计和结果检查**

其中三个时间限制要写进接口条件：

1. 本回合刚做好的菜，最早下回合上菜。
2. 本回合上菜，下一回合吃完。
3. 本回合刚吃完，最早下回合付款。

例如 Pasta 的完整过程是：**第 1 回合入座，第 2 回合点单并备餐一次，第 3 回合完成备餐，第 4 回合上菜，第 5 回合吃完，第 6 回合付款。**

完整文档还包含一条手工推演的胜利路径和一条失败路径，供实现后验证；目前这些是测试规格，尚未经过程序运行验证。本草案按任务书的 Stage 1 范围整理，后续再随阶段扩展更新 UML。:codex-file-citation{path="/Users/edward/Desktop/NTU/Y2S1/SC2002/Group Assignment/SC2002_Restaurant_Rush_Group_Assignment(1).docx" purpose="source"}
<!-- original-text-end:chat-02-m007 -->

<!-- transcript-message-end:chat-02-m007 -->

<!-- transcript-message-start:chat-02-m008 -->
## Message 008 - User

Time: 2026-10-25T16:31:14+08:00.

<!-- original-text-start:chat-02-m008 -->
帮我把这份 Stage 1 的类职责表、类关系、接口约定和 UML 草图整理成英文版，方便发给组员讨论，先不要写代码。
<!-- original-text-end:chat-02-m008 -->

<!-- transcript-message-end:chat-02-m008 -->

<!-- transcript-message-start:chat-02-m009 -->
## Message 009 - Assistant

Time: 2026-10-25T16:31:37+08:00.

<!-- original-text-start:chat-02-m009 -->
我会把现有设计稿整理成适合组内讨论的英文版，保留职责分配、接口约定和 UML，并标明哪些是小组需要确认的设计建议。这次只处理设计文档，不写实现代码。
<!-- original-text-end:chat-02-m009 -->

<!-- transcript-message-end:chat-02-m009 -->

<!-- transcript-message-start:chat-02-m010 -->
## Message 010 - Assistant

Time: 2026-10-25T16:32:08+08:00.

<!-- original-text-start:chat-02-m010 -->
现有 UML 已是英文，可以直接复用。我会把正文完整翻译，包括状态定义、方法契约和手工推演的胜负场景，再将文档与图打包，方便你们转发给组员。
<!-- original-text-end:chat-02-m010 -->

<!-- transcript-message-end:chat-02-m010 -->

<!-- transcript-message-start:chat-02-m011 -->
## Message 011 - Assistant

Time: 2026-10-25T16:33:41+08:00.

<!-- original-text-start:chat-02-m011 -->
已整理成完整英文版，包含类职责、类关系、接口约定、状态规则、UML 草图及验收场景，并标明小组需要讨论的设计选择。**未编写实现代码。**

- [查看英文设计文档](</Users/edward/Documents/ChatGPT/SC2002 Group Assignment/design/Stage_1_Design_Discussion_EN.md>)
- [下载分享包 ZIP](</Users/edward/Documents/ChatGPT/SC2002 Group Assignment/design/Stage_1_Design_Discussion_EN.zip>)：包含英文文档、UML 图片和可编辑的 Mermaid 图源，方便转发给组员。
<!-- original-text-end:chat-02-m011 -->

<!-- transcript-message-end:chat-02-m011 -->

<!-- transcript-message-start:chat-02-m012 -->
## Message 012 - User

Time: 2026-10-25T16:40:12+08:00.

<!-- original-text-start:chat-02-m012 -->
帮我把英文版整理成 PDF，把 UML 图也放进去，提交到这个文件夹中[https://github.com/Cooolc327/SC2002_Group_Assignment/tree/main/Stage%201](https://github.com/Cooolc327/SC2002_Group_Assignment/tree/main/Stage%201) 最后把UML的图片单独整理出来 这样方便编辑
<!-- original-text-end:chat-02-m012 -->

<!-- transcript-message-end:chat-02-m012 -->

<!-- transcript-message-start:chat-02-m013 -->
## Message 013 - Assistant

Time: 2026-10-25T16:40:14+08:00.

<!-- original-text-start:chat-02-m013 -->
我会把英文设计稿排成 PDF，放入可放大的 UML 图，并单独准备 UML 图片和可编辑图源。之后检查目标仓库，将文件提交到 `Stage 1` 文件夹。
<!-- original-text-end:chat-02-m013 -->

<!-- transcript-message-end:chat-02-m013 -->

<!-- transcript-message-start:chat-02-m014 -->
## Message 014 - Assistant

Time: 2026-10-25T16:40:37+08:00.

<!-- original-text-start:chat-02-m014 -->
我会使用 PDF 技能进行排版和逐页检查，并用 guarded-git-publish 流程核对提交文件。UML 会提供 PNG 便于查看、SVG 便于矢量编辑，以及 Mermaid 图源便于修改类和关系。
<!-- original-text-end:chat-02-m014 -->

<!-- transcript-message-end:chat-02-m014 -->

<!-- transcript-message-start:chat-02-m015 -->
## Message 015 - Assistant

Time: 2026-10-25T16:42:36+08:00.

<!-- original-text-start:chat-02-m015 -->
仓库的 `Stage 1` 文件夹目前只有一个占位文件，写入权限已确认。PDF 会保留完整英文内容，并给 UML 单独留一页；我也会补一份 draw.io 文件，让组员可以直接修改类框、连线和基数。
<!-- original-text-end:chat-02-m015 -->

<!-- transcript-message-end:chat-02-m015 -->

<!-- transcript-message-start:chat-02-m016 -->
## Message 016 - Assistant

Time: 2026-10-25T16:47:13+08:00.

<!-- original-text-start:chat-02-m016 -->
PDF 已生成，UML 已以矢量图嵌入独立页面，放大后文字仍可保持清晰。我正在检查分页和表格换页，并整理单独的 UML 编辑文件；检查完成后会提交到仓库。
<!-- original-text-end:chat-02-m016 -->

<!-- transcript-message-end:chat-02-m016 -->

<!-- transcript-message-start:chat-02-m017 -->
## Message 017 - Assistant

Time: 2026-10-25T16:48:47+08:00.

<!-- original-text-start:chat-02-m017 -->
15 页 PDF 已逐页检查，表格和 UML 显示正常。UML 的 PNG、SVG、Mermaid 和 draw.io 文件也已整理完成；接下来提交到 `Stage 1`，并核对远端文件是否与本地版本一致。
<!-- original-text-end:chat-02-m017 -->

<!-- transcript-message-end:chat-02-m017 -->

<!-- transcript-message-start:chat-02-m018 -->
## Message 018 - Assistant

Time: 2026-10-25T16:51:27+08:00.

<!-- original-text-start:chat-02-m018 -->
已提交到 [GitHub 的 Stage 1 文件夹](https://github.com/Cooolc327/SC2002_Group_Assignment/tree/main/Stage%201)，提交编号 `3c5dee6`，并核对远端文件一致。

英文 PDF 共 **15 页**，包含 UML 图，已逐页检查：

:codex-file-citation{path="/Users/edward/Documents/ChatGPT/SC2002 Group Assignment/output/pdf/Stage_1_Design_Discussion_EN.pdf" purpose="output"}

UML 已单独整理到 [UML 文件夹](https://github.com/Cooolc327/SC2002_Group_Assignment/tree/main/Stage%201/UML)，包含 PNG、SVG、Mermaid 和 **draw.io 可编辑文件**，可以修改类框、连线和基数。

[下载 UML 编辑包 ZIP](</Users/edward/Documents/ChatGPT/SC2002 Group Assignment/output/Stage_1_UML_Edit_Pack.zip>)

<oai-mem-citation>
<citation_entries>
skills/guarded-git-publish/SKILL.md:24-30|note=[file allowlist and remote verification]
MEMORY.md:1051-1052|note=[byte audit before publication]
</citation_entries>
<rollout_ids>
01a08624-3f9d-70a3-a854-e6b2847a2ba1
</rollout_ids>
</oai-mem-citation>
<!-- original-text-end:chat-02-m018 -->

<!-- transcript-message-end:chat-02-m018 -->

<!-- transcript-message-start:chat-02-m019 -->
## Message 019 - User

Time: 2026-10-27T23:17:23+08:00.

<!-- original-text-start:chat-02-m019 -->
给我仔细讲解一下这个uml的图上的内容
<!-- original-text-end:chat-02-m019 -->

<!-- transcript-message-end:chat-02-m019 -->

<!-- transcript-message-start:chat-02-m020 -->
## Message 020 - Assistant

Time: 2026-10-27T23:17:35+08:00.

<!-- original-text-start:chat-02-m020 -->
我先重新看一下 UML 图和对应的设计约定，再按“图的读法、每个类的职责、对象之间的关系、一次完整服务流程”来讲解。
<!-- original-text-end:chat-02-m020 -->

<!-- transcript-message-end:chat-02-m020 -->

<!-- transcript-message-start:chat-02-m021 -->
## Message 021 - Assistant

Time: 2026-10-27T23:19:09+08:00.

<!-- original-text-start:chat-02-m021 -->
图里最容易混淆的是三组概念：`GameController` 管执行顺序，`Restaurant` 管关联状态，实体类管自身的合法变化；订单状态和顾客状态也各有用途。我会结合 Pasta 从点单到付款的六回合例子，把这些区别串起来。
<!-- original-text-end:chat-02-m021 -->

<!-- transcript-message-end:chat-02-m021 -->

<!-- transcript-message-start:chat-02-m022 -->
## Message 022 - Assistant

Time: 2026-10-27T23:21:22+08:00.

<!-- original-text-start:chat-02-m022 -->
这张 UML 图描述的是：**一个玩家的决定如何经过界面、控制器和员工对象，最终改变顾客、桌子和订单的状态。**

我按我们前面整理的 [UML 草图](</Users/edward/Documents/ChatGPT/SC2002 Group Assignment/design/stage-1-classes.svg>) 和 [英文设计说明](</Users/edward/Documents/ChatGPT/SC2002 Group Assignment/design/Stage_1_Design_Discussion_EN.md>) 来讲。它目前是 **Stage 1 的设计提案**，部分参数类型、查询方法和辅助类型在图中做了省略。

读整张图时，先抓住这一条主线：

**玩家输入 → RestaurantCLI → GameController → 员工对象 → Restaurant → 具体实体**

下面逐步展开。

**先看每个方框里面怎么读。**

每个方框代表一个“类”，通常分成三部分：

| 位置 | 内容 | 图中的例子 |
|---|---|---|
| 最上面 | 类名 | `Customer` |
| 中间 | 属性：对象需要保存的数据 | `satisfaction: int` |
| 最下面 | 方法：对象能够提供的操作 | `waitingPenalty() int` |

例如，`Customer` 是顾客的共同定义；实际运行时，C1、C2、C3 是这个类创建出来的不同对象，每个人都有自己的满意度和状态。

属性和方法前面的符号表示访问权限：

| 符号 | 含义 | 例子 |
|---|---|---|
| `-` | `private`，私有 | `-satisfaction: int` |
| `+` | `public`，公有 | `+waitingPenalty() int` |
| `~` | package visibility，同一个包内可访问 | `~markServed(turn)` |

`-satisfaction: int` 表示：顾客对象内部保存一个整数类型的满意度字段，其他类通过受控操作来改变它。

`+chooseMenuItem(items) MenuItem` 可以拆开读成：

- 方法名：`chooseMenuItem`。
- 输入参数：`items`，可供选择的菜单。
- 返回结果：一个 `MenuItem` 对象。
- `+` 表示这是对外提供的方法。

括号后面的 `MenuItem`、`GameView`、`ActionResult` 都是返回类型。有些方法没有标返回类型，是草图中的简写，不能直接据此认定它一定返回 `void`。

---

最上面的 **`RestaurantCLI` 是玩家接触的命令行界面。**

CLI 是 Command-Line Interface，也就是终端里的文字菜单。它负责：

- 显示当前回合、收入、顾客、桌子和订单。
- 询问玩家要执行什么操作。
- 读取玩家输入。
- 显示操作成功或失败的信息。
- 游戏结束后显示结果。

它的属性是：

`-controller: GameController`

意思是：**CLI 内部保存一个 GameController 对象的引用，通过它操作游戏。**

这里可以把“引用”理解为：CLI 知道应该找哪个控制器处理请求。

它的 `run()` 方法负责整个界面交互循环，例如：

**显示状态 → 收集服务员选择 → 提交操作 → 显示结果 → 收集厨师选择 → 结束本回合。**

CLI 可以判断“用户输入的内容是不是数字”。至于“这张桌子能不能坐”“这个订单能不能上菜”，由后面的控制和领域对象判断。

这样，游戏规则有统一的位置，以后修改界面时也容易保持规则一致。

---

下面的 **`GameController` 负责游戏的时间和执行顺序。**

它有两个特别重要的属性：

| 属性 | 含义 |
|---|---|
| `currentTurn: int` | 当前是第几回合，例如第 5 回合 |
| `phase: TurnPhase` | 当前回合进行到哪个步骤，例如正在等待服务员选择 |

**turn 和 phase 表达的是两个不同层次的进度。**

例如：

- `currentTurn = 5`：现在是第五回合。
- `phase = AWAIT_CHEF`：第五回合的服务员操作已经完成，现在轮到厨师。

它提供的五个主要方法是：

| 方法 | 作用 |
|---|---|
| `startTurn()` | 开始新回合，增加回合数，加入本回合到达的顾客 |
| `getView()` | 查询当前状态，不推进游戏 |
| `submitWaiterAction(action)` | 提交玩家选择的服务员操作 |
| `submitChefAction(action)` | 提交玩家选择的厨师操作 |
| `finishTurn()` | 执行用餐、付款、扣满意度、离店和结果检查 |

一个回合内部的阶段大致是：

**AWAIT_WAITER → AWAIT_CHEF → AUTO → TURN_ENDED**

也就是：

**等服务员选择 → 等厨师选择 → 自动处理 → 本回合结束。**

如果服务员操作被拒绝，控制器继续停留在 `AWAIT_WAITER`，让玩家重新选择。第 18 回合结束后进入 `FINISHED`。

因此，GameController 最主要回答的是：

> **现在应该执行哪一步？谁可以操作？什么时候检查结果？**

---

左边绿色的 **`Restaurant` 负责餐厅内部多个对象之间的一致性。**

这是整张图中最需要理解的一个类。

它保存和管理本局的：

- 顾客记录。
- 等待队列。
- 两张桌子。
- 订单记录。
- 三种菜单项。

图中只写出了 `waitingQueue: List<Customer>` 这一个字段，其余对象集合通过连线表达了出来。

它与 GameController 的分工可以通过“付款”来理解：

- **GameController**：现在到了付款步骤。
- **Cashier**：找到最早符合付款条件的顾客。
- **Restaurant**：协调顾客、订单和桌子一起完成付款变化。
- **Order、Customer、Table**：各自执行合法的内部状态变化。

付款涉及的变化至少有：

1. 订单变成 `PAID`。
2. 顾客变成 `PAID`。
3. 保存付款时的满意度。
4. 释放桌子。
5. 该订单开始计入收入统计。

这些变化需要作为一次完整的业务操作完成。否则就可能出现“顾客已经付款，但桌子还被占着”的错误。

Restaurant 的方法分别对应这些业务操作：

| 方法 | 负责什么 |
|---|---|
| `seatCustomer(customerId, tableId)` | 将等待顾客安排到空桌，更新队列、桌子和顾客 |
| `placeOrder(customerId)` | 让顾客选菜，创建订单，更新顾客状态 |
| `serveOrder(orderId, turn)` | 检查上菜资格，同时更新订单和顾客 |
| `advancePreparation(orderId, turn)` | 推进一个备餐单位 |
| `advanceEating(turn)` | 让符合时间条件的顾客吃完，进入待付款状态 |
| `settlePayment(customerId, turn)` | 完成付款及相关状态变化 |
| `applyWaitingPenalty(turn)` | 扣满意度，必要时处理离店 |
| `getSummary()` | 汇总已付款人数、收入、满意度等信息 |

**GameController 管流程，Restaurant 管一次业务操作涉及的关联变化。** 把这两个职责分清，后面分工写代码时会顺很多。

---

下面的 **`Customer` 表示一名顾客自身的状态和行为。**

它的四个字段是：

| 属性 | 含义 |
|---|---|
| `id` | 顾客编号 |
| `satisfaction` | 当前满意度 |
| `status` | 当前所处的服务状态 |
| `readyToPayTurn` | 哪一回合刚刚进入待付款状态 |

顾客状态的正常变化是：

**WAITING → SEATED → WAITING_FOR_FOOD → EATING → READY_TO_PAY → PAID**

对应：

**排队 → 已入座 → 等待食物 → 用餐 → 等待付款 → 已付款**

如果在尚未上菜时满意度降到零，则进入 `LEFT`，表示离店。

它的三个方法是在表达“顾客自己的行为”：

| 方法 | Stage 1 的含义 |
|---|---|
| `chooseMenuItem(items)` | 按约定规则选择菜品 |
| `waitingPenalty()` | 返回等待一回合要扣多少满意度，普通顾客为 8 |
| `quoteBillCents(item)` | 返回该顾客点这道菜应支付的金额，普通顾客付原价 |

这几个方法也是以后扩展顾客类型的位置。

例如 Stage 2 加入 VIP 后，可以让 VIP 的等待扣分和报价行为发生变化。控制器仍然调用相同的方法，具体结果由顾客对象决定，这就为多态提供了实际用途。

---

**`Table` 只管理桌子的占用情况。**

它保存：

- `id`：桌子编号。
- `occupant`：当前占用它的顾客。

桌子空闲时，`occupant` 可以没有值；有人入座后，它指向对应的 Customer 对象。

三个方法的含义是：

- `isAvailable()`：查询桌子是否空闲。
- `seat(customer)`：让一名顾客占用桌子。
- `release()`：清空占用关系。

注意，`Table.seat()` 和 `Restaurant.seatCustomer()` 的范围不同：

- `Table.seat()` 改变桌子自身的占用情况。
- `Restaurant.seatCustomer()` 还要同时处理等待队列和顾客状态。

所以，玩家的一次“入座”操作通过 Restaurant 协调，而不是只调用 Table 的方法。

---

**`MenuItem` 描述一种菜品。**

它保存三项数据：

| 属性 | 例子 |
|---|---|
| `name` | Pasta |
| `priceCents` | 1800，表示 $18.00 |
| `preparationUnits` | 2，表示需要两个备餐单位 |

`MenuItem` 在本设计中是不可变对象：创建好后，这些数据保持不变。

这里尤其要区分两个概念：

- **MenuItem 描述菜品本身的标准信息。**
- **Order 记录某位顾客点这道菜后的处理进度。**

例如，C1 和 C4 都点 Pasta，它们可以引用同一个 Pasta 菜单对象，但各自有独立的订单和备餐进度。

---

**`Order` 记录一笔具体订单从下单到付款的过程。**

它是图中字段最多的类，因为需要保存进度和时间。

| 属性 | 含义 |
|---|---|
| `id` | 订单编号 |
| `status` | 订单目前的状态 |
| `preparedUnits` | 已完成多少备餐单位 |
| `billCents` | 下单时锁定的应付金额 |
| `readyTurn` | 哪一回合完成备餐 |
| `servedTurn` | 哪一回合上菜 |
| `paidTurn` | 哪一回合付款 |
| `satisfactionAtPayment` | 付款时记录的顾客满意度 |

订单状态的正常变化是：

**PLACED → PREPARING → READY → SERVED → PAID**

对应：

**已下单 → 备餐中 → 已做好 → 已上菜 → 已付款**

一单位菜品可以直接从 `PLACED` 变成 `READY`。顾客离店时，尚未上菜的订单可以进入 `CANCELLED`。

这些时间字段是为了执行作业里的回合限制。例如：

- `readyTurn = 3`：第三回合刚做好，最早第四回合上菜。
- `servedTurn = 4`：第四回合上菜，第五回合吃完。
- 顾客的 `readyToPayTurn = 5`：第五回合刚吃完，最早第六回合付款。

图中这些字段使用 `Integer`，是为了让“事件尚未发生”能够用空值表达。

Order 的方法也对应受控的状态变化：

- `advanceOneUnit(turn)`：增加一个备餐单位，完成后进入 READY。
- `markServed(turn)`：标记上菜并保存上菜回合。
- `markPaid(turn, satisfaction)`：保存付款回合和满意度。
- `cancel()`：取消尚未上菜的有效订单。

**顾客状态和订单状态需要同时存在。** 例如顾客已经吃完、处于 `READY_TO_PAY` 时，订单仍然是 `SERVED`；是否有付款资格由顾客状态及时间决定。

---

右边三个类 **`Waiter`、`Chef`、`Cashier` 表示员工角色和任务额度。**

| 类 | 这一回合能做什么 |
|---|---|
| `Waiter` | 入座、点单、上菜三选一，或者 WAIT |
| `Chef` | 为一个订单推进一个备餐单位，或者 WAIT |
| `Cashier` | 自动收取一笔符合条件的付款 |

三者都有：

`lastActionTurn: int`

它表示最近一次使用任务额度的回合。

例如当前是第 5 回合，而 Waiter 的 `lastActionTurn` 已经是 5，就说明这一回合的额度已经使用。

对于 Waiter 和 Chef：

- 成功操作或主动 WAIT 后，记录本回合已使用额度。
- 无效操作被拒绝后，保留额度，让玩家重新选择。

Waiter 和 Chef 的 `execute(action, restaurant, turn)` 接收：

- `action`：玩家选择的任务。
- `restaurant`：需要操作的餐厅对象。
- `turn`：当前回合，用于检查任务额度和时间资格。

Cashier 的 `processPayment(restaurant, turn)` 自动选择最早符合付款条件的顾客，因此不需要玩家传入一个付款选择。

---

接着看连线。**线型说明对象之间是什么关系，线旁边的数字说明可以关联多少个对象。**

| 线型 | 含义 | 图中的例子 |
|---|---|---|
| 实线箭头 | 有一个可导航的关联，通常表示保存引用、知道如何找到对方 | CLI → GameController |
| 带实心菱形的线 | 组合，菱形端是拥有和管理整体的对象 | Restaurant ◆— Table |
| 虚线箭头 | 使用依赖，方法执行时需要使用对方 | Chef ⇢ Restaurant |

组合表示本设计把这些对象作为一局游戏的一部分统一拥有和管理。它表达的是设计上的归属，并不表示 Java 会在某个时刻立即自动删除对象。

比如，顾客付款后仍保留在本局记录中，供统计使用。

虚线依赖则可以结合方法参数理解：Chef 的方法接收一个 Restaurant，并在执行备餐任务时使用它。

**这些箭头表达结构和依赖；实际运行先后由控制器和回合约定说明。**

数字可以这样读：

| 关系 | 具体含义 |
|---|---|
| GameController — Restaurant 两端为 `1` | 一个控制器管理一个 Restaurant |
| GameController — 三名员工各为 `1` | 每局分别有一名 Waiter、Chef、Cashier |
| Restaurant — Customer 为 `0..6` | 随着游戏推进，本局登记 0 到 6 名顾客 |
| Restaurant — Table 为 `2` | 基础配置有两张桌子 |
| Restaurant — Order 为 `0..6` | 本局最多六笔订单，尚未点单时可以是零笔 |
| Restaurant — MenuItem 为 `3` | 有三种基础菜品 |
| Table — Customer 两端为 `0..1` | 每桌最多一位顾客，每位顾客最多占一桌 |
| Order — Customer：顾客端 `1`，订单端 `0..1` | 每笔订单属于一名顾客，每名顾客可以暂时没有订单，也可以有一笔 |
| Order — MenuItem：菜品端 `1`，订单端 `0..*` | 每单点一种菜品，一种菜品可以被多笔订单引用 |

读基数时，可以从另一端选定一个对象，再看这一端的数字。

例如选定一名 Customer，订单端是 `0..1`，因此这名顾客可以没有订单，也可以有一笔订单。

图中的 `2`、`3`、`0..6` 是本作业基础配置下的数量约束。

---

还有两个图中出现了名字、但没有单独画方框的类型。

**`GameView` 是给界面显示的只读状态快照。** 它包含当前回合、桌子、队列、订单、满意度和员工可选任务等信息。CLI 通过它了解餐厅状况。

**`ActionResult` 是一次操作的处理结果。** 至少包含：

| 字段 | 作用 |
|---|---|
| `accepted` | 操作是否被接受 |
| `message` | 成功信息或具体失败原因 |

比如玩家尝试把 C2 安排到已经有人坐的 T1，结果可以表达为：

- `accepted` 为 false。
- `message` 表示 T1 已被占用。
- 队列、桌子、顾客状态和员工额度都保持原样。
- CLI 显示原因，再让玩家选择。

---

最后，把这些类放进一次真实的服务过程里看。

第一回合，玩家选择“让 C1 坐到 T1”，调用过程是：

**RestaurantCLI → GameController.submitWaiterAction → Waiter.execute → Restaurant.seatCustomer → Table 和 Customer**

这条调用链中：

1. CLI 收集 C1、T1，形成入座请求。
2. GameController 检查目前是否处于 Waiter 操作阶段。
3. Waiter 检查本回合是否还有额度。
4. Restaurant 检查 C1 是否仍在等待、T1 是否空闲。
5. 条件全部满足后，协调移出队列、占用桌子和改变顾客状态。
6. 操作成功，Waiter 记录额度已使用，控制器进入 Chef 阶段。
7. CLI 显示更新后的状态。

接下来，只跟踪 C1 点 Pasta 的过程：

| 回合 | 主要操作 | 关键变化 |
|---|---|---|
| 1 | Waiter 安排 C1 入座 | Table 被占用；Customer 进入 SEATED；回合末满意度 92 |
| 2 | Waiter 点单；Chef 做第一单位 | 创建 Pasta Order；Customer 等食物；满意度 84 |
| 3 | Chef 做第二单位 | Order 变为 READY，记录 `readyTurn = 3`；满意度 76 |
| 4 | Waiter 上菜 | Order 变为 SERVED；Customer 进入 EATING；满意度保持 76 |
| 5 | 自动处理用餐 | Customer 进入 READY_TO_PAY，记录 `readyToPayTurn = 5` |
| 6 | Cashier 收款 | Customer 和 Order 变为 PAID；记录满意度 76；释放桌子；收入增加 $18 |

这段过程展示了各类的配合：控制器推进时间，员工执行有限的任务，Restaurant 协调关联变化，实体保存各自的状态与记录。

当前草图还省略了枚举框、构造方法、常规查询方法，以及部分完整参数类型。后续你们落实设计时，需要把这些细节与实际类结构同步；Stage 2 的继承和多态关系则在加入 VIP、Critic 时继续扩展。

<oai-mem-citation>
<citation_entries>
MEMORY.md:209-216|note=[design scope responsibility boundaries and temporal rules checked against current files]
</citation_entries>
<rollout_ids>
01a171ac-4ad0-7053-a39a-a46a3a3234c7
</rollout_ids>
</oai-mem-citation>
<!-- original-text-end:chat-02-m022 -->

<!-- transcript-message-end:chat-02-m022 -->
