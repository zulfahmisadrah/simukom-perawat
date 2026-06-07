package com.zulfahmi.simukomperawat.ads

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

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
}
