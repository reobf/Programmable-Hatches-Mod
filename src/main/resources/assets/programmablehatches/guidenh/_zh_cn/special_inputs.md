---
navigation:
  title: "特殊输入：数据球、催化剂与虚拟物品"
  parent: index.md
  icon: gregtech:gt.blockmachines:22083
  keywords:
    - "特殊输入：数据球、催化剂与虚拟物品"
    - "数据球"
    - "催化剂"
    - "数据访问仓"
    - "多电路"
    - "虚拟输入"
    - "data orb"
    - "catalyst"
item_ids:
  - "<gregtech:gt.blockmachines:22083>"
  - "<programmablehatches:orbswitcher>"
  - "<gregtech:gt.blockmachines:22084>"
  - "<gregtech:gt.blockmachines:22162>"
  - "<gregtech:gt.blockmachines:22087>"
  - "<gregtech:gt.blockmachines:22150>"
  - "<programmablehatches:proghatches.cover:90>"
---

# 特殊输入：数据球、催化剂与虚拟物品

数据球、催化剂和固定不消耗物品需要不同的供给方式。先确定机器要求的是数据选择、真实物品替换，还是虚拟物品标记。

## 元素复制机切换数据球

<ItemLink id="gregtech:gt.blockmachines:22083" showIcon="left" /> 为大型元素复制机保存数据球，仓内电路槽的 1～16 号编程电路决定生效的格子。<ItemLink id="programmablehatches:orbswitcher" showIcon="left" /> 按输入仓收到的配置更换这一格的数据球，让同一台机器按订单加工不同元素：

1. 在数据球仓的电路槽设置 1 号编程电路。
2. 在数据球仓旁放置存储总线，把总线所在的 ME 网络接到切换器的绿色目标端。这个网络存放当前数据球，单独供电。
3. 把每种元素的数据球存入蓝色库存端连接的 ME 网络。
4. 用扳手让切换器正面对准元素复制机上的 <ItemLink id="gregtech:gt.blockmachines:22001" showIcon="left" />，打开切换器，把“匹配 NBT”设为 true。数据球靠 NBT 区分元素。
5. 在 <ItemLink id="gregtech:gt.blockmachines:22048" showIcon="left" /> 放入同种数据球作为模板，把对应的编程器电路写入该元素的加工样板。
6. 下单后，输入仓把编程器电路携带的数据球写入电路槽。切换器把旧数据球退回库存网络，再把新数据球送进目标网络。

切换器读取仓室的主电路槽，这里使用编程二合一输入仓；缓冲仓把配置保存在各自的配方缓存中。样板和提供器的配置方法见 [编程二合一仓：电路与模具](programmed_inputs.md)。

先下单两种不同元素的小批量，观察数据球切换与实际产物。

## 化工厂催化剂切换

催化剂物品切换器根据目标 GT 方块电路槽里的物品，维持催化剂目标库存：

1. 蓝色库存端接包含备选催化剂的库存网络。
2. 绿色目标端接实际放置催化剂的仓室所对应的网络。
3. 用扳手把正面指向带电路槽的 GT 方块。
4. 在电路槽标记所需催化剂，并配置 NBT、损害值匹配选项。
5. 切换标记，观察旧催化剂退出和目标催化剂补入，再启动相应配方。

<Flowchart width="300" height="125" canvasWidth="340" canvasHeight="125">
  <FlowNode id="n0" x="52" y="51" size="72" shape="rect" color="#657b91" label="催化剂库存" />
  <FlowNode id="n1" x="166" y="51" size="72" shape="rect" color="#657b91" label="切换器" />
  <FlowNode id="n2" x="280" y="51" size="72" shape="rect" color="#657b91" label="目标仓室" />
  <FlowEdge from="n0" to="n1" />
  <FlowEdge from="n1" to="n2" />
</Flowchart>

自动化催化剂仓（ME）读取其网络中活跃区域的第一个催化剂。催化剂的补充和更换仍需由库存与切换流程完成；先确认目标仓实际得到的是配方要求的催化剂。

## 从 ME 读取研究闪存

数据访问仓（ME）从网络收集研究闪存，适合集中管理装配线的数据访问。将需要的闪存放入仓室可访问的 ME 网络，再检查目标配方是否获得相应研究数据。

## 固定虚拟物品与多电路

虚拟输入总线可标记多种虚拟物品，适合机器明确支持的非消耗输入。先核对配方语义，再把真实消耗材料放在正常输入路径。

多电路输入总线和二合一仓提供多个电路槽。电路拓展覆盖板则保存常用物品标记，便于在 GUI 中切换电路槽。把“同时提供多个选择”与“切换当前选择”分别验证，确认机器匹配到预期配方。
