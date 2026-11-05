---
navigation:
  title: "库存控制：定量补料与按库存开机"
  parent: index.md
  icon: programmablehatches:amountmaintainer
  keywords:
    - "库存控制：定量补料与按库存开机"
    - "数量维持总线"
    - "发信控制覆盖板"
    - "限制存储输入"
    - "优先级过滤"
    - "Amount Maintainer"
    - "restricted"
item_ids:
  - "<programmablehatches:amountmaintainer>"
  - "<programmablehatches:proghatches.cover:100>"
  - "<gregtech:gt.blockmachines:22163>"
  - "<gregtech:gt.blockmachines:22164>"
  - "<gregtech:gt.blockmachines:22173>"
  - "<gregtech:gt.blockmachines:22174>"
---

# 库存控制：定量补料与按库存开机

数量维持总线把普通容器中的指定物品或流体维持在目标数量。发信控制覆盖板根据 ME 库存控制机器运行；限制存储输入仓则控制机器一次能够看到多少材料。

## 示例：给机器旁的箱子维持 64 个铁锭

1. 给箱子贴数量维持总线，将总线接入有铁锭库存的 ME 网络。
2. 在配置槽标记铁锭，把期望数量设为 64。
3. 从箱子取走 16 个，观察总线补回 64 个。
4. 在箱子中放入其他物品，确认它们保留；总线只调整已标记的类型。
5. 按物流需要设置更新间隔。先从较慢更新开始，确认补料速度后调整。

需要维持流体时，用流体容器标记，并将标记模式切换成“识别为流体”；识别为物品时，维持的是容器本身。

<Flowchart width="300" height="125" canvasWidth="340" canvasHeight="125">
  <FlowNode id="n0" x="52" y="51" size="72" shape="rect" color="#657b91" label="ME 库存" />
  <FlowNode id="n1" x="166" y="51" size="72" shape="rect" color="#657b91" label="数量维持" />
  <FlowNode id="n2" x="280" y="51" size="72" shape="rect" color="#657b91" label="目标容器" />
  <FlowEdge from="n0" to="n1" />
  <FlowEdge from="n1" to="n2" />
</Flowchart>

## 实际回放：修改数量、补齐和退回

回放先打开数量维持总线的 GUI，把目标从 16 改成 64；从箱子取走 16 个后自动补齐，额外放入 16 个后把超出部分退回 ME。箱子里另外放了 3 个金锭，它们在整个过程中始终保留。

<GameScene width="277" height="198" zoom={1.6} rotateY={135} interactive={true}>
  <ImportStructure src="/assets/stock_control.snbt" formed={true} />
  <ImportPonder src="/assets/stock_control_ponder.json" autoplay={false} loop={false} />
  <MUI2Gui x="1" y="1" z="1" part="EAST" from="180" to="340" scale="2" showMain="true">
    <MUI2Click at="220" target="maintainer_amount" value="64" />
  </MUI2Gui>
</GameScene>

## 库存不足时自动合成

安装合成卡，并为目标材料准备合成配方和空闲 CPU。总线可通过合成补足缺失材料。先用一个小目标数量验证合成完成、产物入网和补入容器的完整过程。

安装红石卡后可以选择常开、常关、信号控制、反转或边沿触发。也可用绑定到红石 P2P 的内存卡指定远端控制信号；同时核对“维持”与“清空”模式，以及 P2P 离线时的行为。

## 按 ME 库存开关机器

将发信控制覆盖板安装在目标机器上，配置要检测的材料、数量阈值和“大于等于／小于等于”条件。覆盖板优先检测机器自身连接的网络，或通过机器上的 ME 覆盖板访问网络。

例如维持一批常用金属板：库存低于设定阈值时允许机器工作，达到库存目标后停止继续生产。先调整少量库存，观察开关方向是否符合预期。

## 限制一次拉取多少材料

限制存储输入仓和总线提供数量下限、上限和倍数选项。下限用于材料足够成批时再暴露输入；上限限制可见数量；倍数模式可按下限的整数倍或二次幂倍取整。

先用每次需要 4 个材料的配方演示：下限设 4，检查库存为 3、4、7、8 时机器能看到多少材料。刷新按钮用于查询当前结果，机器实际执行时会再次访问库存。

优先级过滤存储输入仓／总线适合把输入限制在指定优先级的存储中。给子网存储总线设置清晰的优先级，再在输入仓选择对应值。
