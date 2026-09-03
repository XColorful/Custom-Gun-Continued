[English](#English)

# 学习社区的积累
> wiki 版本：`0.0.8`.0

从开源社区中**学习前人是怎么做的**。

当你需要了解某个内容时，开源社区拥有丰富的代码积累：
- 📦寻找实现相同或相似功能的开源模组
- 🧐阅读并分析别人的实现方式
- 💡学习其中的思路和技巧

### ❓为什么需要学习前人？

在模组开发中，你可能会遇到几类典型问题：
- 配置：怎么配置 Gradle / Mixin？
- 实现：怎么注册物品、实体、方块等内容？
- 测试：代码写完了，为什么游戏里就是不生效？

这些问题往往让人头疼，因为：
- Minecraft 官方不会专门提供详细的底层文档
- 各类开发框架的文档通常要浏览代码里的 Javadocs
- 文档通常不含真实使用示例，没有调试说明

因此，**主动去看前人走过的路**，有助于解决问题。
> 别人的经验不会自动喂到嘴边

### 🔎去哪里寻找线索？

面对不熟悉的东西，可以按这个思路寻找参考：
- 看原版实现：Minecraft 本身是否已有实现类似功能的地方？先看官方的使用方式
- 看其他模组：是否有同类模组实现了相似效果？学习他们开源的解决思路
- 寻找通用库：这类模组为了兼容性，通常考虑了更多边界情况，涵盖内容也更全面

当遇到疑难杂症时，可以思考：这是否是多个模组普遍存在的问题？是否已经有模组解决了？
> 如果没有找到能参考的，那就需要自己下一番功夫去摸索和研究了😓

### ✍️如何学习？

看到他人的代码实现时：
- 抓主干：识别关键的 API 调用和数据流向
- 按需整合：结合实际场景调整，不强行套用不相干的架构
- 遵守规范：注意开源协议，尊重原作者的劳动成果

### 🤝开源共享，互相帮助

当你从开源社区中学习并有所收获时，请不要忘记将这份力量传递下去：
- 继续开源：让知识不断层，让后人也能少走弯路
- 完善文档：**系统整理积累的经验**，也更方便他人查阅
- 回馈社区：在其他新人遇到同类问题时，顺手指出解决方向

优秀的开发者并不是什么都独自想出来的：善于站在前人的肩膀上学习，与开源社区共同成长。
> ⭐从开源中来，到开源中去

# English
> wiki version: `0.0.8`.0

**Learn how those who came before us did it** in the open source community.

When you need to learn about something, the open source community has a lot of code to learn from:
- 📦Find open source mods that do the same or similar things
- 🧐Read and understand how others implemented them
- 💡Learn the ideas and tricks behind them

### ❓Why learn from those who came before us?

When developing mods, you may run into several common types of problems:
- Configuration: How do I configure Gradle / Mixin?
- Implementation: How do I register items, entities, blocks, and other content?
- Testing: The code is done, so why doesn't it work in the game?

These problems can be frustrating because:
- Minecraft does not provide detailed documentation for its underlying code
- Documentation for development frameworks often means digging through Javadocs in the code
- Documentation usually does not include real usage examples or debugging instructions

So, **looking at how those who came before us solved problems** can help you solve your own problems.
> Other people's experience won't just come to you.

### 🔎Where to look for clues?

When you run into something you don't know, try looking in these places:
- Look at vanilla: Does Minecraft already do something similar? Start by seeing how the official code does it
- Look at other mods: Has another mod done something similar? Learn how they solved the problem
- Look for common libraries: These libraries usually consider more edge cases for compatibility and cover more situations

When you run into a difficult problem, ask yourself: Is this a problem that many mods have? Has another mod already solved it?
> If you can't find anything to learn from, you'll have to spend some time figuring it out yourself😓

### ✍️How to learn?

When looking at someone else's code:
- Follow the main thread: Find the key API calls and see how the data flows
- Use what you need: Adjust it to your situation instead of forcing an unrelated architecture into your project
- Follow the rules: Pay attention to open source licenses and respect the original author's work

### 🤝Open source sharing, helping each other

When you learn something from the open source community, don't forget to pass it on:
- Keep things open source: Keep the knowledge flowing and help others avoid unnecessary detours
- Improve documentation: **Organize what you've learned**, and also make it easier for others to find
- Give back to the community: When other newcomers run into the same problem, point them in the right direction

Great developers don't come up with everything on their own: They know how to learn from those who came before them and grow together with the open source community.
> ⭐From open source, to open source