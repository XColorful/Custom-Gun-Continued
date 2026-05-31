[English](#English)

# 配方数据
> wiki 版本：`0.0.0`.0

- `type`（String）：
- `table_ingredients`/`materials`（List<\_TableIngredientData>）：
	- `ingredient_filter_data`/`item`（\_IngredientFilterData）：
		- `item_filter_location`/`item`（ResourceLocation）：
		- `tag_filter_location`/`tag`（ResourceLocation）：
	- `ingredient_count`/`count`（int）：
- `table_result`/`result`（\_TableResultData）：
	- `recipe_result_type`/`type`（RecipeResultType）：
	- `recipe_result_location`/`id`（ResourceLocation）：

```json
{
	"type": "customgun:table_recipe",
	"table_ingredients": [
		{
			"ingredient_filter_data": {
				"item_filter_location": "namespace:location",
				"tag_filter_location": "namespace:location"
			},
			"ingredient_count": 1
		}
	],
	"table_result": {
		"recipe_result_type": "",
		"recipe_result_location": "namespace:location"
	}
}
```

# English
> wiki verison: `0.0.0`.0

- `type`(String):
- `table_ingredients`/`materials`(List<\_TableIngredientData>):
	- `ingredient_filter_data`/`item`(\_IngredientFilterData):
		- `item_filter_location`/`item`(ResourceLocation):
		- `tag_filter_location`/`tag`(ResourceLocation):
	- `ingredient_count`/`count`(int):
- `table_result`/`result`(\_TableResultData):
	- `recipe_result_type`/`type`(RecipeResultType):
	- `recipe_result_location`/`id`(ResourceLocation):

```json
{
	"type": "customgun:table_recipe",
	"table_ingredients": [
		{
			"ingredient_filter_data": {
				"item_filter_location": "namespace:location",
				"tag_filter_location": "namespace:location"
			},
			"ingredient_count": 1
		}
	],
	"table_result": {
		"recipe_result_type": "",
		"recipe_result_location": "namespace:location"
	}
}
```