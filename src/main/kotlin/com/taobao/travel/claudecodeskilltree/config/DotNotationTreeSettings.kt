package com.taobao.travel.claudecodeskilltree.config

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBTextField
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.CollectionListModel
import java.awt.BorderLayout
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.ListSelectionModel

/**
 * 插件配置界面
 * 允许用户配置目标目录、分隔符等选项
 */
class DotNotationTreeConfigurable(private val project: Project) : Configurable {

    private lateinit var mainPanel: JPanel
    private lateinit var directoryList: JBList<String>
    private lateinit var directoryListModel: CollectionListModel<String>
    private lateinit var separatorField: JBTextField
    private lateinit var enabledCheckbox: com.intellij.ui.components.JBCheckBox
    private lateinit var showIconsCheckbox: com.intellij.ui.components.JBCheckBox

    private val settings: DotNotationTreeState
        get() = project.getService(DotNotationTreeState::class.java)

    override fun getDisplayName(): String = "Claude Code Skill Tree View"

    override fun createComponent(): JComponent {
        mainPanel = JPanel(BorderLayout())

        // 创建目录列表
        directoryListModel = CollectionListModel(settings.targetDirectories)
        directoryList = JBList(directoryListModel)
        directoryList.selectionMode = ListSelectionModel.SINGLE_SELECTION

        // 创建工具栏装饰器（添加、删除按钮）
        val toolbarDecorator = ToolbarDecorator.createDecorator(directoryList)
            .setAddAction { addDirectory() }
            .setRemoveAction { removeDirectory() }
            .setEditAction { editDirectory() }

        val directoryPanel = JPanel(BorderLayout())
        directoryPanel.add(toolbarDecorator.createPanel(), BorderLayout.CENTER)

        // 创建选项面板
        val optionsPanel = JPanel()

        // 分隔符输入
        separatorField = JBTextField(settings.separator, 10)
        val separatorPanel = JPanel()
        separatorPanel.add(com.intellij.ui.components.JBLabel("分隔符:"))
        separatorPanel.add(separatorField)
        separatorPanel.add(com.intellij.ui.components.JBLabel("""
            <html><small>用于分隔文件夹层级的字符<br>默认为 "." (点号)</small></html>
        """.trimIndent()))

        // 启用复选框
        enabledCheckbox = com.intellij.ui.components.JBCheckBox("启用插件", settings.enabled)

        // 显示图标复选框
        showIconsCheckbox = com.intellij.ui.components.JBCheckBox("显示虚拟节点图标", settings.showVirtualNodeIcons)

        optionsPanel.add(enabledCheckbox)
        optionsPanel.add(showIconsCheckbox)
        optionsPanel.add(separatorPanel)

        // 创建主布局
        val topPanel = JPanel()
        topPanel.layout = BoxLayout(topPanel, BoxLayout.Y_AXIS)
        topPanel.add(com.intellij.ui.components.JBLabel("<html><b>目标目录配置</b></html>"))
        topPanel.add(directoryPanel)
        topPanel.add(optionsPanel)

        // 添加说明文本
        val descriptionPanel = JPanel()
        descriptionPanel.add(com.intellij.ui.components.JBLabel("<html><b>使用说明:</b></html>"))
        descriptionPanel.add(com.intellij.ui.components.JBLabel("""
            <html>
            <div style="padding-left: 10px;">
            <p>配置目标目录后，该目录下的点号命名文件夹将显示为树形结构。</p>
            <p><b>示例:</b></p>
            <ul>
                <li>配置目录: <code>.claude/skills</code></li>
                <li>文件夹: <code>superpowers.test-driven-development</code></li>
                <li>显示为: <code>superpowers / test-driven-development</code></li>
            </ul>
            </div>
            </html>
        """.trimIndent()))

        mainPanel.add(topPanel, BorderLayout.NORTH)
        mainPanel.add(descriptionPanel, BorderLayout.CENTER)

        return mainPanel
    }

    private fun addDirectory() {
        val directory = com.intellij.openapi.ui.Messages.showInputDialog(
            project,
            "输入目标目录路径（相对于项目根目录）:\n例如: .claude/skills",
            "添加目标目录",
            com.intellij.openapi.ui.Messages.getQuestionIcon(),
            "",
            null
        )

        if (!directory.isNullOrBlank()) {
            if (!directoryListModel.items.contains(directory)) {
                directoryListModel.add(directory)
            }
        }
    }

    private fun removeDirectory() {
        val selected = directoryList.selectedValue
        if (selected != null) {
            directoryListModel.remove(selected)
        }
    }

    private fun editDirectory() {
        val selected = directoryList.selectedValue
        if (selected != null) {
            val newDirectory = com.intellij.openapi.ui.Messages.showInputDialog(
                project,
                "编辑目标目录路径:",
                "编辑目标目录",
                com.intellij.openapi.ui.Messages.getQuestionIcon(),
                selected,
                null
            )

            if (!newDirectory.isNullOrBlank() && newDirectory != selected) {
                val items = directoryListModel.items.toList()
                val index = items.indexOf(selected)
                if (index >= 0) {
                    directoryListModel.setElementAt(newDirectory, index)
                }
            }
        }
    }

    @Throws(ConfigurationException::class)
    override fun apply() {
        settings.targetDirectories = ArrayList(directoryListModel.items)
        settings.separator = separatorField.text
        settings.enabled = enabledCheckbox.isSelected
        settings.showVirtualNodeIcons = showIconsCheckbox.isSelected

        // 刷新项目视图
        com.intellij.openapi.project.ProjectManager.getInstance().openProjects.forEach { project ->
            com.intellij.ide.projectView.ProjectView.getInstance(project).refresh()
        }
    }

    override fun reset() {
        directoryListModel.removeAll()
        settings.targetDirectories.forEach { directoryListModel.add(it) }
        separatorField.text = settings.separator
        enabledCheckbox.isSelected = settings.enabled
        showIconsCheckbox.isSelected = settings.showVirtualNodeIcons
    }

    override fun isModified(): Boolean {
        return ArrayList(directoryListModel.items) != settings.targetDirectories ||
                separatorField.text != settings.separator ||
                enabledCheckbox.isSelected != settings.enabled ||
                showIconsCheckbox.isSelected != settings.showVirtualNodeIcons
    }
}
