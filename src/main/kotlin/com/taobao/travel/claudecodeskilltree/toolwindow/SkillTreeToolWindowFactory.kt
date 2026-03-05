package com.taobao.travel.claudecodeskilltree.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.treeStructure.Tree
import com.taobao.travel.claudecodeskilltree.config.DotNotationTreeState
import com.taobao.travel.claudecodeskilltree.tree.SkillTreeCellRenderer
import com.taobao.travel.claudecodeskilltree.tree.SkillTreeModel
import com.taobao.travel.claudecodeskilltree.tree.SkillTreeNode
import java.awt.BorderLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.tree.DefaultTreeCellRenderer

/**
 * Skills Tree 工具窗口工���
 * 创建并初始化显示 skills 树形结构的工具窗口
 */
class SkillTreeToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        // 创建主面板
        val mainPanel = JBPanel<JBPanel<*>>(BorderLayout())

        // 创建树组件
        val treePanel = createSkillTree(project)
        val tree = treePanel.first

        // 添加刷新按钮面板
        val toolbarPanel = createToolbar(project, tree)

        // 组装界面
        mainPanel.add(toolbarPanel, BorderLayout.NORTH)
        mainPanel.add(treePanel.second, BorderLayout.CENTER)

        // 添加到工具窗口
        val contentManager = toolWindow.contentManager
        val content = contentManager.factory.createContent(mainPanel, "", false)
        contentManager.addContent(content)
    }

    /**
     * 创建技能树
     */
    private fun createSkillTree(project: Project): Pair<Tree, JPanel> {
        val settings = project.getService(DotNotationTreeState::class.java)

        if (!settings.enabled) {
            // 如果插件未启用，显示提示信息
            val panel = JBPanel<JBPanel<*>>()
            panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
            panel.add(JBLabel("Claude Code Skill Tree View 插件未启用"))
            val tree = Tree()
            return Pair(tree, panel)
        }

        // 构建树模型
        val treeModel = SkillTreeModel(project)
        val tree = Tree(treeModel)

        // 将 Project 存储为树的客户端属性，供 Renderer 使用
        tree.putClientProperty("project.key", project)

        // 设置自定义渲染器 - 显示图标和样式
        tree.cellRenderer = SkillTreeCellRenderer()

        // 设置树的选择模式
        tree.selectionModel.selectionMode = javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION

        // 设置树的外观
        tree.rowHeight = 24  // 增加行高以更好显示图标
        tree.showsRootHandles = true  // 显示展开/折叠手柄
        tree.isRootVisible = false  // 隐藏根节点

        // 添加双击导航监听器
        tree.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    val path = tree.selectionPath
                    if (path != null) {
                        val node = path.lastPathComponent as? SkillTreeNode
                        node?.let {
                            navigateToFile(it, project)
                        }
                    }
                }
            }
        })

        // 创建包含树的滚动面板
        val scrollPane = javax.swing.JScrollPane(tree)
        val panel = JBPanel<JBPanel<*>>(BorderLayout())
        panel.add(scrollPane, BorderLayout.CENTER)

        return Pair(tree, panel)
    }

    /**
     * 创建工具栏
     */
    private fun createToolbar(project: Project, tree: Tree): JPanel {
        val panel = JBPanel<JBPanel<*>>()

        // 添加刷新按钮
        val refreshButton = JButton("刷新")
        refreshButton.addActionListener {
            refreshTree(tree, project)
        }

        // 添加展开全部按钮
        val expandButton = JButton("展开全部")
        expandButton.addActionListener {
            expandAll(tree)
        }

        // 添加折叠全部按钮
        val collapseButton = JButton("折叠全部")
        collapseButton.addActionListener {
            collapseAll(tree)
        }

        // 添加配置按钮
        val configButton = JButton("配置")
        configButton.addActionListener {
            showConfigDialog(project)
            refreshTree(tree, project)
        }

        panel.add(refreshButton)
        panel.add(expandButton)
        panel.add(collapseButton)
        panel.add(configButton)

        return panel
    }

    /**
     * 刷新树
     */
    private fun refreshTree(tree: Tree, project: Project) {
        val newModel = SkillTreeModel(project)
        tree.model = newModel

        // 展开第一层
        val root = tree.model.root as? SkillTreeNode
        if (root != null) {
            for (i in 0 until root.childCount) {
                val child = root.getChildAt(i) as? SkillTreeNode
                if (child != null) {
                    // 展开根节点下的第一层
                    tree.expandRow(i)
                }
            }
        }
    }

    /**
     * 展开全部节点
     */
    private fun expandAll(tree: Tree) {
        var row = 0
        while (row < tree.rowCount) {
            tree.expandRow(row)
            row++
        }
    }

    /**
     * 折叠全部节点
     */
    private fun collapseAll(tree: Tree) {
        var row = tree.rowCount - 1
        while (row >= 0) {
            tree.collapseRow(row)
            row--
        }
    }

    /**
     * 显示配置对话框
     */
    private fun showConfigDialog(project: Project) {
        com.intellij.openapi.options.ShowSettingsUtil.getInstance().showSettingsDialog(
            project,
            "Claude Code Skill Tree View"
        )
    }

    /**
     * 导航到文件
     */
    private fun navigateToFile(node: SkillTreeNode, project: Project) {
        val virtualFile = node.virtualFile
        if (virtualFile != null) {
            // 在项目视图中定位并选择文件
            com.intellij.ide.projectView.ProjectView.getInstance(project)
                .select(virtualFile, virtualFile, true)

            // 如果是文件，打开它
            if (!virtualFile.isDirectory) {
                com.intellij.openapi.fileEditor.FileEditorManager.getInstance(project)
                    .openFile(virtualFile, true, true)
            }
        }
    }
}
