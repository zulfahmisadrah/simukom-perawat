package com.zulfahmi.simukomperawat.ads

class QuestionPackAccessPolicy {
    enum class StartAction {
        SHOW_REWARDED_AD,
        OPEN_QUESTION_PACK
    }

    fun getStartAction(hasLoadedRewardedAd: Boolean): StartAction {
        return if (hasLoadedRewardedAd) {
            StartAction.SHOW_REWARDED_AD
        } else {
            StartAction.OPEN_QUESTION_PACK
        }
    }

    fun getNormalStartAction(): StartAction {
        return StartAction.OPEN_QUESTION_PACK
    }

    fun requiresRewardedAdForPack(pack: Int): Boolean {
        return pack > 1
    }

    fun canOpenAfterRewardedAdClosed(rewardEarned: Boolean): Boolean {
        return rewardEarned
    }

    fun canOpenAfterRewardedAdShowFailed(): Boolean {
        return false
    }
}
