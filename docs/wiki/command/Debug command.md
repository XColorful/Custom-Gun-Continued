[English](#English)

# 调试指令
> wiki 版本：`0.0.1`.0

## 服务端指令

### 设置调试模式
> _/customgun debug [enable]_

开启或关闭调试模式
- `enable`（bool）：调试模式开关

### 测试IO
> _/customgun debug testIO [lenient]_

测试JSON输入/输出序列化
- `lenient`（bool）：是否宽松模式

### 测试枪械数据
> _/customgun debug testGunData [rl]_

测试读取指定枪械数据并输出到文件
- `rl`（字符串）：枪械数据的ResourceLocation

### 导出全部数据
> _/customgun debug testAllData [indent] [path]_

将所有已加载的数据包导出到指定路径
- `indent`（整数）：JSON缩进空格数
- `path`（字符串）：导出目录路径

### 测试Mixin
> _/customgun debug mixinTest [target]_

验证Mixin是否正确应用
- `target`（字面量）：测试目标，可选值为`ILivingShooter`、`GunProjectile`

## 客户端指令

### 测试声音文件
> _/customgun debug testGetSound [rl]_

测试指定声音文件是否可被资源管理器加载
- `rl`（字符串）：声音文件的ResourceLocation

# English
> wiki verison: `0.0.1`.0

## Server command

### Set debug mode
> _/customgun debug [enable]_

Enable or disable debug mode
- `enable` (bool): Debug mode toggle

### Test IO
> _/customgun debug testIO [lenient]_

Test JSON input/output serialization
- `lenient` (bool): Lenient mode

### Test gun data
> _/customgun debug testGunData [rl]_

Read specified gun data and output to file
- `rl` (string): ResourceLocation of the gun data

### Export all data
> _/customgun debug testAllData [indent] [path]_

Export all loaded data packs to the specified path
- `indent` (integer): Number of JSON indent spaces
- `path` (string): Export directory path

### Test Mixin
> _/customgun debug mixinTest [target]_

Verify that Mixin is correctly applied
- `target` (literal): Test target, options are `ILivingShooter`, `GunProjectile`

## Client command

### Test sound file
> _/customgun debug testGetSound [rl]_

Test whether the specified sound file can be loaded by the resource manager
- `rl` (string): ResourceLocation of the sound file
