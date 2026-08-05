package com.zulfahmi.simukomperawat.model

data class ExperimentalPack(
    val id: String,
    val title: String,
    val packNumber: Long,
) {
    val roomPackNumber: Int
        get() {
            require(packNumber in 1..Int.MAX_VALUE.toLong())
            return packNumber.toInt()
        }
}
