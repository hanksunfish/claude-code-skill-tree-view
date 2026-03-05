package com.taobao.travel.claudecodeskilltree.config

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.ProjectManager
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.table.JBTable
import java.awt.*
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder
import javax.swing.border.TitledBorder
import javax.swing.table.DefaultTableModel

/**
 * 插件配置界面
 * 提供插件设置的图形化配置界面
 */
class DotNotationTreeConfigurable : Configurable {

    private var enableIconsCheckBox: JCheckBox? = null
    private var iconTable: JBTable? = null
    private var tableModel: DefaultTableModel? = null
    private var settings: DotNotationTreeState? = null
    private val iconDir = "icons/"

    // 列名
    private val columnNames = arrayOf("预览", "文件名")

    override fun getDisplayName(): String {
        return "Claude Code Skill Tree View"
    }

    override fun getHelpTopic(): String? {
        return null
    }

    override fun createComponent(): JComponent {
        val mainPanel = JPanel()
        mainPanel.layout = BoxLayout(mainPanel, BoxLayout.Y_AXIS)
        mainPanel.border = EmptyBorder(10, 10, 10, 10)

        // ========== 第一部分：启用开关 ==========
        enableIconsCheckBox = JCheckBox("启用自定义虚拟节点图标")
        enableIconsCheckBox?.toolTipText = "启用后，虚拟节点将显示自定义图标而非默认文件夹图标"
        enableIconsCheckBox?.font = Font(enableIconsCheckBox?.font?.name, Font.BOLD, 13)

        // ========== 第二部分：图标管理 ==========
        val tablePanel = JPanel(BorderLayout(10, 10))
        tablePanel.border = TitledBorder(
            LineBorder(JBColor.border()),
            "图标列表（16x16 像素 PNG 图片）",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            Font(null, Font.PLAIN, 12)
        )

        // 表格模型
        tableModel = object : DefaultTableModel(columnNames, 0) {
            override fun isCellEditable(row: Int, column: Int): Boolean = false
        }

        iconTable = JBTable(tableModel)
        iconTable?.rowHeight = 30
        iconTable?.columnModel?.getColumn(0)?.preferredWidth = 50  // 预览列宽度
        iconTable?.columnModel?.getColumn(1)?.preferredWidth = 200 // 文件名列宽度

        // 设置列渲染器 - 第一列显示图片
        iconTable?.columnModel?.getColumn(0)?.cellRenderer = IconRenderer()
        iconTable?.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

        val scrollPane = JScrollPane(iconTable)
        scrollPane.preferredSize = Dimension(300, 150)

        // 按钮面板
        val buttonPanel = JPanel(FlowLayout(FlowLayout.LEFT, 5, 5))

        val addButton = JButton("+ 添加")
        addButton.addActionListener { addIconFile() }

        val removeButton = JButton("- 删除")
        removeButton.addActionListener { removeIconFile() }

        val upButton = JButton("↑ 上移")
        upButton.addActionListener { moveIconUp() }

        val downButton = JButton("↓ 下移")
        downButton.addActionListener { moveIconDown() }

        buttonPanel.add(addButton)
        buttonPanel.add(removeButton)
        buttonPanel.add(Box.createHorizontalStrut(20))
        buttonPanel.add(upButton)
        buttonPanel.add(downButton)

        // ========== 第三部分：说明 ==========
        val notePanel = JPanel()
        notePanel.layout = BoxLayout(notePanel, BoxLayout.Y_AXIS)
        notePanel.border = TitledBorder(
            LineBorder(JBColor.border()),
            "使用说明",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            Font(null, Font.PLAIN, 12)
        )

        val noteLabel = JLabel("""
            <html>
            <ul>
                <li>图标文件必须放在 <code>src/main/resources/icons/</code> 目录</li>
                <li>推荐图片尺寸: 16x16 像素 PNG 格式</li>
                <li>虚拟节点会随机显示列表中的某个图标</li>
                <li>点击树中虚拟节点的图标可预览大图</li>
            </ul>
            </html>
        """.trimIndent())
        noteLabel.font = Font(null, Font.PLAIN, 12)

        notePanel.add(noteLabel)

        // 组装面板
        mainPanel.add(enableIconsCheckBox!!)
        mainPanel.add(Box.createVerticalStrut(15))
        mainPanel.add(scrollPane)
        mainPanel.add(Box.createVerticalStrut(5))
        mainPanel.add(buttonPanel)
        mainPanel.add(Box.createVerticalStrut(15))
        mainPanel.add(notePanel)

        return mainPanel
    }

