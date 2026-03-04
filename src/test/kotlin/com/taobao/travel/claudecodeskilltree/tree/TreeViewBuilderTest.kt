package com.taobao.travel.claudecodeskilltree.tree

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test
import org.junit.Assert.*

class TreeViewBuilderTest : BasePlatformTestCase() {

    private fun createMockFile(name: String): VirtualFile {
        return object : LightVirtualFile(name) {
            override fun isDirectory(): Boolean = true
        }
    }

    @Test
    fun testBuildTreeForSingleDotNotationFolder() {
        val files = listOf(createMockFile("a.b.c"))
        val builder = TreeViewBuilder()

        val roots = builder.buildVirtualTree(files, ".")

        assertEquals(1, roots.size)
        assertEquals("a", roots[0].name)
        assertTrue(roots[0].isVirtual)

        val b = roots[0].children[0]
        assertEquals("b", b.name)
        assertTrue(b.isVirtual)

        val c = b.children[0]
        assertEquals("c", c.name)
        assertTrue(c.isReal)
    }

    @Test
    fun testHandleOverlappingFolderNamesWithSmartGrouping() {
        val files = listOf(
            createMockFile("a.b"),
            createMockFile("a.b.c"),
            createMockFile("a.b.c.d")
        )
        val builder = TreeViewBuilder()

        val roots = builder.buildVirtualTree(files, ".")

        assertEquals(1, roots.size)
        val a = roots[0]
        assertEquals("a", a.name)

        val b = a.children[0]
        assertEquals("b", b.name)
        // b should be both virtual (has children) and real (represents a.b)
        assertTrue(b.isVirtual)  // Has children
        assertNotNull(b.realFile) // Represents the real file a.b
        assertEquals(1, b.children.size) // Should have c node

        val c = b.children[0]
        assertEquals("c", c.name)
        assertTrue(c.isVirtual)
        assertNotNull(c.realFile) // Represents the real file a.b.c
        assertEquals(1, c.children.size) // Should have d node

        val d = c.children[0]
        assertEquals("d", d.name)
        assertTrue(d.isReal)
        assertNotNull(d.realFile)
    }

    @Test
    fun testHandleNonDotNotationFoldersWithoutModification() {
        val files = listOf(
            createMockFile("normal-folder"),
            createMockFile("a.b.c")
        )
        val builder = TreeViewBuilder()

        val roots = builder.buildVirtualTree(files, ".")

        // Should have both roots
        assertEquals(2, roots.size)

        val normal = roots.find { it.name == "normal-folder" }
        assertTrue(normal?.isReal == true)

        val a = roots.find { it.name == "a" }
        assertTrue(a?.isVirtual == true)
    }

    @Test
    fun testHandleMultipleRootLevelFolders() {
        val files = listOf(
            createMockFile("a.b"),
            createMockFile("x.y.z")
        )
        val builder = TreeViewBuilder()

        val roots = builder.buildVirtualTree(files, ".")

        assertEquals(2, roots.size)
        assertEquals("a", roots[0].name)
        assertEquals("x", roots[1].name)
    }
}
