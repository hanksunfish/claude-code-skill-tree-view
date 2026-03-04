# Project View Tree Mode Implementation Plan

> **For Claude:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement a toggleable tree view mode in IntelliJ IDEA's Project View that displays dot-notation folders (e.g., `a.b.c.d`) as hierarchical folders (e.g., `a/b/c/d`)

**Architecture:** Custom ProjectView with AbstractTreeStructure-based virtual tree building, using smart grouping algorithm to handle overlapping folder names. Read-only mode in tree view with visual distinction between virtual and real nodes.

**Tech Stack:** IntelliJ Platform SDK, Kotlin, JDK 17+, AbstractTreeStructure, ProjectViewNodeDecorator

---

## File Structure

```
src/main/kotlin/com/taobao/travel/claudecodeskilltree/
├── tree/
│   ├── VirtualTreeNode.kt           # NEW: Virtual tree node data structure
│   ├── SkillsTreeStructure.kt        # NEW: AbstractTreeStructure implementation
│   └── TreeViewBuilder.kt            # NEW: Smart grouping algorithm
├── view/
│   ├── SkillsProjectView.kt          # NEW: Custom ProjectView
│   └── ProjectViewManager.kt         # NEW: Manages view switching
├── decorator/
│   └── SkillsProjectViewNodeDecorator.kt  # MODIFY: Update for new behavior
├── action/
│   └── ToggleTreeViewAction.kt       # MODIFY: Update to use new view switching
└── config/
    └── DotNotationTreeState.kt       # MODIFY: Already has treeViewEnabled field

src/main/resources/META-INF/
└── plugin.xml                        # MODIFY: Register new extensions

src/test/kotlin/com/taobao/travel/claudecodeskilltree/
├── tree/
│   ├── VirtualTreeNodeTest.kt        # NEW
│   ├── SkillsTreeStructureTest.kt    # NEW
│   └── TreeViewBuilderTest.kt        # NEW
└── view/
    └── ProjectViewManagerTest.kt     # NEW
```

---

## Chunk 1: Core Virtual Tree Structure

### Task 1: Create VirtualTreeNode Data Structure

**Files:**
- Create: `src/main/kotlin/com/taobao/travel/claudecodeskilltree/tree/VirtualTreeNode.kt`
- Test: `src/test/kotlin/com/taobao/travel/claudecodeskilltree/tree/VirtualTreeNodeTest.kt`

- [ ] **Step 1: Write failing tests for VirtualTreeNode**

```kotlin
package com.taobao.travel.claudecodeskilltree.tree

import com.intellij.openapi.vfs.VirtualFile
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VirtualTreeNodeTest {

    @Test
    fun `should create virtual node with correct properties`() {
        val node = VirtualTreeNode("test", null, isVirtual = true)

        assertEquals("test", node.name)
        assertNull(node.realFile)
        assertTrue(node.isVirtual)
        assertFalse(node.isReal)
    }

    @Test
    fun `should create real node with correct properties`() {
        val mockFile = object : VirtualFile() {
            override fun getName() = "real"
            override fun isValid() = true
            override fun isDirectory() = true
            override fun getParent() = null
            override fun getChildren() = null
            override fun getOutputStream() = null
            override fun getInputStream() = null
            override fun getLength() = 0
            override fun refresh(withAsync: Boolean, recursive: Boolean) = Unit
            override fun getModificationCount() = 0
        }

        val node = VirtualTreeNode("test", mockFile, isVirtual = false)

        assertEquals("test", node.name)
        assertEquals(mockFile, node.realFile)
        assertFalse(node.isVirtual)
        assertTrue(node.isReal)
    }

    @Test
    fun `should support parent-child relationships`() {
        val parent = VirtualTreeNode("parent", null, isVirtual = true)
        val child = VirtualTreeNode("child", null, isVirtual = true, parent = parent)

        parent.addChild(child)

        assertEquals(1, parent.children.size)
        assertEquals(child, parent.children[0])
        assertEquals(parent, child.parent)
    }

    @Test
    fun `should find existing child by name`() {
        val parent = VirtualTreeNode("parent", null, isVirtual = true)
        val child = VirtualTreeNode("child", null, isVirtual = true)
        parent.addChild(child)

        val found = parent.findChild("child")

        assertEquals(child, found)
    }

    @Test
    fun `should return null when child not found`() {
        val parent = VirtualTreeNode("parent", null, isVirtual = true)

        val found = parent.findChild("nonexistent")

        assertNull(found)
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew test --tests VirtualTreeNodeTest`
Expected: FAIL with "VirtualTreeNode not defined"

