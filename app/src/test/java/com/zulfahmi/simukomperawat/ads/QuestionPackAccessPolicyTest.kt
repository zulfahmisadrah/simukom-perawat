package com.zulfahmi.simukomperawat.ads

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import com.zulfahmi.simukomperawat.model.LatihanPack
import com.zulfahmi.simukomperawat.model.PackAccessType

class QuestionPackAccessPolicyTest {
    private val policy = QuestionPackAccessPolicy()

    @Test
    fun startActionShowsRewardedAdWhenAdIsReady() {
        assertEquals(
            QuestionPackAccessPolicy.StartAction.SHOW_REWARDED_AD,
            policy.getStartAction(hasLoadedRewardedAd = true)
        )
    }

    @Test
    fun startActionOpensQuestionPackWhenAdIsUnavailable() {
        assertEquals(
            QuestionPackAccessPolicy.StartAction.OPEN_QUESTION_PACK,
            policy.getStartAction(hasLoadedRewardedAd = false)
        )
    }

    @Test
    fun normalStartAlwaysOpensQuestionPack() {
        assertEquals(
            QuestionPackAccessPolicy.StartAction.OPEN_QUESTION_PACK,
            policy.getNormalStartAction()
        )
    }

    @Test
    fun questionPackOpensOnlyAfterRewardIsEarned() {
        assertTrue(policy.canOpenAfterRewardedAdClosed(rewardEarned = true))
        assertFalse(policy.canOpenAfterRewardedAdClosed(rewardEarned = false))
    }

    @Test
    fun questionPackDoesNotOpenWhenRewardedAdFailsToShow() {
        assertFalse(policy.canOpenAfterRewardedAdShowFailed())
    }

    @Test
    fun rewardedAdIsRequiredOnlyForPacksAfterFirstPack() {
        assertFalse(policy.requiresRewardedAdForPack(1))
        assertTrue(policy.requiresRewardedAdForPack(2))
        assertTrue(policy.requiresRewardedAdForPack(5))
    }

    @Test
    fun remoteFreePackOpensWithoutAnAd() {
        assertEquals(
            PackOpenAction.OPEN,
            policy.actionFor(remotePack("free", PackAccessType.FREE)),
        )
    }

    @Test
    fun remoteAdPackRequiresAnAd() {
        assertEquals(
            PackOpenAction.SHOW_REWARDED_AD,
            policy.actionFor(remotePack("ad", PackAccessType.REWARDED_AD)),
        )
    }

    @Test
    fun remotePremiumPackShowsAvailabilityMessage() {
        assertEquals(
            PackOpenAction.SHOW_PREMIUM_MESSAGE,
            policy.actionFor(remotePack("premium", PackAccessType.PREMIUM)),
        )
    }

    private fun remotePack(
        firestoreId: String,
        accessType: PackAccessType,
    ): LatihanPack = LatihanPack.remote(
        roomPack = 10_000,
        firestoreId = firestoreId,
        title = "Paket",
        categoryId = "jiwa",
        categoryName = "Keperawatan Jiwa",
        displayNumber = 1,
        accessType = accessType,
    )
}
