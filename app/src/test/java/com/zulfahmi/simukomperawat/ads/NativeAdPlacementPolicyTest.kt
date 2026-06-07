package com.zulfahmi.simukomperawat.ads

import com.zulfahmi.simukomperawat.model.NativeAdItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NativeAdPlacementPolicyTest {
    private val policy = NativeAdPlacementPolicy()

    @Test
    fun insertsNativeAdAfterSecondArticleWhenFeedHasEnoughItems() {
        val items = policy.withNativeAdAfterIndex(
            listOf("article 1", "article 2", "article 3"),
            afterIndex = 1,
            placement = NativeAdItem.Placement.TIPS_FEED
        )

        assertEquals(4, items.size)
        assertEquals("article 1", items[0])
        assertEquals("article 2", items[1])
        assertTrue(items[2] is NativeAdItem)
        assertEquals("article 3", items[3])
    }

    @Test
    fun doesNotInsertNativeAdWhenFeedIsTooShort() {
        val items = policy.withNativeAdAfterIndex(
            listOf("article 1"),
            afterIndex = 1,
            placement = NativeAdItem.Placement.TIPS_FEED
        )

        assertEquals(listOf("article 1"), items)
    }

    @Test
    fun insertsNativeAdEveryTenChatItems() {
        val chatItems = (1..21).map { "chat $it" }
        val items = policy.withNativeAdEveryInterval(
            chatItems,
            interval = 10,
            placement = NativeAdItem.Placement.FORUM_FEED
        )

        assertEquals(23, items.size)
        assertTrue(items[10] is NativeAdItem)
        assertTrue(items[21] is NativeAdItem)
    }
}