- [ ] **Step 3: Implement VirtualTreeNode**

```kotlin
package com.taobao.travel.claudecodeskilltree.tree

import com.intellij.openapi.vfs.VirtualFile

/**
 * Virtual tree node representing either a virtual directory or a real file
 * @property name Display name of the node
 * @property realFile The actual physical file (null for virtual nodes)
 * @property isVirtual Whether this node is virtual (true) or real (false)
 * @property parent Parent node in the tree
 * @property children List of child nodes
 */
data class VirtualTreeNode(
    val name: String,
    val realFile: VirtualFile?,
    val isVirtual: Boolean,
    val parent: VirtualTreeNode? = null,
    val children: MutableList<VirtualTreeNode> = mutableListOf()
) {
    /**
     * Returns true if this node represents a real physical file
     */
    val isReal: Boolean
        get() = !isVirtual && realFile != null

    /**
     * Adds a child node to this node
     */
    fun addChild(child: VirtualTreeNode) {
        children.add(child)
    }

    /**
     * Finds a child node by name
     * @return The child node if found, null otherwise
     */
    fun findChild(name: String): VirtualTreeNode? {
        return children.find { it.name == name }
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew test --tests VirtualTreeNodeTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/kotlin/com/taobao/travel/claudecodeskilltree/tree/VirtualTreeNode.kt
git add src/test/kotlin/com/taobao/travel/claudecodeskilltree/tree/VirtualTreeNodeTest.kt
git commit -m "feat: add VirtualTreeNode data structure

- Support both virtual and real nodes
- Parent-child relationship management
- Find child by name functionality"
```

---

### Task 2: Implement TreeViewBuilder with Smart Grouping

**Files:**
- Create: `src/main/kotlin/com/taobao/travel/claudecodeskilltree/tree/TreeViewBuilder.kt`
- Test: `src/test/kotlin/com/taobao/travel/claudecodeskilltree/tree/TreeViewBuilderTest.kt`

- [ ] **Step 1: Write failing tests for smart grouping algorithm**

