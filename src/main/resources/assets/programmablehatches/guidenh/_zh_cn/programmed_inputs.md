---
navigation:
  title: 编程二合一仓：电路与模具
  parent: index.md
  icon: gregtech:gt.blockmachines:22033
  keywords:
    - 编程二合一
    - 缓冲
    - 编程器电路
    - 提供器
    - 模具
    - NC
    - 阻挡模式
item_ids:
  - "<gregtech:gt.blockmachines:22000>"
  - "<gregtech:gt.blockmachines:22001>"
  - "<gregtech:gt.blockmachines:22002>"
  - "<gregtech:gt.blockmachines:22003>"
  - "<gregtech:gt.blockmachines:22004>"
  - "<gregtech:gt.blockmachines:22005>"
  - "<gregtech:gt.blockmachines:22006>"
  - "<gregtech:gt.blockmachines:22007>"
  - "<gregtech:gt.blockmachines:22008>"
  - "<gregtech:gt.blockmachines:22009>"
  - "<gregtech:gt.blockmachines:22010>"
  - "<gregtech:gt.blockmachines:22011>"
  - "<gregtech:gt.blockmachines:22012>"
  - "<gregtech:gt.blockmachines:22013>"
  - "<gregtech:gt.blockmachines:22014>"
  - "<gregtech:gt.blockmachines:22032>"
  - "<gregtech:gt.blockmachines:22033>"
  - "<gregtech:gt.blockmachines:22034>"
  - "<gregtech:gt.blockmachines:22035>"
  - "<gregtech:gt.blockmachines:22036>"
  - "<gregtech:gt.blockmachines:22037>"
  - "<gregtech:gt.blockmachines:22038>"
  - "<gregtech:gt.blockmachines:22039>"
  - "<gregtech:gt.blockmachines:22040>"
  - "<gregtech:gt.blockmachines:22041>"
  - "<gregtech:gt.blockmachines:22042>"
  - "<gregtech:gt.blockmachines:22043>"
  - "<gregtech:gt.blockmachines:22044>"
  - "<gregtech:gt.blockmachines:22045>"
  - "<gregtech:gt.blockmachines:22046>"
  - "<gregtech:gt.blockmachines:22048>"
  - "<gregtech:gt.blockmachines:22118>"
---

# 编程二合一仓：电路与模具

<ItemLink id="gregtech:gt.blockmachines:22001" showIcon="left" /> 接收物品和流体，并用样板携带的编程器电路设置本次加工的虚拟配置。<ItemLink id="gregtech:gt.blockmachines:22033" showIcon="left" /> 进一步把整组输入收进配方缓存，保存材料数量和这组配方的虚拟配置。

## 接线与样板放在哪里

1. 把仓室装入支持二合一输入的 GregTech 多方块结构。让 ME 接口的输出面贴住仓室正面，流体配方使用流体兼容的 ME 接口。
2. **在 ME 接口中开启阻挡模式**，让接口按完整输入组发送材料。普通版和缓冲版都使用这个设置。
3. 加工样板放在 **ME 接口的样板槽**。编程二合一仓负责接收和处理输入。
4. 把 <ItemLink id="gregtech:gt.blockmachines:22048" showIcon="left" /> 接到同一 ME 网络，线缆接其正面，准备一个频道。把配方产物通过输出总线、输出仓接回网络，并为多方块与 ME 网络配置供电。

## 配方里的 NC 物品

NEI 中标有 **NC** 的输入用于选择或匹配配方，实际加工保留该物品。集成电路、挤压模具、铸模、透镜都可以采用编程器电路表示各自的配置。

以下用铁杆配方演示：

<RecipeFor id="gregtech:gt.metaitem.01:23032" handlerId="gt.recipe.extruder" input="gregtech:gt.metaitem.01:32351" fallbackText="1 个铁锭与挤压模具（杆）加工为 2 根铁杆，模具标记为 NC。" />

| 位置 | 放入什么 | 作用 |
| --- | --- | --- |
| 编程器电路提供器 | <ItemLink id='gregtech:gt.metaitem.01:32351' showIcon="left" /> 的真实物品模板 | 模板留在提供器，向 ME 提供对应编程器电路的合成 |
| ME 接口中的加工样板 | 1 <ItemLink id='minecraft:iron_ingot:0' showIcon="left" /> + 1 <ItemLink id='programmablehatches:prog_circuit:0:{targetCircuit:{Count:1b,Damage:32351s,string_id:"gregtech:gt.metaitem.01"}}' showIcon="left" /> → 2 <ItemLink id='gregtech:gt.metaitem.01:23032' showIcon="left" /> | 每份材料携带本次所需的模具配置 |
| 仓室的虚拟槽 | 挤压模具（杆）的配置 | 机器用它匹配配方的 NC 输入 |

