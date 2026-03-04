package com.taobao.travel.claudecodeskilltree.util

import com.intellij.openapi.vfs.VirtualFile
import com.taobao.travel.claudecodeskilltree.config.DotNotationTreeState
import com.taobao.travel.claudecodeskilltree.parser.DotNotationParser

/**
 * 文件工具类
 * 提供文件相关的辅助方法
 */
object FileUtils {

    /**
     * 检查文件是否为目录
     */
    fun isDirectory(file: VirtualFile?): Boolean {
        return file?.isDirectory == true
    }

    /**
     * 获取文件扩展名
     */
    fun getExtension(file: VirtualFile): String? {
        return file.extension
    }

    /**
     * 检查文件是否存在
     */
    fun exists(file: VirtualFile?): Boolean {
        return file?.isValid == true
    }

    /**
     * 获取文件大小（格式化）
     */
    fun getFormattedFileSize(file: VirtualFile): String {
        val size = file.length
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
            else -> "${size / (1024 * 1024 * 1024)} GB"
        }
    }

    /**
     * 获取相对路径
     */
    fun getRelativePath(basePath: String, fullPath: String): String {
        return if (fullPath.startsWith(basePath)) {
            fullPath.substring(basePath.length).trimStart('/')
        } else {
            fullPath
        }
    }

    /**
     * 检查文件名是否包含分隔符
     */
    fun hasSeparator(fileName: String, project: com.intellij.openapi.project.Project): Boolean {
        val settings = project.getService(DotNotationTreeState::class.java)
        return fileName.contains(settings.separator)
    }

    /**
     * 分割文件名
     */
    fun splitFileName(fileName: String, project: com.intellij.openapi.project.Project): List<String> {
        val settings = project.getService(DotNotationTreeState::class.java)
        return fileName.split(settings.separator)
    }

    /**
     * 构建树形路径
     */
    fun buildTreePath(parts: List<String>): String {
        return parts.joinToString(" / ")
    }

    /**
     * 检查是否为有效文件夹名
     */
    fun isValidFolderName(name: String): Boolean {
        // 检查是否包含非法字符
        val invalidChars = setOf('/', '\\', ':', '*', '?', '"', '<', '>', '|')
        return !name.any { it in invalidChars } && name.isNotBlank()
    }

    /**
     * 规范化路径
     */
    fun normalizePath(path: String): String {
        return path.replace("\\", "/")
    }

    /**
     * 获取文件的 MIME 类型
     */
    fun getMimeType(file: VirtualFile): String? {
        return file.fileType?.defaultExtension
    }

    /**
     * 检查是否为隐藏文件
     */
    fun isHiddenFile(file: VirtualFile): Boolean {
        return file.name.startsWith(".")
    }
}