```kotlin
package com.taobao.travel.claudecodeskilltree.tree

import com.intellij.openapi.vfs.VirtualFile
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TreeViewBuilderTest {

    private fun createMockFile(name: String): VirtualFile {
        return object : VirtualFile() {
            override fun getName() = name
            override fun isValid() = true
            override fun isDirectory() = true
            override fun getParent() = null
            override fun getChildren() = null
            override fun getOutputStream() = null
            override fun getInputStream() = null
            override fun getLength() = 0
            override fun refresh(withAsync: Boolean, recursive: Boolean) = Unit
            override fun getModificationCount() = 0
        }
    }

    @Test
    fun `should build tree for single dot-notation folder`() {
        val files = listOf(createMockFile("a.b.c"))
        val builder = TreeViewBuilder()

        val roots = builder.buildVirtualTree(files, ".")

        assertEquals(1, roots.size)
        assertEquals("a", roots[0].name)
        assertTrue(roots[0].isVirtual)

        val b = roots[0].children[0]
        assertEquals("b", b.name)
        assertTrue(b.isVirtual)

        val c = b.children[0]
        assertEquals("c", c.name)
        assertTrue(c.isReal)
    }

    @Test
    fun `should handle overlapping folder names with smart grouping`() {
        val files = listOf(
            createMockFile("a.b"),
            createMockFile("a.b.c"),
            createMockFile("a.b.c.d")
        )
        val builder = TreeViewBuilder()

        val roots = builder.buildVirtualTree(files, ".")

        assertEquals(1, roots.size)
        val a = roots[0]
        assertEquals("a", a.name)

        val b = a.children[0]
        assertEquals("b", b.name)
        assertEquals(2, b.children.size) // Should have both real content and c node

        // Find the 'c' virtual node
        val c = b.children.find { it.name == "c" }
        assertTrue(c?.isVirtual == true)

        // c should have real content and d as child
        assertTrue(c.children.any { it.isReal })
        assertTrue(c.children.any { it.name == "d" })
    }

    @Test
    fun `should handle non-dot-notation folders without modification`() {
        val files = listOf(
            createMockFile("normal-folder"),
            createMockFile("a.b.c")
        )
        val builder = TreeViewBuilder()

        val roots = builder.buildVirtualTree(files, ".")

        // Should have both roots
        assertEquals(2, roots.size)

        val normal = roots.find { it.name == "normal-folder" }
        assertTrue(normal?.isReal == true)

        val a = roots.find { it.name == "a" }
        assertTrue(a?.isVirtual == true)
    }

    @Test
    fun `should handle multiple root level folders`() {
        val files = listOf(
            createMockFile("a.b"),
            createMockFile("x.y.z")
        )
        val builder = TreeViewBuilder()

        val roots = builder.buildVirtualTree(files, ".")

        assertEquals(2, roots.size)
        assertEquals("a", roots[0].name)
        assertEquals("x", roots[1].name)
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew test --tests TreeViewBuilderTest`
Expected: FAIL with "TreeViewBuilder not defined"

- [ ] **Step 3: Implement TreeViewBuilder**

```kotlin
package com.taobao.travel.claudecodeskilltree.tree

import com.intellij.openapi.vfs.VirtualFile

/**
 * Builder for creating virtual tree structures from dot-notation folders
 * Implements smart grouping to handle overlapping folder names
 */
class TreeViewBuilder {

    /**
     * Builds a virtual tree structure from a list of files
     * @param files List of files to include in the tree
     * @param separator Character used to split folder names (default: ".")
     * @return List of root nodes in the virtual tree
     */
    fun buildVirtualTree(files: List<VirtualFile>, separator: String = "."): List<VirtualTreeNode> {
        val roots = mutableListOf<VirtualTreeNode>()

        for (file in files) {
            if (!file.isDirectory) continue

            val fileName = file.name
            if (!fileName.contains(separator)) {
                // Not a dot-notation folder, add as-is
                roots.add(VirtualTreeNode(fileName, file, isVirtual = false))
                continue
            }

            // Parse the dot-notation name
            val parts = fileName.split(separator)
            if (parts.size <= 1) continue

            // Build or find the root node
            var current = roots.find { it.name == parts[0] }
            if (current == null) {
                current = VirtualTreeNode(parts[0], null, isVirtual = true)
                roots.add(current)
            }

            // Build the path recursively
            buildPath(current, parts, 1, file)
        }

        return roots
    }

    /**
     * Recursively builds the path for a file
     * @param parent Current parent node
     * @param parts Parts of the dot-notation name
     * @param index Current index in parts
     * @param realFile The actual physical file
     */
    private fun buildPath(
        parent: VirtualTreeNode,
        parts: List<String>,
        index: Int,
        realFile: VirtualFile
    ) {
        if (index >= parts.size) return

        val partName = parts[index]
        val isLast = (index == parts.size - 1)

        // Check if a child with this name already exists
        var child = parent.findChild(partName)

        if (child == null) {
            // Create a new child node
            child = VirtualTreeNode(
                name = partName,
                realFile = if (isLast) realFile else null,
                isVirtual = !isLast,
                parent = parent
            )
            parent.addChild(child)
        } else if (isLast) {
            // Child exists but this is the last part
            // Update it to be a real node if it wasn't already
            if (child.isVirtual) {
                // Convert virtual node to real node
                child = child.copy(realFile = realFile, isVirtual = false)
                // Replace in parent's children list
                val index = parent.children.indexOfFirst { it.name == partName }
                if (index >= 0) {
                    parent.children[index] = child
                }
            }
        }

        // Continue building the path
        if (!isLast) {
            buildPath(child, parts, index + 1, realFile)
        }
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew test --tests TreeViewBuilderTest`
Expected: PASS (may need iteration)

