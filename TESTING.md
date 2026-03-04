# 插件测试指南

## 测试环境

测试文件夹已创建在项目的 `.claude/skills` 目录下：

```
.claude/skills/
├── superpowers.test-driven-development/
│   └── README.md
├── superpowers.brainstorming/
│   └── README.md
├── superpowers.verification-before-completion/
├── web-design.accessibility/
│   └── guide.md
├── web-design.responsive/
├── git.branch-management/
├── git.merge-conflicts/
├── documentation.markdown/
└── documentation.api-docs/
```

## 启动插件

### 方法 1：使用 Gradle 任务（推荐用于开发调试）

```bash
cd /Users/sanbai/Downloads/claude-code-skill-tree-view
./gradlew runIde
```

这会启动一个新的 IDEA 实例，插件已自动安装。

### 方法 2：安装构建的插件

```bash
# 构建插件
./gradlew buildPlugin

# 在 IDEA 中安装
# Settings → Plugins → ⚙️ → Install Plugin from Disk
# 选择 build/distributions/ 下的 .zip 文件
```

## 测试步骤

### 1. 打开工具窗口

1. 在 IDEA 左侧工具栏找到 **Skills Tree** 窗口
2. 如果没有看���，点击菜单：View → Tool Windows → Skills Tree

### 2. 验证树形结构显示

在 Skills Tree 工具窗口中，应该看到如下树形结构：

```
📁 .claude/skills/
├── 📁 superpowers/
│   ├── 📁 test-driven-development/
│   ├── 📁 brainstorming/
│   └── 📁 verification-before-completion/
├── 📁 web-design/
│   ├── 📁 accessibility/
│   └── 📁 responsive/
├── 📁 git/
│   ├── 📁 branch-management/
│   └── 📁 merge-conflicts/
└── 📁 documentation/
    ├── 📁 markdown/
    └── 📁 api-docs/
```

### 3. 测试导航功能

1. 双击树中的任意节点（如 `test-driven-development`）
2. 应该在项目视图中选中对应的实际文件夹

### 4. 测试刷新功能

1. 点击工具窗口顶部的"刷新"按钮
2. 树应该重新构建并显示最新内容

### 5. 测试配置功能

1. 点击"���置"按钮
2. 应该打开插件配置页面
3. 尝试添加/删除/编辑目标目录
4. 修改分隔符（例如改为 `/`）
5. 点击"Apply"后，树应该根据新配置更新

### 6. 测试项目视图装饰器

1. 在项目视图中导航到 `.claude/skills` 目录
2. 观察文件夹名称是否被装饰（显示层级结构的一部分）

## 运行单元测试

```bash
# 运行所有测试
./gradlew test

# 运行特定测试类
./gradlew test --tests "DotNotationParserTest"

# 查看测试报告
open build/reports/tests/test/index.html
```

### 当前测试用例

- ✅ `testParsePathWithDotSeparator` - 测试点号分隔符解析
- ✅ `testParsePathWithoutSeparator` - 测试无分隔符的情况
- ✅ `testNeedsParsing` - 测试是否需要解析
- ✅ `testGetDepth` - 测试层级深度计算
- ✅ `testVirtualPathConversion` - 测试虚拟路径转换
- ✅ `testCustomSeparator` - 测试自定义分隔符

## 调试技巧

### 在运行中的 IDEA 实例中调试

1. 在代码中设置断点
2. 使用 Debug 模式运行：`./gradlew runIde --debug-jvm`
3. 在 IDEA 中附加调试器到 localhost:5005

### 查看日志

IDEA 的日志位于：
- macOS: `~/Library/Logs/IntelliJIdea*/idea.log`
- 插件相关日志搜索 "ClaudeCodeSkillTreeView"

## 常见问题

### Q: 工具窗口不显示？

A: 确保：
1. 插件已启用（Settings → Tools → Claude Code Skill Tree View）
2. 目标目录配置正确（`.claude/skills`）
3. 项目已重新加载

### Q: 文件夹没有转换为树形结构？

A: 检查：
1. 文件夹名称是否包含分隔符（默认为 `.`）
2. 文件夹是否在配置的目标目录下
3. 插件是否已启用

### Q: 测试失败？

A: 尝试：
1. 清理构建：`./gradlew clean`
2. 重新构建：`./gradlew build`
3. 检查 JDK 版本（需要 JDK 17+）

## 下一步

- 添加更多测试用例
- 测试边界情况（空文件夹、特殊字符等）
- 性能测试（大量文件夹）
- 集成测试（与实际 Claude Code 工作流）
