---
navigation:
  title: "远程分子装配：P2P、导管与接口"
  parent: index.md
  icon: programmablehatches:proghatches.part.ma.p2p
  keywords:
    - "远程分子装配：P2P、导管与接口"
    - "分子装配"
    - "P2P"
    - "MA模式"
    - "分子装配导管"
    - "Molecular Assembler"
item_ids:
  - "<programmablehatches:proghatches.part.ma.p2p>"
  - "<programmablehatches:proghatches.ma_iface>"
---

# 远程分子装配：P2P、导管与接口

MA 路由传递的是分子装配请求。适合将放样板的接口与实际执行合成的设备分开布置。

## 用分子装配 P2P 连接远端装配室

1. 在主网接口旁安装分子装配 P2P 输入端。
2. 在远端分子装配室旁安装输出端。
3. 用内存卡建立对应关系，并给相关 ME 设备提供频道与电力。
4. 在接口中放一张普通工作台合成样板。
5. 下单少量物品，观察远端装配室工作和产物回到原网络。

先用工作台配方验证完整路径，再扩展接口和装配室数量。分子装配导管可用于相应的请求连接，按导管网络检查端点和设备方向。

<Flowchart width="300" height="125" canvasWidth="340" canvasHeight="125">
  <FlowNode id="n0" x="52" y="51" size="72" shape="rect" color="#657b91" label="样板接口" />
  <FlowNode id="n1" x="166" y="51" size="72" shape="rect" color="#657b91" label="MA 路由" />
  <FlowNode id="n2" x="280" y="51" size="72" shape="rect" color="#657b91" label="装配设备" />
  <FlowEdge from="n0" to="n1" />
  <FlowEdge from="n1" to="n2" />
</Flowchart>

## 将请求交给普通容器

分子装配接口从一侧接收 MA 请求，把原料传递到对面的容器。安装时检查请求入口和容器所在面，给结果准备独立回程。

该接口处理的样板最多含一种流体。多流体加工可按设备能力选择合适的样板输入总成。

## 二合一仓的 MA 模式

支持 MA 模式的二合一仓可直接从普通 ME 接口接收样板原料。使用普通接口，并按该模式的说明组织接口周围的容器，保证接口选择预期的请求接收设备。

常规物流模式与 MA 模式的原料传递路径不同。配置后用单份订单观察材料进入的位置，再扩大任务规模。
