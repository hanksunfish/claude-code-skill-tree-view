package com.taobao.travel.claudecodeskilltree.tree

import com.intellij.openapi.vfs.VirtualFile

/**
 * Builder for creating virtual tree structures from dot-notation folders
 * Implements smart grouping to handle overlapping folder names
 */
class TreeViewBuilder {

    /**
     * Builds a virtual tree structure from a list of files
     * @param files List of files to include in the tree
     * @param separator Character used to split folder names (default: ".")
     * @return List of root nodes in the virtual tree
     */
    fun buildVirtualTree(files: List<VirtualFile>, separator: String = "."): List<VirtualTreeNode> {
        val roots = mutableListOf<VirtualTreeNode>()

        for (file in files) {
            if (!file.isDirectory) continue

            val fileName = file.name
            if (!fileName.contains(separator)) {
                // Not a dot-notation folder, add as-is
                roots.add(VirtualTreeNode(fileName, file, isVirtual = false))
                continue
            }

            // Parse the dot-notation name
            val parts = fileName.split(separator)
            if (parts.size <= 1) continue

            // Build or find the root node
            var current = roots.find { it.name == parts[0] }
            if (current == null) {
                current = VirtualTreeNode(parts[0], null, isVirtual = true)
                roots.add(current)
            }

            // Build the path recursively
            buildPath(current, parts, 1, file)
        }

        return roots
    }

    /**
     * Recursively builds the path for a file
     * @param parent Current parent node
     * @param parts Parts of the dot-notation name
     * @param index Current index in parts
     * @param realFile The actual physical file
     */
    private fun buildPath(
        parent: VirtualTreeNode,
        parts: List<String>,
        index: Int,
        realFile: VirtualFile
    ) {
        if (index >= parts.size) return

        val partName = parts[index]
        val isLast = (index == parts.size - 1)

        // Check if a child with this name already exists
        var child = parent.findChild(partName)

        if (child == null) {
            // Create a new child node
            child = VirtualTreeNode(
                name = partName,
                realFile = if (isLast) realFile else null,
                isVirtual = !isLast,
                parent = parent
            )
            parent.addChild(child)
        } else {
            // Child already exists - need smart grouping
            if (isLast) {
                // This file represents the actual folder at this path
                if (child.isVirtual) {
                    // Convert virtual node to have real content too
                    // Update it to have the real file but keep being virtual (for children)
                    child = child.copy(realFile = realFile, isVirtual = false)
                    val idx = parent.children.indexOfFirst { it.name == partName }
                    if (idx >= 0) {
                        parent.children[idx] = child
                    }
                }
                // If child is already real, nothing to do
            } else {
                // Not the last part, so we're building a path through this node
                if (child.isReal) {
                    // This node is real but we need to go deeper
                    // Convert it to virtual so it can have children
                    // But keep track that it has real content
                    child = child.copy(isVirtual = true)
                    val idx = parent.children.indexOfFirst { it.name == partName }
                    if (idx >= 0) {
                        parent.children[idx] = child
                    }
                }
            }
        }

        // Continue building the path
        if (!isLast) {
            buildPath(child, parts, index + 1, realFile)
        }
    }
}
