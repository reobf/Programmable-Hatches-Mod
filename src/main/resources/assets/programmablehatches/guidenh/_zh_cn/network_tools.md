---
navigation:
  title: "小型子网与线传频道"
  parent: index.md
  icon: programmablehatches:proghatches.exciter
  keywords:
    - "小型子网与线传频道"
    - "线传频道"
    - "CoW"
    - "自组网络激励器"
    - "ME覆盖板"
    - "Subnet Exciter"
item_ids:
  - "<programmablehatches:proghatches.exciter>"
  - "<programmablehatches:proghatches.part.cow>"
  - "<programmablehatches:proghatches.cover:32>"
  - "<programmablehatches:proghatches.cover:33>"
  - "<programmablehatches:proghatches.cover:34>"
  - "<programmablehatches:proghatches.cover:35>"
  - "<programmablehatches:proghatches.cover:37>"
---

# 小型子网与线传频道

自组网络激励器为符合条件的小型 AE 网络供电。线传频道面板则通过 GT 导线把两个 ME 连接点连接起来。两者适合减少分散设备的布线。

## 建立独立小子网

1. 将存储总线、接口和少量线缆组成一个没有 ME 控制器的小网络。
2. 安装自组网络激励器。
3. 检查节点在线状态，并确认总频道需求符合无控制器网络的限制。
4. 从终端测试实际存取，再连接需要服务的机器。

激励器要求网络中没有控制器，也不向外输送能量。能源元件、充能器和石英纤维等不符合它的工作条件。扩展设备前重新检查网络构成。

## 通过 GT 导线传递频道

线传频道面板用一对同频率端点工作：

1. 在两处 ME 连接点安装线传频道面板。
2. 用 GT 导线连通这两个面板。
3. 用内存卡绑定两个端点。
4. 检查连接后的 ME 设备在线状态与频道使用量。

一对端点最多传递 32 个频道。一个 GT 线网中，同一频率最多放两个面板；需要其他独立连接时，使用各自的频率与端点。面板可以放在致密线缆上。

<Flowchart width="300" height="125" canvasWidth="340" canvasHeight="125">
  <FlowNode id="n0" x="52" y="51" size="72" shape="rect" color="#657b91" label="ME 端点" />
  <FlowNode id="n1" x="166" y="51" size="72" shape="rect" color="#657b91" label="GT 导线" />
  <FlowNode id="n2" x="280" y="51" size="72" shape="rect" color="#657b91" label="ME 端点" />
  <FlowEdge from="n0" to="n1" />
  <FlowEdge from="n1" to="n2" />
</Flowchart>

## 紧凑覆盖板连接

ME 接口覆盖板把接口做在 GT 方块表面；对应的二合一版本支持其流体功能。P2P 接口覆盖板作为输出端使用内存卡绑定。ME 覆盖板用于让频道穿过仓室，其通道容量按覆盖板说明配置。

先用一个接口、一台机器和一个小订单验证，再扩大到多台设备。覆盖板、P2P 和子网分别承担连接、路由和库存边界，接线时逐段检查。
