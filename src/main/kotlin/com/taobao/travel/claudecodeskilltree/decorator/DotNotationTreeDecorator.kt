package com.taobao.travel.claudecodeskilltree.decorator

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ProjectViewNodeDecorator
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.taobao.travel.claudecodeskilltree.config.DotNotationTreeState
import com.taobao.travel.claudecodeskilltree.parser.DotNotationParser
import com.taobao.travel.claudecodeskilltree.util.ColoredIcon
import javax.swing.Icon

/**
 * 点号树形结构装饰器
 * 将点号命名的文件夹以树形结构展示
 */
class DotNotationTreeDecorator : ProjectViewNodeDecorator {

    // 使用缓存避免频繁创建 Parser 和 Settings 对象
    private var cachedProject: Project? = null
    private var cachedSettings: DotNotationTreeState? = null
    private var cachedParser: DotNotationParser? = null

    // 蓝色文件夹图标（懒加载）
    private val blueFolderIcon: Icon by lazy { ColoredIcon.createBlueFolderIcon() }

    override fun decorate(node: ProjectViewNode<*>?, data: PresentationData?) {
        if (node == null || data == null) {
            return
        }

        val project = node.project ?: return

        // 检查插件是否启用（使用缓存）
        val settings = getSettings(project)
        if (!settings.enabled) {
            return
        }

        // 只处理目录节点
        if (node !is PsiDirectoryNode) {
            return
        }

        val virtualFile = node.virtualFile ?: return
        val parser = getParser(project)

        // 检查是否在目标目录内（使用缓存的 parser）
        if (!parser.isInTargetDirectory(virtualFile)) {
            return
        }

        val fileName = virtualFile.name

        // 如果启用了树形显示模式，且文件名包含分隔符，则修改显示名称
        if (settings.treeViewEnabled && parser.needsParsing(fileName)) {
            val parts = parser.parsePath(fileName)
            if (parts.size > 1) {
                // 简单实现：显示最后一部分
                // TODO: 需要根据实际的目录结构来计算应该显示哪一层
                data.presentableText = parts.last()
            }
        } else {
            // 未启用树形显示时，保持原样
            data.presentableText = fileName
        }

        // 为 skills 根目录设置蓝色图标
        if (isSkillsRootDirectory(virtualFile, project)) {
            data.setIcon(blueFolderIcon)
        }
    }

    /**
     * 判断是否为 skills 根目录（如 .claude/skills）
     */
    private fun isSkillsRootDirectory(file: VirtualFile, project: Project): Boolean {
        val settings = getSettings(project)
        val projectBasePath = project.basePath ?: return false

        return settings.targetDirectories.any { targetDir ->
            val targetFullPath = "$projectBasePath/$targetDir"
            file.path == targetFullPath
        }
    }

    /**
     * 获取缓存的 Settings
     */
    private fun getSettings(project: Project): DotNotationTreeState {
        if (cachedProject !== project || cachedSettings == null) {
            cachedProject = project
            cachedSettings = project.getService(DotNotationTreeState::class.java)
            // 清除 parser 缓存，因为 settings 可能已更改
            cachedParser = null
        }
        return cachedSettings!!
    }

    /**
     * 获取缓存的 Parser
     */
    private fun getParser(project: Project): DotNotationParser {
        if (cachedProject !== project || cachedParser == null) {
            cachedProject = project
            cachedParser = DotNotationParser(project)
        }
        return cachedParser!!
    }
}
