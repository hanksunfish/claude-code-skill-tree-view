package com.taobao.travel.claudecodeskilltree.parser

import com.taobao.travel.claudecodeskilltree.config.DotNotationTreeState
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileFilter

/**
 * 虚拟树节点
 * 表示解析后的树形结构中的一个节点
 */
data class VirtualTreeNode(
    val name: String,
    var originalFile: VirtualFile?,
    val parent: VirtualTreeNode? = null,
    var children: MutableList<VirtualTreeNode> = mutableListOf()
) {
    val fullPath: String
        get() {
            val path = mutableListOf<String>()
            var current: VirtualTreeNode? = this
            while (current != null) {
                path.add(0, current.name)
                current = current.parent
            }
            return path.joinToString("/")
        }

    val isVirtual: Boolean
        get() = originalFile == null

    fun addChild(child: VirtualTreeNode) {
        children.add(child)
    }

    fun findChild(name: String): VirtualTreeNode? {
        return children.find { it.name == name }
    }
}

/**
 * 点号解析器
 * 负责将点号命名的文件夹解析为树形结构
 */
class DotNotationParser(private val project: Project) {

    private val settings: DotNotationTreeState
        get() = project.getService(DotNotationTreeState::class.java)

    /**
     * 检查给定的虚拟文件是否在目标目录内
     */
    fun isInTargetDirectory(file: VirtualFile): Boolean {
        if (!settings.enabled) {
            return false
        }

        val projectBasePath = project.basePath ?: return false

        // 获取文件的父目录路径
        val parentPath = file.parent?.path ?: return false

        return settings.targetDirectories.any { targetDir ->
            // 构建完整的目标目录路径
            val targetFullPath = "$projectBasePath/$targetDir"
            // 只检查文件是否直接在目标目录下（父目录完全匹配）
            parentPath == targetFullPath
        }
    }

    /**
     * 检查给定的文件名是否包含分隔符（需要解析）
     */
    fun needsParsing(fileName: String): Boolean {
        if (!settings.enabled) {
            return false
        }
        return fileName.contains(settings.separator)
    }

    /**
     * 解析文件夹名称，创建虚拟树节点
     * @param fileName 文件夹名称，如 "superpowers.test-driven-development"
     * @param originalFile 原始虚拟文件
     * @return 虚拟树的根节点
     */
    fun parse(fileName: String, originalFile: VirtualFile): VirtualTreeNode {
        val parts = fileName.split(settings.separator)
        if (parts.size == 1) {
            // 没有分隔符，直接返回原始文件节点
            return VirtualTreeNode(fileName, originalFile)
        }

        // 创建虚拟树结构
        var root = VirtualTreeNode(parts[0], null)
        var current = root

        for (i in 1 until parts.size) {
            val child = VirtualTreeNode(parts[i], null, parent = current)
            current.addChild(child)
            current = child
        }

        // 最后一个节点的原始文件设置为实际文件
        current.originalFile = originalFile

        return root
    }

    /**
     * 解析文件夹名称，返回路径部分列表
     * @param fileName 文件夹名称，如 "superpowers.test-driven-development"
     * @return 路径部分列表，如 ["superpowers", "test-driven-development"]
     */
    fun parsePath(fileName: String): List<String> {
        if (!needsParsing(fileName)) {
            return listOf(fileName)
        }
        return fileName.split(settings.separator)
    }

    /**
     * 获取虚拟节点的显示名称
     * @param fileName 原始文件名
     * @param depth 节点深度（0 为根）
     * @return 该深度层的显示名称
     */
    fun getDisplayName(fileName: String, depth: Int): String? {
        val parts = parsePath(fileName)
        return if (depth < parts.size) parts[depth] else null
    }

    /**
     * 计算文件名的层级深度
     * @param fileName 文件夹名称
     * @return 层级数（至少为 1）
     */
    fun getDepth(fileName: String): Int {
        return parsePath(fileName).size
    }

    /**
     * 构建完整的虚拟树
     * @param files 文件列表
     * @return 树的根节点列表
     */
    fun buildVirtualTree(files: List<VirtualFile>): List<VirtualTreeNode> {
        val roots = mutableListOf<VirtualTreeNode>()

        for (file in files) {
            if (!file.isDirectory || !isInTargetDirectory(file)) {
                continue
            }

            val fileName = file.name
            if (!needsParsing(fileName)) {
                // 不需要解析，直接添加为根节点
                roots.add(VirtualTreeNode(fileName, file))
                continue
            }

            // 解析并添加到树中
            val parsed = parse(fileName, file)
            mergeIntoTree(roots, parsed)
        }

        return roots
    }

    /**
     * 将解析的节点合并到现有树中
     */
    private fun mergeIntoTree(roots: MutableList<VirtualTreeNode>, node: VirtualTreeNode) {
        // 查找是否已存在相同名称的根节点
        val existingRoot = roots.find { it.name == node.name }
        if (existingRoot != null) {
            // 合并子节点
            mergeChildren(existingRoot, node)
        } else {
            // 添加新的根节点
            roots.add(node)
        }
    }

    /**
     * 递归合并子节点
     */
    private fun mergeChildren(target: VirtualTreeNode, source: VirtualTreeNode) {
        for (sourceChild in source.children) {
            val targetChild = target.findChild(sourceChild.name)
            if (targetChild != null) {
                // 递归合并
                mergeChildren(targetChild, sourceChild)
            } else {
                // 添加新子节点
                target.addChild(sourceChild)
            }
        }
    }

    /**
     * 查找匹配某个虚拟路径的所有文件
     * @param virtualPath 虚拟路径，如 "superpowers/test-driven-development"
     * @param files 要搜索的文件列表
     * @return 匹配的文件列表
     */
    fun findFilesByVirtualPath(virtualPath: String, files: List<VirtualFile>): List<VirtualFile> {
        val pathParts = virtualPath.split("/").dropLast(1)
        val lastName = virtualPath.split("/").lastOrNull() ?: return emptyList()

        return files.filter { file ->
            if (!isInTargetDirectory(file)) {
                return@filter false
            }

            val fileName = file.name
            if (!needsParsing(fileName)) {
                return@filter fileName == lastName
            }

            val parts = parsePath(fileName)
            parts.size == pathParts.size + 1 && parts.dropLast(1) == pathParts && parts.last() == lastName
        }
    }

    /**
     * 将虚拟路径转换为原始文件名
     * @param virtualPath 虚拟路径，如 "superpowers/test-driven-development"
     * @return 原始文件名，如 "superpowers.test-driven-development"
     */
    fun virtualPathToFileName(virtualPath: String): String {
        return virtualPath.replace("/", settings.separator)
    }

    /**
     * 将原始文件名转换为虚拟路径
     * @param fileName 原始文件名，如 "superpowers.test-driven-development"
     * @return 虚拟路径，如 "superpowers/test-driven-development"
     */
    fun fileNameToVirtualPath(fileName: String): String {
        return fileName.replace(settings.separator, "/")
    }
}
