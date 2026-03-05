package com.taobao.travel.claudecodeskilltree.config

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.Messages
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.table.JBTable
import com.intellij.util.ImageLoader
import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileOutputStream
import javax.imageio.ImageIO
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
        scrollPane.preferredSize = Dimension(320, 180)

        // 按钮面板
        val buttonPanel = JPanel(FlowLayout(FlowLayout.LEFT, 5, 5))

        val uploadButton = JButton("📁 上传图片")
        uploadButton.addActionListener { uploadIconFile() }

        val removeButton = JButton("- 删除")
        removeButton.addActionListener { removeIconFile() }

        val upButton = JButton("↑ 上移")
        upButton.addActionListener { moveIconUp() }

        val downButton = JButton("↓ 下移")
        downButton.addActionListener { moveIconDown() }

        buttonPanel.add(uploadButton)
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
                <li>点击"上传图片"按钮选择本地 PNG 图片</li>
                <li>图片会自动缩放为 16x16 像素</li>
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
     * 上传图标文件
     */
    private fun uploadIconFile() {
        // 创建文件选择器
        val fileChooser = JFileChooser()
        fileChooser.dialogTitle = "选择图标图片"
        fileChooser.fileSelectionMode = JFileChooser.FILES_ONLY
        fileChooser.isMultiSelectionEnabled = true

        // 设置文件过滤器 - 只显示图片文件
        val imageFilter = javax.swing.filechooser.FileNameExtensionFilter(
            "图片文件 (PNG, JPG, GIF, SVG)",
            "png", "jpg", "jpeg", "gif", "svg"
        )
        fileChooser.fileFilter = imageFilter

        // 显示选择对话框
        val result = fileChooser.showOpenDialog(null)

        if (result == JFileChooser.APPROVE_OPTION) {
            val selectedFiles = fileChooser.selectedFiles

            for (file in selectedFiles) {
                if (file != null) {
                    try {
                        // 复制并缩放图片到图标目录
                        val savedFile = saveIconFile(file)
                        if (savedFile != null) {
                            // 加载图标并添加到表格
                            val icon = loadIcon(savedFile.name)
                            tableModel?.addRow(arrayOf(icon, savedFile.name))
                        }
                    } catch (e: Exception) {
                        Messages.showErrorDialog("上传图片失败: ${e.message}", "错误")
                    }
                }
            }
        }
    }

    /**
     * 保存图标文件到资源目录
     */
    private fun saveIconFile(sourceFile: File): File? {
        // 获取图标资源目录
        val resourceDir = getIconsDirectory()

        // 生成唯一的文件名
        val originalName = sourceFile.nameWithoutExtension
        val extension = if (sourceFile.extension.isNotEmpty()) sourceFile.extension else "png"
        var targetFile = File(resourceDir, "$originalName.$extension")

        // 如果文件已存在，添加数字后缀
        var counter = 1
        while (targetFile.exists()) {
            targetFile = File(resourceDir, "$originalName-$counter.$extension")
            counter++
        }

        // 读取并处理图片
        try {
            val image = ImageIO.read(sourceFile)

            // 如果图片不是16x16，进行缩放
            val scaledImage: BufferedImage
            if (image.width != 16 || image.height != 16) {
                scaledImage = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
                val graphics = scaledImage.createGraphics()
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
                graphics.drawImage(image, 0, 0, 16, 16, null)
                graphics.dispose()
            } else {
                scaledImage = image
            }

            // 保存图片
            ImageIO.write(scaledImage, extension, targetFile)

            return targetFile
        } catch (e: Exception) {
            Messages.showErrorDialog("处理图片失败: ${e.message}", "错误")
            return null
        }
    }

    /**
     * 获取图标资源目录
     */
    private fun getIconsDirectory(): File {
        // 尝试获取资源目录
        val classLoader = DotNotationTreeConfigurable::class.java.classLoader

        // 尝试通过类路径找到资源目录
        val resourceUrl = classLoader.getResource("icons/")
        if (resourceUrl != null) {
            // 从 URL 获取实际文件路径
            val urlPath = resourceUrl.path
            // 处理 URL 编码
            val decodedPath = java.net.URLDecoder.decode(urlPath, "UTF-8")
            // 处理文件协议
            val filePath = if (decodedPath.startsWith("file:")) {
                decodedPath.removePrefix("file:")
            } else {
                decodedPath
            }
            return File(filePath)
        }

        // 如果找不到资源目录，使用默认路径（相对于项目根目录）
        val projectRoot = File("src/main/resources")
        val iconsDir = File(projectRoot, "icons")
        if (!iconsDir.exists()) {
            iconsDir.mkdirs()
        }
        return iconsDir
    }

    /**
     * 删除选中的图标
     */
    private fun removeIconFile() {
        val selectedRow = iconTable?.selectedRow ?: -1
        if (selectedRow >= 0) {
            val fileName = tableModel!!.getValueAt(selectedRow, 1).toString()

            // 询问是否删除文件
            val result = Messages.showYesNoDialog(
                "是否删除图标文件 \"$fileName\"？",
                "确认删除",
                Messages.getQuestionIcon()
            )

            if (result == Messages.YES) {
                // 删除文件
                try {
                    val iconsDir = getIconsDirectory()
                    val iconFile = File(iconsDir, fileName)
                    if (iconFile.exists()) {
                        iconFile.delete()
                    }
                } catch (e: Exception) {
                    // 忽略删除文件失败
                }

                // 从表格中移除
                tableModel!!.removeRow(selectedRow)
            }
        } else {
            Messages.showMessageDialog("请先选择要删除的图标！", "提示", Messages.getInformationIcon())
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
            val url = DotNotationTreeConfigurable::class.java.classLoader.getResource("icons/$fileName")
            if (url != null) {
                val image = ImageIO.read(url)
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
