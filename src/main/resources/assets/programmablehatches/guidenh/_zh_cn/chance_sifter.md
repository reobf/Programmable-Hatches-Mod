---
navigation:
  title: "概率样板：沙砾筛燧石"
  parent: index.md
  icon: programmablehatches:chance_controller
  keywords:
    - 概率样板
    - 自动补货
    - 大型筛选机
    - 沙砾
    - 燧石
    - 存款
item_ids:
  - "<programmablehatches:chance_controller>"
  - "<programmablehatches:chance_toolkit:0>"
  - "<programmablehatches:chance_toolkit:1>"
  - "<gregtech:gt.blockmachines:15542>"
---

# 概率样板：沙砾筛燧石

用 <ItemLink id="gregtech:gt.blockmachines:15542" showIcon="left" /> 筛选 <ItemLink id="minecraft:gravel" showIcon="left" />，通过 <ItemLink id="programmablehatches:chance_controller" showIcon="left" /> 为概率样板补充原料，直到凑齐样板承诺的燧石数量。多出的燧石进入控制器存款，用于后续缺口。

## 配方概率与样板数量

<RecipeFor id="minecraft:flint" handlerId="gt.recipe.sifter" input="minecraft:gravel" fallbackText="每份沙砾有六个燧石输出槽，概率为 100%、90%、80%、60%、33%、25%。" />

每份沙砾的六个输出槽分别按 100%、90%、80%、60%、33%、25% 判定，平均产出 3.88 个燧石。4 份沙砾的真实期望是 15.52 个，本教程把样板写成：

4 沙砾 → 15 燧石

样板承诺的产出略低于实际期望。某批少于承诺量时，控制器补充原料继续筛选；多出的部分存为存款，供后续出现缺口的缓存优先使用。

## 准备与接线

- 大型筛选机安装 <ItemLink id="gregtech:gt.blockmachines:22069" showIcon="left" /> 和 ME 输出总线，并按结构要求配置能源仓、维护仓和消声器。
- 输入总成、ME 输出总线和概率样板控制器接在同一个 ME 网络上，为网络准备供电与频道。
- 网络内放置合成 CPU、存储元件，以及一批沙砾。自动补货会使用初次下单以外的沙砾。
- 携带 <ItemLink id="programmablehatches:chance_toolkit:1" showIcon="left" />，Shift+右键切换到“编码时标记样板”。

## 编码和安装概率样板

1. 在 ME 样板终端创建加工样板，设置输入为 4 沙砾、输出为 15 燧石，并在携带已启用的概率样板工具箱时编码。
2. 查看样板提示，确认它带有“概率样板：由概率样板控制器补货”标记。
3. 打开样板输入总成，点击 + 打开样板窗口，把样板放入槽位。
4. 在 ME 终端下单 15 个燧石。AE 向输入总成发送 4 份沙砾，缓存同时记下 15 个燧石的期望数量。

## 演示：少产、补货、存款

下面回放一段实际筛选过程。演示先暂停原料补货以便观察少产留下的缺口，再在控制器 GUI 中选择“仅用库存”并点击“应用”。新放置的控制器默认使用“仅用库存”。

按播放依次查看样板窗口、缓存窗口和控制器窗口。控制器出现后，可以观察模式按钮上的演示点击、期望数量、累计补货与存款的变化；鼠标悬停可查看物品和按钮说明。实际游戏中，每批筛选产量会随概率变化。

<GameScene width="277" height="198" zoom={1.4} rotateY={135} interactive={true}>
  <ImportStructure src="/assets/chance_sifter.snbt" formed={true} />
  <ImportPonder src="/assets/chance_sifter_ponder.json" autoplay={false} loop={false} />
  <MUI2Gui x="1" y="1" z="2" from="200" to="360" scale="2" showMain="true">
    <MUI2Click at="240" target="open_patterns" />
  </MUI2Gui>
  <MUI2Gui x="1" y="1" z="2" from="360" to="540" scale="2" showMain="true">
    <MUI2Click at="380" target="open_buffer_0" />
  </MUI2Gui>
  <MUI2Gui x="0" y="1" z="3" from="550" to="1020" scale="2" showMain="true">
    <MUI2Click at="640" target="chance_mode" />
    <MUI2Click at="660" target="chance_apply" />
  </MUI2Gui>
</GameScene>

演示使用自供电 ME 控制器、调试能源仓和创造维护仓。实际搭建时按机器要求配置正常供电与维护。

控制器按机器实际每次使用的原料量补货。本例每次筛选使用 1 份沙砾，因此补入 1 份沙砾在统计中相当于 0.25 份样板，按样板折算的期望产出是 3.75 个燧石。

## 看懂控制器状态

| 字段 | 含义 |
| --- | --- |
| 期望数量 | 相关缓存仍需通过概率产出补齐的数量 |
| 存款 | 已进入控制器、可用于后续缺口的多余产物 |
| 待收 | 已记录为多产、正在返回 ME 网络的产物 |
| 按样板计算的产出 | 已执行的加工次数按样板承诺比例折算的产出 |
| 实际产出 | 机器实际掷出的产物总量 |
| 按主控配方期望的产出 | 按真实配方概率计算的平均产出，用于对照实际结果 |
| 累计补货 | 追加原料折算的样板份数及其承诺产出 |

多产燧石作为控制器存款保存。其他概率缓存出现燧石缺口时，控制器先释放存款给等待中的合成任务，再为剩余缺口补原料。需要将存款放回普通 ME 库存时，使用“退回存款”。

## 补货模式

| 模式 | 原料来源 |
| --- | --- |
| 关闭 | 存款继续抵扣缺口，原料由玩家补充 |
| 仅用库存 | 从 ME 库存提取沙砾；适合本教程 |
| 仅合成 | 向 ME 合成系统下单原料，收到足够数量后补入缓存 |
| 优先库存，再合成 | 先用库存，再为剩余原料下单 |

选择模式后点击“应用”。使用涉及合成的模式时，准备原料合成样板和空闲合成 CPU，让补货订单可以执行。

## 检查配置

- 样板携带概率标记，放在样板输入总成的样板槽中。
- 控制器与输入总成、输出总线属于同一 ME 网络。
- 输出总线将实际产物送回网络，供合成 CPU 收取并让控制器接收多产。
- “仅用库存”模式有额外沙砾可用；涉及合成的模式有原料配方与空闲 CPU。
- 机器完成结构检查，具有能源和有效维护状态。

后续可以把概率样板接入 [钯处理循环](cyclic_patterns.md)，用相同的补货机制补齐循环中的概率产物。