- [ ] **Step 5: Commit**

```bash
git add src/main/kotlin/com/taobao/travel/claudecodeskilltree/tree/TreeViewBuilder.kt
git add src/test/kotlin/com/taobao/travel/claudecodeskilltree/tree/TreeViewBuilderTest.kt
git commit -m "feat: implement TreeViewBuilder with smart grouping

- Build virtual tree from dot-notation folders
- Handle overlapping names (a.b, a.b.c, a.b.c.d)
- Support multiple root level folders
- Preserve non-dot-notation folders"
```

---

## Chunk 2: Project View Integration

### Task 3: Create SkillsTreeStructure

**Files:**
- Create: `src/main/kotlin/com/taobao/travel/claudecodeskilltree/tree/SkillsTreeStructure.kt`
- Test: `src/test/kotlin/com/taobao/travel/claudecodeskilltree/tree/SkillsTreeStructureTest.kt`

- [ ] **Step 1: Write failing tests**

```kotlin
package com.taobao.travel.claudecodeskilltree.tree

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.taobao.travel.claudecodeskilltree.config.DotNotationTreeState
import org.junit.Test
import org.mockito.Mockito.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SkillsTreeStructureTest {

    @Test
    fun `should return skills directory as root element`() {
        val project = mock(Project::class.java)
        val settings = mock(DotNotationTreeState::class.java)
        `when`(project.getService(DotNotationTreeState::class.java)).thenReturn(settings)
        `when`(settings.targetDirectories).thenReturn(arrayListOf(".claude/skills"))
        `when`(project.basePath).thenReturn("/test/project")

        val structure = SkillsTreeStructure(project)
        val root = structure.getRootElement()

        assertTrue(root is VirtualTreeNode)
        assertEquals("skills", root.name)
    }

    @Test
    fun `should return child elements for virtual node`() {
        // Test implementation
    }
}
```

- [ ] **Step 2: Implement SkillsTreeStructure**

```kotlin
package com.taobao.travel.claudecodeskilltree.tree

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileFilter
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.util.treeView.AbstractTreeStructure
import com.taobao.travel.claudecodeskilltree.config.DotNotationTreeState

/**
 * Tree structure for displaying skills in hierarchical view
 * Implements AbstractTreeStructure for IDEA integration
 */
class SkillsTreeStructure(
    private val project: Project
) : AbstractTreeStructure() {

    private val settings: DotNotationTreeState
        get() = project.getService(DotNotationTreeState::class.java)

    private val builder = TreeViewBuilder()
    private var rootCache: VirtualTreeNode? = null

    override fun getRootElement(): Any {
        if (rootCache != null) {
            return rootCache!!
        }

        val projectBasePath = project.basePath ?: return VirtualTreeNode("No Project", null, false)

        // Find skills directory
        val skillsDir = settings.targetDirectories.firstNotNullOfOrNull { targetDir ->
            val targetPath = "$projectBasePath/$targetDir"
            com.intellij.openapi.vfs.VfsUtil.findFileByIoFile(java.io.File(targetPath), true)
        }

        if (skillsDir == null || !skillsDir.isDirectory) {
            return VirtualTreeNode("Skills Not Found", null, false)
        }

        // Build virtual tree
        val children = skillsDir.children
        if (children != null) {
            val roots = builder.buildVirtualTree(children.toList(), settings.separator)
            rootCache = VirtualTreeNode("skills", skillsDir, false).apply {
                roots.forEach { addChild(it) }
            }
        } else {
            rootCache = VirtualTreeNode("skills", skillsDir, false)
        }

        return rootCache!!
    }

    override fun getChildElements(element: Any): Array<Any> {
        if (element is VirtualTreeNode) {
            return element.children.toTypedArray()
        }
        return emptyArray()
    }

    override fun getParentElement(element: Any): Any? {
        if (element is VirtualTreeNode) {
            return element.parent
        }
        return null
    }

    override fun commit(element: Any?) = Unit

    override fun hasSomethingToCommit(element: Any?): Boolean = false

    override fun isToBuildChildrenInBackground(element: Any?): Boolean = false
}
```

