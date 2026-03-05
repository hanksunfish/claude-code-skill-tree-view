# Virtual Node Icon with Click Preview Implementation Plan

> **For Claude:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add custom icon for virtual nodes that can be clicked to show enlarged preview in IDEA's preview panel.

**Architecture:**
- Load custom icon from resources directory for all virtual nodes
- Add mouse listener to detect clicks on icon area in tree
- Use IntelliJ's built-in image preview component to display enlarged icon
- Maintain existing double-click navigation behavior

**Tech Stack:**
- IntelliJ Platform SDK (IconLoader, ImageViewer, JBPopup)
- Swing/AWT event handling (MouseListener, MouseEvent)
- Kotlin for implementation

---

## File Structure

**New Files:**
- `src/main/resources/icons/virtual-node-icon.png` - Custom icon for virtual nodes

**Modified Files:**
- `src/main/kotlin/com/taobao/travel/claudecodeskilltree/tree/SkillTreeNode.kt` - Update getIcon() to return custom icon for virtual nodes
- `src/main/kotlin/com/taobao/travel/claudecodeskilltree/toolwindow/SkillTreeToolWindowFactory.kt` - Add icon click detection and preview

**Key Design Decisions:**
- All virtual nodes share the same icon (simple, maintainable)
- Click detection uses row bounds and icon position approximation
- Preview uses IntelliJ's ImageViewer for native feel
- Single click on icon triggers preview, double click remains for navigation

---

## Task 1: Create Icon Resource Directory and Add Placeholder

**Files:**
- Create: `src/main/resources/icons/virtual-node-icon.png`

- [ ] **Step 1: Create icons directory structure**

Run:
```bash
mkdir -p src/main/resources/icons
```

- [ ] **Step 2: Add placeholder icon (user will replace)**

Create a temporary placeholder icon. For now, create a simple SVG that can be converted to PNG:

Create `src/main/resources/icons/virtual-node-icon.svg`:
```xml
<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16">
  <rect width="16" height="16" fill="#FFD700" rx="2"/>
  <text x="8" y="12" font-size="10" text-anchor="middle" fill="#000">V</text>
</svg>
```

**Note:** User will provide the actual PNG icon. This placeholder is for testing.

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/icons/
git commit -m "feat: add icon directory and placeholder for virtual node icon"
```

---

## Task 2: Update SkillTreeNode to Use Custom Icon

**Files:**
- Modify: `src/main/kotlin/com/taobao/travel/claudecodeskilltree/tree/SkillTreeNode.kt:51-64`

- [ ] **Step 1: Update getIcon() method to load custom icon for virtual nodes**

Modify the `getIcon()` method in `SkillTreeNode.kt`:

```kotlin
/**
 * 获取图标
 */
fun getIcon(project: com.intellij.openapi.project.Project): javax.swing.Icon {
    return if (isVirtual) {
        // 虚拟节点使用自定义图标
        com.intellij.openapi.util.IconLoader.getIcon(
            "/icons/virtual-node-icon.png",
            this::class.java.classLoader
        )
    } else {
        if (virtualFile?.isDirectory == true) {
            com.intellij.icons.AllIcons.Nodes.Folder
        } else {
            // 使用文件类型管理器获取正确的文件类型图标
            val fileType = com.intellij.openapi.fileTypes.FileTypeManager.getInstance()
                .getFileTypeByFile(virtualFile!!)
            fileType.getIcon()
        }
    }
}
```

- [ ] **Step 2: Test build to verify icon loads**

Run:
```bash
./gradlew buildPlugin
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add src/main/kotlin/com/taobao/travel/claudecodeskilltree/tree/SkillTreeNode.kt
git commit -m "feat: use custom icon for virtual nodes"
```

---

## Task 3: Add Icon Click Detection and Preview

**Files:**
- Modify: `src/main/kotlin/com/taobao/travel/claudecodeskilltree/toolwindow/SkillTreeToolWindowFactory.kt:104-117`

- [ ] **Step 1: Add icon click detection to mouse listener**

Replace the existing `mouseClicked` method in the `MouseAdapter` (around line 105-117) with enhanced version:

```kotlin
// 添加鼠标点击监听器
tree.addMouseListener(object : MouseAdapter() {
    override fun mouseClicked(e: MouseEvent) {
        // 检测是否为图标点击
        val path = tree.getPathForLocation(e.x, e.y)
        if (path != null) {
            val node = path.lastPathComponent as? SkillTreeNode
            if (node != null) {
                // 计算图标位置（在行的左侧）
                val row = tree.getRowForPath(path)
                val rowBounds = tree.getRowBounds(row)
                val iconX = rowBounds.x + tree.insets.left
                val iconWidth = 20  // 图标宽度估计值
                val iconHeight = 20 // 图标高度估计值

                // 检查点击是否在图标区域内
                if (e.x >= iconX && e.x <= iconX + iconWidth &&
                    e.y >= rowBounds.y && e.y <= rowBounds.y + iconHeight) {

                    // 单击图标：显示预览
                    if (e.clickCount == 1 && node.isVirtual) {
                        showIconPreview(node, project)
                        return
                    }
                }

                // 双击：导航到文件
                if (e.clickCount == 2) {
                    navigateToFile(node, project)
                }
            }
        }
    }
})
```

- [ ] **Step 2: Add icon preview method**

Add this new method to `SkillTreeToolWindowFactory` class:

```kotlin
/**
 * 显示图标预览
 */
