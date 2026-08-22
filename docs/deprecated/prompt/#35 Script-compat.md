> #35 提示词

```
阅读dev.xcolorful.customgun.core.api.gun.script.context包下的GunScriptApi、_GunScriptBackCompat、IGunScriptBackCompat、IGunScriptContextAccess这四个类，完成以下任务：
- 将_GunScriptBackCompat中protected static的方法移到IGunScriptBackCompat，接口参数同GunScriptApi里的
- 使用IGunScriptContextAccess来代替直接访问GunScriptApi成员变量，如果遇到IGunScriptContextAccess里尚未提供的变量，则留空并附上注释即可
- _GunScriptBackCompat中的private static方法，将其参数改成传入IGunScriptBackCompat后，将其访问改为default static，并让IGunScriptBackCompat的default方法调用
- 对可能返回LuaValue.NIL的函数，添加@Nullable
- 移植完成后，_GunScriptBackCompat已有的实现不要删除，我需要对照检查
- 对于GunScriptApi已被IGunScriptBackCompat default代理，而不是留空并附上注释的接口，删除

编辑规范：
- 对于从IGunScriptContextAccess获取的变量，如果为@Nullable，则必须在获取时添加@Nullable装饰器
- IGunScriptContextAccess的getter必须单独开一行赋值成局部变量，不得inline进传参

你能浏览的上下文仅限于以上提到的类，不得引入外部类或思考更多

IGunScriptBackCompat已经给了一个getCachedProperty作为示范，现在立即完成剩余的，完成后交我检查
```

```
把_GunScriptBackCompat原方法的javadoc复制到IGunScriptBackCompat对应位置
```
