> #79 提示词

# 

```
将当前最新commit的wiki部分同步到.wiki仓库，注意文件名带横杠
```

# 

我先确认两个仓库的状态以及最新提交涉及的 wiki 文件。

源码仓库最新提交正是 wiki 变更。我对比两边的 Home 与 Sidebar，再按横杠命名同步。

现在把三处变更写入 wiki 仓库：版本号、两个导航链接，以及新建 `Shooter-config.md`。

已把最新提交 `349510b48` 的 3 个 wiki 文件同步到 `.wiki` 仓库：

- [Home.md](../Custom-Gun-Continued.wiki/Home.md)：中英文版本号更新为 `26.10.29`，并在双端配置/Common configuration 下加入射手配置链接
- [_Sidebar.md](../Custom-Gun-Continued.wiki/_Sidebar.md)：中英文侧边栏各加入一条射手配置链接
- [Shooter-config.md](../Custom-Gun-Continued.wiki/Shooter-config.md)：新建，文件名按要求使用横杠（源文件为 `Shooter config.md`）

校验结果：`Home.md` 和 `_Sidebar.md` 的 diff 与源码提交完全一致；新页面内容与源码相同，仅补了一个文件末尾换行（与原仓库其他配置文档一致）。当前 `.wiki` 仓库改动未提交、未推送，待你审阅。
