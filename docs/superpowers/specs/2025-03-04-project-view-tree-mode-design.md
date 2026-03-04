# Project View 树形展示模式设计文档

**日期**: 2025-03-04
**作者**: Claude + sanbai
**��态**: 设计阶段

## 1. 概述

### 1.1 目标

在 IntelliJ IDEA 的 Project 视图中实现可切换的树形展示模式，将 Claude Code skills 目录中点号命名的文件夹（如 `a.b.c.d`）以层级树形结构（`a/b/c/d`）展示。

### 1.2 背景

- Claude Code 使用点号命名来组织 skills（如 `superpowers.test-driven-development`）
- 当前插件只在侧边栏工具窗口中展示树形结构
- 用户希��在 Project 视图中也能看到树形结构，且可切换扁平/树形模式

### 1.3 约束

- 树形模式下为**只读模式**，不支持编辑操作
- 只对 `.claude/skills` 目录应用（可配置扩展到其他目录）
- 不能修改物理文件系统结构
- 保持与 IDEA 标准 Project View 的一致性

---

## 2. 功能需求

### 2.1 用户交互

| 操作 | 行为 |
|------|------|
| 右键 skills 目录 → "启用树形显示" | 切换到树形视图模式 |
| 右键 → "恢复扁平显示" | 切换回标准扁平视图 |
| 点击虚拟节点 (a/) | 导航到对应的真实文件夹 |
| 尝试编辑/重命名/删除 | 显示"树形模式下不支持编辑"提示 |

### 2.2 视觉设计

- **虚拟节点**：灰色文件夹图标
- **真实文件夹**：标准文件夹图标
- **skills 根目录**：蓝色边框文件夹图标
- **保持 IDEA 标准**：选中高亮、展开/折叠动画等

---

## 3. 架���设计

### 3.1 系统架构图

```
┌─────────────────────────────────────────────┐
│         Project View Selector               │
│  (标准视图 / 树形视图 切换)                 │
└────────────┬────────────────────────────────┘
             │
      ┌──────┴──────┐
      │             │
      ▼             ▼
┌──────────┐  ┌─────────────────────┐
│ Standard │  │ TreeViewProjectView │
│ProjectView│  │  (自定义视图)       │
└──────────┘  └─────────┬───────────┘
                         │
                         ▼
                ┌────────────────────┐
                │SkillsTreeStructure │
                │  (虚拟树构建)      │
                └─────────┬──────────┘
                          │
                          ▼
                ┌────────────────────┐
                │ Virtual Node       │
                │  Builder           │
                │ (智能分组算法)     │
                └─────────┬──────────┘
                          │
                          ▼
                ┌────────────────────┐
                │NodeDescriptor      │
                │  (样式+交互)       │
                └────────────────────┘
```

### 3.2 核心组件

#### 3.2.1 SkillsProjectView

**职责**：自定义的 Project View，用于树形展示模式

**继承**：
```kotlin
class SkillsProjectView(
    project: Project
) : ProjectViewImpl(project) {

    override fun createTreeStructure(): AbstractTreeStructure {
        return SkillsTreeStructure(project)
    }
}
```

**关键方法**：
- `createTreeStructure()`: 返回自定义的树结构
- `dispose()`: 清理资源

#### 3.2.2 SkillsTreeStructure

**职责**：构建虚拟树结构，处理点号命名文件夹

**继承**：
```kotlin
class SkillsTreeStructure(
    private val project: Project
) : AbstractTreeStructure() {

    override fun getRootElement(): Any {
        // 返回 skills 根目录
    }

    override fun getChildElements(element: Any): Array<Any> {
        // 返回子元素（虚拟节点或真实文件夹）
    }

    override fun getParentElement(element: Any): Any? {
        // 返回父元素
    }
}
```

