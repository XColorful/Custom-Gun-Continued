[English](#English)

# 配件数据
> wiki 版本：`0.0.1`.3

- `ads`（\_SimpleModifierData）：
	- `shared_base_add`/`addend`（float）：
	- `shared_percent_add`/`percent`（float）：
	- `unique_multiplier`/`multiplier`（float）：
	- `script_function`/`function`（String）：
---
- `armor_ignore_percent`/`armor_ignore`（\_SimpleModifierData）：
	- `shared_base_add`/`addend`（float）：
	- `shared_percent_add`/`percent`（float）：
	- `unique_multiplier`/`multiplier`（float）：
	- `script_function`/`function`（String）：
- `headshot_multiplier`/`head_shot`（\_SimpleModifierData）：
	- `shared_base_add`/`addend`（float）：
	- `shared_percent_add`/`percent`（float）：
	- `unique_multiplier`/`multiplier`（float）：
	- `script_function`/`function`（String）：
- `damage_calculation`/`damage`（\_SimpleModifierData）：
	- `shared_base_add`/`addend`（float）：
	- `shared_percent_add`/`percent`（float）：
	- `unique_multiplier`/`multiplier`（float）：
	- `script_function`/`function`（String）：
- `bullet_speed`/`ammo_speed`（\_SimpleModifierData）：
	- `shared_base_add`/`addend`（float）：
	- `shared_percent_add`/`percent`（float）：
	- `unique_multiplier`/`multiplier`（float）：
	- `script_function`/`function`（String）：
- `pierce_count`/`pierce`（\_SimpleModifierData）：
	- `shared_base_add`/`addend`（float）：
	- `shared_percent_add`/`percent`（float）：
	- `unique_multiplier`/`multiplier`（float）：
	- `script_function`/`function`（String）：
- `fire_aspect`/`ignite`（\_FireAspectModifierData）：
	- `ignite_entity`/`entity`（boolean）：
	- `ignite_block`/`block`（boolean）：
- `knockback_strength`/`knockback`（\_SimpleModifierData）：
	- `shared_base_add`/`addend`（float）：
	- `shared_percent_add`/`percent`（float）：
	- `unique_multiplier`/`multiplier`（float）：
	- `script_function`/`function`（String）：
- `bullet_explosion`/`explosion`（\_BulletExplosionModifierData）：
	- `enable_explode`/`explode`（boolean）：
	- `explode_damage`/`damage`（\_SimpleModifierData）：
		- `shared_base_add`/`addend`（float）：
		- `shared_percent_add`/`percent`（float）：
		- `unique_multiplier`/`multiplier`（float）：
		- `script_function`/`function`（String）：
	- `explode_scale`/`radius`（\_SimpleModifierData）：
		- `shared_base_add`/`addend`（float）：
		- `shared_percent_add`/`percent`（float）：
		- `unique_multiplier`/`multiplier`（float）：
		- `script_function`/`function`（String）：
	- `max_delay_seconds`/`delay`（\_SimpleModifierData）：
		- `shared_base_add`/`addend`（float）：
		- `shared_percent_add`/`percent`（float）：
		- `unique_multiplier`/`multiplier`（float）：
		- `script_function`/`function`（String）：
	- `enable_knockback`/`knockback`（boolean）：
	- `enable_world_destruction`/`destroy_block`（boolean）：
---
- `rpm`（\_SimpleModifierData）：
	- `shared_base_add`/`addend`（float）：
	- `shared_percent_add`/`percent`（float）：
	- `unique_multiplier`/`multiplier`（float）：
	- `script_function`/`function`（String）：
- `recoil_data`/`recoil`（\_RecoilDataModifierData）：
	- `pitch_recoil`/`pitch`（\_SimpleModifierData）：
		- `shared_base_add`/`addend`（float）：
		- `shared_percent_add`/`percent`（float）：
		- `unique_multiplier`/`multiplier`（float）：
		- `script_function`/`function`（String）：
	- `yaw_recoil`/`yaw`（\_SimpleModifierData）：
		- `shared_base_add`/`addend`（float）：
		- `shared_percent_add`/`percent`（float）：
		- `unique_multiplier`/`multiplier`（float）：
		- `script_function`/`function`（String）：
- `effective_range`（\_SimpleModifierData）：
	- `shared_base_add`/`addend`（float）：
	- `shared_percent_add`/`percent`（float）：
	- `unique_multiplier`/`multiplier`（float）：
	- `script_function`/`function`（String）：
- ~~`weight`/`weight_modifier`~~
	- ~~`shared_base_add`/`addend`~~
	- ~~`shared_percent_add`/`percent`~~
	- ~~`unique_multiplier`/`multiplier`~~
- `muzzle`/`silence`（\_MuzzleModifierData）：
	- `fire_sound_type`/`use_silence_sound`（FireSoundType）：
- `aim_inaccuracy`（\_SimpleModifierData）：
	- `shared_base_add`/`addend`（float）：
	- `shared_percent_add`/`percent`（float）：
	- `unique_multiplier`/`multiplier`（float）：
	- `script_function`/`function`（String）：
- `sneak_inaccuracy`（\_SimpleModifierData）：
	- `shared_base_add`/`addend`（float）：
	- `shared_percent_add`/`percent`（float）：
	- `unique_multiplier`/`multiplier`（float）：
	- `script_function`/`function`（String）：
- `prone_inaccuracy`/`lie_inaccuracy`（\_SimpleModifierData）：
	- `shared_base_add`/`addend`（float）：
	- `shared_percent_add`/`percent`（float）：
	- `unique_multiplier`/`multiplier`（float）：
	- `script_function`/`function`（String）：
- `other_inaccuracy`/`inaccuracy`（\_SimpleModifierData）：
	- `shared_base_add`/`addend`（float）：
	- `shared_percent_add`/`percent`（float）：
	- `unique_multiplier`/`multiplier`（float）：
	- `script_function`/`function`（String）：
---
- `melee`（\_MeleeModifierData）：
	- `melee_damage`/`damage`（float）：
	- `melee_distance`/`distance`（float）：
	- `range_angle`（float）：
	- `damage_delay_seconds`/`prep`（float）：
	- `extra_cooldown`/`cooldown`（float）：
	- `knockback_strength`/`knockback`（float）：
	- `target_effect`/`effects`（List<\_TargetEffectData>）：
		- `effect_location`/`id`（ResourceLocation）：
		- `seconds`/`time`（int）：
		- `amplifier`（int）：
		- `hide_particles`（boolean）：
---
- `magazine_category`/`extended_mag_level`（MagazineCategory）：

```json
{
	"ads": {
		"shared_base_add": 0.0,
		"shared_percent_add": 0.0,
		"unique_multiplier": 1.0,
		"script_function": ""
	},
	
	"armor_ignore_percent": {
		"shared_base_add": 0.0,
		"shared_percent_add": 0.0,
		"unique_multiplier": 1.0,
		"script_function": ""
	},
	"headshot_multiplier": {
		"shared_base_add": 0.0,
		"shared_percent_add": 0.0,
		"unique_multiplier": 1.0,
		"script_function": ""
	},
	"damage_calculation": {
		"shared_base_add": 0.0,
		"shared_percent_add": 0.0,
		"unique_multiplier": 1.0,
		"script_function": ""
	},
	"bullet_speed": {
		"shared_base_add": 0.0,
		"shared_percent_add": 0.0,
		"unique_multiplier": 1.0,
		"script_function": ""
	},
	"pierce_count": {
		"shared_base_add": 0.0,
		"shared_percent_add": 0.0,
		"unique_multiplier": 1.0,
		"script_function": ""
	},
	"fire_aspect": {
		"ignite_entity": false,
		"ignite_block": false
	},
	"knockback_strength": {
		"shared_base_add": 0.0,
		"shared_percent_add": 0.0,
		"unique_multiplier": 1.0,
		"script_function": ""
	},
	"bullet_explosion": {
		"enable_explode": false,
		"explode_damage": {
			"shared_base_add": 0.0,
			"shared_percent_add": 0.0,
			"unique_multiplier": 1.0,
			"script_function": ""
		},
		"explode_scale": {
			"shared_base_add": 0.0,
			"shared_percent_add": 0.0,
			"unique_multiplier": 1.0,
			"script_function": ""
		},
		"max_delay_seconds": {
			"shared_base_add": 0.0,
			"shared_percent_add": 0.0,
			"unique_multiplier": 1.0,
			"script_function": ""
		},
		"enable_knockback": false,
		"enable_world_destruction": false
	},
	
	"rpm": {
		"shared_base_add": 0.0,
		"shared_percent_add": 0.0,
		"unique_multiplier": 1.0,
		"script_function": ""
	},
	"recoil_data": {
		"pitch_recoil": {
			"shared_base_add": 0.0,
			"shared_percent_add": 0.0,
			"unique_multiplier": 1.0,
			"script_function": ""
		},
		"yaw_recoil": {
			"shared_base_add": 0.0,
			"shared_percent_add": 0.0,
			"unique_multiplier": 1.0,
			"script_function": ""
		}
	},
	"effective_range": {
		"shared_base_add": 0.0,
		"shared_percent_add": 0.0,
		"unique_multiplier": 1.0,
		"script_function": ""
	},
	"muzzle": {
		"fire_sound_type": ""
	},
	"aim_inaccuracy": {
		"shared_base_add": 0.0,
		"shared_percent_add": 0.0,
		"unique_multiplier": 1.0,
		"script_function": ""
	},
	"sneak_inaccuracy": {
		"shared_base_add": 0.0,
		"shared_percent_add": 0.0,
		"unique_multiplier": 1.0,
		"script_function": ""
	},
	"prone_inaccuracy": {
		"shared_base_add": 0.0,
		"shared_percent_add": 0.0,
		"unique_multiplier": 1.0,
		"script_function": ""
	},
	"other_inaccuracy": {
		"shared_base_add": 0.0,
		"shared_percent_add": 0.0,
		"unique_multiplier": 1.0,
		"script_function": ""
	},
	
	"melee": {
		"melee_damage": 0.0,
		"melee_distance": 1.0,
		"range_angle": 30.0,
		"damage_delay_seconds": 0.1,
		"extra_cooldown": 0.0,
		"knockback_strength": 0.0,
		"target_effect": [
			{
				"effect_location": "",
				"seconds": 0,
				"amplifier": 0,
				"hide_particles": false
			}
		]
	},
	
	"magazine_category": ""
}
```

# English
> wiki verison: `0.0.1`.3

- `ads`(\_SimpleModifierData):
	- `shared_base_add`/`addend`(float):
	- `shared_percent_add`/`percent`(float):
	- `unique_multiplier`/`multiplier`(float):
	- `script_function`/`function`(String):
---
- `armor_ignore_percent`/`armor_ignore`(\_SimpleModifierData):
	- `shared_base_add`/`addend`(float):
	- `shared_percent_add`/`percent`(float):
	- `unique_multiplier`/`multiplier`(float):
	- `script_function`/`function`(String):
- `headshot_multiplier`/`head_shot`(\_SimpleModifierData):
	- `shared_base_add`/`addend`(float):
	- `shared_percent_add`/`percent`(float):
	- `unique_multiplier`/`multiplier`(float):
	- `script_function`/`function`(String):
- `damage_calculation`/`damage`(\_SimpleModifierData):
	- `shared_base_add`/`addend`(float):
	- `shared_percent_add`/`percent`(float):
	- `unique_multiplier`/`multiplier`(float):
	- `script_function`/`function`(String):
- `bullet_speed`/`ammo_speed`(\_SimpleModifierData):
	- `shared_base_add`/`addend`(float):
	- `shared_percent_add`/`percent`(float):
	- `unique_multiplier`/`multiplier`(float):
	- `script_function`/`function`(String):
- `pierce_count`/`pierce`(\_SimpleModifierData):
	- `shared_base_add`/`addend`(float):
	- `shared_percent_add`/`percent`(float):
	- `unique_multiplier`/`multiplier`(float):
	- `script_function`/`function`(String):
- `fire_aspect`/`ignite`(\_FireAspectModifierData):
	- `ignite_entity`/`entity`(boolean):
	- `ignite_block`/`block`(boolean):
- `knockback_strength`/`knockback`(\_SimpleModifierData):
	- `shared_base_add`/`addend`(float):
	- `shared_percent_add`/`percent`(float):
	- `unique_multiplier`/`multiplier`(float):
	- `script_function`/`function`(String):
- `bullet_explosion`/`explosion`(\_BulletExplosionModifierData):
	- `enable_explode`/`explode`(boolean):
	- `explode_damage`/`damage`(\_SimpleModifierData):
		- `shared_base_add`/`addend`(float):
		- `shared_percent_add`/`percent`(float):
		- `unique_multiplier`/`multiplier`(float):
		- `script_function`/`function`(String):
	- `explode_scale`/`radius`(\_SimpleModifierData):
		- `shared_base_add`/`addend`(float):
		- `shared_percent_add`/`percent`(float):
		- `unique_multiplier`/`multiplier`(float):
		- `script_function`/`function`(String):
	- `max_delay_seconds`/`delay`(\_SimpleModifierData):
		- `shared_base_add`/`addend`(float):
		- `shared_percent_add`/`percent`(float):
		- `unique_multiplier`/`multiplier`(float):
		- `script_function`/`function`(String):
	- `enable_knockback`/`knockback`(boolean):
	- `enable_world_destruction`/`destroy_block`(boolean):
---
- `rpm`(\_SimpleModifierData):
	- `shared_base_add`/`addend`(float):
	- `shared_percent_add`/`percent`(float):
	- `unique_multiplier`/`multiplier`(float):
	- `script_function`/`function`(String):
- `recoil_data`/`recoil`(\_RecoilDataModifierData):
	- `pitch_recoil`/`pitch`(\_SimpleModifierData):
		- `shared_base_add`/`addend`(float):
		- `shared_percent_add`/`percent`(float):
		- `unique_multiplier`/`multiplier`(float):
		- `script_function`/`function`(String):
	- `yaw_recoil`/`yaw`(\_SimpleModifierData):
		- `shared_base_add`/`addend`(float):
		- `shared_percent_add`/`percent`(float):
		- `unique_multiplier`/`multiplier`(float):
		- `script_function`/`function`(String):
- `effective_range`(\_SimpleModifierData):
	- `shared_base_add`/`addend`(float):
	- `shared_percent_add`/`percent`(float):
	- `unique_multiplier`/`multiplier`(float):
	- `script_function`/`function`(String):
- ~~`weight`/`weight_modifier`~~
	- ~~`shared_base_add`/`addend`~~
	- ~~`shared_percent_add`/`percent`~~
	- ~~`unique_multiplier`/`multiplier`~~
- `muzzle`/`silence`(\_MuzzleModifierData):
	- `fire_sound_type`/`use_silence_sound`(FireSoundType):
- `aim_inaccuracy`(\_SimpleModifierData):
	- `shared_base_add`/`addend`(float):
	- `shared_percent_add`/`percent`(float):
	- `unique_multiplier`/`multiplier`(float):
	- `script_function`/`function`(String):
- `sneak_inaccuracy`(\_SimpleModifierData):
	- `shared_base_add`/`addend`(float):
	- `shared_percent_add`/`percent`(float):
	- `unique_multiplier`/`multiplier`(float):
	- `script_function`/`function`(String):
- `prone_inaccuracy`/`lie_inaccuracy`(\_SimpleModifierData):
	- `shared_base_add`/`addend`(float):
	- `shared_percent_add`/`percent`(float):
	- `unique_multiplier`/`multiplier`(float):
	- `script_function`/`function`(String):
- `other_inaccuracy`/`inaccuracy`(\_SimpleModifierData):
	- `shared_base_add`/`addend`(float):
	- `shared_percent_add`/`percent`(float):
	- `unique_multiplier`/`multiplier`(float):
	- `script_function`/`function`(String):
---
- `melee`(\_MeleeModifierData):
	- `melee_damage`/`damage`(float):
	- `melee_distance`/`distance`(float):
	- `range_angle`(float):
	- `damage_delay_seconds`/`prep`(float):
	- `extra_cooldown`/`cooldown`(float):
	- `knockback_strength`/`knockback`(float):
	- `target_effect`/`effects`(List<\_TargetEffectData>):
		- `effect_location`/`id`(ResourceLocation):
		- `seconds`/`time`(int):
		- `amplifier`(int):
		- `hide_particles`(boolean):
---
- `magazine_category`/`extended_mag_level`(MagazineCategory):

```json
{
	"ads": {
		"shared_base_add": 0.0,
		"shared_percent_add": 0.0,
		"unique_multiplier": 1.0,
		"script_function": ""
	},
	
	"armor_ignore_percent": {
		"shared_base_add": 0.0,
		"shared_percent_add": 0.0,
		"unique_multiplier": 1.0,
		"script_function": ""
	},
	"headshot_multiplier": {
		"shared_base_add": 0.0,
		"shared_percent_add": 0.0,
		"unique_multiplier": 1.0,
		"script_function": ""
	},
	"damage_calculation": {
		"shared_base_add": 0.0,
		"shared_percent_add": 0.0,
		"unique_multiplier": 1.0,
		"script_function": ""
	},
	"bullet_speed": {
		"shared_base_add": 0.0,
		"shared_percent_add": 0.0,
		"unique_multiplier": 1.0,
		"script_function": ""
	},
	"pierce_count": {
		"shared_base_add": 0.0,
		"shared_percent_add": 0.0,
		"unique_multiplier": 1.0,
		"script_function": ""
	},
	"fire_aspect": {
		"ignite_entity": false,
		"ignite_block": false
	},
	"knockback_strength": {
		"shared_base_add": 0.0,
		"shared_percent_add": 0.0,
		"unique_multiplier": 1.0,
		"script_function": ""
	},
	"bullet_explosion": {
		"enable_explode": false,
		"explode_damage": {
			"shared_base_add": 0.0,
			"shared_percent_add": 0.0,
			"unique_multiplier": 1.0,
			"script_function": ""
		},
		"explode_scale": {
			"shared_base_add": 0.0,
			"shared_percent_add": 0.0,
			"unique_multiplier": 1.0,
			"script_function": ""
		},
		"max_delay_seconds": {
			"shared_base_add": 0.0,
			"shared_percent_add": 0.0,
			"unique_multiplier": 1.0,
			"script_function": ""
		},
		"enable_knockback": false,
		"enable_world_destruction": false
	},
	
	"rpm": {
		"shared_base_add": 0.0,
		"shared_percent_add": 0.0,
		"unique_multiplier": 1.0,
		"script_function": ""
	},
	"recoil_data": {
		"pitch_recoil": {
			"shared_base_add": 0.0,
			"shared_percent_add": 0.0,
			"unique_multiplier": 1.0,
			"script_function": ""
		},
		"yaw_recoil": {
			"shared_base_add": 0.0,
			"shared_percent_add": 0.0,
			"unique_multiplier": 1.0,
			"script_function": ""
		}
	},
	"effective_range": {
		"shared_base_add": 0.0,
		"shared_percent_add": 0.0,
		"unique_multiplier": 1.0,
		"script_function": ""
	},
	"muzzle": {
		"fire_sound_type": ""
	},
	"aim_inaccuracy": {
		"shared_base_add": 0.0,
		"shared_percent_add": 0.0,
		"unique_multiplier": 1.0,
		"script_function": ""
	},
	"sneak_inaccuracy": {
		"shared_base_add": 0.0,
		"shared_percent_add": 0.0,
		"unique_multiplier": 1.0,
		"script_function": ""
	},
	"prone_inaccuracy": {
		"shared_base_add": 0.0,
		"shared_percent_add": 0.0,
		"unique_multiplier": 1.0,
		"script_function": ""
	},
	"other_inaccuracy": {
		"shared_base_add": 0.0,
		"shared_percent_add": 0.0,
		"unique_multiplier": 1.0,
		"script_function": ""
	},
	
	"melee": {
		"melee_damage": 0.0,
		"melee_distance": 1.0,
		"range_angle": 30.0,
		"damage_delay_seconds": 0.1,
		"extra_cooldown": 0.0,
		"knockback_strength": 0.0,
		"target_effect": [
			{
				"effect_location": "",
				"seconds": 0,
				"amplifier": 0,
				"hide_particles": false
			}
		]
	},
	
	"magazine_category": ""
}
```