---
navigation:
  title: "OC 自动化：IO 枢纽与任务式投料"
  parent: index.md
  icon: programmablehatches:proghatches.iohub
  keywords:
    - "OC 自动化：IO 枢纽与任务式投料"
    - "IO枢纽"
    - "OpenComputers"
    - "OC"
    - "无线外设"
    - "dualhatch"
    - "iohub"
item_ids:
  - "<programmablehatches:proghatches.iohub>"
  - "<gregtech:gt.blockmachines:22165>"
  - "<programmablehatches:proghatches.peripheral_station>"
  - "<programmablehatches:proghatches.oc.peripheral_card>"
  - "<programmablehatches:proghatches.oc.redstone>"
  - "<programmablehatches:proghatches.oc.api>"
---

# OC 自动化：IO 枢纽与任务式投料

IO 枢纽把物品栏、流体槽、ME 网络访问和搬运接口集中到一个 OC 组件中。可编程二合一输入枢纽（ME）则提供按数据库条目精确投料的任务接口。

## 先确认组件连接

给 OC 计算机供电，用 OC 线缆连接设备，再让设备连到所需 ME 网络。通过下面的程序查看组件名称和地址：

```lua
local component = require("component")
for address, kind in component.list() do
  print(kind, address)
end
```

IO 枢纽的组件类型为 `iohub`，二合一输入枢纽为 `dualhatch`。多个同类设备同时存在时，使用完整地址选择目标。

## IO 枢纽从读取开始

IO 枢纽包含 32 个物品槽和 8 个流体槽，并提供相邻容器访问与 ME 操作。先查询物品栏大小、槽内物品数或流体量，再测试一次少量转移。

`transferItem` 和 `transferFluid` 用于相邻容器之间的搬运；`getCustomName` 可读取枢纽名称。ME 升级的存储记录函数在这里叫 `storeAE`，以区分物品栏控制提供的 `store`。

为枢纽起名后，在程序日志中同时打印名称和地址，有助于核对正在操作的设备。

## 二合一输入枢纽精确投料

先在 OC 数据库的第一个条目记录一种测试物品，保证 ME 库存和输入空间充足。以下例子添加一个“取 4 个物品”的任务并提交：

```lua
local component = require("component")
local hatch = component.proxy("二合一输入枢纽的完整地址")
local database = "数据库的完整地址"

local task = assert(hatch.addTask(database, 0, 4, false))
print(task.id, task.state)

for _, result in ipairs(hatch.submitTask()) do
  print(result.id, result.state)
end
```

此设备的数据库条目编号从 0 开始。最后一个参数为 false 时取物品，为 true 时按数据库中流体容器所表示的流体取量。

`addTask` 加入待提交列表；`state` 查询待提交任务；`cancelTask` 取消待提交任务；`submitTask` 一次提交当前列表。也可以用 `executeTask` 执行单个任务。

逐项检查提交返回的状态：`executed`、材料不足、网络断开或部分执行。部分执行后先查询实际库存，只补缺少的部分。使用 `isEmpty` 和 `refund` 检查输入区并退回余料。

## 无线外设与辅助卡

无线外设驱动站连接需要暴露给计算机的设备；手持无线外设桥接卡右键绑定，再把卡装入计算机卡槽。保持一对一连接、同维度和 16 格范围，驱动站一侧的 OC 网络只放一台计算机。

GT 高级无线红石卡用于访问 GT 无线红石，先绑定 UUID 再使用私有频道。物品 API 卡提供矿辞查询以及物品数字 ID 与字符串 ID 的转换。