**智能分组算法**：
```kotlin
fun buildVirtualTree(files: List<VirtualFile>): List<VirtualTreeNode> {
    val roots = mutableListOf<VirtualTreeNode>()

    for (file in files) {
        val parts = file.name.split(".")
        var current = roots.find { it.name == parts[0] }

        if (current == null) {
            current = VirtualTreeNode(parts[0], null, isVirtual = true)
            roots.add(current)
        }

        // 递归构建子节点
        buildPath(current, parts, 1, file)
    }

    return roots
}

private fun buildPath(
    parent: VirtualTreeNode,
    parts: List<String>,
    index: Int,
    realFile: VirtualFile
) {
    if (index >= parts.size) return

    val child = parent.children.find { it.name == parts[index] }

    if (child == null) {
        val isLast = (index == parts.size - 1)
        val newNode = VirtualTreeNode(
            parts[index],
            if (isLast) realFile else null,
            isVirtual = !isLast
        )
        parent.addChild(newNode)

        if (!isLast) {
            buildPath(newNode, parts, index + 1, realFile)
        }
    } else {
        buildPath(child, parts, index + 1, realFile)
    }
}
```

#### 3.2.3 VirtualTreeNode

**职责**：表示虚拟树节点（可以是虚拟的或真实的）

```kotlin
data class VirtualTreeNode(
    val name: String,
    val realFile: VirtualFile?,
    val isVirtual: Boolean,
    val parent: VirtualTreeNode? = null,
    val children: MutableList<VirtualTreeNode> = mutableListOf()
) {
    val isReal: Boolean
        get() = !isVirtual && realFile != null
}
```

#### 3.2.4 SkillsProjectViewNodeDecorator

**职责**：为节点应用样式和行为

```kotlin
class SkillsProjectViewNodeDecorator : ProjectViewNodeDecorator {

    override fun decorate(node: ProjectViewNode<*>?, data: PresentationData?) {
        // 应用图标和颜色
        val virtualNode = getVirtualNode(node)

        if (virtualNode?.isVirtual == true) {
            data.setIcon(GRAY_FOLDER_ICON)
        } else {
            data.setIcon(STANDARD_FOLDER_ICON)
        }

        // skills 根目录特殊图标
        if (isSkillsRoot(node)) {
            data.setIcon(BLUE_FOLDER_ICON)
        }
    }
}
```

#### 3.2.5 ProjectViewSelector

**职责**：管理视图切换

```kotlin
class ProjectViewSelector {

    fun switchToTreeView(project: Project) {
        val projectView = ProjectView.getInstance(project)
        projectView.setViewId(TREE_VIEW_ID)
    }

    fun switchToStandardView(project: Project) {
        val projectView = ProjectView.getInstance(project)
        projectView.setViewId(StandardProjectViewFactory.ID)
    }
}
```

---

## 4. 数据流

### 4.1 树形构建流程

```
1. 用户右键 → "启用树形显示"
         ↓
2. ProjectViewSelector.switchToTreeView()
         ↓
3. SkillsProjectView 初始化
         ↓
4. SkillsTreeStructure.getRootElement()
         ↓
5. 遍历 skills 目录下的文件夹
         ↓
6. 对每个文件夹调用 buildVirtualTree()
         ↓
7. 解析点号名称（a.b.c.d → [a, b, c, d]）
         ↓
8. 智能分组：合并相同前缀的路径
         ↓
9. 创建 VirtualTreeNode 层级结构
         ↓
10. 渲染到 Project View
```

### 4.2 点击导航流程

```
1. 用户点击虚拟节点 "a"
         ↓
2. 查找对应的真实文件夹
         ↓
3. 找到最匹配的真实文件（如 a.b.c）
         ↓
4. ProjectView.select(virtualFile)
         ↓
5. 在视图中高亮并滚动到该文件
```

---

## 5. 状态管理

### 5.1 配置状态

```kotlin
// DotNotationTreeState.kt
class DotNotationTreeState {
    var treeViewEnabled: Boolean = false  // 是否启用树形视图
    var targetDirectories: ArrayList<String> = arrayListOf(".claude/skills")
}
```

### 5.2 状态持久化

