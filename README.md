# Claude Code Skill Tree View

[![Version](https://img.shields.io/badge/version-1.0.1-blue.svg)](https://github.com/hankssunfish/claude-code-skill-tree-view)
[![IntelliJ](https://img.shields.io/badge/IntelliJ-2023.2%2B-green.svg)](https://plugins.jetbrains.com)
[![License](https://img.shields.io/badge/license-MIT-purple.svg)](LICENSE)

一个 IntelliJ IDEA 插件，用于将 Claude Code 的 skills 目录中的点号命名文件夹以树形结构展示。

## 项目简介

Claude Code 使用点号命名（如 `superpowers.test-driven-development`）来���织 skills 文件夹。本插件将这些扁平的点号命名文件夹转换为直观的层级树形结构，便于浏览和管理。

## 界面截图

![配置界面](https://maas-log-prod.cn-wlcb.ufileos.com/anthropic/d6659d59-7c70-4fed-89ec-4b5f5d86646b/2cd06da9c22f8e10ccff48e897ebdc4e.png?UCloudPublicKey=TOKEN_e15ba47a-d098-4fbd-9afc-a0dcf0e4e621&Expires=1773144688&Signature=Ty5nSCgNx7PVKcX26m9cHzsaUEw=)

![树形结构展示](https://maas-log-prod.cn-wlcb.ufileos.com/anthropic/d6659d59-7c70-4fed-89ec-4b5f5d86646b/c22337a72c2a98ff011e54c273a422e6.png?UCloudPublicKey=TOKEN_e15ba47a-d098-4fbd-9afc-a0dcf0e4e621&Expires=1773144688&Signature=e/pS4Kbv7KLoTcB3GvUh18jXAWY=)

## 核心功能

- 🌳 **树形结构展示** - 在独立工具窗口中将 `xxx.xxx.xxx` 格式的文件夹显示为层级树形结构
- 🎯 **智能路径���析** - 支持自定义分隔符，只对指定目录应用转换规则
- 📂 **智能节点合并** - 自动处理 `a.b` 和 `a.b.c` 等重叠情况，正确合并子节点
- 🔄 **实时刷新** - 支持手动刷新和配置后自动刷新
- 🎨 **虚拟节点支持** - 区分虚拟父节点和实际文件节点，可自定义图标
- ⏳ **索引状态友好提示** - 项目索引期间显示友好提示，完成后自动刷新

## 快速开始

### 安装插件

1. 下载构建好的插件 ZIP 文件
2. 打开 **Settings/Preferences** → **Plugins**
3. 点击 ⚙️ 图标 → **Install Plugin from Disk...**
4. 选择 ZIP 文件并重启 IDEA

### 基本配置

1. 打开 **Settings/Preferences** → **Tools** → **Claude Code Skill Tree View**
2. 添加需要转换的目标目录（默认：`.claude/skills`）
3. 在 **Skills Tree** 工具窗口查看树形结构

### 使用示例

**配置前（实际文件结构）：**
```
.claude/skills/
├��─ superpowers.test-driven-development/
├── superpowers.brainstorming/
└── web-design.accessibility/
```

**配置后（工具窗口显示）：**
```
📁 skills/
├── 📁 superpowers/
│   ├── 📁 test-driven-development/
│   └── 📁 brainstorming/
└── 📁 web-design/
    └── 📁 accessibility/
```

## 技术架构

```
src/main/kotlin/com/taobao/travel/claudecodeskilltree/
├── config/              # 配置管理
│   ├── DotNotationTreeState.kt      # 插件状态持久化
│   └── DotNotationTreeConfigurable.kt  # 配置界面
├── parser/              # 路径解析
│   └── DotNotationParser.kt         # 点号命名解析器
├── tree/                # 树形结构
│   ├── SkillTreeNode.kt            # 树节点定义
│   ├── SkillTreeModel.kt           # 树模型（含智能合并）
│   └── SkillTreeCellRenderer.kt    # 单元格渲染器
├── toolwindow/          # 工具窗口
│   ├── SkillTreeToolWindowFactory.kt  # 窗口工厂
│   └── IndexingPanel.kt             # 索引状态面板
├── decorator/           # 项目视图装饰
│   └── DotNotationTreeDecorator.kt  # 蓝色图标标识
└── util/                # 工具类
    ├── PluginUtils.kt               # 插件工具方法
    └── ColoredIcon.kt               # 图标着色
```

**核心设计：**
- **解析器** - 将点号路径转换为层级结构
- **树模型** - 智能合并重叠路径，构建完整树形结构
- **渲染器** - 区分虚拟节点和实际节点，提供自定义图标支持

## 开发指南

### 环境要求

- IntelliJ IDEA 2023.2 或更高版本
- JDK 17 或更高版本
- Gradle 8.8

### 构建和运行

```bash
# 构建插件
./gradlew buildPlugin

# 运行测试
./gradlew test

# 调试插件（启动测试 IDE 实例）
./gradlew runIde

# 验证插件
./gradlew verifyPlugin
```

### 插件发布

```bash
# 发布到 JetBrains Marketplace
./gradlew publishPlugin
```

## 贡献指南

欢迎提交 Issue 和 Pull Request！

### 开发流程

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

### 代码规范

- 遵循 Kotlin 编码规范
- 添加必要的单元测试
- 更新相关文档

## 许可证

本项目采用 [MIT License](LICENSE) 开源协议。

## 作者

[hanksunfish](https://github.com/hanksunfish)

## 致谢

- [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html)
- [Claude Code](https://claude.ai/code) 社区

---

**注意**：本插件仅修改显示方式，不会改变实际的文件系统结构。Claude Code 仍然看到的是扁平的文件夹结构。
