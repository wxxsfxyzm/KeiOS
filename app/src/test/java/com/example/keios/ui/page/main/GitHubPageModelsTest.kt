package com.example.keios.ui.page.main

import org.junit.Test
import kotlin.test.assertEquals

class GitHubPageModelsTest {
    @Test
    fun `format release value keeps tag when release name is placeholder`() {
        assertEquals("Release · 0.9.100", formatReleaseValue("Release", "0.9.100"))
        assertEquals("随风而行 · v0.5.1", formatReleaseValue("随风而行", "v0.5.1"))
    }

    @Test
    fun `format release value avoids duplicate tag when name and tag match`() {
        assertEquals("v0.0.8", formatReleaseValue("v0.0.8", "v0.0.8"))
        assertEquals("3.8.0", formatReleaseValue("3.8.0", "3.8.0"))
    }
}
