package com.zulfahmi.simukomperawat.ads

import android.app.Activity
import androidx.annotation.StringRes
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

object AdMobManager {
    fun loadAdaptiveBanner(activity: Activity, adView: AdView, @StringRes adUnitIdRes: Int) {
        adView.adUnitId = activity.getString(adUnitIdRes)
        adView.setAdSize(getAdaptiveBannerSize(activity, adView))
        adView.loadAd(AdRequest.Builder().build())
    }

    fun loadMediumRectangle(activity: Activity, adView: AdView, @StringRes adUnitIdRes: Int) {
        adView.adUnitId = activity.getString(adUnitIdRes)
        adView.setAdSize(AdSize.MEDIUM_RECTANGLE)
        adView.loadAd(AdRequest.Builder().build())
    }

    private fun getAdaptiveBannerSize(activity: Activity, adView: AdView): AdSize {
        val displayMetrics = activity.resources.displayMetrics
        val adWidthPixels = if (adView.width > 0) adView.width.toFloat() else displayMetrics.widthPixels.toFloat()
        val adWidth = (adWidthPixels / displayMetrics.density).toInt()

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }
}
