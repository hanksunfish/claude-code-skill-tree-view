package com.taobao.travel.claudecodeskilltree.tree

import com.intellij.openapi.project.Project
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes
import java.awt.Component
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

/**
 * Skills Tree Cell Renderer
 * 自定义树单元格渲染器，显示图标和文本
 */
class SkillTreeCellRenderer : ColoredTreeCellRenderer() {

    override fun customizeCellRenderer(
        tree: JTree,
        value: Any?,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean
    ) {
        if (value is SkillTreeNode) {
            // 获取 Project 对象
            val project = tree.getClientProperty("project.key") as? Project
            if (project != null) {
                // 设置图标
                icon = value.getIcon(project)
            } else {
                // 如果无法获取 Project，使用默认图标
                icon = value.getIcon(com.intellij.openapi.project.ProjectManager.getInstance().defaultProject)
            }

            // 设置文本
            if (value.isVirtual) {
                // 虚拟节点使用灰色斜体
                append(value.name, SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES)
            } else {
                // 真实节点使用正常文本
                append(value.name)

                // 如果是文件，添加文件类型提示
                if (value.virtualFile != null && !value.virtualFile.isDirectory) {
                    val extension = value.virtualFile.extension
                    if (!extension.isNullOrEmpty()) {
                        append(" ($extension)", SimpleTextAttributes.GRAY_ATTRIBUTES)
                    }
                }
            }

            // 设置工具提示
            toolTipText = buildTooltip(value)
        } else {
            // 非自定义节点，使用默认渲染
            append(value?.toString() ?: "")
        }
    }

    private fun buildTooltip(node: SkillTreeNode): String {
        val sb = StringBuilder()
        sb.append(node.name)

        if (node.isVirtual) {
            sb.append(" (虚拟节点)")
        }

        if (node.virtualFile != null) {
            sb.append("\n路径: ").append(node.virtualFile.path)

            // 显示文件信息
            if (!node.virtualFile.isDirectory) {
                val size = node.virtualFile.length
                sb.append("\n大小: ").append(formatSize(size))
            }
        }

        return sb.toString()
    }

    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }
}
