package com.taobao.travel.claudecodeskilltree.parser

import com.taobao.travel.claudecodeskilltree.config.DotNotationTreeState
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test
import org.junit.Assert.*

/**
 * 点号解析器测试
 */
class DotNotationParserTest : BasePlatformTestCase() {

    @Test
    fun testParsePathWithDotSeparator() {
        val settings = project.getService(DotNotationTreeState::class.java)
        settings.separator = "."

        val parser = DotNotationParser(project)

        val fileName = "superpowers.test-driven-development"
        val parts = parser.parsePath(fileName)

        // test-driven-development 使用连字符，不会被点号分割
        assertEquals(2, parts.size)
        assertEquals("superpowers", parts[0])
        assertEquals("test-driven-development", parts[1])
    }

    @Test
    fun testParsePathWithoutSeparator() {
        val settings = project.getService(DotNotationTreeState::class.java)
        settings.separator = "."

        val parser = DotNotationParser(project)

        val fileName = "normal-folder"
        val parts = parser.parsePath(fileName)

        assertEquals(1, parts.size)
        assertEquals("normal-folder", parts[0])
    }

    @Test
    fun testNeedsParsing() {
        val settings = project.getService(DotNotationTreeState::class.java)
        settings.separator = "."

        val parser = DotNotationParser(project)

        assertTrue(parser.needsParsing("category.subcategory.item"))
        assertFalse(parser.needsParsing("normal-folder"))
        assertFalse(parser.needsParsing(""))
    }

    @Test
    fun testGetDepth() {
        val settings = project.getService(DotNotationTreeState::class.java)
        settings.separator = "."

        val parser = DotNotationParser(project)

        assertEquals(3, parser.getDepth("a.b.c"))
        assertEquals(1, parser.getDepth("single"))
        assertEquals(2, parser.getDepth("parent.child"))
        assertEquals(2, parser.getDepth("superpowers.test-driven-development"))
    }

    @Test
    fun testVirtualPathConversion() {
        val settings = project.getService(DotNotationTreeState::class.java)
        settings.separator = "."

        val parser = DotNotationParser(project)

        val fileName = "web-design.accessibility"
        val virtualPath = parser.fileNameToVirtualPath(fileName)

        assertEquals("web-design/accessibility", virtualPath)
    }

    @Test
    fun testCustomSeparator() {
        val settings = project.getService(DotNotationTreeState::class.java)
        settings.separator = "/"

        val parser = DotNotationParser(project)

        val fileName = "web-design/accessibility"
        val parts = parser.parsePath(fileName)

        assertEquals(2, parts.size)
        assertEquals("web-design", parts[0])
        assertEquals("accessibility", parts[1])
    }
}
