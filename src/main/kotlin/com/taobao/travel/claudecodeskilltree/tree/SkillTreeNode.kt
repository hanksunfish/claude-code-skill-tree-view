package com.taobao.travel.claudecodeskilltree.tree

import com.intellij.openapi.vfs.VirtualFile
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeNode

/**
 * Skills 树节点
 * 表示树中的一个节点，可以是虚拟节点或实际文件节点
 */
class SkillTreeNode(
    val name: String,
    val virtualFile: VirtualFile?,
    val isVirtual: Boolean = false
) : DefaultMutableTreeNode() {

    private val _children: MutableList<SkillTreeNode> = mutableListOf()

    val children: List<SkillTreeNode>
        get() = _children

    /**
     * 添加子节点
     */
    fun addChild(child: SkillTreeNode) {
        _children.add(child)
        child.setParent(this)
    }

    /**
     * 查找子节点
     */
    fun findChild(name: String): SkillTreeNode? {
        return _children.find { it.name == name }
    }

    /**
     * 获取显示文本
     */
    fun getDisplayText(): String {
        return if (isVirtual) {
            "$name"
        } else {
            name
        }
    }

    /**
     * 获取图标
     */
    fun getIcon(project: com.intellij.openapi.project.Project): javax.swing.Icon {
        return if (isVirtual) {
            com.intellij.icons.AllIcons.Nodes.Folder
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

    override fun toString(): String {
        return getDisplayText()
    }

    override fun getChildAt(index: Int): TreeNode? {
        return _children.getOrNull(index)
    }

    override fun getChildCount(): Int {
        return _children.size
    }

    override fun getIndex(node: TreeNode?): Int {
        return if (node is SkillTreeNode) {
            _children.indexOf(node)
        } else {
            -1
        }
    }

    override fun getAllowsChildren(): Boolean {
        return true
    }

    override fun isLeaf(): Boolean {
        return _children.isEmpty()
    }

    @Suppress("UNCHECKED_CAST")
    override fun children(): java.util.Enumeration<TreeNode> {
        return java.util.Collections.enumeration(_children) as java.util.Enumeration<TreeNode>
    }
}
