[English](#English)

# 配方数据
> wiki 版本：`0.0.1`.2

- `type`（String）：
- `table_ingredients`/`materials`（List<\_TableIngredientData>）：
	- `ingredient_filter_data`/`item`（\_IngredientFilterData）：
		- `item_filter_location`/`item`（ResourceLocation）：
		- `tag_filter_location`/`tag`（ResourceLocation）：
	- `ingredient_count`/`count`（int）：
- `table_result`/`result`（\_TableResultData）：
	- `result_type`/`type`（RecipeResultType）：
	- `result_count`/`count`（int）：
	- `result_item`/`item`（\_ResultItemData）：
		- `item_location`/`item`（ResourceLocation）：
		- `item_nbt`/`nbt`（CompoundTag）：
	- ~~`ammo_count`~~
	- ~~`attachments`~~
	- ~~`nbt`~~
	- `tab_group_location`/`group`（ResourceLocation）：
	- `pojo_location`/`id`（ResourceLocation）：

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
		"result_type": "",
		"result_count": 1,
		"result_item": {
			"item_location": "namespace:location",
			"item_nbt": {
			}
		},
		"tab_group_location": "namespace:location",
		"pojo_location": "namespace:location"
	}
}
```

# English
> wiki verison: `0.0.1`.2

- `type`(String):
- `table_ingredients`/`materials`(List<\_TableIngredientData>):
	- `ingredient_filter_data`/`item`(\_IngredientFilterData):
		- `item_filter_location`/`item`(ResourceLocation):
		- `tag_filter_location`/`tag`(ResourceLocation):
	- `ingredient_count`/`count`(int):
- `table_result`/`result`(\_TableResultData):
	- `result_type`/`type`(RecipeResultType):
	- `result_count`/`count`(int):
	- `result_item`/`item`(\_ResultItemData):
		- `item_location`/`item`(ResourceLocation):
		- `item_nbt`/`nbt`(CompoundTag):
	- ~~`ammo_count`~~
	- ~~`attachments`~~
	- ~~`nbt`~~
	- `tab_group_location`/`group`(ResourceLocation):
	- `pojo_location`/`id`(ResourceLocation):

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
		"result_type": "",
		"result_count": 1,
		"result_item": {
			"item_location": "namespace:location",
			"item_nbt": {
			}
		},
		"tab_group_location": "namespace:location",
		"pojo_location": "namespace:location"
	}
}
```