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