提供器生成的是**记录物品与配置的编程器电路**。原模具留在提供器内，网络支付 AE 能量后取得电路；仓室消耗收到的电路，并写入虚拟槽。虚拟槽中的图标表示机器使用的配置。

集成电路同样处理：在提供器放入设置好号码的集成电路模板，样板输入使用对应号码的编程器电路。每一种需要的模具、透镜或电路号码各准备一个模板。

## 用编程工具箱编码

将 <ItemLink id="programmablehatches:prog_toolkit:1" showIcon="left" /> 放在背包中，设为常规模式，在 ME 样板终端使用 NEI 的一键编码功能。工具箱会把 NC 输入转换为对应的编程器电路。

编码完成后检查输入与产出：本例是 **1 铁锭 + 1 编程器电路（挤压模具·杆）→ 2 铁杆**。在提供器中配置相应模板后，AE 即可为合成任务供应电路。需要在无 NC 配方前清空上轮虚拟配置时，使用工具箱的“无 NC 物品时添加空编程器电路”模式。

## 普通仓：每份材料携带自己的配置

按播放查看：接线 → 打开提供器 GUI → 依次放入杆模具和板模具 → 提供电路时模板保留 → 安装样板 → 打开普通仓 GUI → 收到铁锭和电路 → 虚拟槽显示杆模具 → 加工消耗铁锭 → 下一张样板切换到板模具。鼠标悬停可查看模板和仓内物品的提示。

演示展示机器的输入端，仓室安装位置按对应多方块的结构要求确定。演示中的 ME 控制器自带供电。

<GameScene width="277" height="198" zoom={1.7} rotateY={135} interactive={true}>
  <ImportStructure src="/assets/programmed_inputs.snbt" />
  <ImportPonder src="/assets/programmed_inputs_ponder.json" autoplay={false} loop={false} />
  <MUI2Gui x="1" y="1" z="2" from="60" to="280" scale="2" showMain="true" />
  <MUI2Gui x="3" y="1" z="1" from="340" to="900" scale="2" showMain="true">

  </MUI2Gui>
</GameScene>

普通仓的一份输入使用同一套虚拟配置。接口的阻挡模式让上一份材料完成后再发送下一份；下一份编程器电路设置新的配置，因此同一台机器可以交替执行铁杆和铁板样板。

## 缓冲仓：输入区与配方缓存

缓冲版沿用相同的接口连接、阻挡模式、样板和提供器配置。它分成两个区域：

- **输入区**：接收接口发送的一组物品与流体。
- **配方缓存**：记录这一组的物品类型、数量、槽位顺序和流体，再保存材料与对应的虚拟配置。机器从缓存读取材料。

第一组材料进入缓存后，输入区空出，接口可以继续发送。后续与记录一致的输入累积进对应缓存，供机器按其处理能力与并行数消耗。具有多个缓存的型号分别保存各组配方与配置。

<GameScene width="277" height="198" zoom={1.7} rotateY={135} interactive={true}>
  <ImportStructure src="/assets/buffered_inputs.snbt" />
  <ImportPonder src="/assets/buffered_inputs_ponder.json" autoplay={false} loop={false} />
  <MUI2Gui x="1" y="1" z="2" from="60" to="280" scale="2" showMain="true" />
  <MUI2Gui x="3" y="1" z="1" from="340" to="900" scale="2" showMain="true">
    <MUI2Click at="500" target="open_buffer_0" />
  </MUI2Gui>
</GameScene>

演示先打开提供器 GUI 展示模具模板，再切到缓冲仓 GUI。第一份铁锭与编程器电路进入缓存后，主窗口的输入区清空。点击左上角的 **+** 打开 0 号缓存，可以看到铁锭及右侧的虚拟模具；第二份相同输入到达后，铁锭增加到 2 个，模具配置仍由这个缓存保存。

每份配方专用的电路、模具或透镜随样板发送到对应缓存。主窗口的共享虚拟配置适合所有缓存共同使用的条件。

## 检查配置

- 提供器已接入 ME 网络，拥有频道，槽内放置的是所需号码的集成电路或对应模具、透镜模板。
- 加工样板的 NC 输入已经转换为相应的编程器电路。
- ME 接口开启阻挡模式，输出面与仓室正面相接。
- 仓室保持编程器模式开启。
- 缓冲版通过 **+** 查看配方缓存中的材料与虚拟配置，主窗口用于检查待接收的输入。

[高炉保护气与 ME 流体库存升级](fluid_stocking.md)：把共用气体配置在仓室中，更换保护气时保留原有加工样板。
