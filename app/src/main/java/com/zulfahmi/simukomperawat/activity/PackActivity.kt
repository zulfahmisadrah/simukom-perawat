package com.zulfahmi.simukomperawat.activity

import android.content.Intent
import android.os.Bundle
import android.transition.Explode
import android.transition.Slide
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.zulfahmi.simukomperawat.R
import com.zulfahmi.simukomperawat.adapter.RvAdapter
import com.zulfahmi.simukomperawat.ads.QuestionPackAccessPolicy
import com.zulfahmi.simukomperawat.databinding.ActivityPackBinding
import com.zulfahmi.simukomperawat.model.ExperimentalPack
import com.zulfahmi.simukomperawat.model.QuestionMode
import com.zulfahmi.simukomperawat.repository.ExperimentalPackRepository
import com.zulfahmi.simukomperawat.repository.ExperimentalQuestionMapper
import com.zulfahmi.simukomperawat.utlis.Commons
import com.zulfahmi.simukomperawat.utlis.CustomConfirmDialog
import java.util.Locale


class PackActivity : AppCompatActivity() {

    companion object{
        const val TAG = "PackActivity"
        const val EXTRA_QUESTION_TYPE = "question_type"
        const val TOTAL_PACK_LATIHAN = 5
        const val TOTAL_PACK_SIMULASI = 2
    }

    private lateinit var binding: ActivityPackBinding
    private val questionPackAccessPolicy = QuestionPackAccessPolicy()
    private var rewardedAd: RewardedAd? = null
    private var rewardEarned = false
    private var pendingQuestionPack = 0
    private var questionType = ""
    private lateinit var experimentalRepository: ExperimentalPackRepository
    private var experimentalPacks: List<ExperimentalPack> = emptyList()
    private var isSyncingExperimentalPack = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Commons.setFullscreenLayout(this)
        binding = ActivityPackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val slide = Slide()
        slide.slideEdge = Gravity.END
        window.enterTransition = slide
        window.returnTransition = Explode()

        MobileAds.initialize(this) {}
        loadRewardedAd()

        questionType = intent.getStringExtra(EXTRA_QUESTION_TYPE) ?: throw IllegalArgumentException("Question type is required")
        binding.tvType.text = questionType.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.ROOT
            ) else it.toString()
        }

        when (QuestionMode.fromWireValue(questionType)) {
            QuestionMode.LATIHAN -> showStaticPacks(TOTAL_PACK_LATIHAN)
            QuestionMode.SIMULASI -> showStaticPacks(TOTAL_PACK_SIMULASI)
            QuestionMode.EXPERIMENTAL -> showExperimentalPacks()
        }

        binding.imgbtnBack.setOnClickListener { onBackPressed() }
    }

    private fun showStaticPacks(total: Int) {
        val paketAdapter = RvAdapter(setJumlahPaket(total)) { _, position ->
            val selectedPack = position + 1
            if (questionPackAccessPolicy.requiresRewardedAdForPack(selectedPack)) {
                confirmRewardedAdBeforeOpeningPack(selectedPack)
            } else {
                openGuide(selectedPack)
            }
        }

        binding.recyclerview.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = paketAdapter
        }
    }

    private fun showExperimentalPacks() {
        experimentalRepository = ExperimentalPackRepository(this)
        Toast.makeText(this, "Memuat paket Experimental...", Toast.LENGTH_SHORT).show()
        experimentalRepository.fetchPacks(
            onSuccess = { packs ->
                experimentalPacks = packs
                if (packs.isEmpty()) {
                    Toast.makeText(this, "Belum ada paket Experimental yang dipublikasikan.", Toast.LENGTH_LONG).show()
                    return@fetchPacks
                }
                binding.recyclerview.apply {
                    layoutManager = GridLayoutManager(context, 3)
                    adapter = RvAdapter(packs.map { it.packNumber.toString() }) { _, position ->
                        syncExperimentalPack(experimentalPacks[position])
                    }
                }
            },
            onError = { message ->
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            },
        )
    }

    private fun syncExperimentalPack(pack: ExperimentalPack) {
        if (isSyncingExperimentalPack) return
        isSyncingExperimentalPack = true
        Toast.makeText(this, "Menyiapkan ${pack.title}...", Toast.LENGTH_SHORT).show()
        experimentalRepository.syncPack(
            pack = pack,
            onSuccess = {
                isSyncingExperimentalPack = false
                openGuide(ExperimentalQuestionMapper.PACK)
            },
            onError = { message ->
                isSyncingExperimentalPack = false
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            },
        )
    }

    private fun setJumlahPaket(total: Int): List<String> {
        val listNumber = ArrayList<String>()
        for (i in 1..total) {
            listNumber.add(i.toString())
        }
        return listNumber
    }

    private fun confirmRewardedAdBeforeOpeningPack(pack: Int) {
        CustomConfirmDialog(
            this,
            "Tonton Iklan",
            "Tonton iklan sampai selesai untuk membuka paket $pack?",
            btnPositiveText = "Tonton",
            btnNegativeText = "Batal"
        ) {
            pendingQuestionPack = pack
            showRewardedAd()
        }.show()
    }

    private fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(this, getString(R.string.ad_reward_pack_access), adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError.toString())
                rewardedAd = null
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Log.d(TAG, "Rewarded ad was loaded.")
                rewardedAd = ad
                rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.d(TAG, adError.toString())
                        rewardedAd = null
                        if (questionPackAccessPolicy.canOpenAfterRewardedAdShowFailed()) {
                            openGuide(pendingQuestionPack)
                        } else {
                            Toast.makeText(this@PackActivity, "Iklan belum siap. Silakan coba lagi.", Toast.LENGTH_SHORT).show()
                            loadRewardedAd()
                        }
                    }

                    override fun onAdShowedFullScreenContent() {
                        rewardedAd = null
                    }

                    override fun onAdDismissedFullScreenContent() {
                        rewardedAd = null
                        if (questionPackAccessPolicy.canOpenAfterRewardedAdClosed(rewardEarned)) {
                            openGuide(pendingQuestionPack)
                        } else {
                            Toast.makeText(this@PackActivity, "Tonton iklan sampai selesai untuk membuka paket soal", Toast.LENGTH_SHORT).show()
                            loadRewardedAd()
                        }
                        rewardEarned = false
                    }
                }
            }
        })
    }

    private fun showRewardedAd() {
        rewardEarned = false
        rewardedAd?.show(this, OnUserEarnedRewardListener {
            rewardEarned = true
        }) ?: run {
            Toast.makeText(this, "Iklan belum siap. Silakan coba lagi.", Toast.LENGTH_SHORT).show()
            loadRewardedAd()
        }
    }

    private fun openGuide(pack: Int) {
        startActivity(
            Intent(this, GuideActivity::class.java)
                .putExtra(GuideActivity.EXTRA_QUESTION_TYPE, questionType)
                .putExtra(GuideActivity.EXTRA_QUESTION_PACK, pack),
            Commons.setIntentTransition(this)
        )
    }

}
