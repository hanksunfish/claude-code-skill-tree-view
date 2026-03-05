package com.taobao.travel.claudecodeskilltree.config

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * 插件配置状态类
 * 用于持久化存储插件的配置信息
 */
@State(
    name = "DotNotationTreeSettings",
    storages = [Storage("DotNotationTreeSettings.xml")]
)
@Service(Service.Level.PROJECT)
class DotNotationTreeState : PersistentStateComponent<DotNotationTreeState> {

    /**
     * 目标目录列表
     * 这些目录下的点号命名文件夹将被转换为树形结构
     * 示例：[".claude/skills", ".claude/project/skills"]
     */
    var targetDirectories: ArrayList<String> = arrayListOf(".claude/skills")

    /**
     * 分隔符，默认为点号
     * 可以配置为其他字符，如 "/" 或 "//"
     */
    var separator: String = "."

    /**
     * 是否启用插件
     */
    var enabled: Boolean = true

    /**
     * 是否显示虚拟节点图标
     */
    var showVirtualNodeIcons: Boolean = true

    /**
     * 图标文件列表（用逗号分隔）
     * 这些文件应位于 src/main/resources/icons/ 目录
     */
    var iconFiles: String = "virtual-node-icon-0.png,virtual-node-icon-1.png,virtual-node-icon-2.png"

    /**
     * 是否在项目视图中启用树形显示
     * true: 在 Project 视图中将点号命名文件夹显示为树形结构
     * false: 保持扁平显示（默认）
     */
    var treeViewEnabled: Boolean = false

    override fun getState(): DotNotationTreeState = this

    override fun loadState(state: DotNotationTreeState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(): DotNotationTreeState {
            return com.intellij.openapi.project.ProjectManager.getInstance().openProjects
                .firstNotNullOfOrNull { project ->
                    project.getService(DotNotationTreeState::class.java)
                } ?: throw IllegalStateException("No project found")
        }
    }
}
