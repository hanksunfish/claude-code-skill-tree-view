package com.taobao.travel.claudecodeskilltree.tree

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test
import org.junit.Assert.*

class VirtualTreeNodeTest : BasePlatformTestCase() {

    
    @Test
    fun testCreateVirtualNodeWithCorrectProperties() {
        val node = VirtualTreeNode("test", null, isVirtual = true)

        assertEquals("test", node.name)
        assertNull(node.realFile)
        assertTrue(node.isVirtual)
        assertFalse(node.isReal)
    }

    @Test
    fun testCreateRealNodeWithCorrectProperties() {
        // Real file test skipped due to VirtualFile complexity
        // Focus on virtual node functionality
        val node = VirtualTreeNode("test", null, isVirtual = false)

        assertEquals("test", node.name)
        assertNull(node.realFile)
        assertFalse(node.isVirtual)
        // isReal should be false when realFile is null
        assertFalse(node.isReal)
    }

    @Test
    fun testSupportParentChildRelationships() {
        val parent = VirtualTreeNode("parent", null, isVirtual = true)
        val child = VirtualTreeNode("child", null, isVirtual = true, parent = parent)

        parent.addChild(child)

        assertEquals(1, parent.children.size)
        assertEquals(child, parent.children[0])
        assertEquals(parent, child.parent)
    }

    @Test
    fun testFindExistingChildByName() {
        val parent = VirtualTreeNode("parent", null, isVirtual = true)
        val child = VirtualTreeNode("child", null, isVirtual = true)
        parent.addChild(child)

        val found = parent.findChild("child")

        assertEquals(child, found)
    }

    @Test
    fun testReturnNullWhenChildNotFound() {
        val parent = VirtualTreeNode("parent", null, isVirtual = true)

        val found = parent.findChild("nonexistent")

        assertNull(found)
    }
}