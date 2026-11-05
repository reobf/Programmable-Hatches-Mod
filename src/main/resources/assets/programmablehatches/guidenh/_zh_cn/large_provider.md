---
navigation:
  title: 大型编程器电路提供器
  parent: index.md
  icon: gregtech:gt.blockmachines:22116
  keywords:
    - 大提供器
    - 预构提供器
    - 提供器访问仓
    - 并行
    - 编程器电路
item_ids:
  - "<gregtech:gt.blockmachines:22116>"
  - "<gregtech:gt.blockmachines:22117>"
  - "<gregtech:gt.blockmachines:22143>"
  - "<gregtech:gt.blockmachines:22144>"
  - "<gregtech:gt.blockmachines:22145>"
  - "<gregtech:gt.blockmachines:22146>"
  - "<gregtech:gt.blockmachines:22148>"
  - "<programmablehatches:proghatches.chip>"
---

# 大型编程器电路提供器

<ItemLink id="gregtech:gt.blockmachines:22116" showIcon="left" /> 将多个提供器汇总到一个控制器上，统一向 ME 网络发布编程器电路合成。控制器只占一个频道，适合集中供应大量集成电路、模具、模头和透镜的编程器电路。

先了解 [普通编程器电路提供器与不消耗物品](programmed_inputs.md)。普通提供器保存真实模板；网络合成的是携带模板信息的编程器电路，模板留在提供器中。

## 从前往后搭建

机器的截面为 3×3，沿控制器背面方向延长。使用蓝图查看所选长度的完整结构。

1. 搭建正面：控制器在正面中央，正面外壳位置安装能源仓和维护仓。
2. 添加中间段：中央放福鲁伊克斯块，或用并行处理单元替换。
3. 在中间段与中央方块相邻的外壳位置安装提供器组件。
4. 完成尾部。尾部中央可以使用提供器组件；尾部采用中央提供器时，保持该层其余位置为外壳。
5. 将主 ME 网络连到控制器正面，为机器提供 EU 和维护。

嵌入结构的普通提供器由大提供器统一发布合成。它们的单机 ME 连接会关闭，接线集中在控制器上。

## 三种提供器组件

| 组件 | 放什么／接什么 | 用途 |
| --- | --- | --- |
| 普通编程器电路提供器 | 放入真实集成电路、模具或其他模板 | 自定义要供应的编程器电路 |
| 预构电路提供器 | 选择所需类别的预构组件 | 一次提供该类别预置的编程器电路 |
| 提供器访问仓 | 正面连接另一个提供器网络 | 将那个网络里的提供器汇总到大提供器 |

当前可用的预构类别：

- <ItemLink id="gregtech:gt.blockmachines:22143" showIcon="left" />：集成电路。
- <ItemLink id="gregtech:gt.blockmachines:22144" showIcon="left" />：模具。
- <ItemLink id="gregtech:gt.blockmachines:22145" showIcon="left" />：刀具。
- <ItemLink id="gregtech:gt.blockmachines:22146" showIcon="left" />：模头。
- <ItemLink id="gregtech:gt.blockmachines:22148" showIcon="left" />：透镜。

预构提供器作为大提供器的结构组件使用。普通提供器适合补充预构类别以外的模板。

## 演示：搭建、汇总和设置数量

场景从分阶段搭建开始，随后打开普通提供器 GUI 查看铸锭模具。右侧预构组件供应模头；访问仓连接独立子网，子网中的普通提供器保存板模具。

最后打开大提供器主 GUI，点击并行按钮，在弹窗中将单次提供数量设为 4。演示随后展示升级芯片和闲置编程器电路清理按钮。

<GameScene width="277" height="198" zoom={1.6} rotateY={135} interactive={true}>
  <ImportStructure src="/assets/large_provider.snbt" formed={true} />
  <ImportPonder src="/assets/large_provider_ponder.json" autoplay={false} loop={false} />
  <MUI2Gui x="2" y="1" z="3" from="420" to="500" scale="2" showMain="true" />
  <MUI2Gui x="3" y="1" z="2" from="660" to="820" scale="2" showMain="true">
    <MUI2Click at="720" target="lpcp_open_parallel" />
    <MUI2Click at="750" target="lpcp_multiplier" value="4" />
  </MUI2Gui>
  <MUI2Gui x="3" y="1" z="2" from="920" to="1000" scale="2" showMain="true">
    <MUI2Click at="950" target="lpcp_cleanup" />
  </MUI2Gui>
</GameScene>

演示使用调试能源仓、创造维护仓和自供电 ME 控制器。实际搭建使用满足功率和维护要求的设备。

## 提供器访问仓如何接线

<ItemLink id="gregtech:gt.blockmachines:22117" showIcon="left" /> 放在大提供器结构中，其正面连接提供器所在的子网；大提供器控制器连接需要使用这些编程器电路的主网。子网要有供电，并为其中的普通提供器和访问仓提供频道。

可以把常用模板集中保存在一个提供器网络，再由访问仓读取。保持访问关系为单向：主网读取模板子网，模板子网中的提供器沿既定方向提供模板。访问链形成环时，控制器会报“检测到环”；检查访问仓与控制器的网络归属并断开回路。

## 并行处理单元与单次提供数量

并行处理单元增加允许设置的单次提供数量上限。打开控制器 GUI 的并行弹窗，填写所需数量；服务器会按当前结构限制数值。

该数值决定每份发布样板生成多少个编程器电路。例如设为 4 后，一份提供器样板生成 4 个。它与下游加工机器的并行数分别配置。

本场景从控制器下单 8 个铸锭模具编程器电路，使用每份 4 个的样板完成订单；普通提供器中的真实模具仍然保留。

## 功率、升级芯片与库存清理

每个提供器或访问仓组件需要 256 EU/t。场景有三个组件，因此控制器运行功率为 768 EU/t。增加组件后，按新的功率需求配置能源仓。

把 <ItemLink id="programmablehatches:proghatches.chip" showIcon="left" /> 放入控制器的物品槽，机器会安装并消耗芯片，启用瞬时完成支持。单次提供数量仍按结构和 GUI 设置执行。

“移除网络中未使用的编程器电路”按钮会定期清理连接网络中库存的编程器电路。适合将这类物品按需生成的网络；需要保留手动储备时，保持该选项关闭。真实模板保存在普通提供器内。
