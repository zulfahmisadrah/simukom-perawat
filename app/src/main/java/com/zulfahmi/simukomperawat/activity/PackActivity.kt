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
    private var pendingRemotePack: LatihanPack? = null
    private var pendingStaticPack: Int? = null
    private var questionType = ""
    private lateinit var firestoreQuestionRepository: FirestoreQuestionRepository
    private var isPreparingPackage = false
    private var remoteCategories: List<LatihanCategory> = emptyList()
    private var activeRemoteCategory: LatihanCategory? = null
    private lateinit var remotePackAdapter: LatihanPackAdapter

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
            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
        }

        when (QuestionMode.fromWireValue(questionType)) {
            QuestionMode.LATIHAN -> showLatihanPacks()
            QuestionMode.SIMULASI -> {
                binding.materiSection.visibility = View.GONE
                showStaticPacks(TOTAL_PACK_SIMULASI)
            }
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
                openGuide(selectedPack)
            }
        }

        binding.recyclerview.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = paketAdapter
        }
    }

    private fun showLatihanPacks() {
        showStaticPacks(TOTAL_PACK_LATIHAN)
        showRemotePacks()
    }

    private fun showRemotePacks() {
        binding.materiSection.visibility = View.VISIBLE
        binding.btnSelectCategory.visibility = View.VISIBLE
        binding.recyclerviewMateri.visibility = View.VISIBLE
        firestoreQuestionRepository.fetchPublishedLatihanPacks(
            onSuccess = ::renderRemoteCategories,
            onError = {
                binding.btnSelectCategory.visibility = View.GONE
                binding.recyclerviewMateri.visibility = View.GONE
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            },
        )
    }

    private fun renderRemoteCategories(packs: List<LatihanPack>) {
        remoteCategories = LatihanPack.groupByCategory(packs)
        activeRemoteCategory = remoteCategories.firstOrNull()
        remotePackAdapter = LatihanPackAdapter(emptyList(), ::onRemotePackSelected)
        binding.recyclerviewMateri.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = remotePackAdapter
        }
        binding.btnSelectCategory.setOnClickListener { showCategorySelector() }
        if (activeRemoteCategory == null) {
            binding.btnSelectCategory.visibility = View.GONE
            binding.recyclerviewMateri.visibility = View.GONE
            return
        }
        renderActiveRemoteCategory()
    }

    private fun renderActiveRemoteCategory() {
        val category = activeRemoteCategory ?: return
        binding.btnSelectCategory.text = "${category.name} (${category.packs.size} paket)"
        remotePackAdapter.submitList(category.packs)
    }

    private fun showCategorySelector() {
        val activeIndex = remoteCategories.indexOf(activeRemoteCategory).coerceAtLeast(0)
        val labels = remoteCategories.map { "${it.name} (${it.packs.size} paket)" }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle(R.string.pilih_materi)
            .setSingleChoiceItems(labels, activeIndex) { dialog, selectedIndex ->
                activeRemoteCategory = remoteCategories[selectedIndex]
                renderActiveRemoteCategory()
                dialog.dismiss()
            }
            .show()
    }

    private fun onRemotePackSelected(pack: LatihanPack) {
        when (questionPackAccessPolicy.actionFor(pack)) {
            PackOpenAction.OPEN -> prepareRemotePackageAndOpenGuide(pack)
            PackOpenAction.SHOW_REWARDED_AD -> {
                pendingRemotePack = pack
                confirmRewardedAdBeforeOpeningPack(pack.adPromptLabel)
            }
            PackOpenAction.SHOW_PREMIUM_MESSAGE -> {
                Toast.makeText(this, "Paket premium belum tersedia.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun prepareRemotePackageAndOpenGuide(pack: LatihanPack) {
        if (isPreparingPackage) return
        isPreparingPackage = true
        firestoreQuestionRepository.refreshPackage(
            type = QuestionMode.LATIHAN.wireValue,
            pack = pack.roomPack,
            firestorePackId = requireNotNull(pack.firestoreId),
            onReady = {
                isPreparingPackage = false
                openGuide(pack.roomPack)
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
        pendingRemotePack?.let { pack ->
            pendingRemotePack = null
            prepareRemotePackageAndOpenGuide(pack)
            return
        }
        pendingStaticPack?.let { pack ->
            pendingStaticPack = null
            openGuide(pack)
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
