package com.zulfahmi.simukomperawat.ads

import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.File

class BannerLayoutPolicyTest {
    @Test
    fun bannerLayoutsUseContainersInsteadOfXmlAdViews() {
        val layoutDirectory = listOf(
            File("src/main/res/layout"),
            File("app/src/main/res/layout")
        ).first { it.exists() }

        val layoutFiles = layoutDirectory.listFiles { file ->
            file.extension == "xml"
        }.orEmpty()

        val filesWithXmlAdViews = layoutFiles
            .filter { it.readText().contains("com.google.android.gms.ads.AdView") }
            .map { it.name }

        assertFalse(filesWithXmlAdViews.toString(), filesWithXmlAdViews.isNotEmpty())
    }
}
