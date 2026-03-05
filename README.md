# Claude Code Skill Tree View

一个 IntelliJ IDEA 插件，用于将 Claude Code 的 skills 目录中的点号命名文件夹以树形结构展示。

## 功能特性

- 🌳 **树形结构展示** - 在独立工具窗口中将 `xxx.xxx.xxx` 格式的文件夹显示为层级树形结构
- 🎯 **指定目录转换** - 只在指定的目录（如 `.claude/skills`）内应用转换
- ⚙️ **可配置** - 支持自定义目标目录、分隔符等选项
- 🔄 **实时刷新** - 支持手动刷新和配置后自动刷新
- 🎨 **虚拟节点显示** - 区分虚拟节点和实际文件节点
- 📂 **智能合并** - 自动处理 `a.b` 和 `a.b.c` 等重叠情况，正确合并子节点
- 🔄 **索引状态显示** - 项目索引期间显示友好提示，完成后自动刷新

## 安装

### 从源码构建

```bash
# 克隆项目
git clone <repository-url>
cd claude-code-skill-tree-view

# 构建插件
./gradlew buildPlugin

# 生成的插件位于：build/distributions/
```

### 在 IDEA 中安装

1. 打开 **Settings/Preferences** → **Plugins**
2. 点击 ⚙️ 图标 → **Install Plugin from Disk...**
3. 选择构建生成的 `.zip` 文件
4. 重启 IDEA

## 使用方法

### 1. 配置目标目录

1. 打开 **Settings/Preferences** → **Tools** → **Claude Code Skill Tree View**
2. 在"目标目录"列表中添加需要转换的目录（相对于项目根目录）
3. 默认配置：`.claude/skills`

### 2. 查看树形结构

在 IDEA 左侧工具栏找到 **Skills Tree** 窗口，点击窗口即可看到树形结构的 skills 列表。双击节点可在项目视图中定位到对应文件夹。

### 3. 配置选项

- **分隔符**：用于分隔层级的字符（默认：`.`）
- **启用插件**：全局开关，控制插件是否生效
- **显示虚拟节点图标**：为虚拟父节点显示特殊图标

## 使用示例

### 配置前

```
.claude/skills/
├── superpowers.test-driven-development/
├── superpowers.brainstorming/
├── web-design.accessibility/
└── web-design.responsive/
```

### 配置后（在工具窗口中显示为）

```
📁 skills/
├── 📁 superpowers/
│   ├── 📁 test-driven-development/
│   └── 📁 brainstorming/
└── 📁 web-design/
    ├── 📁 accessibility/
    └── 📁 responsive/
```

## 项目结构

```
src/main/kotlin/com/taobao/travel/claudecodeskilltree/
├── config/              # 配置相关类
│   ├── DotNotationTreeState.kt      # 插件状态配置
│   └── DotNotationTreeConfigurable.kt  # 配置界面
├── parser/              # 解析器
│   └── DotNotationParser.kt         # 点号解析器
├── decorator/           # 装饰器
│   └── DotNotationTreeDecorator.kt  # 项目视图装饰器（蓝色图标标识）
├── tree/                # 树形结构
│   ├── SkillTreeNode.kt            # 树节点
│   ├── SkillTreeModel.kt           # 树模型（含智能合并逻辑）
│   └── SkillTreeCellRenderer.kt    # 树单元格渲染器
├── toolwindow/          # 工具窗口
│   └── SkillTreeToolWindowFactory.kt  # 工具窗口工厂
└── util/                # 工具类
    └── PluginUtils.kt               # 插件工具类
```

## 开发

### 环境要求

- IntelliJ IDEA 2023.2 或更高版本
- JDK 17 或更高版本
- Gradle 8.8

### 构建命令

```bash
# 编译代码
./gradlew compileKotlin

# 运行测试
./gradlew test

# 构建插件
./gradlew buildPlugin

# 运行带有插件的 IDEA 实例（用于调试）
./gradlew runIde
```

### 调试

1. 在 IDEA 中打开项目
2. 运行 `runIde` 任务
3. 会启动一个新的 IDEA 实例，插件已安装
4. 在新实例中测试插件功能

## 贡献

欢迎提交 Issue 和 Pull Request！

## 许可证

MIT License

## 作者

sanbai

## 致谢

- IntelliJ Platform SDK
- Claude Code 社区

---

**注意**：本插件仅修改显示方式，不会改变实际的文件系统结构。Claude Code 仍然看到的是扁平的文件夹结构。