- 状态保存在 `DotNotationTreeSettings.xml`
- 切换视图模式时自动保存
- 重启 IDEA 后恢复上次选择

---

## 6. 边界情况处理

| 场景 | 处理方式 |
|------|----------|
| `a.b` 和 `a.b.c` 同时存在 | 智能分组为 `a/b/(真实)` 和 `a/b/c/(真实)` |
| 无点号文件夹 `normal` | 保持原样，不创建虚拟节点 |
| 文件夹包含特殊字符 `a-b.c` | 按点号分割，`a-b` 作为一个整体 |
| 切换模式时正在编辑 | 保存编辑状态，切换后恢复 |
| skills 目录不存在 | 显示提示，不启用树形模式 |
| 大量文件夹（1000+） | 懒加载子节点，避免性能问题 |

---

## 7. 性能考虑

### 7.1 优化策略

1. **懒加载**：只展开可见节点的子节点
2. **缓存**：缓存已构建的虚拟树结构
3. **增量更新**：监听文件系统变化，只更新变化的��分
4. **异步构建**：在后台线程构建树结构

### 7.2 性能指标

- 切换视图响应时间 < 500ms
- 展开/折叠节点响应时间 < 100ms
- 支持 1000+ 文件夹不卡顿

---

## 8. 测试计划

### 8.1 单元测试

- `SkillsTreeStructureTest`: 测试树构建逻辑
- `VirtualTreeNodeTest`: 测试虚拟节点操作
- 智能分组算法测试：验证边界情况

### 8.2 集成测试

- 视图切换测试
- 导航功能测试
- 只读模式验证

### 8.3 测试用例

| ID | 场景 | 期望结果 |
|----|------|----------|
| T1 | 切换到树形模式 | Project View 显示树形结构 |
| T2 | 点击虚拟节点 | 导航到真实文件夹 |
| T3 | 尝试重命名 | 显示"不支持编辑"提示 |
| T4 | a.b 和 a.b.c 共存 | 正确分组显示 |
| T5 | 切换回扁平模式 | 恢复原始视图 |

---

## 9. 实现计划

### 9.1 开发阶段

**阶段 1：核心结构**
- 实现 `VirtualTreeNode`
- 实现 `SkillsTreeStructure`
- 实现智能分组算法

**阶段 2：视图集成**
- 实现 `SkillsProjectView`
- 集成到 `ProjectViewSelector`
- 实现视图切换逻辑

**阶段 3：样式和交互**
- 实现 `SkillsProjectViewNodeDecorator`
- 添加图标和颜色
- 实现点击导航

**阶段 4：完善和优化**
- 实现只读模式
- 性能优化
- 边界情况处理

### 9.2 文件清单

```
src/main/kotlin/com/taobao/travel/claudecodeskilltree/
├── tree/
│   ├── VirtualTreeNode.kt           (新建)
│   ├── SkillsTreeStructure.kt        (新建)
│   └── TreeViewBuilder.kt            (新建)
├── view/
│   ├── SkillsProjectView.kt          (新建)
│   └── ProjectViewManager.kt         (新建)
├── decorator/
│   └── SkillsProjectViewNodeDecorator.kt (重命名+修改)
├── action/
│   └── ToggleTreeViewAction.kt       (已存在，需更新)
└── config/
    └── DotNotationTreeState.kt       (已存在，已有 treeViewEnabled)
```

---

## 10. 风险和依赖

### 10.1 技术风险

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| IDEA API 兼容性 | 高 | 查阅官方文档，使用稳定 API |
| 性能问题 | 中 | 实现缓存和懒加载 |
| 与其他插件冲突 | 低 | 使用标准的 Project View 扩展点 |

### 10.2 外部依赖

- IntelliJ Platform SDK
- JDK 17+
- IDEA 2023.2+

---

## 11. 未来扩展

- 支持多个目标目录的树形展示
- 支持自定义分隔符（不仅是点号）
- 支持拖拽重组虚拟节点
- 导出/导入树形结构配置

---

**文档版本**: 1.0
**最后更新**: 2025-03-04
