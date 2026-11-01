---
navigation:
  title: 循环样板：钯处理
  parent: index.md
  icon: gregtech:gt.blockmachines:22182
  keywords:
    - 循环样板
    - 配平
    - 种子
    - 概率样板
    - 钯
item_ids:
  - "<gregtech:gt.blockmachines:22182>"
---

# 循环样板：钯处理

<ItemLink id="gregtech:gt.blockmachines:22182" showIcon="left" /> 把一组样板当作一个循环执行：组里一部分物品既是原料又是产物，总成算出每张样板各运行几次，把整轮循环合成一张样板发布给 AE。下单时 AE 提供这张样板上的净原料和编程器电路，循环里流转的物品由总成保存的种子提供。

贵金属处理线产出的富钯氨是这条钯处理循环的净输入，氨用于加氨回收，钯金属粉作为中间物在两条路线间循环。钯处理同时用到循环和概率产出：<ItemLink id="bartworks:gt.bwMetaGenerateddust:55" showIcon="left" /> 和 <ItemLink id='ae2fc:fluid_drop:0:{Fluid:"palladium enriched ammonia",DisplayOnly:1b}' showIcon="left" /> 在线内产出后立即用掉，<ItemLink id="bartworks:gt.bwMetaGenerateddust:53" showIcon="left" /> 由筛选机按概率产出，再送回反应釜。

## 基本概念

- **循环物品**：在同一组样板里既被消耗又被产出的物品。
- **配平**：每张样板的运行次数，使循环物品的投入和产出相抵。配平写成一张样板发布，列出净原料、净产物及执行所需的编程器电路。
- **种子**：一轮循环开始时投入的循环物品。总成保存种子库存，产物进入 ME 时收回本轮投入的种子。
- **概率样板**：产物写成期望数量的样板。掷出的产物少于样板上写的数量时，<ItemLink id="programmablehatches:chance_controller" showIcon="left" /> 补充原料继续运行，直到产出样板上写的数量。

## 物料流向

<Flowchart align="center" width="340" height="358" canvasWidth="328" canvasHeight="348">
  <FlowNode id="enriched" x="52" y="98" size="72" shape="circle" color="#95cc75" textColor="#28352c" label="富钯氨" item='ae2fc:fluid_drop:0:{Fluid:"palladium enriched ammonia",DisplayOnly:1b}' />
  <FlowNode id="enrich" x="164" y="98" size="76" shape="rect" color="#777f87" textColor="#ffffff" label="化学反应釜|加氨回收 ×6" item='gregtech:gt.blockmachines:1169' />
  <FlowNode id="ammonia" x="276" y="98" size="72" shape="circle" color="#95cc75" textColor="#28352c" label="氨" item='ae2fc:fluid_drop:0:{Fluid:"ammonia",DisplayOnly:1b}' />
  <FlowNode id="react" x="52" y="198" size="76" shape="rect" color="#777f87" textColor="#ffffff" label="化学反应釜|沉淀 ×1" item='gregtech:gt.blockmachines:1169' />
  <FlowNode id="powder" x="164" y="198" size="72" shape="circle" color="#c93838" textColor="#ffffff" label="钯金属粉" item='bartworks:gt.bwMetaGenerateddust:53' />
  <FlowNode id="me" x="52" y="22" size="32" shape="rect" color="#e0e4e8" textColor="#343a40" label="上游" />
  <FlowNode id="raw" x="52" y="298" size="72" shape="circle" color="#e0c675" textColor="#343a40" label="再沉淀|钯粉" item='bartworks:gt.bwMetaGenerateddust:54' />
  <FlowNode id="salt" x="164" y="298" size="72" shape="circle" color="#d5d8db" textColor="#343a40" label="钯盐粉" item='bartworks:gt.bwMetaGenerateddust:55' />
  <FlowNode id="sift" x="276" y="298" size="76" shape="rect" color="#777f87" textColor="#ffffff" label="筛选机|筛选 ×1" item='gregtech:gt.blockmachines:15542' />
  <FlowEdge from="ammonia" to="enrich" label="6k L" color="#7f8994" dashed="true" labelX="0" labelY="-9" />
  <FlowEdge from="powder" to="enrich" label="6" color="#578d45" dashed="false" labelX="8" labelY="0" />
  <FlowEdge from="enrich" to="enriched" label="6k L" color="#578d45" dashed="false" labelX="0" labelY="-9" />
  <FlowEdge from="enriched" to="react" label="9k L" color="#578d45" dashed="false" labelX="0" labelY="0" />
  <FlowEdge from="powder" to="react" label="9" color="#b47935" dashed="false" labelX="0" labelY="-7" />
  <FlowEdge from="react" to="salt" label="16" color="#b47935" dashed="false" labelX="8" labelY="-5" />
  <FlowEdge from="salt" to="sift" label="16" color="#b47935" dashed="false" labelX="0" labelY="-8" />
  <FlowEdge from="sift" to="powder" label="15" color="#b47935" dashed="false" labelX="7" labelY="-4" />
  <FlowEdge from="react" to="raw" label="2" color="#9b792e" dashed="false" labelX="8" labelY="0" />
  <FlowEdge from="me" to="enriched" label="3k L" color="#7f8994" dashed="true" labelX="19" labelY="0" />
