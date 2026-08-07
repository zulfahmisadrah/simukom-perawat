package com.zulfahmi.simukomperawat.activity

import android.content.Intent
import android.os.Bundle
import android.transition.Explode
import android.transition.Slide
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
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
import com.zulfahmi.simukomperawat.adapter.LatihanPackAdapter
import com.zulfahmi.simukomperawat.adapter.RvAdapter
import com.zulfahmi.simukomperawat.ads.PackOpenAction
import com.zulfahmi.simukomperawat.ads.QuestionPackAccessPolicy
import com.zulfahmi.simukomperawat.databinding.ActivityPackBinding
import com.zulfahmi.simukomperawat.model.LatihanCategory
import com.zulfahmi.simukomperawat.model.LatihanPack
import com.zulfahmi.simukomperawat.model.QuestionMode
import com.zulfahmi.simukomperawat.repository.FirestoreQuestionRepository
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
    private var pendingLatihanPack: LatihanPack? = null
    private var pendingStaticPack: Int? = null
    private var questionType = ""
    private lateinit var firestoreQuestionRepository: FirestoreQuestionRepository
    private var isPreparingPackage = false
    private var latihanCategories: List<LatihanCategory> = emptyList()
    private var activeLatihanCategory: LatihanCategory? = null
    private lateinit var latihanPackAdapter: LatihanPackAdapter

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
        firestoreQuestionRepository = FirestoreQuestionRepository(this)

        questionType = intent.getStringExtra(EXTRA_QUESTION_TYPE) ?: throw IllegalArgumentException("Question type is required")
        binding.tvType.text = questionType.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.ROOT
            ) else it.toString()
        }

        when (QuestionMode.fromWireValue(questionType)) {
            QuestionMode.LATIHAN -> showLatihanPacks()
            QuestionMode.SIMULASI -> showStaticPacks(TOTAL_PACK_SIMULASI)
        }

        binding.imgbtnBack.setOnClickListener { onBackPressed() }
    }

    private fun showStaticPacks(total: Int) {
        binding.btnSelectCategory.visibility = View.GONE
        val paketAdapter = RvAdapter(setJumlahPaket(total)) { _, position ->
            val selectedPack = position + 1
            if (questionPackAccessPolicy.requiresRewardedAdForPack(selectedPack)) {
                pendingStaticPack = selectedPack
                confirmRewardedAdBeforeOpeningPack("paket $selectedPack")
            } else {
                preparePackageAndOpenGuide(selectedPack, null)
            }
        }

        binding.recyclerview.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = paketAdapter
        }
    }

    private fun showLatihanPacks() {
        binding.btnSelectCategory.visibility = View.VISIBLE
        firestoreQuestionRepository.fetchPublishedLatihanPacks(
            onSuccess = { remotePacks -> renderLatihanCategories(remotePacks + LatihanPack.bundled()) },
            onError = { renderLatihanCategories(LatihanPack.bundled()) },
        )
    }

    private fun renderLatihanCategories(packs: List<LatihanPack>) {
        latihanCategories = LatihanPack.groupByCategory(packs)
        activeLatihanCategory = latihanCategories.firstOrNull()
        latihanPackAdapter = LatihanPackAdapter(emptyList(), ::onLatihanPackSelected)
        binding.recyclerview.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = latihanPackAdapter
        }
        binding.btnSelectCategory.setOnClickListener { showCategorySelector() }
        renderActiveLatihanCategory()
    }

    private fun renderActiveLatihanCategory() {
        val category = activeLatihanCategory ?: return
        binding.btnSelectCategory.text = "Kategori: ${category.name} (${category.packs.size} paket)"
        latihanPackAdapter.submitList(category.packs)
    }

    private fun showCategorySelector() {
        val activeIndex = latihanCategories.indexOf(activeLatihanCategory).coerceAtLeast(0)
        val labels = latihanCategories.map { "${it.name} (${it.packs.size} paket)" }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Pilih kategori")
            .setSingleChoiceItems(labels, activeIndex) { dialog, selectedIndex ->
                activeLatihanCategory = latihanCategories[selectedIndex]
                renderActiveLatihanCategory()
                dialog.dismiss()
            }
            .show()
    }

    private fun onLatihanPackSelected(pack: LatihanPack) {
        when (questionPackAccessPolicy.actionFor(pack)) {
            PackOpenAction.OPEN -> preparePackageAndOpenGuide(pack.roomPack, pack.firestoreId)
            PackOpenAction.SHOW_REWARDED_AD -> {
                pendingLatihanPack = pack
                confirmRewardedAdBeforeOpeningPack(pack.title)
            }
            PackOpenAction.SHOW_PREMIUM_MESSAGE -> {
                Toast.makeText(this, "Paket premium belum tersedia.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun preparePackageAndOpenGuide(pack: Int, firestorePackId: String?) {
        if (questionType != QuestionMode.LATIHAN.wireValue) {
            openGuide(pack)
            return
        }
        if (isPreparingPackage) return
        isPreparingPackage = true
        Toast.makeText(this, "Menyiapkan soal untuk penggunaan offline...", Toast.LENGTH_SHORT).show()
        firestoreQuestionRepository.refreshPackage(
            type = QuestionMode.LATIHAN.wireValue,
            pack = pack,
            firestorePackId = firestorePackId ?: FirestoreQuestionRepository.firestorePackId(questionType, pack),
            onReady = {
                isPreparingPackage = false
                openGuide(pack)
            },
            onError = { message ->
                isPreparingPackage = false
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

    private fun confirmRewardedAdBeforeOpeningPack(packTitle: String) {
        CustomConfirmDialog(
            this,
            "Tonton Iklan",
            "Tonton iklan sampai selesai untuk membuka $packTitle?",
            btnPositiveText = "Tonton",
            btnNegativeText = "Batal"
        ) {
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
                            openPendingPack()
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
                            openPendingPack()
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

    private fun openPendingPack() {
        pendingLatihanPack?.let { pack ->
            pendingLatihanPack = null
            preparePackageAndOpenGuide(pack.roomPack, pack.firestoreId)
            return
        }
        pendingStaticPack?.let { pack ->
            pendingStaticPack = null
            preparePackageAndOpenGuide(pack, null)
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
