[English](#English)

# 枪械显示
> wiki 版本：`0.0.4`.2

- `model_location`/`model`（ResourceLocation）：
- `model_transform`/`transform`（\_ModelTransform）：
	- `scale`（\_ModelTransformScale）：
		- `third_person`/`thirdperson`（float[]）：
		- `ground_scale`/`ground`（float[]）：
		- `fixed_scale`/`fixed`（float[]）：
- `texture_location`/`texture`（ResourceLocation）：
- `slot_texture_location`/`slot`（ResourceLocation）：
---
- `hud_texture_location`/`hud`（ResourceLocation）：
- `hud_empty_texture_location`/`hud_empty`（ResourceLocation）：
---
- `gun_model_type`/`model_type`（GunModelType）：
- `lod_display`/`lod`（\_LodDisplay）：
	- `model_location`/`model`（ResourceLocation）：
	- `texture_location`/`texture`（ResourceLocation）：
- `enable_transparency`（boolean）：
---
- `iron_zoom_scale`/`iron_zoom`（float）：
- `iron_view_fov`/`iron_fov`（float）：
- `enable_crosshair`/`crosshair`（boolean）：
- `muzzle_flash_display`/`muzzle_flash`（\_MuzzleFlashDisplay）：
	- `texture_location`/`texture`（ResourceLocation）：
	- `texture_scale`/`scale`（float）：
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
- `surround_display_by_hotbar`/`surround`（Map\<String, \_SurroundDisplay>）：
	- `pos`（float[]）：
	- `rotate`（float[]）：
	- `scale`（float[]）：
- `surround_display_by_offhand`/`offhand_surround`（\_SurroundDisplay）：
	- `pos`（float[]）：
	- `rotate`（float[]）：
	- `scale`（float[]）：
- `damage_display_type`/`damage_display`（DamageDisplayType）：
- `ammo_count_type`/`ammo_count`（AmmoCountType）：
- `ammo_display_override`/`ammo_override`（\_AmmoDisplayOverride）：
	- `ammo_particle`/`particle`（\_AmmoParticle）：
		- `particle_location`/`name`（ResourceLocation）：
		- `delta`（float[]）：
		- `speed`（float）：
		- `count`（int）：
		- `lifetime_ticks`/`life_time`（int）：
	- `tracer_color`（Color）：
---
- `gun_animation_location`/`animation`（ResourceLocation）：
- `script_location`/`script`（ResourceLocation）：
- `script_param`/`param`（Map\<String, Object>）：
- `shell_ejection_param`/`shell_ejection`（\_ShellEjectionParam）：
	- `base_velocity`/`initial_velocity`（float[]）：
	- `randomize_velocity`/`random_velocity`（float[]）：
	- `acceleration`（float[]）：
	- `angular_velocity`（float[]）：
	- `lifetime_seconds`/`living_time`（float）：
- `shooter_animation_category`/`third_person_animation`（IShooterAnimationCategory）：
- `player_animator_location`/`player_animator`（ResourceLocation）：
- `player_animator_fixed_hand`/`player_animator_fixed`（boolean）：
- `gun_sounds`/`sounds`（Map\<GunSoundType, ResourceLocation>）：
- `preload_sound_location`/`preload_sounds`（List\<ResourceLocation>）：
---
- `controllable_data`/`controllable`（\_ControllableData）：

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
	
	"hud_texture_location": "namespace:location",
	"hud_empty_texture_location": "namespace:location",
	
	"gun_model_type": "",
	"lod_display": {
		"model_location": "namespace:location",
		"texture_location": "namespace:location"
	},
	"enable_transparency": false,
	
	"iron_zoom_scale": 0.0,
	"iron_view_fov": 0.0,
	"enable_crosshair": false,
	"muzzle_flash_display": {
		"texture_location": "namespace:location",
		"texture_scale": 0.0
	},
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
	"surround_display_by_hotbar": {
		"": {
			"pos": [0.0, 0.0, 0.0],
			"rotate": [0.0, 0.0, 0.0],
			"scale": [0.0, 0.0, 0.0]
		}
	},
	"surround_display_by_offhand": {
		"pos": [0.0, 0.0, 0.0],
		"rotate": [0.0, 0.0, 0.0],
		"scale": [0.0, 0.0, 0.0]
	},
	"damage_display_type": "",
	"ammo_count_type": "",
	"ammo_display_override": {
		"ammo_particle": {
			"particle_location": "namespace:location",
			"delta": [0.0, 0.0, 0.0],
			"speed": 0.0,
			"count": 0,
			"lifetime_ticks": 0
		},
		"tracer_color": "#FFFFFF"
	},
	
	"gun_animation_location": "namespace:location",
	"script_location": "namespace:location",
	"script_param": {
		"String": "",
		"Double": 0.00,
		"Boolean": false
	},
	"shell_ejection_param": {
		"base_velocity": [0.0, 0.0, 0.0],
		"randomize_velocity": [0.0, 0.0, 0.0],
		"acceleration": [0.0, 0.0, 0.0],
		"angular_velocity": [0.0, 0.0, 0.0],
		"lifetime_seconds": 0.0
	},
	"shooter_animation_category": "",
	"player_animator_location": "namespace:location",
	"player_animator_fixed_hand": false,
	"gun_sounds": {
		"FIRE": "namespace:location"
	},
	"preload_sound_location": [
		"namespace:location"
	],
	
	"controllable_data": {
	}
}
```

# English
> wiki verison: `0.0.4`.2

- `model_location`/`model`(ResourceLocation):
- `model_transform`/`transform`(\_ModelTransform):
	- `scale`(\_ModelTransformScale):
		- `third_person`/`thirdperson`(float[]):
		- `ground_scale`/`ground`(float[]):
		- `fixed_scale`/`fixed`(float[]):
- `texture_location`/`texture`(ResourceLocation):
- `slot_texture_location`/`slot`(ResourceLocation):
---
- `hud_texture_location`/`hud`(ResourceLocation):
- `hud_empty_texture_location`/`hud_empty`(ResourceLocation):
---
- `gun_model_type`/`model_type`(GunModelType):
- `lod_display`/`lod`(\_LodDisplay):
	- `model_location`/`model`(ResourceLocation):
	- `texture_location`/`texture`(ResourceLocation):
- `enable_transparency`(boolean):
---
- `iron_zoom_scale`/`iron_zoom`(float):
- `iron_view_fov`/`iron_fov`(float):
- `enable_crosshair`/`crosshair`(boolean):
- `muzzle_flash_display`/`muzzle_flash`(\_MuzzleFlashDisplay):
	- `texture_location`/`texture`(ResourceLocation):
	- `texture_scale`/`scale`(float):
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
- `surround_display_by_hotbar`/`surround`(Map\<String, \_SurroundDisplay>):
	- `pos`(float[]):
	- `rotate`(float[]):
	- `scale`(float[]):
- `surround_display_by_offhand`/`offhand_surround`(\_SurroundDisplay):
	- `pos`(float[]):
	- `rotate`(float[]):
	- `scale`(float[]):
- `damage_display_type`/`damage_display`(DamageDisplayType):
- `ammo_count_type`/`ammo_count`(AmmoCountType):
- `ammo_display_override`/`ammo_override`(\_AmmoDisplayOverride):
	- `ammo_particle`/`particle`(\_AmmoParticle):
		- `particle_location`/`name`(ResourceLocation):
		- `delta`(float[]):
		- `speed`(float):
		- `count`(int):
		- `lifetime_ticks`/`life_time`(int):
	- `tracer_color`(Color):
---
- `gun_animation_location`/`animation`(ResourceLocation):
- `script_location`/`script`(ResourceLocation):
- `script_param`/`param`(Map\<String, Object>):
- `shell_ejection_param`/`shell_ejection`(\_ShellEjectionParam):
	- `base_velocity`/`initial_velocity`(float[]):
	- `randomize_velocity`/`random_velocity`(float[]):
	- `acceleration`(float[]):
	- `angular_velocity`(float[]):
	- `lifetime_seconds`/`living_time`(float):
- `shooter_animation_category`/`third_person_animation`(IShooterAnimationCategory):
- `player_animator_location`/`player_animator`(ResourceLocation):
- `player_animator_fixed_hand`/`player_animator_fixed`(boolean):
- `gun_sounds`/`sounds`(Map\<GunSoundType, ResourceLocation>):
- `preload_sound_location`/`preload_sounds`(List\<ResourceLocation>):
---
- `controllable_data`/`controllable`(\_ControllableData):

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
	
	"hud_texture_location": "namespace:location",
	"hud_empty_texture_location": "namespace:location",
	
	"gun_model_type": "",
	"lod_display": {
		"model_location": "namespace:location",
		"texture_location": "namespace:location"
	},
	"enable_transparency": false,
	
	"iron_zoom_scale": 0.0,
	"iron_view_fov": 0.0,
	"enable_crosshair": false,
	"muzzle_flash_display": {
		"texture_location": "namespace:location",
		"texture_scale": 0.0
	},
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
	"surround_display_by_hotbar": {
		"": {
			"pos": [0.0, 0.0, 0.0],
			"rotate": [0.0, 0.0, 0.0],
			"scale": [0.0, 0.0, 0.0]
		}
	},
	"surround_display_by_offhand": {
		"pos": [0.0, 0.0, 0.0],
		"rotate": [0.0, 0.0, 0.0],
		"scale": [0.0, 0.0, 0.0]
	},
	"damage_display_type": "",
	"ammo_count_type": "",
	"ammo_display_override": {
		"ammo_particle": {
			"particle_location": "namespace:location",
			"delta": [0.0, 0.0, 0.0],
			"speed": 0.0,
			"count": 0,
			"lifetime_ticks": 0
		},
		"tracer_color": "#FFFFFF"
	},
	
	"gun_animation_location": "namespace:location",
	"script_location": "namespace:location",
	"script_param": {
		"String": "",
		"Double": 0.00,
		"Boolean": false
	},
	"shell_ejection_param": {
		"base_velocity": [0.0, 0.0, 0.0],
		"randomize_velocity": [0.0, 0.0, 0.0],
		"acceleration": [0.0, 0.0, 0.0],
		"angular_velocity": [0.0, 0.0, 0.0],
		"lifetime_seconds": 0.0
	},
	"shooter_animation_category": "",
	"player_animator_location": "namespace:location",
	"player_animator_fixed_hand": false,
	"gun_sounds": {
		"FIRE": "namespace:location"
	},
	"preload_sound_location": [
		"namespace:location"
	],
	
	"controllable_data": {
	}
}
```