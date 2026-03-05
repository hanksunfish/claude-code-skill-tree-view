# Indexing State Display Implementation Plan

> **For Claude:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 显示"正在索引项目..."状态面板，确保在项目索引期间 Skills Tree 工具窗口不会空白，并在索引完成后自动刷新到正常树视图。

**Architecture:**
- 在 `SkillTreeToolWindowFactory` 中检测 DumbMode（索引状态）
- 创建新的 `IndexingPanel` 组件显示索引提示
- 实现 `DumbModeListener` 监听索引完成事件，自动切换到树视图
- 工具栏在所有状态下保持可用

**Tech Stack:**
- IntelliJ Platform SDK (DumbService, DumbModeListener)
- Swing/JPanel for UI components
- Kotlin

---

## File Structure

**New Files:**
- `src/main/kotlin/com/taobao/travel/claudecodeskilltree/toolwindow/IndexingPanel.kt` - 索引状态面板组件

**Modified Files:**
- `src/main/kotlin/com/taobao/travel/claudecodeskilltree/toolwindow/SkillTreeToolWindowFactory.kt` - 添加 DumbMode 检测和状态切换逻辑

---

## Chunk 1: Create IndexingPanel Component

### Task 1: Create IndexingPanel class

**Files:**
- Create: `src/main/kotlin/com/taobao/travel/claudecodeskilltree/toolwindow/IndexingPanel.kt`

- [ ] **Step 1: Write the IndexingPanel class**

```kotlin
package com.taobao.travel.claudecodeskilltree.toolwindow

import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

/**
 * 索引状态面板
 * 在项目索引期间显示提示信息
 */
class IndexingPanel : JBPanel<JBPanel<*>>() {

    init {
        layout = BorderLayout()
        border = JBUI.Borders.empty(20)

        // 创建中心提示区域
        val centerPanel = JPanel(FlowLayout(FlowLayout.CENTER))
        centerPanel.isOpaque = false

        // 添加图标和文本
        val iconLabel = JBLabel()
        iconLabel.icon = UIUtil.getLoadingIcon()
        centerPanel.add(iconLabel)

        val textLabel = JBLabel("<html><div style='text-align: center;'>" +
                "<h3>正在索引项目...</h3>" +
                "<p style='color: #808080;'>请稍候，索引完成后将自动刷新</p>" +
                "</div></html>")
        textLabel.border = EmptyBorder(0, 10, 0, 0)
        centerPanel.add(textLabel)

        add(centerPanel, BorderLayout.CENTER)
    }
}
```

- [ ] **Step 2: Verify the file compiles**

Run: `./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit the new file**

```bash
git add src/main/kotlin/com/taobao/travel/claudecodeskilltree/toolwindow/IndexingPanel.kt
git commit -m "feat: add IndexingPanel component for displaying indexing state"
```

---

## Chunk 2: Modify SkillTreeToolWindowFactory to Support Indexing State

### Task 2: Add DumbMode detection and panel switching logic

**Files:**
- Modify: `src/main/kotlin/com/taobao/travel/claudecodeskilltree/toolwindow/SkillTreeToolWindowFactory.kt`

- [ ] **Step 1: Read the current file to understand structure**

Run: Read the file at `src/main/kotlin/com/taobao/travel/claudecodeskilltree/toolwindow/SkillTreeToolWindowFactory.kt`
Expected: See current implementation of createToolWindowContent

- [ ] **Step 2: Modify createToolWindowContent to detect DumbMode**

Replace the entire `createToolWindowContent` method with:

```kotlin
override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    // 创建主面板
    val mainPanel = JBPanel<JBPanel<*>>(BorderLayout())

    // 检测是否在索引状态
    val dumbService = com.intellij.openapi.project.DumbService.getInstance(project)

    if (dumbService.isDumb) {
        // 索引期间：显示索引状态面板
        val indexingPanel = IndexingPanel()
        mainPanel.add(indexingPanel, BorderLayout.CENTER)

        // 添加简化工具栏（索引期间只有刷新按钮）
        val toolbarPanel = createIndexingToolbar(project)
        mainPanel.add(toolbarPanel, BorderLayout.NORTH)

        // 添加到工具窗口
        val contentManager = toolWindow.contentManager
        val content = contentManager.factory.createContent(mainPanel, "", false)
        contentManager.addContent(content)

        // 监听索引完成事件，完成后刷新到正常视图
        dumbService.runWhenSmart(Runnable {
            refreshToNormalView(project, toolWindow)
        })
    } else {
        // 正常状态：显示树视图
        val treePanel = createSkillTree(project)
        val tree = treePanel.first

        // 添加完整工具栏
        val toolbarPanel = createToolbar(project, tree)

        // 组装界面
        mainPanel.add(toolbarPanel, BorderLayout.NORTH)
        mainPanel.add(treePanel.second, BorderLayout.CENTER)

        // 添加到工具窗口
        val contentManager = toolWindow.contentManager
        val content = contentManager.factory.createContent(mainPanel, "", false)
        contentManager.addContent(content)
    }
}
```

- [ ] **Step 3: Add createIndexingToolbar method**

Add this new method after the `createToolbar` method:

```kotlin
/**
 * 创建索引期间的简化工具栏
 */
