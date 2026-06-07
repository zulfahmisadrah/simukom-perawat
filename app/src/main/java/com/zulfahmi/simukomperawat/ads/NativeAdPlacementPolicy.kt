package com.zulfahmi.simukomperawat.ads

import com.zulfahmi.simukomperawat.model.NativeAdItem

class NativeAdPlacementPolicy {
    fun withNativeAdAfterIndex(
        items: List<Any>,
        afterIndex: Int,
        placement: NativeAdItem.Placement
    ): List<Any> {
        if (items.size <= afterIndex) return items

        val monetizedItems = ArrayList<Any>()
        items.forEachIndexed { index, item ->
            monetizedItems.add(item)
            if (index == afterIndex) {
                monetizedItems.add(NativeAdItem(placement))
            }
        }
        return monetizedItems
    }

    fun withNativeAdEveryInterval(
        items: List<Any>,
        interval: Int,
        placement: NativeAdItem.Placement
    ): List<Any> {
        if (interval <= 0) return items

        val monetizedItems = ArrayList<Any>()
        items.forEachIndexed { index, item ->
            monetizedItems.add(item)
            if ((index + 1) % interval == 0) {
                monetizedItems.add(NativeAdItem(placement))
            }
        }
        return monetizedItems
    }
}
