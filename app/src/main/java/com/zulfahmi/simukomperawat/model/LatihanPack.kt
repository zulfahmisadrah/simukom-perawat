package com.zulfahmi.simukomperawat.model

enum class PackAccessType {
    FREE,
    REWARDED_AD,
    PREMIUM;

    companion object {
        fun fromWireValue(value: String?): PackAccessType = when (value) {
            "free" -> FREE
            "premium" -> PREMIUM
            else -> REWARDED_AD
        }
    }
}

data class LatihanCategory(
    val id: String,
    val name: String,
    val packs: List<LatihanPack>,
)

data class LatihanPack(
    val roomPack: Int,
    val firestoreId: String?,
    val title: String,
    val categoryId: String,
    val categoryName: String,
    val displayNumber: Int,
    val accessType: PackAccessType,
    val isRemote: Boolean,
) {
    val number: Int
        get() = displayNumber

    val cardNumber: String
        get() = displayNumber.toString()

    val adPromptLabel: String
        get() = "paket $displayNumber"

    companion object {
        private const val SQLITE_PACK_COUNT = 5
        const val BUNDLED_CATEGORY_ID = "bundled"
        const val BUNDLED_CATEGORY_NAME = "Paket Bawaan"

        fun sqlite(number: Int): LatihanPack {
            require(number in 1..SQLITE_PACK_COUNT)
            return LatihanPack(
                roomPack = number,
                firestoreId = "legacy_latihan_paket_$number",
                title = "Paket $number",
                categoryId = BUNDLED_CATEGORY_ID,
                categoryName = BUNDLED_CATEGORY_NAME,
                displayNumber = number,
                accessType = PackAccessType.REWARDED_AD,
                isRemote = false,
            )
        }

        fun remote(
            roomPack: Int,
            firestoreId: String,
            title: String,
            categoryId: String,
            categoryName: String,
            displayNumber: Int,
            accessType: PackAccessType,
        ): LatihanPack {
            require(roomPack > 0)
            require(firestoreId.isNotBlank())
            return LatihanPack(
                roomPack = roomPack,
                firestoreId = firestoreId,
                title = title,
                categoryId = categoryId,
                categoryName = categoryName,
                displayNumber = displayNumber,
                accessType = accessType,
                isRemote = true,
            )
        }

        fun remote(number: Int, firestoreId: String): LatihanPack = remote(
            roomPack = number,
            firestoreId = firestoreId,
            title = "Paket $number",
            categoryId = "uncategorized",
            categoryName = "Kategori Lainnya",
            displayNumber = number,
            accessType = PackAccessType.REWARDED_AD,
        )

        fun bundled(): List<LatihanPack> = (1..SQLITE_PACK_COUNT).map(::sqlite)

        fun groupByCategory(packs: List<LatihanPack>): List<LatihanCategory> {
            return packs.groupBy { it.categoryId }
                .map { (id, values) ->
                    LatihanCategory(
                        id = id,
                        name = values.first().categoryName,
                        packs = values.sortedBy { it.title },
                    )
                }
                .sortedWith(
                    compareBy<LatihanCategory> { it.id != BUNDLED_CATEGORY_ID }
                        .thenBy { it.name },
                )
        }

        fun merge(remotePacks: List<LatihanPack>): List<LatihanPack> {
            return bundled() + remotePacks
                .filter { it.displayNumber > SQLITE_PACK_COUNT }
                .distinctBy { it.displayNumber }
                .sortedBy { it.displayNumber }
        }
    }
}
