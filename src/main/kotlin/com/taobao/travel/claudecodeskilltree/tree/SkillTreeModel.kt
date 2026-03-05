package com.taobao.travel.claudecodeskilltree.tree

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.taobao.travel.claudecodeskilltree.config.DotNotationTreeState
import com.taobao.travel.claudecodeskilltree.parser.DotNotationParser
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode

/**
 * Skills 树模型
 * 负责构建和管理 skills 目录的树形结构
 */
class SkillTreeModel(private val project: Project) : DefaultTreeModel(buildRoot(project)) {

    companion object {
        /**
         * 构建树的根节点
         */
        private fun buildRoot(project: Project): TreeNode {
            val settings = project.getService(DotNotationTreeState::class.java)

            if (!settings.enabled) {
                return SkillTreeNode("插件未启用", null)
            }

            // 创建根节点
            val root = SkillTreeNode("Skills", null)
            val projectBasePath = project.basePath

            if (projectBasePath == null) {
                return SkillTreeNode("无项目路径", null)
            }

            // 遍历所有目标目录
            for (targetDir in settings.targetDirectories) {
                val targetPath = "$projectBasePath/$targetDir"
                val targetDirFile = com.intellij.openapi.vfs.VfsUtil.findFileByIoFile(
                    java.io.File(targetPath),
                    true
                )

                if (targetDirFile != null && targetDirFile.isDirectory) {
                    // 构建该目录下的树
                    val targetNode = buildTargetDirectoryTree(targetDirFile, targetDir, settings.separator)
                    mergeIntoTree(root, targetNode)
                }
            }

            // 如果没有找到任何目标目录
            if (root.children.isEmpty()) {
                val emptyNode = SkillTreeNode("未找到目标目录，请创建 .claude/skills 目录", null)
                root.addChild(emptyNode)
            }

            return root
        }

        /**
         * 构建目标目录的树
         */
        private fun buildTargetDirectoryTree(
            targetDirFile: VirtualFile,
            targetDirName: String,
            separator: String
        ): SkillTreeNode {
            val rootNode = SkillTreeNode(targetDirName, targetDirFile)

            // 获取所有子项（文件和文件夹）
            val children = targetDirFile.children
            if (children != null) {
                // 按文件名排序，文件夹在前
                val sortedChildren = children.sortedWith(compareBy(
                    { !it.isDirectory },  // 文件夹优先
                    { it.name.lowercase() }  // 然后按名称排序
                ))

                for (child in sortedChildren) {
                    if (child.isDirectory) {
                        // 处理文件夹
                        val childTree = buildDirectoryTree(child, separator)
                        mergeIntoTree(rootNode, childTree)
                    } else {
                        // 直接添加文件
                        val fileNode = SkillTreeNode(child.name, child, isVirtual = false)
                        rootNode.addChild(fileNode)
                    }
                }
            }

            return rootNode
        }

        /**
         * 构建单个目录的树（处理点号命名）
         */
        private fun buildDirectoryTree(
            directory: VirtualFile,
            separator: String
        ): SkillTreeNode {
            val fileName = directory.name

            if (!fileName.contains(separator)) {
                // 不需要解析，直接返回节点并添加其内容
                val node = SkillTreeNode(fileName, directory, isVirtual = false)
                addFilesToNode(node, directory)
                return node
            }

            // 解析点号命名
            val parts = fileName.split(separator)
            val rootNode = SkillTreeNode(parts[0], null, isVirtual = true)
            var currentParent = rootNode

            for (i in 1 until parts.size) {
                val isLast = (i == parts.size - 1)
                val childNode = if (isLast) {
                    // 最后一个部分对应实际文件夹
                    val realNode = SkillTreeNode(parts[i], directory, isVirtual = false)
                    // 添加该目录下的文件
                    addFilesToNode(realNode, directory)
                    realNode
                } else {
                    // 中间部分是虚拟节点
                    SkillTreeNode(parts[i], null, isVirtual = true)
                }
                currentParent.addChild(childNode)
                currentParent = childNode
            }

            return rootNode
        }

        /**
         * 添加目录下的文件到节点（递归）
         */
        private fun addFilesToNode(node: SkillTreeNode, directory: VirtualFile) {
            val children = directory.children ?: return

            // 按名称排序
            val sortedChildren = children.sortedWith(compareBy(
                { !it.isDirectory },  // 文件夹优先
                { it.name.lowercase() }
            ))

            for (child in sortedChildren) {
                val childNode = SkillTreeNode(child.name, child, isVirtual = false)
                node.addChild(childNode)

                // 如果是子文件夹，递归添加其内容
                if (child.isDirectory) {
                    addFilesToNode(childNode, child)
                }
            }
        }

        /**
         * 智能合并子树
         * 处理 a.b 和 a.b.c 的合并
         */
        private fun mergeIntoTree(parentNode: SkillTreeNode, childNode: SkillTreeNode) {
            val existingChild = parentNode.findChild(childNode.name)

            if (existingChild != null) {
                // 存在同名节点，需要合并
                when {
                    // 两个都是虚拟节点：递归合并子节点
                    existingChild.isVirtual && childNode.isVirtual -> {
                        for (grandchild in childNode.children) {
                            mergeIntoTree(existingChild, grandchild)
                        }
                    }

                    // 已存在虚拟，新的是真实：将真实的子节点合并到虚拟节点中
                    existingChild.isVirtual && !childNode.isVirtual -> {
                        for (grandchild in childNode.children) {
                            // 递归合并，避免重复
                            mergeIntoTree(existingChild, grandchild)
                        }
                    }

                    // 已存在真实，新的是虚拟：这是 a.b 和 a.b.c 的情况
                    // 将虚拟节点的子节点合并到真实节点
                    !existingChild.isVirtual && childNode.isVirtual -> {
                        for (grandchild in childNode.children) {
                            val existingGrandchild = existingChild.findChild(grandchild.name)
                            if (existingGrandchild != null) {
                                // 子节点也存在，继续递归合并
                                mergeIntoTree(existingGrandchild, grandchild)
                            } else {
                                // 子节点不存在，添加
                                existingChild.addChild(grandchild)
                            }
                        }
                    }

                    // 两个都是真实节点：合并文件，避免重复
                    else -> {
                        val existingNames = existingChild.children.map { it.name }.toSet()
                        for (grandchild in childNode.children) {
                            if (grandchild.name !in existingNames) {
                                existingChild.addChild(grandchild)
                            }
                        }
                    }
                }
            } else {
                // 不存在同名节点，直接添加
                parentNode.addChild(childNode)
            }
        }
    }

    /**
     * 刷新树模型
     */
    fun refresh() {
        val newRoot = buildRoot(project)
        setRoot(newRoot)
    }
}