- [ ] **Step 3: Run tests**

Run: `./gradlew test --tests SkillsTreeStructureTest`

- [ ] **Step 4: Commit**

```bash
git add src/main/kotlin/com/taobao/travel/claudecodeskilltree/tree/SkillsTreeStructure.kt
git commit -m "feat: add SkillsTreeStructure

- Implement AbstractTreeStructure for IDEA integration
- Build virtual tree from skills directory
- Cache root element for performance"
```

---

### Task 4: Create SkillsProjectView

**Files:**
- Create: `src/main/kotlin/com/taobao/travel/claudecodeskilltree/view/SkillsProjectView.kt`

- [ ] **Step 1: Implement SkillsProjectView**

```kotlin
package com.taobao.travel.claudecodeskilltree.view

import com.intellij.ide.projectView.ProjectView
import com.intellij.ide.projectView.impl.ProjectViewImpl
import com.intellij.openapi.project.Project
import com.taobao.travel.claudecodeskilltree.tree.SkillsTreeStructure

/**
 * Custom ProjectView for tree mode display
 */
class SkillsProjectView(project: Project) : ProjectViewImpl(project) {

    override fun createTreeStructure(): com.intellij.ide.util.treeView.AbstractTreeStructure {
        return SkillsTreeStructure(project)
    }

    companion object {
        const val VIEW_ID = "SkillsTreeView"
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/kotlin/com/taobao/travel/claudecodeskilltree/view/SkillsProjectView.kt
git commit -m "feat: add SkillsProjectView

- Custom ProjectView implementation
- Uses SkillsTreeStructure for tree building"
```

---

### Task 5: Create ProjectViewManager

**Files:**
- Create: `src/main/kotlin/com/taobao/travel/claudecodeskilltree/view/ProjectViewManager.kt`
- Test: `src/test/kotlin/com/taobao/travel/claudecodeskilltree/view/ProjectViewManagerTest.kt`

- [ ] **Step 1: Write tests**

```kotlin
package com.taobao.travel.claudecodeskilltree.view

import com.intellij.openapi.project.Project
import org.junit.Test
import org.mockito.Mockito.*
import kotlin.test.verify

class ProjectViewManagerTest {

    @Test
    fun `should switch to tree view`() {
        val project = mock(Project::class.java)
        val manager = ProjectViewManager(project)

        manager.switchToTreeView()

        // Verification would go here
    }
}
```

- [ ] **Step 2: Implement ProjectViewManager**

```kotlin
package com.taobao.travel.claudecodeskilltree.view

import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.project.Project
import com.taobao.travel.claudecodeskilltree.config.DotNotationTreeState

/**
 * Manages switching between standard and tree view modes
 */
class ProjectViewManager(private val project: Project) {

    private val settings: DotNotationTreeState
        get() = project.getService(DotNotationTreeState::class.java)

    /**
     * Switch to tree view mode
     */
    fun switchToTreeView() {
        val projectView = ProjectView.getInstance(project)
        // Note: Custom view registration happens in plugin.xml
        settings.treeViewEnabled = true
        projectView.refresh()
    }

    /**
     * Switch back to standard view mode
     */
    fun switchToStandardView() {
        val projectView = ProjectView.getInstance(project)
        settings.treeViewEnabled = false
        projectView.refresh()
    }

    /**
     * Check if tree view is currently enabled
     */
    fun isTreeViewEnabled(): Boolean {
        return settings.treeViewEnabled
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add src/main/kotlin/com/taobao/travel/claudecodeskilltree/view/ProjectViewManager.kt
git commit -m "feat: add ProjectViewManager

- Manage view switching between standard and tree modes
- Persist view mode state in settings"
```

---

### Task 6: Update ToggleTreeViewAction

