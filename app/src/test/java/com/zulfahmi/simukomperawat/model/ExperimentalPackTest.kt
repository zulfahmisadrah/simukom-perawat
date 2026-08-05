package com.zulfahmi.simukomperawat.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ExperimentalPackTest {
    @Test
    fun keepsEachFirestorePackInItsOwnRoomCacheSlot() {
        val pack = ExperimentalPack(id = "firestore-pack", title = "Paket 7", packNumber = 7)

        assertEquals(7, pack.roomPackNumber)
    }
}
