package com.zulfahmi.simukomperawat.model

data class LatihanPack(
    val number: Int,
    val firestoreId: String,
) {
    companion object {
        private const val SQLITE_PACK_COUNT = 5

        fun sqlite(number: Int): LatihanPack {
            require(number in 1..SQLITE_PACK_COUNT)
            return LatihanPack(number, "legacy_latihan_paket_$number")
        }

        fun remote(number: Int, firestoreId: String): LatihanPack {
            require(number > 0)
            require(firestoreId.isNotBlank())
            return LatihanPack(number, firestoreId)
        }

        fun merge(remotePacks: List<LatihanPack>): List<LatihanPack> {
            return (1..SQLITE_PACK_COUNT).map(::sqlite) + remotePacks
                .filter { it.number > SQLITE_PACK_COUNT }
                .distinctBy { it.number }
                .sortedBy { it.number }
        }
    }
}
