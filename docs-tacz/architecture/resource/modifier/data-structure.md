[English](#English)

# JSON 数据结构与格式要求

> 配件 JSON 文件的格式规范与反序列化流程。

## 文件位置与反序列化

配件数据定义在数据包的 `data/<namespace>/attachments/data/` 目录下，文件名即配件 ID。

反序列化由索引序列化器负责：遍历 modifier 注册表中的每个 modifier，用其 ID 在 JSON 中查找对应 key，解析后存入配件数据的 modifier 字段（一个 Map 结构）。

## 通用的修改值结构

每个 modifier 字段可包含四个值：

- `addend`：加数，直接累加到 base 值上
- `percent`：百分比加成，按 `1 + percent` 累加
- `multiplier`：乘数，按乘积累乘
- `function`：可选的 Lua 表达式，在数值计算后执行二次处理

计算公式：`(defaultValue + Σaddend) × max(Σ(1+percent), 0) × Π(max(multiplier, 0))`。Lua 环境中变量 `x` 为当前计算值、`r` 为原始默认值，输出必须赋值给 `y`。

## 配件总数据

配件数据以 Map 为核心字段，由反序列化过程填充。此外还有少量直接字段（如重量、扩容弹匣等级），不属于 modifier 体系。

## 复合型修改器

部分修改器不使用上述四字段结构，而是自定义数据结构：

- **消音器**：额外包含布尔字段（是否使用消音音效）
- **爆炸**：含启用开关（布尔）、多个子 Modifier（半径/伤害/延迟等）、击退/破坏方块等开关
- **散布**：五种射击姿态各有独立的 modifier 字段
- **移动速度修正**：使用自定义结构（基础/瞄准/换弹时的速度系数），不使用 addend/percent/multiplier 模式

# English
