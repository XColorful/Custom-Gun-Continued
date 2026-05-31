[English](#English)

# 子弹显示
> wiki 版本：`0.0.0`.0

- `model_location`/`model`（ResourceLocation）：
- `model_transform`/`transform`（\_ModelTransform）：
	- `scale`（\_ModelTransformScale）：
		- `third_person`/`thirdperson`（float[]）：
		- `ground_scale`/`ground`（float[]）：
		- `fixed_scale`/`fixed`（float[]）：
- `texture_location`/`texture`（ResourceLocation）：
- `slot_texture_location`/`slot`（ResourceLocation）：
---
- `ammo_entity_display`/`ammo_entity`（\_AmmoEntityDisplay）：
- `shell_display`/`shell`（\_ShellDisplay）：
---
- `ammo_particle`/`particle`（\_AmmoParticle）：
- `tracer_color`（Color）：

```json
{
	"model_location": "namespace:location",
	"model_transform": {
		"scale": {
			"third_person": [0.0, 0.0, 0.0],
			"ground_scale": [0.0, 0.0, 0.0],
			"fixed_scale": [0.0, 0.0, 0.0]
		}
	},
	"texture_location": "namespace:location",
	"slot_texture_location": "namespace:location",
	
	"ammo_entity_display": {
		"model_location": "namespace:location",
		"texture_location": "namespace:location"
	},
	"shell_display": {
		"model_location": "namespace:location",
		"texture_location": "namespace:location"
	},
	
	"ammo_particle": {
		"particle_location": "namespace:location",
		"delta": [0.0, 0.0, 0.0],
		"speed": 0.0,
		"count": 0,
		"lifetime_ticks": 0
	},
	"tracer_color": "#FFFFFF"
}
```

# English
> wiki verison: `0.0.0`.0

- `model_location`/`model`(ResourceLocation):
- `model_transform`/`transform`(\_ModelTransform):
	- `scale`(\_ModelTransformScale):
		- `third_person`/`thirdperson`(float[]):
		- `ground_scale`/`ground`(float[]):
		- `fixed_scale`/`fixed`(float[]):
- `texture_location`/`texture`(ResourceLocation):
- `slot_texture_location`/`slot`(ResourceLocation):

- `ammo_entity_display`/`ammo_entity`(\_AmmoEntityDisplay):
- `shell_display`/`shell`(\_ShellDisplay):

- `ammo_particle`/`particle`(\_AmmoParticle):
- `tracer_color`(Color):

```json
{
	"model_location": "namespace:location",
	"model_transform": {
		"scale": {
			"third_person": [0.0, 0.0, 0.0],
			"ground_scale": [0.0, 0.0, 0.0],
			"fixed_scale": [0.0, 0.0, 0.0]
		}
	},
	"texture_location": "namespace:location",
	"slot_texture_location": "namespace:location",
	
	"ammo_entity_display": {
		"model_location": "namespace:location",
		"texture_location": "namespace:location"
	},
	"shell_display": {
		"model_location": "namespace:location",
		"texture_location": "namespace:location"
	},
	
	"ammo_particle": {
		"particle_location": "namespace:location",
		"delta": [0.0, 0.0, 0.0],
		"speed": 0.0,
		"count": 0,
		"lifetime_ticks": 0
	},
	"tracer_color": "#FFFFFF"
}
```