**Files:**
- Modify: `src/main/kotlin/com/taobao/travel/claudecodeskilltree/action/ToggleTreeViewAction.kt`

- [ ] **Step 1: Update action to use ProjectViewManager**

```kotlin
package com.taobao.travel.claudecodeskilltree.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.taobao.travel.claudecodeskilltree.config.DotNotationTreeState
import com.taobao.travel.claudecodeskilltree.view.ProjectViewManager
import com.taobao.travel.claudecodeskilltree.util.PluginUtils

/**
 * Toggle tree view display mode
 */
class ToggleTreeViewAction : AnAction() {

    override fun update(e: AnActionEvent) {
        val project = e.project
        if (project == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        val settings = project.getService(DotNotationTreeState::class.java)

        // Only show in skills directory
        val virtualFile = e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.VIRTUAL_FILE)
        val isInTargetDir = virtualFile != null && PluginUtils.isInTargetDirectory(project, virtualFile)

        e.presentation.isEnabledAndVisible = isInTargetDir && settings.enabled

        // Update menu text based on current state
        e.presentation.text = if (settings.treeViewEnabled) {
            "恢复扁平显示"
        } else {
            "启用树形显示"
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val manager = ProjectViewManager(project)

        if (manager.isTreeViewEnabled()) {
            manager.switchToStandardView()
            PluginUtils.logInfo("已恢复扁平显示模式")
        } else {
            manager.switchToTreeView()
            PluginUtils.logInfo("已启用树形显示模式")
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/kotlin/com/taobao/travel/claudecodeskilltree/action/ToggleTreeViewAction.kt
git commit -m "refactor: update ToggleTreeViewAction to use ProjectViewManager

- Simplify action logic
- Use ProjectViewManager for view switching"
```

---

### Task 7: Update plugin.xml

**Files:**
- Modify: `src/main/resources/META-INF/plugin.xml`

- [ ] **Step 1: Register new extensions**

```xml
<!-- Update the actions section -->
<actions>
    <!-- Update existing action -->
    <action id="ClaudeCodeSkillTreeView.ToggleTreeView"
            class="com.taobao.travel.claudecodeskilltree.action.ToggleTreeViewAction"
            text="启用树形显示"
            description="切换项目视图中的树形显示模式">
        <add-to-group group-id="ProjectViewPopupMenu" anchor="after" relative-to-action="EditSource"/>
    </action>
</actions>
```

- [ ] **Step 2: Commit**

```bash
git add src/main/resources/META-INF/plugin.xml
git commit -m "feat: register tree view components in plugin.xml"
```

---

## Chunk 3: Visual Styling and Read-Only Mode

### Task 8: Update Node Decorator

**Files:**
- Modify: `src/main/kotlin/com/taobao/travel/claudecodeskilltree/decorator/DotNotationTreeDecorator.kt`

- [ ] **Step 1: Update decorator for new tree view**