</Flowchart>

图表初始显示全图，滚轮可缩放，拖动可平移，双击可恢复全图。悬停在物料节点上可查看提示。连线上的数字是一轮的数量。

**钯金属粉有两个去向，对应两条循环：**

- **沉淀循环**（橙色）：钯金属粉和富钯氨经过**沉淀**，产出再沉淀钯粉和钯盐粉；钯盐粉经过**筛选**，重新产出钯金属粉。
- **加氨回收循环**（绿色）：另一部分钯金属粉与氨进行**加氨回收**，重新变为富钯氨，回到沉淀步骤。

沉淀与筛选这一路每轮消耗 9 个、返回 15 个钯金属粉，净增加 6 个。**加氨回收为增殖的钯金属粉提供回流去向**，把它重新用于富钯氨的生产。

循环样板输入总成自动计算三个样板的执行次数，把钯金属粉分配到这两条路线，并将净多余量控制在设定容差内，从而减少积压和浪费。每轮筛选返回 15 个钯金属粉，其中 9 个用于沉淀，6 个用于加氨回收。回收得到的 6000 L 富钯氨与上游补入的 3000 L 富钯氨合并，供沉淀消耗。每轮另需 6000 L 氨，金色的再沉淀钯粉是净产物。

## 三张样板

| 样板 | 机器 | 原料 | 产物 |
| --- | --- | --- | --- |
| 沉淀 | <ItemLink id="gregtech:gt.blockmachines:1169" showIcon="left" />，电路 9 | 9 <ItemLink id="bartworks:gt.bwMetaGenerateddust:53" showIcon="left" />、9000 L <ItemLink id='ae2fc:fluid_drop:0:{Fluid:"palladium enriched ammonia",DisplayOnly:1b}' showIcon="left" /> | 16 <ItemLink id="bartworks:gt.bwMetaGenerateddust:55" showIcon="left" />、2 <ItemLink id="bartworks:gt.bwMetaGenerateddust:54" showIcon="left" /> |
| **加氨回收** | <ItemLink id="gregtech:gt.blockmachines:1169" showIcon="left" />，电路 1 | 1 <ItemLink id="bartworks:gt.bwMetaGenerateddust:53" showIcon="left" />、1000 L <ItemLink id='ae2fc:fluid_drop:0:{Fluid:"ammonia",DisplayOnly:1b}' showIcon="left" /> | 1000 L <ItemLink id='ae2fc:fluid_drop:0:{Fluid:"palladium enriched ammonia",DisplayOnly:1b}' showIcon="left" /> |
| 筛选 | <ItemLink id="gregtech:gt.blockmachines:15542" showIcon="left" />，概率样板 | 16 <ItemLink id="bartworks:gt.bwMetaGenerateddust:55" showIcon="left" /> | 15 <ItemLink id="bartworks:gt.bwMetaGenerateddust:53" showIcon="left" /> |

