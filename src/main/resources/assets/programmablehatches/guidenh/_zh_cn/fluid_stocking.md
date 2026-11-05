---
navigation:
  title: "ME 流体库存升级：高炉保护气"
  parent: index.md
  icon: programmablehatches:prog_upgrades:2
  keywords:
    - ME流体库存升级
    - 流体拉取
    - 高炉
    - 保护气
    - 氮气
    - 氩气
item_ids:
  - "<programmablehatches:prog_upgrades:2>"
---

# ME 流体库存升级：高炉保护气

给高炉的二合一输入仓安装 <ItemLink id="programmablehatches:prog_upgrades:2" showIcon="left" />，让仓室直接使用 ME 库存中的保护气。加工样板只记录固体原料和需要的编程器电路；气体由共享槽单独配置。

更换保护气时，修改仓室里的流体标记即可。兼容新保护气的加工样板可以继续使用，后续加工按新标记的气体匹配配方。

## 示例：磷掺杂硅晶体

本例使用高炉中的磷掺杂硅晶体配方。两种保护气配方使用相同的固体材料与 3 号电路，线圈温度要求为 2484 K。

| 配置 | 固体输入 | 保护气用量 | 基础时间 | 输出 |
| --- | --- | --- | --- | --- |
| 氮气 | 64 太阳能级硅粉、2 小型砷化镓晶体、8 磷粉；电路 3 | 8000 L 氮气 | 600 秒 | 1 磷掺杂硅晶体 |
| 氩气 | 相同 | 6800 L 氩气 | 480 秒 | 相同 |

<ItemLink id='ae2fc:fluid_drop:0:{Fluid:"nitrogen",DisplayOnly:1b}' showIcon="left" /> 与 <ItemLink id='ae2fc:fluid_drop:0:{Fluid:"argon",DisplayOnly:1b}' showIcon="left" /> 的用量和时间来自各自的机器配方。实际运行时间还受高炉电压、线圈和超频影响。

## 样板怎么写

加工样板的输入写为 64 <ItemLink id="gregtech:gt.metaitem.01:2856" showIcon="left" />、2 <ItemLink id="gregtech:gt.metaitem.03:32191" showIcon="left" />、8 <ItemLink id="gregtech:gt.metaitem.01:2021" showIcon="left" />，以及携带 3 号集成电路的编程器电路；输出写为 1 <ItemLink id="gregtech:gt.metaitem.03:32031" showIcon="left" />。气体留给仓室的 ME 流体槽供给。

编程器电路仍是样板输入。把真实的 3 号集成电路放入编程电路提供器，网络即可为样板供应对应的编程器电路。配置方式见 [编程二合一输入仓](programmed_inputs.md)。

AE 合成计划据此准备固体材料和编程器电路。保护气需要提前存入同一 ME 网络，或由其他生产线持续补充；机器每次开始加工时，从网络扣除该配方实际需要的气体。

## 安装和配置

1. 将输入仓接入高炉。样板输入总成直接连接 ME 网络；使用普通编程二合一仓或缓冲仓时，让 ME 接口贴住仓室正面，仓室从这个接口所属的网络拉取气体。
2. 手持 ME 流体库存升级，右键输入仓安装。每个升级增加一个流体标记槽，各类库存升级合计最多安装 4 个。
3. 打开仓室主 GUI，点击电路图标，打开共享槽窗口。
4. 手持装有氮气的流体容器，左键点击左侧流体标记槽。它记录流体种类，容器仍留在手中；右侧槽显示 ME 中对应流体的库存。
5. 将上述加工样板放入样板窗口，确保网络内有氮气，然后下单磷掺杂硅晶体。

同一个标记槽由仓室的所有输入缓存共享。左侧显示的是流体标记，机器使用的气体数量由配方决定。

## 演示：一张样板，切换两种保护气

播放后依次查看升级安装、样板窗口和共享槽窗口。左侧标记从氮气切换为氩气，右侧显示对应库存及一次加工后的扣减。最后重新打开样板窗口，确认仍是同一张样板。鼠标悬停可以查看样板与流体说明。

<GameScene width="277" height="198" zoom={1.7} rotateY={135} interactive={true}>
  <ImportStructure src="/assets/fluid_stocking.snbt" formed={true} />
  <ImportPonder src="/assets/fluid_stocking_ponder.json" autoplay={false} loop={false} />
  <MUI2Gui x="2" y="0" z="2" from="150" to="260" scale="2" showMain="true">
    <MUI2Click at="200" target="open_patterns" />
  </MUI2Gui>
  <MUI2Gui x="2" y="0" z="2" from="260" to="800" scale="2" showMain="true">
    <MUI2Click at="270" target="open_shared" />
  </MUI2Gui>
  <MUI2Gui x="2" y="0" z="2" from="820" to="940" scale="2" showMain="true">
    <MUI2Click at="850" target="open_patterns" />
  </MUI2Gui>
</GameScene>

演示使用自供电 ME 控制器、调试能源仓与创造维护仓加快回放。实际搭建按高炉的温度、电压、能源和维护要求配置。

## 更换保护气

等当前加工完成后，打开共享槽窗口，空手左键清除氮气标记，再拿装有氩气的容器左键标记氩气。确认右侧已有氩气库存，即可继续下单；原有加工样板保持不变。

本例每次加工扣除 8000 L 氮气或 6800 L 氩气。切换到氩气后，网络里剩余的氮气继续保留。

## 使用条件

- 在 NEI 中确认目标配方支持所选保护气，而且固体输入、产物和电路编号相同。
- 高炉满足对应配方的温度和电压要求。
- 保护气储存在输入仓可访问的 ME 网络中。气体不足时，加工等待库存补足。
- 涉及电路编号变化的样板，仍按各自配方要求设置编程器电路。
- 多种配方共用一组标记时，为它们选择都支持的保护气；需要不同气体时，按配方分开配置输入仓。

这种配置让共用保护气集中在仓室中管理。更换兼容的保护气，只需修改仓室标记。
