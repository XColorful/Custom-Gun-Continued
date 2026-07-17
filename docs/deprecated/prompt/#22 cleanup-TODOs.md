> #22 提示词

```
完成xiao.customgun.core.resource.data.recipefilter.RecipeFilterData的TODO
- 你可以查\docs-tacz\TaCZ Migration Mapping.md来找对应的原模组的逻辑，可通过MCP访问TaCZ
```

# 

```
xiao.customgun.core.recipe下除了TableIngredient以外均已完成

研究并完成xiao.customgun.core.recipe.TableIngredient的TODO，以下内容辅助你加速搜索：
- TaCZ对应的调用链终点在GunSmithTableSerializer.fromNetwork
- TableIngredient调用链为TableRecipeSerializer._fromJson->TableRecipe.fromPojo->TableIngredient.fromPojo
```

```
public static Ingredient of(TagKey<Item> p_204133_)这个方法在1.21.4+就没有了
我把源码复制到了util.IngredientUtils里，请完整阅读，你对比高版本跟1.20.1之间的差异，针对这个一个方法做一个封装，使其接受的参数在1.20.1-高版本都适用
```

# 

```
研究下xiao.customgun.client.resource.instance.assets.GunDisplayInstance#loadScriptParams的TODO并完成

以下内容辅助你加速搜索：
- GunDisplayInstance对应TaCZ类为com.tacz.guns.client.resource.GunDisplayInstance，你可以在\docs-tacz\下的迁移映射里找到
- CGC的GunDisplayInstance是移除了异步加载的，你只需要完成脚本的TODO即可
- 完成后立即向我报告，我需要亲自检查
```

# 

```
研究下xiao.customgun.client.command.sub._ReloadCommand.reloadClientPack里TODO提到的问题，执行这个客户端指令时游戏会卡死
- 是否能在这个时机手动重载资源包？
- 是否有必要保留这个指令？
```

```
如果去掉.get()，是否影响服务端xiao.customgun.core.command.sub.ReloadCommand指令的逻辑？
```

```
那看看怎么样使客户端指令单独执行逻辑，而服务端保持原先的逻辑和功能？
```

```
执行修改，并且往\docs\wiki\command\Reload command.md里补充简要的功能说明
```