private fun showIconPreview(node: SkillTreeNode, project: Project) {
    try {
        // 加载图标
        val iconUrl = this::class.java.classLoader.getResource("icons/virtual-node-icon.png")
        if (iconUrl != null) {
            val image = javax.imageio.ImageIO.read(iconUrl)

            // 使用 IntelliJ 的 ImageViewer 组件显示
            val imageViewer = com.intellij.ui.ImageViewer()
            imageViewer.setImage(image)
            imageViewer.setPreferredSize(java.awt.Dimension(400, 400))

            // 创建弹出窗口
            val builder = com.intellij.openapi.ui.popup.JBPopupFactory.getInstance()
                .createComponentPopupBuilder(imageViewer, null)
                .setTitle("Virtual Node Icon - ${node.name}")
                .setMovable(true)
                .setResizable(true)
                .setRequestFocus(true)
                .setDimensionServiceKey(null, null, null)

            // 显示在鼠标位置或屏幕中央
            builder.createPopup().showInFocusCenter()
        }
    } catch (e: Exception) {
        com.intellij.openapi.notification.NotificationGroupManager.getInstance()
            .getNotificationGroup("Claude Code Skill Tree View")
            ?.createNotification(
                "Failed to load icon preview",
                e.message ?: "Unknown error",
                com.intellij.notification.NotificationType.ERROR
            )
            ?.notify(project)
    }
}
```

- [ ] **Step 3: Test build**

Run:
```bash
./gradlew buildPlugin
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add src/main/kotlin/com/taobao/travel/claudecodeskilltree/toolwindow/SkillTreeToolWindowFactory.kt
git commit -m "feat: add icon click detection and preview for virtual nodes"
```

---

## Task 4: Add Notification Group Configuration

**Files:**
- Modify: `src/main/resources/META-INF/plugin.xml`

- [ ] **Step 1: Add notification group to plugin.xml**

Find the `<extensions>` section in `plugin.xml` and add:

```xml
<!-- Notification group for error messages -->
<notificationGroup id="Claude Code Skill Tree View" displayType="BALLOON"/>
```

- [ ] **Step 2: Verify plugin.xml structure**

Run:
```bash
./gradlew buildPlugin
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/META-INF/plugin.xml
git commit -m "feat: add notification group for icon preview errors"
```

---

## Task 5: Manual Testing

**Files:**
- Test: Manual verification in running IDE instance

- [ ] **Step 1: Run IDE with plugin**

Run:
```bash
./gradlew runIde
```

- [ ] **Step 2: Verify virtual node icon display**

In the running IDE:
1. Open the Skills Tree tool window
2. Look at virtual nodes (e.g., "superpowers" parent nodes)
3. Verify custom icon is displayed instead of default folder icon
4. Take screenshot for documentation

- [ ] **Step 3: Test icon click preview**

1. Click on the icon of a virtual node (single click)
2. Verify preview popup appears showing enlarged icon
3. Verify popup is resizable and movable
4. Close popup and try different virtual nodes

- [ ] **Step 4: Verify double-click navigation still works**

1. Double-click on a virtual node's icon or text
2. Verify it still navigates to the file/folder
3. Verify single click on icon doesn't trigger navigation
4. Verify double click on icon doesn't trigger preview (navigation takes priority)

- [ ] **Step 5: Test with real icon (when provided)**

1. Replace `src/main/resources/icons/virtual-node-icon.png` with actual icon
2. Rebuild plugin: `./gradlew buildPlugin`
3. Restart IDE and verify icon displays correctly
4. Test preview with real icon

---

## Task 6: Update Documentation

**Files:**
- Modify: `USAGE_GUIDE.md`

- [ ] **Step 1: Document virtual node icon feature**

Add to USAGE_GUIDE.md after the configuration options section:

```markdown
### Virtual Node Icons

Virtual nodes (intermediate nodes in the tree structure) display a custom icon:
- 🖼️ **Click the icon** to view enlarged version
- 📁 **Double-click** to navigate to the file/folder

To customize the icon, replace:
```
src/main/resources/icons/virtual-node-icon.png
```

Recommended icon size: 16x16 or 32x32 pixels (PNG format)
```

- [ ] **Step 2: Commit documentation**

```bash
git add USAGE_GUIDE.md
git commit -m "docs: add virtual node icon preview documentation"
```

---

## Verification Checklist

After implementation, verify:

- [ ] Virtual nodes display custom icon (not default folder icon)
- [ ] Single click on icon shows preview popup
- [ ] Preview popup displays icon at actual size
- [ ] Preview popup is resizable and movable
- [ ] Double-click navigation still works correctly
- [ ] Clicking outside icon area doesn't trigger preview
- [ ] Error handling works if icon file is missing
- [ ] Plugin builds without errors
- [ ] Documentation is updated

---

## Notes

**Icon Detection Logic:**
The icon click detection uses approximate positioning. The icon is expected to be in the left portion of the row (roughly 20x20 pixels from the left edge). This works with standard tree cell renderers but may need adjustment if custom renderers change icon placement.

**Alternative Approaches Considered:**
1. **Exact hit testing:** Would require accessing renderer internals - more complex
2. **Separate icon component:** Would require custom UI - overkill for this feature
3. **Context menu:** Less discoverable than direct click

**Future Enhancements:**
- Allow different icons per node type via configuration
- Add animation/scale effect on icon hover
- Support icons from external URLs
- Add keyboard shortcut for preview (e.g., Space bar)

---

## Implementation Order

Execute tasks in sequence:
1. Task 1 (Icon setup)
2. Task 2 (Node icon update)
3. Task 4 (Notification config - can be done in parallel)
4. Task 3 (Click detection & preview)
5. Task 5 (Testing)
6. Task 6 (Documentation)
