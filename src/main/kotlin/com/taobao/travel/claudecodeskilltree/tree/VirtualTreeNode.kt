package com.taobao.travel.claudecodeskilltree.tree

import com.intellij.openapi.vfs.VirtualFile

/**
 * Virtual tree node representing either a virtual directory or a real file
 * @property name Display name of the node
 * @property realFile The actual physical file (null for virtual nodes)
 * @property isVirtual Whether this node is virtual (true) or real (false)
 * @property parent Parent node in the tree
 * @property children List of child nodes
 */
data class VirtualTreeNode(
    val name: String,
    val realFile: VirtualFile?,
    val isVirtual: Boolean,
    val parent: VirtualTreeNode? = null,
    val children: MutableList<VirtualTreeNode> = mutableListOf()
) {
    /**
     * Returns true if this node represents a real physical file
     */
    val isReal: Boolean
        get() = !isVirtual && realFile != null

    /**
     * Adds a child node to this node
     */
    fun addChild(child: VirtualTreeNode) {
        children.add(child)
    }

    /**
     * Finds a child node by name
     * @return The child node if found, null otherwise
     */
    fun findChild(name: String): VirtualTreeNode? {
        return children.find { it.name == name }
    }
}