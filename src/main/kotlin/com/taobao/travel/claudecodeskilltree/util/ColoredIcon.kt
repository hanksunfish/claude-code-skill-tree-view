package com.taobao.travel.claudecodeskilltree.util

import com.intellij.openapi.util.IconLoader
import com.intellij.ui.JBColor
import com.intellij.ui.icons.RowIcon
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Color
import javax.swing.Icon

/**
 * 带颜色叠加的图标
 * 为原始图标添加颜色效果
 */
class ColoredIcon(
    private val original: Icon,
    private val color: Color
) : Icon {

    override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
        // 绘制原始图标
        original.paintIcon(c, g, x, y)

        // 添加颜色效果
        val g2d = g as? Graphics2D ?: return
        g2d.color = color
        g2d.drawRect(x, y, iconWidth - 1, iconHeight - 1)
    }

    override fun getIconWidth(): Int = original.iconWidth

    override fun getIconHeight(): Int = original.iconHeight

    companion object {
        /**
         * 创建蓝色文件夹图标
         */
        fun createBlueFolderIcon(): Icon {
            return ColoredIcon(
                com.intellij.icons.AllIcons.Nodes.Folder,
                JBColor.BLUE
            )
        }
    }
}
