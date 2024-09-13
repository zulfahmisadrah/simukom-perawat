package com.zulfahmi.simukomperawat.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.Explode
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.zulfahmi.simukomperawat.R
import com.zulfahmi.simukomperawat.databinding.ActivityResultBinding
import com.zulfahmi.simukomperawat.utlis.Commons

class ResultActivity : AppCompatActivity() {
    companion object {
        const val TAG = "ResultActivity"
        const val EXTRA_USER_TIME = "extra_user_time"
        const val EXTRA_TIME_PER_QUESTION = "extra_time_per_question"
        const val EXTRA_USER_ANSWER = "extra_user_answer"
        const val EXTRA_ANSWER = "extra_answer"
        const val EXTRA_QUESTION_TYPE = "extra_question_type"
        const val EXTRA_QUESTION_PACK = "extra_question_pack"
        const val EXTRA_TOTAL_QUESTIONS = "extra_total_questions"
    }

    private lateinit var binding: ActivityResultBinding

    private var mInterstitialAd: InterstitialAd? = null
    private var questionType = ""
    private var totalQuestions = 0
    private var questionPack = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Commons.setFullscreenLayout(this)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.enterTransition = Explode()

        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this, resources.getString(R.string.ad_inters1), adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError.toString())
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "Ad was loaded.")
                mInterstitialAd = interstitialAd
            }
        })

        val listUserAnswer = intent.getStringArrayExtra(EXTRA_USER_ANSWER) as Array<String>
        val listAnswer = intent.getStringArrayExtra(EXTRA_ANSWER) as Array<String>
        val averageTime = intent.getStringExtra(EXTRA_TIME_PER_QUESTION) as String
        val userTime = intent.getStringExtra(EXTRA_USER_TIME) as String
        questionType = intent.getStringExtra(EXTRA_QUESTION_TYPE) as String
        questionPack = intent.getIntExtra(EXTRA_QUESTION_PACK, 0)
        totalQuestions = intent.getIntExtra(EXTRA_TOTAL_QUESTIONS,0)

        val totalAnswered = Commons.countFilledArray(listUserAnswer)
        val totalCorrectAnswer = getTotalCorrectAnswer(listUserAnswer, listAnswer)
        val totalWrongAnswer = totalAnswered-totalCorrectAnswer
        val score = getScore(totalCorrectAnswer, totalQuestions)

        val strTotalAnswered = "Soal terjawab: $totalAnswered dari $totalQuestions"
        val strTotalCorrectAnswer = "Jawaban benar: $totalCorrectAnswer"
        val strTotalWrongAnswer = "Jawaban salah: $totalWrongAnswer"

        binding.tvScore.text = score.toString()
        binding.tvAnswered.text = strTotalAnswered
        binding.tvCorrectAnswer.text = strTotalCorrectAnswer
        binding.tvWrongAnswer.text = strTotalWrongAnswer

        if (questionType!="latihan") {
            binding.tvTime.visibility = View.VISIBLE
            binding.tvAverageTime.visibility = View.VISIBLE

            binding.tvTime.text = userTime
            binding.tvAverageTime.text = averageTime
        }

        binding.btnHome.setOnClickListener {
            if (mInterstitialAd != null)  mInterstitialAd?.show(this)
            finish()
        }
        binding.btnExplanation.setOnClickListener {
            startActivity(
                Intent(this, ExplanationActivity::class.java)
                    .putExtra(ExplanationActivity.EXTRA_QUESTION_TYPE, questionType)
                    .putExtra(ExplanationActivity.EXTRA_QUESTION_PACK, questionPack)
                    .putExtra(ExplanationActivity.EXTRA_USER_ANSWER, listUserAnswer)
                    .putExtra(ExplanationActivity.EXTRA_ANSWER, listAnswer),
                Commons.setIntentTransition(this)
            )
        }

    }

    override fun onBackPressed() {
        Toast.makeText(this, "Tekan tombol KELUAR untuk ke halaman utama", Toast.LENGTH_SHORT).show()
    }

    private fun getTotalCorrectAnswer(listUserAnswer: Array<String>, listAnswer: Array<String>): Int {
        var totalCorrectAnswer = 0
        listUserAnswer.forEachIndexed { i, answer ->
            val correctAnswer = listAnswer[i]
            if (answer == correctAnswer)
                totalCorrectAnswer++
        }
        return totalCorrectAnswer
    }

    private fun getScore(totalCorrectAnswer: Int, totalQuestions: Int): Int{
        return totalCorrectAnswer*100/totalQuestions
    }
}