> #91 提示词

#

```
读 `./docs/architecture/client/render/PLAN.md`和`#89 Render-analysis.md`，现在需要接着修附加弹匣的状态泄漏问题

预计开新一次基准测试，只测1.20.1和26.3的multi场景
- 先只写1.20.1的修复，26.3等一轮结果出来后再说
- 只测进游戏空手时的渲染，因为枪包不变+块数已经得知，可以推算是否泄漏
```

# 

我先读计划文档和 issue。

`visible` 默认 true，只有第一人称 `cleanAnimationTransform()` 会关掉它，展示框路径从不经过该调用。修复方式：在绑定节点时初始化为隐藏。

修复已编译通过。现在更新文档。

修好了，1.20.1 侧只改了一处。

## 根因与修法

`BedrockPart.visible` 默认 `true`，而 `additionalMagazineNode` 只有一个地方会把它关掉——`GunModelObject.cleanAnimationTransform()`，它只在两条第一人称渲染路径的末尾被调用（[GunItemRenderer.java:312](core/src/main/java/dev/xcolorful/customgun/client/renderer/item/GunItemRenderer.java:312)、[AnimateGeoItemRenderer.java:306](core/src/main/java/dev/xcolorful/customgun/client/renderer/item/AnimateGeoItemRenderer.java:306)）。`renderByItem`（展示框 / 掉落物 / 第三人称 / GUI）既不跑 `stateMachine.update()`、也不清理，于是整场会话没持过枪时，`mag_additional` 就一直是可见态，每帧在 [renderAdditionalMagazine](core/src/main/java/dev/xcolorful/customgun/client/model/_GunLoader.java:113) 里多编译一个弹匣。

修复放在节点绑定的地方，而不是再加一处清理：

```java
// _GunLoader.constructMagazineNode
if (additionalMagazine != null) {
    _this.additionalMagazineNode = additionalMagazine.getModelRenderer();
    _this.additionalMagazineNode.visible = false;
}
```

`ModelAdditionalMagazineListener` 仍然在换弹动画每帧 `update()` 时把它打开，第一人称行为不变；`addon` 侧语义也和 `GunModelObject` 的注释一致（「额外弹匣只有当动画中有它的关键帧的时候才渲染」）。26.3 的 [_GunLoader.java](D:/Github/Custom-Gun-Continued-26.3neoforge/core/src/main/java/dev/xcolorful/customgun/client/model/_GunLoader.java) 目前逐字相同，后面同步是同一处改动。

## 验证与编译

`core`、`forge-compat` 均编译通过（只剩既有的 deprecation 警告），IDEA 检查无 error/warning。**未运行时验证**，按约定也未提交。

## 新一轮测试口径

已写进 [TODO.md](docs/architecture/client/render/TODO.md)：只跑 multi、只在进存档空手时按一次 F12，看 `Cubes submitted`。

- 无泄漏 **4005 个立方体/帧**，泄漏 **4554**（每把展示框枪 61 个）
- 1.20.1 跑 6 次应全部落在 4005；26.3 未修，应稳定 4554，正好当对照
- 这段只需要内置探针，Afterburner 可跑可不跑

报告 [5.1](docs/architecture/client/render/benchmark/26.35.11.1-1/测试数据计算报告.md:108) 与结论第 6 条已标注「1.20.1 已修、26.3 待同步」。26.3 的修复等你这一轮数据回来再做。

# 

```
1.20.1和26.3的渲染分支都已经应用修复了，测试内容在26.35.11.1-1/#91 additional-magazine-fix/下
更新下这个benchmark的文档并说明已经修复，后续基准测试不会引入这个问题
```