    /**
     * 添加图标文件
     */
    private fun addIconFile() {
        val fileName = JOptionPane.showInputDialog(
            null,
            "请输入图标文件名（例如: my-icon.png）:",
            "添加图标",
            JOptionPane.QUESTION_MESSAGE
        )

        if (!fileName.isNullOrBlank()) {
            // 检查是否已存在
            for (i in 0 until tableModel!!.rowCount) {
                if (tableModel!!.getValueAt(i, 1) == fileName) {
                    JOptionPane.showMessageDialog(null, "该图标已存在！")
                    return
                }
            }

            // 尝试加载图标
            val icon = loadIcon(fileName.trim())

            // 添加新行
            tableModel!!.addRow(arrayOf(icon, fileName.trim()))
        }
    }

    /**
     * 删除选中的图标
     */
    private fun removeIconFile() {
        val selectedRow = iconTable?.selectedRow ?: -1
        if (selectedRow >= 0) {
            tableModel!!.removeRow(selectedRow)
        } else {
            JOptionPane.showMessageDialog(null, "请先选择要删除的图标！")
        }
    }

    /**
     * 上移图标
     */
    private fun moveIconUp() {
        val selectedRow = iconTable?.selectedRow ?: -1
        if (selectedRow > 0) {
            val icon = tableModel!!.getValueAt(selectedRow, 0)
            val fileName = tableModel!!.getValueAt(selectedRow, 1)
            tableModel!!.removeRow(selectedRow)
            tableModel!!.insertRow(selectedRow - 1, arrayOf(icon, fileName))
            iconTable?.setRowSelectionInterval(selectedRow - 1, selectedRow - 1)
        }
    }

    /**
     * 下移图标
     */
    private fun moveIconDown() {
        val selectedRow = iconTable?.selectedRow ?: -1
        if (selectedRow >= 0 && selectedRow < tableModel!!.rowCount - 1) {
            val icon = tableModel!!.getValueAt(selectedRow, 0)
            val fileName = tableModel!!.getValueAt(selectedRow, 1)
            tableModel!!.removeRow(selectedRow)
            tableModel!!.insertRow(selectedRow + 1, arrayOf(icon, fileName))
            iconTable?.setRowSelectionInterval(selectedRow + 1, selectedRow + 1)
        }
    }

    override fun isModified(): Boolean {
        val currentSettings = getSettings()
        val iconFiles = getIconFilesFromTable()

        return enableIconsCheckBox?.isSelected != currentSettings.showVirtualNodeIcons ||
                iconFiles != currentSettings.iconFiles
    }

    override fun apply() {
        val currentSettings = getSettings()
        currentSettings.showVirtualNodeIcons = enableIconsCheckBox?.isSelected ?: true
        currentSettings.iconFiles = getIconFilesFromTable()
    }

    override fun reset() {
        val currentSettings = getSettings()
        enableIconsCheckBox?.isSelected = currentSettings.showVirtualNodeIcons

        // 清空表格
        tableModel?.rowCount = 0

        // 加载图标文件列表
        val iconFiles = currentSettings.iconFiles.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        for (fileName in iconFiles) {
            val icon = loadIcon(fileName)
            tableModel?.addRow(arrayOf(icon, fileName))
        }
    }

    /**
     * 从表格获取图标文件列表
     */
    private fun getIconFilesFromTable(): String {
        val files = mutableListOf<String>()
        for (i in 0 until tableModel!!.rowCount) {
            files.add(tableModel!!.getValueAt(i, 1).toString())
        }
        return files.joinToString(",")
    }

    /**
     * 加载图标
     */
    private fun loadIcon(fileName: String): Icon? {
        return try {
            val url = DotNotationTreeConfigurable::class.java.classLoader.getResource(iconDir + fileName)
            if (url != null) {
                val image = javax.imageio.ImageIO.read(url)
                ImageIcon(image.getScaledInstance(20, 20, Image.SCALE_DEFAULT))
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun getSettings(): DotNotationTreeState {
        if (settings == null) {
            val project = ProjectManager.getInstance().openProjects.firstOrNull()
            settings = project?.getService(DotNotationTreeState::class.java) ?: DotNotationTreeState()
        }
        return settings!!
    }

    /**
     * 表格图标渲染器
     */
    private inner class IconRenderer : JPanel(), javax.swing.table.TableCellRenderer {

        private val iconLabel = JLabel()

        init {
            layout = FlowLayout(FlowLayout.CENTER, 0, 0)
            add(iconLabel)
            iconLabel.preferredSize = Dimension(20, 20)
        }

        override fun getTableCellRendererComponent(
            table: JTable?,
            value: Any?,
            isSelected: Boolean,
            hasFocus: Boolean,
            row: Int,
            column: Int
        ): Component {
            if (value != null && value is Icon) {
                iconLabel.icon = value
            } else {
                iconLabel.icon = null
            }

            // 选中背景色
            background = if (isSelected) {
                JBColor.namedColor("Table.selectionBackground", JBColor.BLUE)
            } else {
                JBColor.WHITE
            }

            return this
        }
    }
}
