# 

```
测试一下当前是否有加载一个叫read-docs-to-locate-code的Skill，如果有就阅读并告诉我大致内容即可。
```

# 

Skill `read-docs-to-locate-code` 确实已加载。我读一下它的内容再总结给你。

Skill `read-docs-to-locate-code` 已加载并读取成功。以下是其大致内容：

**用途**:通过阅读项目文档来快速路由并定位源码位置,辅助浏览源码任务。

**核心思路**

- 项目文档目录结构与代码包目录保持一致。例如 `./docs/architecture/core/` 对应 `core/src/main/java/dev/xcolorful/customgun/core/`(`dev.xcolorful.customgun.core` 包)。
- 利用这个映射关系,把文档文件夹路径对应到 Java 包路径,从而定位源码。

**路由逻辑**

1. 从 `./docs/` 开始逐层查阅文档。
2. core 和 client 模块架构总览分别在 `./docs/architecture/core/Home.md` 和 `./docs/architecture/client/Home.md`。
3. 架构/API 文档最深层通常是**无内容的占位文件**——设计意图就是靠它的目录路径对应到代码包目录,然后去读代码里最新的 Javadoc/注释。文档非空时(如模块系统性说明),通常整文件夹都是相关文档。

**读取路径决策(依据用户提示词)**

- 用户指定了具体文档 → 每个上层目录至少读一个最可能相关的文档。
- 用户提到文档但没给路径 → 逐层查找;即使没找到,也用过程中定位到的目录辅助确认包目录。

**Wiki 注意事项**

- 一般**不读** `./docs/wiki/` 下的内容;需修改玩家可见内容时才主动浏览。
- 若任务涉及**改 Config / 改 Command / 改 ResourcePojo 子类的字段 Tag 名**,需要同步修改 Wiki;除非用户另有说明。

简单说:这是一条「文档路径 ↔ 代码包路径」的路由映射规则,让我在接到浏览源码的任务时能先查文档定位目录、再跳去读对应代码,而不是盲目搜索。

# 

```
这个skill是否正确加载？你的视角里是否跟其他skill一样被加载？
```

# 

是的,这个 skill 已正确加载,加载方式跟其他 skill 完全一致。

从我的视角看具体是这样:

**它出现在启动时的可用 skill 列表里**(描述行:`通过阅读项目文档实现快速路由并定位源码位置`),说明已被 harness 识别并登记,我可以随时用 `Skill` 工具按名字调用它。刚才我读的 `.claude/skills/read-docs-to-locate-code/SKILL.md` 就是它的定义文件。

几个区分点(供你理解"加载"的层级):

- **有前缀 / 无前缀**:本会话里能看到三种 skill——插件 skill 带 `anthropic-skills:` 前缀(如 `docx`、`pdf`),子目录作用域 skill 会带目录路径前缀,而 `read-docs-to-locate-code` 属于**无前缀的项目级 skill**,和 `dataviz`、`update-config`、`run`、`security-review` 等处于同一层,都是从 `.claude/skills/` 根目录加载的。所以它就是普通的标准加载,没有特殊路径。
    
- **"已登记" ≠ "已载入正文"**:所有 skill(包括 anthropic 插件 skill)都是一样的惰性机制——会话开始时我的上下文里只有它的**名称 + 一行描述**;完整的 SKILL.md 指令正文是在真正调用(`/read-docs-to-locate-code` 或我按需触发)那一刻才载入当前回合的。刚才我是主动 Read 了文件才看到全文,并不是启动时就加载在上下文里。
    
- git status 里的 `AM`(staged + modified)只是说明你最近改过这个文件还没提交,不影响加载。
    

结论:它被加载的方式与其他 skill 无差别,可以正常调用。需要我现在实际触发一次来验证运行效果吗?
