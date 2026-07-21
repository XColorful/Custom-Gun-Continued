> #24 提示词

```
完成xiao.customgun.core.api.event.EventType新增的5个客户端类的相关代码
- 可以通过IDEA的查找引用来参考其他枚举有哪些引用，以及引用的类在别的哪些地方有使用
- 可以获取IDEA当前打开的窗口来进一步加速查找
```

```
我进行了修改并提交了commit，刚才的事件类以我修改的最新版为准

现在对照TaCZ com.tacz.guns.client.input下的每个类在我CGC \docs-tacz\TaCZ Migration Mapping.md里对应的类：
- TaCZ里监听的事件，在xiao.customgun.client.event.custom里也逐个写
- 对于TaCZ有多个@SubscribeEvent同一个事件的，不需要在ClientEventHandlers里重复监听
- 在handleEvent的case里把TaCZ原名称的函数补充，只需要空实现即可
- 对于重复监听事件的，在case里按TaCZ里代码的顺序调用
- 注意在CGC里，ClientTickEven的Pre和Post对应两个EventType，不要搞错了
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

```
我修改了InputKey架构，引入了InputKeyManager，InputKey和MouseButton事件已经统一监听并执行按键过滤（这是为了避免原先模式下鼠标键改成键盘后就没法监听）

现在需要以下修改，以我修改后的AimKey作为示范：
- 把原先监听的key和mouse事件从ClientEventHandlers中移除
- 将原先的监听处理函数移到onKeyInput或onMouseInput里，非key和mouse事件不受此影响
- 没有监听key或input的留空，就像Aimkey里那样
- 如果没有额外监听事件，移除IEventHandler接口

需要修改的范围限制在input.config、input.player、input.shooter和ClientEventHandlers
```

```
- 刚才修改的所有onKeyInput和onMouseInput里，调用方法前面都加上this.
- 没有额外注册事件的，registerEventHandler和unregister都改成返回true
```
