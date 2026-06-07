package com.zulfahmi.simukomperawat.ads

import android.app.Activity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.StringRes
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

object AdMobManager {
    fun loadAdaptiveBanner(activity: Activity, container: ViewGroup, @StringRes adUnitIdRes: Int) {
        val adView = createAdView(activity, container)
        adView.adUnitId = activity.getString(adUnitIdRes)
        adView.setAdSize(getAdaptiveBannerSize(activity, container))
        adView.loadAd(AdRequest.Builder().build())
    }

    fun loadMediumRectangle(activity: Activity, container: ViewGroup, @StringRes adUnitIdRes: Int) {
        val adView = createAdView(activity, container)
        adView.adUnitId = activity.getString(adUnitIdRes)
        adView.setAdSize(AdSize.MEDIUM_RECTANGLE)
        adView.loadAd(AdRequest.Builder().build())
    }

    private fun createAdView(activity: Activity, container: ViewGroup): AdView {
        container.removeAllViews()
        val adView = AdView(activity)
        container.addView(
            adView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )
        return adView
    }

    private fun getAdaptiveBannerSize(activity: Activity, container: ViewGroup): AdSize {
        val displayMetrics = activity.resources.displayMetrics
        val adWidthPixels = if (container.width > 0) container.width.toFloat() else displayMetrics.widthPixels.toFloat()
        val adWidth = (adWidthPixels / displayMetrics.density).toInt()

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }
}
