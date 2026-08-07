package com.zulfahmi.simukomperawat.ads

import com.zulfahmi.simukomperawat.model.LatihanPack
import com.zulfahmi.simukomperawat.model.PackAccessType

enum class PackOpenAction {
    OPEN,
    SHOW_REWARDED_AD,
    SHOW_PREMIUM_MESSAGE,
}

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

    fun actionFor(pack: LatihanPack): PackOpenAction = when {
        !pack.isRemote -> {
            if (requiresRewardedAdForPack(pack.roomPack)) PackOpenAction.SHOW_REWARDED_AD else PackOpenAction.OPEN
        }
        pack.accessType == PackAccessType.FREE -> PackOpenAction.OPEN
        pack.accessType == PackAccessType.PREMIUM -> PackOpenAction.SHOW_PREMIUM_MESSAGE
        else -> PackOpenAction.SHOW_REWARDED_AD
    }

    fun canOpenAfterRewardedAdClosed(rewardEarned: Boolean): Boolean {
        return rewardEarned
    }

    fun canOpenAfterRewardedAdShowFailed(): Boolean {
        return false
    }
}
