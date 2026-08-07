package com.zulfahmi.simukomperawat.model

import org.junit.Assert.assertEquals
import org.junit.Test

class LatihanPackTest {
    @Test
    fun groupsPacksByCategoryWithoutCollapsingDuplicateNumbers() {
        val groups = LatihanPack.groupByCategory(
            listOf(
                LatihanPack.remote(
                    roomPack = 10_000,
                    firestoreId = "jiwa-1",
                    title = "Paket Jiwa Dasar",
                    categoryId = "jiwa",
                    categoryName = "Keperawatan Jiwa",
                    displayNumber = 1,
                    accessType = PackAccessType.FREE,
                ),
                LatihanPack.remote(
                    roomPack = 10_001,
                    firestoreId = "anak-1",
                    title = "Paket Anak Dasar",
                    categoryId = "anak",
                    categoryName = "Keperawatan Anak",
                    displayNumber = 1,
                    accessType = PackAccessType.REWARDED_AD,
                ),
            ),
        )

        assertEquals(listOf("Keperawatan Anak", "Keperawatan Jiwa"), groups.map { it.name })
        assertEquals(listOf(10_001), groups[0].packs.map { it.roomPack })
        assertEquals(listOf(10_000), groups[1].packs.map { it.roomPack })
    }
}
