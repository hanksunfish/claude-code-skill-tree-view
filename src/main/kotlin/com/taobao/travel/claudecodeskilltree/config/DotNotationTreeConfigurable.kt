package com.taobao.travel.claudecodeskilltree.config

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import javax.swing.*

/**
 * 插件配置界面
 * 提供插件设置的图形化配置界面
 */
class DotNotationTreeConfigurable : Configurable {

    private var showVirtualNodeIconsCheckBox: JCheckBox? = null
    private var iconFilesField: JTextField? = null
    private var settings: DotNotationTreeState? = null

    override fun getDisplayName(): String {
        return "Claude Code Skill Tree View"
    }

    override fun getHelpTopic(): String? {
        return null
    }

    override fun createComponent(): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

        // 启用/禁用自定义虚拟节点图标
        showVirtualNodeIconsCheckBox = JCheckBox("显示自定义虚拟节点图标")
        showVirtualNodeIconsCheckBox?.toolTipText = "启用后，虚拟节点将显示自定义图标而非默认文件夹图标"

        // 图标文件列表
        val iconLabel = JLabel("图标文件列表（用逗号分隔）:")
        iconFilesField = JTextField(30)
        iconFilesField?.toolTipText = "例如: virtual-node-icon-0.png, virtual-node-icon-1.png, virtual-node-icon-2.png"

        val iconFilesPanel = JPanel()
        iconFilesPanel.layout = BoxLayout(iconFilesPanel, BoxLayout.X_AXIS)
        iconFilesPanel.add(iconLabel)
        iconFilesPanel.add(Box.createHorizontalStrut(10))
        iconFilesPanel.add(iconFilesField!!)

        // 添加说明
        val noteLabel = JLabel("提示: 图标文件必须位于 src/main/resources/icons/ 目录，且尺寸为 16x16 像素")
        noteLabel.font = java.awt.Font(noteLabel.font.name, java.awt.Font.ITALIC, noteLabel.font.size)

        panel.add(showVirtualNodeIconsCheckBox!!)
        panel.add(Box.createVerticalStrut(10))
        panel.add(iconFilesPanel)
        panel.add(Box.createVerticalStrut(10))
        panel.add(noteLabel)

        return panel
    }

    override fun isModified(): Boolean {
        val currentSettings = getSettings()
        return showVirtualNodeIconsCheckBox?.isSelected != currentSettings.showVirtualNodeIcons ||
                iconFilesField?.text != currentSettings.iconFiles
    }

    override fun apply() {
        val currentSettings = getSettings()
        currentSettings.showVirtualNodeIcons = showVirtualNodeIconsCheckBox?.isSelected ?: true
        currentSettings.iconFiles = iconFilesField?.text ?: "virtual-node-icon-0.png,virtual-node-icon-1.png,virtual-node-icon-2.png"
    }

    override fun reset() {
        val currentSettings = getSettings()
        showVirtualNodeIconsCheckBox?.isSelected = currentSettings.showVirtualNodeIcons
        iconFilesField?.text = currentSettings.iconFiles
    }

    private fun getSettings(): DotNotationTreeState {
        if (settings == null) {
            val project = ProjectManager.getInstance().openProjects.firstOrNull()
            settings = project?.getService(DotNotationTreeState::class.java) ?: DotNotationTreeState()
        }
        return settings!!
    }
}
