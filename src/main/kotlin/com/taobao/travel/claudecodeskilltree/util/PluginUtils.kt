package com.taobao.travel.claudecodeskilltree.util

import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.taobao.travel.claudecodeskilltree.config.DotNotationTreeState

/**
 * 插件工具类
 * 提供各种辅助方法
 */
object PluginUtils {

    /**
     * 检查插件是否启用
     */
    fun isPluginEnabled(project: Project): Boolean {
        val settings = project.getService(DotNotationTreeState::class.java)
        return settings.enabled
    }

    /**
     * 获取目标目录的虚拟文件
     */
    fun getTargetDirectoryFiles(project: Project): List<VirtualFile> {
        val settings = project.getService(DotNotationTreeState::class.java)
        val projectBasePath = project.basePath ?: return emptyList()

        val targetFiles = mutableListOf<VirtualFile>()

        for (targetDir in settings.targetDirectories) {
            val targetPath = "$projectBasePath/$targetDir"
            val targetDirFile = com.intellij.openapi.vfs.VfsUtil.findFileByIoFile(
                java.io.File(targetPath),
                true
            )

            if (targetDirFile != null && targetDirFile.isDirectory) {
                targetFiles.add(targetDirFile)
            }
        }

        return targetFiles
    }

    /**
     * 在项目视图中定位并选择文件
     */
    fun selectFileInProjectView(project: Project, file: VirtualFile) {
        val projectView = ProjectView.getInstance(project)
        projectView.select(file, file, true)
    }

    /**
     * 刷新项目视图
     * 强制重建树结构以应用 TreeStructureProvider 的更改
     */
    fun refreshProjectView(project: Project) {
        com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
            val projectView = ProjectView.getInstance(project)
            // 完全刷新项目视图，这会触发 TreeStructureProvider 重新计算
            projectView.refresh()

            // 同时刷新所有项目文件，确保视图同步
            com.intellij.openapi.vfs.VfsUtil.markDirtyAndRefresh(
                false, // async
                true,  // recursive
                true,  // reloadChildren
                project.baseDir
            )
        }
    }

    /**
     * 检查文件是否在目标目录内
     */
    fun isInTargetDirectory(project: Project, file: VirtualFile): Boolean {
        val settings = project.getService(DotNotationTreeState::class.java)
        val projectBasePath = project.basePath ?: return false

        val filePath = file.path

        return settings.targetDirectories.any { targetDir ->
            val targetFullPath = "$projectBasePath/$targetDir"
            // 文件是目标目录本身，或在目标目录下
            filePath == targetFullPath || filePath.startsWith("$targetFullPath/")
        }
    }

    /**
     * 显示信息消息
     */
    fun showInfoMessage(project: Project, message: String) {
        com.intellij.openapi.ui.Messages.showMessageDialog(
            project,
            message,
            "Claude Code Skill Tree View",
            com.intellij.openapi.ui.Messages.getInformationIcon()
        )
    }

    /**
     * 显示警告消息
     */
    fun showWarningMessage(project: Project, message: String) {
        com.intellij.openapi.ui.Messages.showMessageDialog(
            project,
            message,
            "Claude Code Skill Tree View",
            com.intellij.openapi.ui.Messages.getWarningIcon()
        )
    }

    /**
     * 显示错误消息
     */
    fun showErrorMessage(project: Project, message: String) {
        com.intellij.openapi.ui.Messages.showMessageDialog(
            project,
            message,
            "Claude Code Skill Tree View - Error",
            com.intellij.openapi.ui.Messages.getErrorIcon()
        )
    }

    /**
     * 显示确认对话框
     */
    fun showYesNoDialog(project: Project, message: String, title: String = "确认"): Boolean {
        val result = com.intellij.openapi.ui.Messages.showYesNoDialog(
            project,
            message,
            title,
            com.intellij.openapi.ui.Messages.getQuestionIcon()
        )
        return result == com.intellij.openapi.ui.Messages.YES
    }

    /**
     * 日志输出
     */
    fun logInfo(message: String) {
        com.intellij.openapi.diagnostic.Logger.getInstance(
            "ClaudeCodeSkillTreeView"
        ).info(message)
    }

    fun logWarning(message: String) {
        com.intellij.openapi.diagnostic.Logger.getInstance(
            "ClaudeCodeSkillTreeView"
        ).warn(message)
    }

    fun logError(message: String, throwable: Throwable? = null) {
        val logger = com.intellij.openapi.diagnostic.Logger.getInstance(
            "ClaudeCodeSkillTreeView"
        )
        if (throwable != null) {
            logger.error(message, throwable)
        } else {
            logger.error(message)
        }
    }

    /**
     * 获取插件版本
     */
    fun getPluginVersion(): String {
        return "1.0-SNAPSHOT"
    }

    /**
     * 获取插件名称
     */
    fun getPluginName(): String {
        return "Claude Code Skill Tree View"
    }
}