<RecipeFor id="bartworks:gt.bwMetaGenerateddust:55" handlerId="gt.recipe.largechemicalreactor" input="bartworks:gt.bwMetaGenerateddust:53" fallbackText="沉淀配方：9 个钯金属粉和 9000 L 富钯氨，电路 9。" />

<RecipeFor id="bartworks:gt.bwMetaGenerateddust:53" handlerId="gt.recipe.sifter" fallbackText="筛选配方：1 个钯盐粉按概率产出钯金属粉。" />

**概率样板的产出填写略低于实际期望的保守数量。** 筛选机每个钯盐粉期望产出 0.95 个钯金属粉，16 个钯盐粉期望产出 15.2 个，因此筛选样板填写 15 个。

循环总成按样板承诺的 15 个进行配平：随机产出不足 15 个时，概率样板控制器先用存款抵扣，剩余缺口通过补货继续筛选补齐；超过承诺数量的部分存入控制器，抵扣后续轮次。这样，已发布的循环按固定数量归还金属粉，多产部分单独记账并继续利用。

总成算出的配平是：加氨回收样板运行 6 次，沉淀和筛选样板各运行 1 次。发布的样板是 3000 L 富钯氨和 6000 L 氨合成 2 个 <ItemLink id="bartworks:gt.bwMetaGenerateddust:54" showIcon="left" />。

编程器电路作为执行输入，由 AE 库存或电路提供器供应。总成计算物料配平时排除它们，再按各样板的执行次数汇总进发布样板：每轮要求配置 9 的编程器电路 1 个、配置 1 的编程器电路 6 个；合成倍率同时放大这些数量。总成收到完整输入后，将电路配置写入各自的配方缓冲。

## 准备

- <ItemLink id="gregtech:gt.blockmachines:1169" showIcon="left" />：装 <ItemLink id="gregtech:gt.blockmachines:22182" showIcon="left" />、<ItemLink id="gregtech:gt.blockmachines:2710" showIcon="left" /> 和 <ItemLink id="gregtech:gt.blockmachines:2713" showIcon="left" />。富钯氨经 ME 输出仓回到网络，总成在网络入口收回它。
- <ItemLink id="gregtech:gt.blockmachines:15542" showIcon="left" />：装 <ItemLink id="gregtech:gt.blockmachines:22069" showIcon="left" /> 和 ME 输出总线。
- 两台机器的输入总成、输出总线和输出仓接在同一个 ME 网络上，网络上放一台 <ItemLink id="programmablehatches:chance_controller" showIcon="left" />。
- <ItemLink id="gregtech:gt.blockmachines:22048" showIcon="left" />：接入同一个 ME 网络，在提供器里分别放置配置为 1 和 9 的集成电路模板，供应两种编程器电路。
- 工具：<ItemLink id="programmablehatches:prog_toolkit" showIcon="left" />、<ItemLink id="programmablehatches:chance_toolkit:1" showIcon="left" /> 和 <ItemLink id="gregtech:gt.metaitem.01:32708" showIcon="left" />。

下面是游戏内测试搭建的整条产线。场景先显示总览，按播放键按步骤查看：ME 网络、反应釜、反应釜的仓室、筛选机、筛选机的仓室、接入网络。仓室通过 Fluix 色 ME 玻璃线缆接入网络。测试使用自供电 ME 控制器，实际搭建时为 ME 网络和两台机器配置供电。

<GameScene width="277" height="198" zoom={1.4} interactive={true}>
  <ImportStructure src="/assets/palladium_line.snbt" formed={true} />
  <ImportPonder src="/assets/palladium_line_ponder.json" />
  <MUI2Gui x="1" y="1" z="7" from="620" to="1480" scale="2">
    <MUI2Click at="680" target="open_patterns" />
    <MUI2Click at="880" target="cyclic_lanes_tab" />
    <MUI2Click at="980" target="cyclic_lane_4" />
    <MUI2Click at="1060" target="cyclic_bind_tab" />
    <MUI2Click at="1180" target="cyclic_bind_1" x="1" y="1" z="2" />
    <MUI2Click at="1260" target="cyclic_lanes_tab" />
    <MUI2Click at="1360" target="cyclic_config_tab" />
  </MUI2Gui>
