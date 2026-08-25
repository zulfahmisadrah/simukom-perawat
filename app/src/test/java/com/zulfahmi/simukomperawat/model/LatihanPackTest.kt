package com.zulfahmi.simukomperawat.model

import org.junit.Assert.assertEquals
import org.junit.Test
import com.zulfahmi.simukomperawat.repository.RemotePackKeyAllocator

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

    @Test
    fun allocatesDifferentRoomKeysForDifferentFirestoreIds() {
        val allocator = RemotePackKeyAllocator(firstKey = 10_000)

        assertEquals(10_000, allocator.allocate("jiwa-1"))
        assertEquals(10_001, allocator.allocate("anak-1"))
        assertEquals(10_000, allocator.allocate("jiwa-1"))
    }

    @Test
    fun remotePackCardShowsOnlyFirestorePackNumber() {
        val pack = LatihanPack.remote(
            roomPack = 10_000,
            firestoreId = "jiwa-7",
            title = "Jiwa Lanjutan",
            categoryId = "jiwa",
            categoryName = "Keperawatan Jiwa",
            displayNumber = 7,
            accessType = PackAccessType.PREMIUM,
        )

        assertEquals("7", pack.cardNumber)
    }
}
