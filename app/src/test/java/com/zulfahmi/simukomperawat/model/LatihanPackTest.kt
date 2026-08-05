package com.zulfahmi.simukomperawat.model

import org.junit.Assert.assertEquals
import org.junit.Test

class LatihanPackTest {
    @Test
    fun keepsSqlitePackagesAndAppendsNewFirestorePackage() {
        val merged = LatihanPack.merge(listOf(LatihanPack.remote(6, "remote-6")))

        assertEquals(listOf(1, 2, 3, 4, 5, 6), merged.map { it.number })
        assertEquals("remote-6", merged.last().firestoreId)
    }

    @Test
    fun ignoresRemoteDuplicateOfSqlitePackage() {
        val merged = LatihanPack.merge(listOf(LatihanPack.remote(3, "remote-3")))

        assertEquals(5, merged.size)
        assertEquals("legacy_latihan_paket_3", merged[2].firestoreId)
    }
}
