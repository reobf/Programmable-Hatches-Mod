---
navigation:
  title: "控制与维护：反应堆时序、传感器和傀儡"
  parent: index.md
  icon: programmablehatches:proghatches.reactor_syncer
  keywords:
    - "控制与维护：反应堆时序、传感器和傀儡"
    - "反应堆"
    - "更新同步器"
    - "维护傀儡"
    - "读卡器"
    - "reactor"
    - "golem"
    - "sensor"
item_ids:
  - "<programmablehatches:proghatches.reactor_syncer>"
  - "<programmablehatches:proghatches.card_reader>"
  - "<programmablehatches:proghatches.cover:1>"
---

# 控制与维护：反应堆时序、传感器和傀儡

这组设备适合把机器状态接入控制系统：反应堆更新同步器提供更新时序，传感器卡片读卡器向 OC 提供文本读数，维护傀儡执行多方块维护。

## 先观察反应堆更新时序

在单独的测试反应堆上安装更新同步器，用指示灯或 OC 记录它的输出。配置更新刻与其他刻的红石强度，观察每 20 tick 的反应堆更新周期。

用于换棒或元件调整前，先验证执行器、物流和红石传递的实际延迟。普通红石线路按自身更新节奏传递；需要单 tick 响应时，可按设备说明使用红石 P2P、红石导管或红色合金线。

读档会改变反应堆的更新计数起点。“第一个更新周期不输出信号”可为恢复控制状态留出时间。先在独立测试结构验证读档行为，再把时序接入实际控制程序。

## 从传感器卡读取数据

把已配置的传感器卡放入读卡器，并连接 OC。组件类型为 `cardreader`；第一个卡槽编号为 0。

```lua
local component = require("component")
local reader = component.proxy("读卡器的完整地址")
print(reader.getTitle(0))
```

先核对标题确认读的是正确机器，再用 `getText` 读取相应行。根据实际卡片提供的内容处理单位、空值和字段名称。

## 维护傀儡

安装维护核心后，用傀儡使铃铛绑定多方块主控制器，并确保傀儡可以走到维护仓。制造一次可观察的维护需求，检查傀儡是否到达正确位置并完成维护。

换机器位置或改维护仓位置后，重新检查绑定和通路。

## 无线控制与库存控制

无线控制覆盖板提供公共／私有频道、信号反转和安全模式。先用指示灯观察信号，再连接机器启停。需要按材料库存开关机器时，优先参考 [库存控制教程](stock_control.md)。

把“状态读取、控制判断、执行动作”分别验证，有助于判断等待发生在读数、线路还是执行器。
