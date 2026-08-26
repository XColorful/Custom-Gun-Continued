[English](#English)

# 配件显示
> wiki 版本：`0.0.7`.1

- `model_location`/`model`（ResourceLocation）：
- `texture_location`/`texture`（ResourceLocation）：
- `slot_texture_location`/`slot`（ResourceLocation）：
---
- `lod_display`/`lod`（\_LodDisplay）：
	- `model_location`/`model`（ResourceLocation）：
	- `texture_location`/`texture`（ResourceLocation）：
- `adapter_node_name`/`adapter`（String）：
---
- `enable_sight`/`sight`（boolean）：
- `enable_scope`/`scope`（boolean）：
- `scope_zoom_scale`/`zoom`（float[]）：
- `scope_view_index`/`views`（int[]）：
- `scope_view_fov`/`views_fov`/`fov`（float[]）：
- `show_muzzle`（boolean）：
- `show_mount`（boolean）：
- `model_node_text_display`/`text_show`（Map\<String, \_ModelNodeTextDisplay>）：
	- `text_lang`/`text`（MutableComponent）：
	- `text_scale`/`scale`（float）：
	- `text_color`/`color`（Color）：
	- `text_light`/`light`（int）：
	- `enable_text_shadow`/`shadow`（boolean）：
	- `x_offset_scale`/`align`（float）：
- `laser_display`/`laser`（\_LaserDisplay）：
	- `default_color`（Color）：
	- `enable_customized_color`/`can_edit`（boolean）：
	- `laser_length`/`length`（float）：
	- `laser_width`/`width`（float）：
	- `third_person_laser_length`/`third_person_length`（float）：
	- `third_person_laser_width`/`third_person_width`（float）：
- `attachment_sounds`/`sounds`（Map\<AttachmentSoundType, ResourceLocation>）：

```json
{
	"model_location": "namespace:location",
	"texture_location": "namespace:location",
	"slot_texture_location": "namespace:location",
	
	"lod_display": {
		"model_location": "namespace:location",
		"texture_location": "namespace:location"
	},
	"adapter_node_name": "",
	
	"enable_sight": false,
	"enable_scope": false,
	"scope_zoom_scale": [
		0.0
	],
	"scope_view_index": [
		0
	],
	"scope_view_fov": [
		0.0
	],
	"show_muzzle": false,
	"show_mount": false,
	"model_node_text_display": {
		"": {
			"text_lang": "",
			"text_scale": 0.0,
			"text_color": "#FFFFFF",
			"text_light": 0,
			"enable_text_shadow": false,
			"x_offset_scale": 0.0
		}
	},
	"laser_display": {
		"default_color": "#FFFFFF",
		"enable_customized_color": false,
		"laser_length": 0.0,
		"laser_width": 0.0,
		"third_person_laser_length": 0.0,
		"third_person_laser_width": 0.0
	},
	"attachment_sounds": {
		"": "namespace:location"
	}
}
```

# English
> wiki verison: `0.0.7`.1

- `model_location`/`model`(ResourceLocation):
- `texture_location`/`texture`(ResourceLocation):
- `slot_texture_location`/`slot`(ResourceLocation):
---
- `lod_display`/`lod`(\_LodDisplay):
	- `model_location`/`model`(ResourceLocation):
	- `texture_location`/`texture`(ResourceLocation):
- `adapter_node_name`/`adapter`(String):
---
- `enable_sight`/`sight`(boolean):
- `enable_scope`/`scope`(boolean):
- `scope_zoom_scale`/`zoom`(float[]):
- `scope_view_index`/`views`(int[]):
- `scope_view_fov`/`views_fov`/`fov`(float[]):
- `show_muzzle`(boolean):
- `show_mount`(boolean):
- `model_node_text_display`/`text_show`(Map\<String, \_ModelNodeTextDisplay>):
	- `text_lang`/`text`(MutableComponent):
	- `text_scale`/`scale`(float):
	- `text_color`/`color`(Color):
	- `text_light`/`light`(int):
	- `enable_text_shadow`/`shadow`(boolean):
	- `x_offset_scale`/`align`(float):
- `laser_display`/`laser`(\_LaserDisplay):
	- `default_color`(Color):
	- `enable_customized_color`/`can_edit`(boolean):
	- `laser_length`/`length`(float):
	- `laser_width`/`width`(float):
	- `third_person_laser_length`/`third_person_length`(float):
	- `third_person_laser_width`/`third_person_width`(float):
- `attachment_sounds`/`sounds`(Map\<AttachmentSoundType, ResourceLocation>):

```json
{
	"model_location": "namespace:location",
	"texture_location": "namespace:location",
	"slot_texture_location": "namespace:location",
	
	"lod_display": {
		"model_location": "namespace:location",
		"texture_location": "namespace:location"
	},
	"adapter_node_name": "",
	
	"enable_sight": false,
	"enable_scope": false,
	"scope_zoom_scale": [
		0.0
	],
	"scope_view_index": [
		0
	],
	"scope_view_fov": [
		0.0
	],
	"show_muzzle": false,
	"show_mount": false,
	"model_node_text_display": {
		"": {
			"text_lang": "",
			"text_scale": 0.0,
			"text_color": "#FFFFFF",
			"text_light": 0,
			"enable_text_shadow": false,
			"x_offset_scale": 0.0
		}
	},
	"laser_display": {
		"default_color": "#FFFFFF",
		"enable_customized_color": false,
		"laser_length": 0.0,
		"laser_width": 0.0,
		"third_person_laser_length": 0.0,
		"third_person_laser_width": 0.0
	},
	"attachment_sounds": {
		"": "namespace:location"
	}
}
```