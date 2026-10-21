[English](#English)

# 开镜配置
> wiki 版本：`26.10.27`.3

路径：`Zoom`
- `EnableScrollingZoom`（bool）：是否启用鼠标滚轮缩放
- `ScrollingZoomCycle`（bool）：鼠标滚轮缩放是否循环
- `InvertScrollingZoom`（bool）：是否反转鼠标滚轮缩放
- `ScreenDistanceCoefficient`（浮点，0.0~3.0）：开镜时的屏幕距离系数，使用MDV标准
- `ZoomSensitivityBaseMultiplier`（浮点，0.0~2.0）：开镜灵敏度基础系数
```toml
[Zoom]
	EnableScrollingZoom = true
	ScrollingZoomCycle = false
	InvertScrollingZoom = true
	ScreenDistanceCoefficient = 1.33
	ZoomSensitivityBaseMultiplier = 1.0
```

# English
> wiki version: `26.10.28.1`.4

Path: `Zoom`
- `EnableScrollingZoom` (bool): Whether to enable zoom switching with the mouse scroll wheel
- `ScrollingZoomCycle` (bool): Whether mouse scroll wheel zooming cycles
- `InvertScrollingZoom` (bool): Whether to invert the mouse scroll wheel zoom direction
- `ScreenDistanceCoefficient` (float, 0.0~3.0): Screen distance coefficient for zoom, using MDV standard
- `ZoomSensitivityBaseMultiplier` (float, 0.0~2.0): Zoom sensitivity is multiplied by this factor
```toml
[Zoom]
	EnableScrollingZoom = true
	ScrollingZoomCycle = false
	InvertScrollingZoom = true
	ScreenDistanceCoefficient = 1.33
	ZoomSensitivityBaseMultiplier = 1.0
```
