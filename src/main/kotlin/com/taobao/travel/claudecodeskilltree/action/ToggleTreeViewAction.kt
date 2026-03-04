package com.taobao.travel.claudecodeskilltree.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.taobao.travel.claudecodeskilltree.config.DotNotationTreeState
import com.taobao.travel.claudecodeskilltree.util.PluginUtils

/**
 * 切换树形视图显示模式
 * 右键菜单中的切换选项
 */
class ToggleTreeViewAction : AnAction() {

    override fun update(e: AnActionEvent) {
        val project = e.project
        if (project == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        val settings = project.getService(DotNotationTreeState::class.java)

        // 只在目标目录下显示此菜单项
        val virtualFile = e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.VIRTUAL_FILE)
        val isInTargetDir = virtualFile != null && PluginUtils.isInTargetDirectory(project, virtualFile)

        e.presentation.isEnabledAndVisible = isInTargetDir && settings.enabled

        // 根据当前状态更新菜单文本
        e.presentation.text = if (settings.treeViewEnabled) {
            "恢复扁平显示"
        } else {
            "启用树形显示"
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val settings = project.getService(DotNotationTreeState::class.java)

        // 切换树形视图状态
        settings.treeViewEnabled = !settings.treeViewEnabled

        // 刷新项目视图
        PluginUtils.refreshProjectView(project)

        // 显示提示消息
        val message = if (settings.treeViewEnabled) {
            "已启用树形显示模式"
        } else {
            "已恢复扁平显示模式"
        }
        PluginUtils.logInfo(message)
    }
}
