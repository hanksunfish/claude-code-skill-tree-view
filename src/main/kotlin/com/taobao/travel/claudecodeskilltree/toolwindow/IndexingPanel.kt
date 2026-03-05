package com.taobao.travel.claudecodeskilltree.toolwindow

import com.intellij.icons.AllIcons
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
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
        iconLabel.icon = AllIcons.Actions.Refresh
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