</GameScene>

搭建演示之后，高亮筛选机输入总成，显示数据棒图标和“左键点击记录坐标”；记录完成后，主窗口打开，演示指针点击加号进入样板窗口。沉淀、筛选和加氨回收样板依次进入同一组；指针把筛选样板从车道 0 调到蓝色车道 1，再拿起数据棒点击绑定格，将车道 1 绑定到筛选机。拖动时间轴可查看各阶段；鼠标悬停可查看提示，按住 Shift 可查看样板详情。窗口操作由演示自动完成。

## 第一步：编码样板

1. 带着编程工具箱 MK.II，用 Shift+右键切到常规模式，在样板终端用 NEI 一键编写加氨回收和沉淀两张样板。配方里的电路板写进样板，成为编程器电路。循环样板输入总成按样板里的编程器电路为每次运行设置电路，两张样板共用一台反应釜。
2. 带着概率样板工具箱，用 Shift+右键切到“编码时标记样板”，编码筛选样板：原料 16 个钯盐粉，产物 15 个钯金属粉。编好的样板提示里显示“概率样板：由概率样板控制器补货”。

## 第二步：放入样板

打开循环样板输入总成，点右上方的 <FloatingImage src="gregtech:textures/gui/overlay_button/plus_large.png" x="0" y="0" width="18" height="18" displayWidth="9" wrap="inline" title="添加样板" /> 按钮打开样板窗口。

把三张样板放进第一列。样板窗口的 36 格按列分成四组，默认每组 9 格，第一列就是第 1 组。同一组的样板作为一个循环分析。

## 第三步：把筛选样板交给筛选机

循环样板输入总成通过车道把样板分给其他机器。车道 0 是总成自己，车道 1 到 8 各绑定一个其他机器上的输入总成。

1. 手持数据棒左键大型筛选机上的编程样板输入总成，记录目标位置。
2. 打开“机器分配”页 <ItemImage id="gregtech:gt.metaitem.01:32630" />。默认车道为 0；在筛选样板格上滚动滚轮，或用左右键调整车道。左键点击一次将其切到车道 1，背景变为蓝色。沉淀和加氨回收样板保持车道 0。
3. 打开“车道绑定”页 <ItemImage id="gregtech:gt.metaitem.01:32708" />，拿起记录了位置的数据棒，点击车道 1 的绑定格。绑定完成后显示目标图标和连接状态。

## 第四步：设置循环物品

打开“配平”页 <FloatingImage src="gregtech:textures/gui/overlay_button/cyclic.png" x="0" y="0" width="32" height="32" displayWidth="9" wrap="inline" title="配平" />，第 1 组列出三种循环物品。每行左下角的开关是“作为产物”，右下角的开关是“作为输入”：

| 循环物品 | 作为产物 | 作为输入 |
| --- | --- | --- |
| <ItemLink id="bartworks:gt.bwMetaGenerateddust:55" showIcon="left" /> | <FloatingImage src="gregtech:textures/gui/overlay_button/cross.png" x="0" y="0" width="32" height="32" displayWidth="9" wrap="inline" title="否" /> 否 | <FloatingImage src="gregtech:textures/gui/overlay_button/cross.png" x="0" y="0" width="32" height="32" displayWidth="9" wrap="inline" title="否" /> 否 |
| <ItemLink id='ae2fc:fluid_drop:0:{Fluid:"palladium enriched ammonia",DisplayOnly:1b}' showIcon="left" /> | <FloatingImage src="gregtech:textures/gui/overlay_button/cross.png" x="0" y="0" width="32" height="32" displayWidth="9" wrap="inline" title="否" /> 否 | <FloatingImage src="gregtech:textures/gui/overlay_button/checkmark.png" x="0" y="0" width="32" height="32" displayWidth="9" wrap="inline" title="是" /> 是 |
| <ItemLink id="bartworks:gt.bwMetaGenerateddust:53" showIcon="left" /> | <FloatingImage src="gregtech:textures/gui/overlay_button/cross.png" x="0" y="0" width="32" height="32" displayWidth="9" wrap="inline" title="否" /> 否 | <FloatingImage src="gregtech:textures/gui/overlay_button/cross.png" x="0" y="0" width="32" height="32" displayWidth="9" wrap="inline" title="否" /> 否 |

