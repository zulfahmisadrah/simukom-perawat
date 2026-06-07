package com.zulfahmi.simukomperawat.model

data class NativeAdItem(
    val placement: Placement
) {
    enum class Placement {
        TIPS_FEED,
        FORUM_FEED
    }
}