private fun createIndexingToolbar(project: Project): JPanel {
    val panel = JBPanel<JBPanel<*>>()

    // 只添加刷新按钮
    val refreshButton = JButton("刷新")
    refreshButton.addActionListener {
        // 刷新整个工具窗口
        com.intellij.openapi.wm.ToolWindowManager.getInstance(project)
            .getToolWindow("Skills Tree")
            ?.let { toolWindow ->
                // 移除所有内容并重新创建
                toolWindow.contentManager.removeAllContents()
                createToolWindowContent(project, toolWindow)
            }
    }

    panel.add(refreshButton)

    return panel
}
```

- [ ] **Step 4: Add refreshToNormalView method**

Add this new method after the `navigateToFile` method:

```kotlin
/**
 * 切换到正常树视图
 */
private fun refreshToNormalView(project: Project, toolWindow: ToolWindow) {
    // 移除所有内容
    toolWindow.contentManager.removeAllContents()

    // 重新创建正常视图
    createToolWindowContent(project, toolWindow)
}
```

- [ ] **Step 5: Verify the file compiles**

Run: `./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Run tests**

Run: `./gradlew test`
Expected: All tests pass (or no tests fail due to our changes)

- [ ] **Step 7: Commit the changes**

```bash
git add src/main/kotlin/com/taobao/travel/claudecodeskilltree/toolwindow/SkillTreeToolWindowFactory.kt
git commit -m "feat: add DumbMode detection and indexing state display"
```

---

## Chunk 3: Testing and Verification

### Task 3: Manual testing in development IDE

**Files:**
- No file modifications

- [ ] **Step 1: Build the plugin**

Run: `./gradlew buildPlugin`
Expected: BUILD SUCCESSFUL, plugin zip created in `build/distributions/`

- [ ] **Step 2: Run development IDE**

Run: `./gradlew runIde`
Expected: New IntelliJ IDEA instance opens with plugin installed

- [ ] **Step 3: Test indexing state display**

Manual Steps:
1. In the development IDE, open a project that has `.claude/skills` directory
2. Trigger indexing: `File > Invalidate Caches > Invalidate and Restart`
3. While indexing is running, open the Skills Tree tool window
4. Expected: See "正在索引项目..." message with loading icon
5. Wait for indexing to complete
6. Expected: Tree view automatically appears with skills

- [ ] **Step 4: Test normal state**

Manual Steps:
1. After indexing completes, verify the tree displays correctly
2. Click refresh button - tree should reload
3. Click expand/collapse buttons - should work as expected

- [ ] **Step 5: Test toolbar during indexing**

Manual Steps:
1. Trigger indexing again
2. Verify only the "刷新" button is shown in toolbar
3. Click refresh - should re-check indexing state and update view

- [ ] **Step 6: Document any issues found**

Create notes in `TESTING.md` if any bugs or edge cases are discovered

- [ ] **Step 7: Fix any issues found**

If issues were discovered, fix them and re-test

- [ ] **Step 8: Update documentation**

Update `README.md` to document the new indexing state behavior

Add to "功能特性" section:
```markdown
- 🔄 **索引状态显示** - 项目索引期间显示友好提示，完成后自动刷新
```

- [ ] **Step 9: Final commit**

```bash
git add README.md TESTING.md
git commit -m "docs: update documentation for indexing state feature"
```

---

## Summary

This implementation adds indexing state detection and display to the Skills Tree tool window:

1. **IndexingPanel** - New component that displays a friendly "正在索引项目..." message during indexing
2. **DumbMode Detection** - Uses `DumbService.isDumb()` to detect when project is indexing
3. **Auto-refresh** - Listens for indexing completion and automatically switches to tree view
4. **Simplified Toolbar** - During indexing, only the refresh button is shown
5. **Seamless UX** - Users always see something useful, never a blank panel

The implementation is minimal, focused, and follows IntelliJ Platform best practices for DumbMode handling.
