# Claude Code Skill Tree View - 使用指南

## 🚀 快速开始

### 方式一：在当前 IDEA 中测试（最简单）

1. **启动带插件的 IDEA 实例**
   ```bash
   cd /Users/sanbai/Downloads/claude-code-skill-tree-view
   ./gradlew runIde
   ```

2. **等待启动** - 会打开一个新的 IDEA 窗口，插件已自动安装

3. **打开测试项目** - 新窗口会自动打开当前项目

---

### 方式二：安装到日常使用的 IDEA

1. **构建插件**
   ```bash
   cd /Users/sanbai/Downloads/claude-code-skill-tree-view
   ./gradlew buildPlugin
   ```

2. **安装插件**
   - 在 IDEA 中打开：`Settings/Preferences` → `Plugins`
   - 点击 ⚙️ 图标 → `Install Plugin from Disk...`
   - 选择文件：`build/distributions/claude-code-skill-tree-view-1.0-SNAPSHOT.zip`
   - 点击 `OK`，重启 IDEA

---

## 📖 使用步骤

### 1. 配置目标目录

1. 打开 IDEA 设置：`Settings/Preferences` → `Tools` → `Claude Code Skill Tree View`

2. 在"目标目录"列表中添加目录（相对于项目根目录）
   ```
   .claude/skills
   ```

3. 点击 `Apply`

### 2. 打开 Skills Tree 工具窗口

- 在 IDEA 左侧工具栏找到 **"Skills Tree"** 窗口
- 或通过菜单：`View` → `Tool Windows` → `Skills Tree`

### 3. 查看树形结构

工具窗口会显示转换后的树形结构，例如：

```
原始文件夹名: superpowers.test-driven-development

显示为树形:
📁 superpowers/
  └── 📁 test-driven-development/
```

### 4. 导航功能

- **双击**任意树节点
- 自动在项目视图中定位到对应的实际文件夹

---

## 🎯 实际使用场景

### 场景 1：管理 Claude Code Skills

假设你有以下 skills 文件夹：
```
.claude/skills/
├── superpowers.test-driven-development/
├── superpowers.brainstorming/
├── web-design.accessibility/
└── web-design.responsive/
```

**使用前**：所有文件夹平铺，难以管理
**使用后**：按分类显示树形结构，一目了然

### 场景 2：添加新的 Skill

1. 在文件系统中创建文件夹：
   ```
   .claude/skills/new-category.my-new-skill/
   ```

2. 在 Skills Tree 工具窗口点击 **"刷新"** 按钮

3. 新技能会自动出现在树形结构中

---

## ⚙️ 配置选项

| 选项 | 说明 | 默认值 |
|------|------|--------|
| 启用插件 | 全局开关 | ✅ 启用 |
| 目标目录 | 要转换的目录列表 | `.claude/skills` |
| 分隔符 | 用于分隔层级的字符 | `.` (点号) |
| 显示虚拟节点图标 | 为虚拟节点显示特殊图标 | ✅ 显示 |

### 自定义分隔符示例

如果你习惯使用斜杠：
```
配置分隔符: /
文件夹名: web-design/accessibility
```

如果你想用双斜杠：
```
配置分隔符: //
文件夹名: web-design//accessibility
```

### Virtual Node Icons

Virtual nodes (intermediate nodes in the tree structure) display a custom icon:
- 🖼️ **Click the icon** to view enlarged version
- 📁 **Double-click** to navigate to the file/folder

To customize the icon, replace:
```
src/main/resources/icons/virtual-node-icon.png
```

Recommended icon specifications:
- Format: SVG (scalable vector graphics)
- Size: 16x16 pixels (base size, will scale appropriately)
- Style: Simple, monochrome or limited color palette for best visibility

---

## 🔧 常用操作

### 添加目标目录

1. 打开配置页面
2. 点击 **"添加目录"** 按钮
3. 输入目录路径（如：`.claude/skills`）
4. 点击 `OK`

### 刷新树结构

点击工具窗口顶部的 **"刷新"** 按钮

### 快速导航

**双击**树中的任意节点，IDEA 会：
1. 在项目视图中选中对应的实际文件夹
2. 自动展开该文件夹
3. 滚动到可见区域

---

## 📝 注意事项

⚠️ **重要提示**：
- 插件只改变**显示方式**，不修改实际文件系统
- Claude Code 仍然看到的是扁平的文件夹结构
- 文件夹的实际名称保持不变（如 `superpowers.test-driven-development`）

---

## 🎨 界面预览

### 工具窗口布局

```
┌─────────────────────────────────┐
│ Skills Tree          [刷新] [配置] │
├─────────────────────────────────┤
│ 📁 .claude/skills/              │
│   ├── 📁 superpowers/           │
│   │   ├── 📁 test-driven-dev/   │
│   │   ├── 📁 brainstorming/     │
│   │   └── 📁 verification/      │
│   ├── 📁 web-design/            │
│   │   └── 📁 accessibility/     │
│   └── 📁 git/                   │
│       └── 📁 branch-mgmt/       │
└─────────────────────────────────┘
```

---

## ❓ 常见问题

### Q: 工具窗口找不到？

**A**: 尝试以下方法：
1. 菜单：`View` → `Tool Windows` → `Skills Tree`
2. 检查插件是否启用（Settings → Plugins）
3. 确认目标目录配置正确

### Q: 文件夹没有转换为树形？

**A**: 检查：
1. 文件夹名称是否包含分隔符（默认为 `.`）
2. 是否在配置的目标目录下
3. 点击"刷新"按钮重新加载

### Q: 想用多个目录？

**A**: 在配置页面可以添加多个目标目录：
```
.claude/skills
.claude/project/skills
my-project/skills
```

---

## 🎓 进阶技巧

### 技巧 1：分类组织

使用多级分类：
```
dev.tools.formatter
dev.tools.linter
dev.testing.unit
dev.testing.integration
```

显示为：
```
📁 dev/
  ├── 📁 tools/
  │   ├── 📁 formatter/
  │   └── 📁 linter/
  └── 📁 testing/
      ├── 📁 unit/
      └── 📁 integration/
```

### 技巧 2：快捷键

在配置页面可以为常用操作设置快捷键（Settings → Keymap）

---

## 📚 获取帮助

- 查看完整文档：`README.md`
- 测试指南：`TESTING.md`
- 问题反馈：GitHub Issues

---

**享受有组织的 Claude Code Skills 吧！** 🎉
