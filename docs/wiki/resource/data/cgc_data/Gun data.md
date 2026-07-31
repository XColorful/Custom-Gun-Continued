[English](#English)

# 枪械数据
> wiki 版本：`0.0.1`.5

- `bullet_data`/`bullet`（\_BulletData）：
	- `display_damage`/`damage`（float）：
	- `bullet_skill`/`extra_damage`（\_BulletSkillData）：
		- `armor_ignore_percent`/`armor_ignore`（float）：
		- `headshot_multiplier`/`head_shot_multiplier`（float）：
		- `damage_calculation`/`damage_adjust`（List\<\_DistanceDamageData>）：
			- `distance`（float）：
			- `damage`（float）：
	---
	- `lifetime_seconds`/`life`（float）：
	- `bullet_speed`/`speed`（float）：
	- `gravity`（float）：
	- `friction`（float）：
	---
	- `bullet_split_amount`/`bullet_amount`（int）：
	- `pierce_count`/`pierce`（int）：
	- `tracer_interval`/`tracer_count_interval`（int）：
	---
	- `fire_aspect`/`ignite`（boolean）：
	- `fire_aspect_seconds`/`ignite_entity_time`（int）：
	- `knockback_strength`/`knockback`（float）：
	- `bullet_explosion`/`explosion`（\_ExplosionData）：
		- `enable_explode`/`explode`（boolean）：
		---
		- `explode_damage`/`damage`（float）：
		- `explode_scale`/`radius`（float）：
		- `max_delay_seconds`/`delay`（float）：
		---
		- `enable_knockback`/`knockback`（boolean）：
		- `enable_world_destruction`/`destroy_block`（boolean）：
- `ammo_location`/`ammo`（ResourceLocation）：
- `bolt_type`/`bolt`（BoltType）：
- 
- `rpm`（int）：
- `inaccuracy_data`/`inaccuracy`（\_InaccuracyData）：
	- `stand`（float）：
	- `move`（float）：
	- `ride`（float）：
	- `sneak`（float）：
	- `prone`/`lie`（float）：
	- `aim`（float）：
	- `levitate`（float）：
- `recoil_data`/`recoil`（\_RecoilData）：
	- `pitch_recoil`/`pitch`（List\<\_RecoilEntryData>）：
		- `time`（float）：
		- `value`（float[]）：
	- `yaw_recoil`/`yaw`（List\<\_RecoilEntryData>）：
		- `time`（float）：
		- `value`（float[]）：
- `prone_recoil_multiplier`/`crawl_recoil_multiplier`（float）：
- 
- `weight`（float）：
- `movement_data`/`movement_speed`（\_MovementData）：
	- `base`（float）：
	- `aim`（float）：
	- `reload`（float）：
- 
- `fire_sound_data`/`fire_sound`（\_FireSoundData）：
	- `normal_multiplier`/`fire_multiplier`（float）：
	- `silenced_multiplier`/`silence_multiplier`（float）：
- `hurt_bob_tweak_multiplier`（float）：
- 
- `reload_data`/`reload`（\_ReloadData）：
	- `ammo_feed_type`/`type`（AmmoFeedType）：
	- `free_ammo_feed`/`infinite`（boolean）：
	- `reload_feed`/`feed`（\_ReloadFeedData）：
		- `empty`（float）：
		- `tactical`（float）：
	- `reload_cooldown`/`cooldown`（\_ReloadCooldownData）：
		- `empty`（float）：
		- `tactical`（float）：
---
- `script_location`/`script`（ResourceLocation）：
- `script_param`（Map\<String, Object>）：
---
- `default_fire_mode_type`/`default_fire_mode`（FireModeType）：
- `fire_mode_types`/`fire_mode`（List\<FireModeType>）：
- `fire_mode_adjust_data`/`fire_mode_adjust`（Map\<FireModeType, \_FireModeAdjustData>）：
	- `rpm`（int）：
	- `damage`（float）：
	- `bullet_speed`/`speed`（float）：
	- `knockback_strength`/`knockback`（float）：
	- `armor_ignore_percent`/`armor_ignore`（float）：
	- `headshot_multiplier`/`head_shot_multiplier`（float）：
	- `aim_inaccuracy`（float）：
	- `other_inaccuracy`（float）：
- `burst_data`（\_BurstData）：
	- `bpm`（int）：
	- `burst_amount`/`count`（int）：
	- `shoot_interval_seconds`/`min_interval`（float）：
	- `continuous_shoot`（boolean）：
---
- `melee_data`/`melee`（\_MeleeData）：
	- `gun_base_length`/`distance`（float）：
	- ~~`cooldown`~~
	- `default_melee_data`/`default`（\_DefaultMeleeData）：
		- `melee_type`/`animation_type`（MeleeType）：
		---
		- `melee_damage`/`damage`（float）：
		- `melee_distance`/`distance`（float）：
		- `range_angle`（float）：
		---
		- `damage_delay_seconds`/`prep`（float）：
		- `base_cooldown`/`cooldown`（float）：
		---
		- `knockback_strength`/`knockback`（float）：
- `heat_data`/`heat`（\_HeatData）：
	- `max_heat`/`max`（float）：
	- `heat_per_shot`/`per_shot`（float）：
	- `min_rpm_by_heat`/`min_rpm_mod`（float）：
	- `max_rpm_by_heat`/`max_rpm_mod`（float）：
	- `min_inaccuracy_by_heat`/`min_inaccuracy`（float）：
	- `max_inaccuracy_by_heat`/`max_inaccuracy`（float）：
	- `overheat_locktime_ms`/`over_heat_time`（long）：
	- `cooling_delay_ms`/`cooling_delay`（long）：
	- `cooling_speed_multiplier`/`cooling_multiplier`（float）：
- `charging_data`/`charging`（Map\<FireModeType, \_ChargingData>）：
	- `charge_type`/`type`（ChargeType）：
	- `max_charge`（float）：
	- `fire_threshold`（float）：
	- `recover_by_fire`/`decrease_on_fire`（float）：
	- `charge_per_tick`/`increase_per_tick`（float）：
	- `recover_per_tick`/`decrease_per_tick`（float）：
	- `enable_charge_during_cooldown`/`charge_during_cooldown`（boolean）：
---
- `allow_attachment_types`（List\<AttachmentCategory>）：
- `exclusive_attachments`（Map\<ResourceLocation, AttachmentData>）：
	- [配件数据](https://github.com/XColorful/Custom-Gun-Continued/wiki/Attachment-data)
- `default_mag_size`/`ammo_amount`（int）：
- `extended_mag_ammo_size`/`extended_mag_ammo_amount`（int[]）：
- `builtin_attachments`（Map\<AttachmentCategory, ResourceLocation>）：
---
- `enable_prone`/`can_crawl`（boolean）：
- `enable_slide`/`can_slide`（boolean）：
---
- `draw_time`（float）：
- `put_away_time`（float）：
- `sprint_time`（float）：
- `aim_time`（float）：
- `bolt_action_time`（float）：
- `bolt_feed_time`（float）：

```json
{
	"bullet_data": {
		"display_damage": 0.0,
		"bullet_skill": {
			"armor_ignore_percent": 0.0,
			"headshot_multiplier": 0.0,
			"damage_calculation": [
				{
					"distance": 0.0,
					"damage": 0.0
				}
			]
		},

		"lifetime_seconds": 0.0,
		"bullet_speed": 0.0,
		"gravity": 0.0,
		"friction": 0.0,

		"bullet_split_amount": 0,
		"pierce_count": 0,
		"tracer_interval": 0,

		"fire_aspect": false,
		"fire_aspect_seconds": 0,
		"knockback_strength": 0.0,
		"bullet_explosion": {
			"enable_explode": false,

			"explode_damage": 0.0,
			"explode_scale": 0.0,
			"max_delay_seconds": 0.0,

			"enable_knockback": false,
			"enable_world_destruction": false
		}
	},
	"ammo_location": "namespace:location",
	"bolt_type": "",

	"rpm": 0,
	"inaccuracy_data": {
		"stand": 0.0,
		"move": 0.0,
		"ride": 0.0,
		"sneak": 0.0,
		"prone": 0.0,
		"aim": 0.0,
		"levitate": 0.0
	},
	"recoil_data": {
		"pitch_recoil": [
			{
				"time": 0.0,
				"value": [0.0, 0.0]
			}
		],
		"yaw_recoil": [
			{
				"time": 0.0,
				"value": [0.0, 0.0]
			}
		]
	},
	"prone_recoil_multiplier": 0.0,

	"weight": 0.0,
	"movement_data": {
		"base": 0.0,
		"aim": 0.0,
		"reload": 0.0
	},

	"fire_sound_data": {
		"normal_multiplier": 0.0,
		"silenced_multiplier": 0.0
	},
	"hurt_bob_tweak_multiplier": 0.0,

	"reload_data": {
		"ammo_feed_type": "",
		"free_ammo_feed": false,
		"reload_feed": {
			"empty": 0.0,
			"tactical": 0.0
		},
		"reload_cooldown": {
			"empty": 0.0,
			"tactical": 0.0
		}
	},
	
	"script_location": "namespace:location",
	"script_param": {
		"String": "",
		"Double": 0.00,
		"Boolean": false
	},
	
	"default_fire_mode_type": "",
	"fire_mode_types": [
		""
	],
	"fire_mode_adjust_data": {
		"": {
			"rpm": 0,
			"damage": 0.0,
			"bullet_speed": 0.0,
			"knockback_strength": 0.0,
			"armor_ignore_percent": 0.0,
			"headshot_multiplier": 0.0,
			"aim_inaccuracy": 0.0,
			"other_inaccuracy": 0.0
		}
	},
	"burst_data": {
		"bpm": 0,
		"burst_amount": 0,
		"shoot_interval_seconds": 0.0,
		"continuous_shoot": false
	},
	
	"melee_data": {
		"gun_base_length": 0.0,
		"default_melee_data": {
			"melee_type": "",
			"melee_damage": 0.0,
			"melee_distance": 0.0,
			"range_angle": 0.0,
			"damage_delay_seconds": 0.0,
			"base_cooldown": 0.0,
			"knockback_strength": 0.0
		}
	},
	"heat_data": {
		"max_heat": 0.0,
		"heat_per_shot": 0.0,
		"min_rpm_by_heat": 0.0,
		"max_rpm_by_heat": 0.0,
		"min_inaccuracy_by_heat": 0.0,
		"max_inaccuracy_by_heat": 0.0,
		"overheat_locktime_ms": 0,
		"cooling_delay_ms": 0,
		"cooling_speed_multiplier": 0.0
	},
	"charging_data": {
		"": {
			"charge_type": "",
			"max_charge": 0.0,
			"fire_threshold": 0.0,
			"recover_by_fire": 0.0,
			"charge_per_tick": 0.0,
			"recover_per_tick": 0.0,
			"enable_charge_during_cooldown": true
		}
	},
	
	"allow_attachment_types": [
		""
	],
	"exclusive_attachments": {
		"": {
			// 配件数据
		}
	},
	"default_mag_size": 0,
	"extended_mag_ammo_size": [
		0
	],
	"builtin_attachments": {
		"": "namespace:location"
	},
	
	"enable_prone": false,
	"enable_slide": false,
	
	"draw_time": 0.0,
	"put_away_time": 0.0,
	"sprint_time": 0.0,
	"aim_time": 0.0,
	"bolt_action_time": 0.0,
	"bolt_feed_time": 0.0
}
```

# English
> wiki verison: `0.0.1`.5

- `bullet_data`/`bullet`( \_BulletData):
	- `display_damage`/`damage`(float):
	- `bullet_skill`/`extra_damage`( \_BulletSkillData):
		- `armor_ignore_percent`/`armor_ignore`(float):
		- `headshot_multiplier`/`head_shot_multiplier`(float):
		- `damage_calculation`/`damage_adjust`(List\< \_DistanceDamageData>):
			- `distance`(float):
			- `damage`(float):
	---
	- `lifetime_seconds`/`life`(float):
	- `bullet_speed`/`speed`(float):
	- `gravity`(float):
	- `friction`(float):
	---
	- `bullet_split_amount`/`bullet_amount`(int):
	- `pierce_count`/`pierce`(int):
	- `tracer_interval`/`tracer_count_interval`(int):
	---
	- `fire_aspect`/`ignite`(boolean):
	- `fire_aspect_seconds`/`ignite_entity_time`(int):
	- `knockback_strength`/`knockback`(float):
	- `bullet_explosion`/`explosion`( \_ExplosionData):
		- `enable_explode`/`explode`(boolean):
		---
		- `explode_damage`/`damage`(float):
		- `explode_scale`/`radius`(float):
		- `max_delay_seconds`/`delay`(float):
		---
		- `enable_knockback`/`knockback`(boolean):
		- `enable_world_destruction`/`destroy_block`(boolean):
- `ammo_location`/`ammo`(ResourceLocation):
- `bolt_type`/`bolt`(BoltType):
- 
- `rpm`(int):
- `inaccuracy_data`/`inaccuracy`( \_InaccuracyData):
	- `stand`(float):
	- `move`(float):
	- `ride`(float):
	- `sneak`(float):
	- `prone`/`lie`(float):
	- `aim`(float):
	- `levitate`(float):
- `recoil_data`/`recoil`( \_RecoilData):
	- `pitch_recoil`/`pitch`(List\< \_RecoilEntryData>):
		- `time`(float):
		- `value`(float[]):
	- `yaw_recoil`/`yaw`(List\< \_RecoilEntryData>):
		- `time`(float):
		- `value`(float[]):
- `prone_recoil_multiplier`/`crawl_recoil_multiplier`(float):
- 
- `weight`(float):
- `movement_data`/`movement_speed`( \_MovementData):
	- `base`(float):
	- `aim`(float):
	- `reload`(float):
- 
- `fire_sound_data`/`fire_sound`( \_FireSoundData):
	- `normal_multiplier`/`fire_multiplier`(float):
	- `silenced_multiplier`/`silence_multiplier`(float):
- `hurt_bob_tweak_multiplier`(float):
- 
- `reload_data`/`reload`( \_ReloadData):
	- `ammo_feed_type`/`type`(AmmoFeedType):
	- `free_ammo_feed`/`infinite`(boolean):
	- `reload_feed`/`feed`( \_ReloadFeedData):
		- `empty`(float):
		- `tactical`(float):
	- `reload_cooldown`/`cooldown`( \_ReloadCooldownData):
		- `empty`(float):
		- `tactical`(float):
---
- `script_location`/`script`(ResourceLocation):
- `script_param`(Map\<String, Object>):
---
- `default_fire_mode_type`/`default_fire_mode`(FireModeType):
- `fire_mode_types`/`fire_mode`(List\<FireModeType>):
- `fire_mode_adjust_data`/`fire_mode_adjust`(Map\<FireModeType, \_FireModeAdjustData>):
	- `rpm`(int):
	- `damage`(float):
	- `bullet_speed`/`speed`(float):
	- `knockback_strength`/`knockback`(float):
	- `armor_ignore_percent`/`armor_ignore`(float):
	- `headshot_multiplier`/`head_shot_multiplier`(float):
	- `aim_inaccuracy`(float):
	- `other_inaccuracy`(float):
- `burst_data`( \_BurstData):
	- `bpm`(int):
	- `burst_amount`/`count`(int):
	- `shoot_interval_seconds`/`min_interval`(float):
	- `continuous_shoot`(boolean):
---
- `melee_data`/`melee`( \_MeleeData):
	- `gun_base_length`/`distance`(float):
	- ~~`cooldown`~~
	- `default_melee_data`/`default`( \_DefaultMeleeData):
		- `melee_type`/`animation_type`(MeleeType):
		---
		- `melee_damage`/`damage`(float):
		- `melee_distance`/`distance`(float):
		- `range_angle`(float):
		---
		- `damage_delay_seconds`/`prep`(float):
		- `base_cooldown`/`cooldown`(float):
		---
		- `knockback_strength`/`knockback`(float):
- `heat_data`/`heat`（\_HeatData）：
	- `max_heat`/`max`（float）：
	- `heat_per_shot`/`per_shot`（float）：
	- `min_rpm_by_heat`/`min_rpm_mod`（float）：
	- `max_rpm_by_heat`/`max_rpm_mod`（float）：
	- `min_inaccuracy_by_heat`/`min_inaccuracy`（float）：
	- `max_inaccuracy_by_heat`/`max_inaccuracy`（float）：
	- `overheat_locktime_ms`/`over_heat_time`（long）：
	- `cooling_delay_ms`/`cooling_delay`（long）：
	- `cooling_speed_multiplier`/`cooling_multiplier`（float）：
- `charging_data`/`charging`（Map\<FireModeType, \_ChargingData>）：
	- `charge_type`/`type`（ChargeType）：
	- `max_charge`（float）：
	- `fire_threshold`（float）：
	- `recover_by_fire`/`decrease_on_fire`（float）：
	- `charge_per_tick`/`increase_per_tick`（float）：
	- `recover_per_tick`/`decrease_per_tick`（float）：
	- `enable_charge_during_cooldown`/`charge_during_cooldown`（boolean）：
---
- `allow_attachment_types`(List\<AttachmentCategory>):
- `exclusive_attachments`(Map\<ResourceLocation, AttachmentData>):
	- [Attachment Data](https://github.com/XColorful/Custom-Gun-Continued/wiki/Attachment-data#English)
- `default_mag_size`/`ammo_amount`(int):
- `extended_mag_ammo_size`/`extended_mag_ammo_amount`(int[]):
- `builtin_attachments`(Map\<AttachmentCategory, ResourceLocation>):
---
- `enable_prone`/`can_crawl`(boolean):
- `enable_slide`/`can_slide`(boolean):
---
- `draw_time`(float):
- `put_away_time`(float):
- `sprint_time`(float):
- `aim_time`(float):
- `bolt_action_time`(float):
- `bolt_feed_time`(float):

```json
{
	"bullet_data": {
		"display_damage": 0.0,
		"bullet_skill": {
			"armor_ignore_percent": 0.0,
			"headshot_multiplier": 0.0,
			"damage_calculation": [
				{
					"distance": 0.0,
					"damage": 0.0
				}
			]
		},

		"lifetime_seconds": 0.0,
		"bullet_speed": 0.0,
		"gravity": 0.0,
		"friction": 0.0,

		"bullet_split_amount": 0,
		"pierce_count": 0,
		"tracer_interval": 0,

		"fire_aspect": false,
		"fire_aspect_seconds": 0,
		"knockback_strength": 0.0,
		"bullet_explosion": {
			"enable_explode": false,

			"explode_damage": 0.0,
			"explode_scale": 0.0,
			"max_delay_seconds": 0.0,

			"enable_knockback": false,
			"enable_world_destruction": false
		}
	},
	"ammo_location": "namespace:location",
	"bolt_type": "",

	"rpm": 0,
	"inaccuracy_data": {
		"stand": 0.0,
		"move": 0.0,
		"ride": 0.0,
		"sneak": 0.0,
		"prone": 0.0,
		"aim": 0.0,
		"levitate": 0.0
	},
	"recoil_data": {
		"pitch_recoil": [
			{
				"time": 0.0,
				"value": [0.0, 0.0]
			}
		],
		"yaw_recoil": [
			{
				"time": 0.0,
				"value": [0.0, 0.0]
			}
		]
	},
	"prone_recoil_multiplier": 0.0,

	"weight": 0.0,
	"movement_data": {
		"base": 0.0,
		"aim": 0.0,
		"reload": 0.0
	},

	"fire_sound_data": {
		"normal_multiplier": 0.0,
		"silenced_multiplier": 0.0
	},
	"hurt_bob_tweak_multiplier": 0.0,

	"reload_data": {
		"ammo_feed_type": "",
		"free_ammo_feed": false,
		"reload_feed": {
			"empty": 0.0,
			"tactical": 0.0
		},
		"reload_cooldown": {
			"empty": 0.0,
			"tactical": 0.0
		}
	},
	
	"script_location": "namespace:location",
	"script_param": {
		"String": "",
		"Double": 0.00,
		"Boolean": false
	},
	
	"default_fire_mode_type": "",
	"fire_mode_types": [
		""
	],
	"fire_mode_adjust_data": {
		"": {
			"rpm": 0,
			"damage": 0.0,
			"bullet_speed": 0.0,
			"knockback_strength": 0.0,
			"armor_ignore_percent": 0.0,
			"headshot_multiplier": 0.0,
			"aim_inaccuracy": 0.0,
			"other_inaccuracy": 0.0
		}
	},
	"burst_data": {
		"bpm": 0,
		"burst_amount": 0,
		"shoot_interval_seconds": 0.0,
		"continuous_shoot": false
	},
	
	"melee_data": {
		"gun_base_length": 0.0,
		"default_melee_data": {
			"melee_type": "",
			"melee_damage": 0.0,
			"melee_distance": 0.0,
			"range_angle": 0.0,
			"damage_delay_seconds": 0.0,
			"base_cooldown": 0.0,
			"knockback_strength": 0.0
		}
	},
	"heat_data": {
		"max_heat": 0.0,
		"heat_per_shot": 0.0,
		"min_rpm_by_heat": 0.0,
		"max_rpm_by_heat": 0.0,
		"min_inaccuracy_by_heat": 0.0,
		"max_inaccuracy_by_heat": 0.0,
		"overheat_locktime_ms": 0,
		"cooling_delay_ms": 0,
		"cooling_speed_multiplier": 0.0
	},
	"charging_data": {
		"": {
			"charge_type": "",
			"max_charge": 0.0,
			"fire_threshold": 0.0,
			"recover_by_fire": 0.0,
			"charge_per_tick": 0.0,
			"recover_per_tick": 0.0,
			"enable_charge_during_cooldown": true
		}
	},
	
	"allow_attachment_types": [
		""
	],
	"exclusive_attachments": {
		"": {
			// Attachment data
		}
	},
	"default_mag_size": 0,
	"extended_mag_ammo_size": [
		0
	],
	"builtin_attachments": {
		"": "namespace:location"
	},
	
	"enable_prone": false,
	"enable_slide": false,
	
	"draw_time": 0.0,
	"put_away_time": 0.0,
	"sprint_time": 0.0,
	"aim_time": 0.0,
	"bolt_action_time": 0.0,
	"bolt_feed_time": 0.0
}
```