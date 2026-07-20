> #24 提示词

```
完成xiao.customgun.core.api.event.EventType新增的5个客户端类的相关代码
- 可以通过IDEA的查找引用来参考其他枚举有哪些引用，以及引用的类在别的哪些地方有使用
- 可以获取IDEA当前打开的窗口来进一步加速查找
```

```
完成新增的ServerPlayerTickEvent和ClientPlayerTickEvent
- 这两个事件都监听PlayerTickEvent
- 在Manager里获取logicalSide来过滤
- getLogicalSide接口使用McSideHelper
- 补充钢刺啊Reload漏掉的监听
```

```
修改一下，像PrepareClientTickEvent那样，给这两事件都增加一个Prepare版本，用来区分Phase（我已经把刚才的修改改成Phase.END）
ServerTickEvent也这样补一个版本
```
