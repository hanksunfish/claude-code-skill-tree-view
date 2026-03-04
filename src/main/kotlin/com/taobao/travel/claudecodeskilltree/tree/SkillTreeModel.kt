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
            val parser = DotNotationParser(project)

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
                    val targetNode = buildTargetDirectoryTree(parser, targetDirFile, targetDir)
                    root.addChild(targetNode)
                }
            }

            // 如果没有找到任何目标目录
            if (root.children.isEmpty()) {
                val emptyNode = SkillTreeNode("未找到目标目录", null)
                root.addChild(emptyNode)
            }

            return root
        }

        /**
         * 构建目标目录的树
         */
        private fun buildTargetDirectoryTree(
            parser: DotNotationParser,
            targetDirFile: VirtualFile,
            targetDirName: String
        ): SkillTreeNode {
            val rootNode = SkillTreeNode(targetDirName, targetDirFile)

            // 获取所有子文件夹
            val children = targetDirFile.children
            if (children != null) {
                // 按文件名排序
                val sortedChildren = children.sortedBy { it.name }

                for (child in sortedChildren) {
                    if (child.isDirectory) {
                        // 构建子节点树
                        val childTree = buildDirectoryTree(parser, child)
                        mergeIntoTree(rootNode, childTree)
                    }
                }
            }

            return rootNode
        }

        /**
         * 构建单个目录的树（处理点号命名）
         */
        private fun buildDirectoryTree(
            parser: DotNotationParser,
            directory: VirtualFile
        ): SkillTreeNode {
            val fileName = directory.name

            if (!parser.needsParsing(fileName)) {
                // 不需要解析，直接返回节点
                return SkillTreeNode(fileName, directory)
            }

            // 解析点号命名
            val parts = parser.parsePath(fileName)
            var currentNode = SkillTreeNode(parts[0], null, isVirtual = true)

            for (i in 1 until parts.size) {
                val childNode = if (i == parts.size - 1) {
                    // 最后一个部分对应实际文件
                    SkillTreeNode(parts[i], directory, isVirtual = false)
                } else {
                    // 中间部分是虚拟节点
                    SkillTreeNode(parts[i], null, isVirtual = true)
                }
                currentNode.addChild(childNode)
                currentNode = childNode
            }

            // 返回根节点
            val rootNode = SkillTreeNode(parts[0], null, isVirtual = true)
            var currentParent = rootNode
            for (i in 1 until parts.size) {
                val childNode = if (i == parts.size - 1) {
                    SkillTreeNode(parts[i], directory, isVirtual = false)
                } else {
                    SkillTreeNode(parts[i], null, isVirtual = true)
                }
                currentParent.addChild(childNode)
                currentParent = childNode
            }

            return rootNode
        }

        /**
         * 将子树合并到主树中
         */
        private fun mergeIntoTree(parentNode: SkillTreeNode, childNode: SkillTreeNode) {
            // 查找是否已存在同名子节点
            val existingChild = parentNode.findChild(childNode.name)

            if (existingChild != null) {
                // 如果存在，合并子节点的子节点
                for (grandchild in childNode.children) {
                    mergeIntoTree(existingChild, grandchild)
                }
            } else {
                // 如果不存在，直接添加
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