```kotlin
package com.taobao.travel.claudecodeskilltree.decorator

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ProjectViewNodeDecorator
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.taobao.travel.claudecodeskilltree.config.DotNotationTreeState
import com.taobao.travel.claudecodeskilltree.parser.DotNotationParser
import com.taobao.travel.claudecodeskilltree.tree.VirtualTreeNode
import com.taobao.travel.claudecodeskilltree.util.ColoredIcon
import javax.swing.Icon

/**
 * Decorator for styling nodes in tree view mode
 */
class DotNotationTreeDecorator : ProjectViewNodeDecorator {

    private var cachedProject: Project? = null
    private var cachedSettings: DotNotationTreeState? = null
    private var cachedParser: DotNotationParser? = null

    private val blueFolderIcon: Icon by lazy { ColoredIcon.createBlueFolderIcon() }
    private val grayFolderIcon: Icon by lazy { createGrayFolderIcon() }

    override fun decorate(node: ProjectViewNode<*>?, data: PresentationData?) {
        if (node == null || data == null) return

        val project = node.project ?: return
        val settings = getSettings(project)

        if (!settings.enabled) return
        if (node !is PsiDirectoryNode) return

        val virtualFile = node.virtualFile ?: return
        val parser = getParser(project)

        if (!parser.isInTargetDirectory(virtualFile)) return

        // Apply blue icon to skills root
        if (isSkillsRootDirectory(virtualFile, project)) {
            data.setIcon(blueFolderIcon)
        }

        // In tree mode, apply gray icons to virtual nodes
        if (settings.treeViewEnabled) {
            // Check if this might be a virtual node
            val fileName = virtualFile.name
            if (parser.needsParsing(fileName)) {
                // This is part of a virtual tree
                data.setIcon(grayFolderIcon)
            }
        }
    }

    private fun isSkillsRootDirectory(file: VirtualFile, project: Project): Boolean {
        val settings = getSettings(project)
        val projectBasePath = project.basePath ?: return false

        return settings.targetDirectories.any { targetDir ->
            val targetFullPath = "$projectBasePath/$targetDir"
            file.path == targetFullPath
        }
    }

    private fun createGrayFolderIcon(): Icon {
        return ColoredIcon(
            com.intellij.icons.AllIcons.Nodes.Folder,
            com.intellij.ui.JBColor.GRAY
        )
    }

    private fun getSettings(project: Project): DotNotationTreeState {
        if (cachedProject !== project || cachedSettings == null) {
            cachedProject = project
            cachedSettings = project.getService(DotNotationTreeState::class.java)
            cachedParser = null
        }
        return cachedSettings!!
    }

    private fun getParser(project: Project): DotNotationParser {
        if (cachedProject !== project || cachedParser == null) {
            cachedProject = project
            cachedParser = DotNotationParser(project)
        }
        return cachedParser!!
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/kotlin/com/taobao/travel/claudecodeskilltree/decorator/DotNotationTreeDecorator.kt
git commit -m "feat: add visual styling for tree view mode

- Blue icon for skills root
- Gray icon for virtual nodes
- Cache settings and parser for performance"
```

---

## Chunk 4: Testing and Documentation

### Task 9: Add Integration Tests

**Files:**
- Create: `src/test/kotlin/com/taobao/travel/claudecodeskilltree/integration/TreeViewIntegrationTest.kt`

- [ ] **Step 1: Write integration tests**

- [ ] **Step 2: Run integration tests**

- [ ] **Step 3: Commit**

---

### Task 10: Update Documentation

**Files:**
- Modify: `README.md`
- Modify: `USAGE_GUIDE.md`

- [ ] **Step 1: Update README with tree view feature**

- [ ] **Step 2: Update usage guide**

- [ ] **Step 3: Commit**

---

### Task 11: Build and Verify

**Files:**
- None (verification step)

- [ ] **Step 1: Build plugin**

Run: `./gradlew buildPlugin -x buildSearchableOptions`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Verify plugin structure**

Run: `ls -lh build/distributions/`
Expected: claude-code-skill-tree-view-1.0-SNAPSHOT.zip

- [ ] **Step 3: Final commit**

```bash
git add docs/
git commit -m "docs: add implementation plan and design docs"
```

---

## Testing Strategy

### Unit Tests
- Each component has its own test file
- Mock dependencies appropriately
- Test edge cases (empty lists, null values, etc.)

### Integration Tests
- Test view switching
- Test tree building with real files
- Test decorator behavior

### Manual Testing Checklist
- [ ] Install plugin in IDEA
- [ ] Create test folders (a.b, a.b.c, a.b.c.d)
- [ ] Switch to tree view
- [ ] Verify tree structure
- [ ] Click virtual nodes
- [ ] Switch back to flat view
- [ ] Verify everything works

---

## Dependencies

This plan requires:
- IntelliJ IDEA 2023.2+
- JDK 17+
- Kotlin compiler
- Gradle 8.8

---

## Completion Criteria

The implementation is complete when:
1. ✅ All tests pass
2. ✅ Plugin builds successfully
3. ✅ Tree view displays correctly
4. ✅ View switching works
5. ✅ Virtual nodes are visually distinct
6. ✅ Skills root has blue icon
7. ✅ Read-only mode enforced
8. ✅ Documentation updated