- 作为产物为否的物品是中间物，一轮里的净多余量受容差限制。
- 作为输入为是的物品，一轮里的净消耗量写进发布的样板，每次合成从 ME 取用。
- 再沉淀钯粉只被产出，自动作为产物。

**钯金属粉作为中间物，容差设为 0%**。配平结果为沉淀 1 次、加氨回收 6 次、筛选 1 次：金属粉投入 9 + 6 = 15 个，筛选返回 15 个，净消耗与净多余量都为 0。每轮净消耗 3000 L 富钯氨和 6000 L 氨，产出 2 个再沉淀钯粉。

这组样板在金属粉容差为 5% 时也得到同样的最小配平；0% 明确要求金属粉完全闭合。概率筛选的多产部分存入概率样板控制器，用于抵扣后续轮次的产出需求。

悬停组按钮或组标题查看配平和警告。设置完成后显示一条配平：3000 L 富钯氨和 6000 L 氨合成 2 个再沉淀钯粉。

## 第五步：放入种子

自动配置种子容量默认开启：保有量自动调到配平所需的数量，配平随即发布，AE 里出现再沉淀钯粉的合成样板。运行概率样板的配平按概率种子倍率保留种子，默认是一次合成所需种子的 4 倍：

| 循环物品 | 一次合成所需 | 保有量 |
| --- | --- | --- |
| <ItemLink id="bartworks:gt.bwMetaGenerateddust:53" showIcon="left" /> | 15 | 60 |
| <ItemLink id="bartworks:gt.bwMetaGenerateddust:55" showIcon="left" /> | 16 | 64 |
| <ItemLink id='ae2fc:fluid_drop:0:{Fluid:"palladium enriched ammonia",DisplayOnly:1b}' showIcon="left" /> | 6000 L | 24000 L |

把这些种子存进 ME 网络，在“配置”页 <ItemImage id="gregtech:gt.metaitem.02:31032" /> 点“调整库存”，总成按保有量从 ME 取出种子。也可以把种子直接放进配平页每行右上角的种子输入格。种子库存低于一次合成所需时，合成任务等库存补足后再推送。

> [!TIP]
> 概率种子倍率在配置页调整，最小为 1。保留的种子越多，能同时进行的合成越多。

## 第六步：下单

在 AE 里下单再沉淀钯粉，每次合成从 ME 取 3000 L 富钯氨、6000 L 氨、配置 9 的编程器电路 1 个和配置 1 的编程器电路 6 个。

1. 总成从种子库存取出一轮所需的循环物品，连同 AE 送来的原料，按配平分给反应釜和筛选机。
2. 产物进入 ME 时，总成收回本轮投入的种子，再沉淀钯粉交给 AE 的合成任务。
3. 筛选机产出的钯金属粉少于 15 个时，概率样板控制器从 ME 提取钯盐粉补给筛选机，直到产出 15 个。控制器默认的补货模式是“仅用库存”，ME 里留一些钯盐粉供补货。
4. 多产的钯金属粉存进概率样板控制器，抵后续轮次的期望数量。控制器界面的“退回存款”把存款放回 ME。

## 检查

悬停“配平”页的组按钮查看这一组的配平和警告：

- **所需种子超过保有量，未发布**：开启自动配置种子容量，或调高保有量。
- **库存低于需求**：点“调整库存”，或在种子输入格放入种子。
- **车道需要绑定可用的目标仓**：用闪存重新绑定车道。
- **这些样板无法组成能净产出它的循环**：检查各循环物品的作为产物和作为输入设置。
