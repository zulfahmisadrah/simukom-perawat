package com.zulfahmi.simukomperawat.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.Explode
import android.view.View
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.zulfahmi.simukomperawat.R
import com.zulfahmi.simukomperawat.utlis.Commons
import kotlinx.android.synthetic.main.activity_result.*

class ResultActivity : AppCompatActivity() {
    companion object{
        const val EXTRA_USER_TIME = "extra_user_time"
        const val EXTRA_TIME_PER_QUESTION = "extra_time_per_question"
        const val EXTRA_USER_ANSWER = "extra_user_answer"
        const val EXTRA_ANSWER = "extra_answer"
        const val EXTRA_QUESTION_TYPE = "extra_question_type"
        const val EXTRA_QUESTION_PACK = "extra_question_pack"
        const val EXTRA_TOTAL_QUESTIONS = "extra_total_questions"
    }

    private lateinit var interstitialAd: InterstitialAd

    private var questionType = ""
    private var totalQuestions = 0
    private var questionPack = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Commons.setFullscreenLayout(this)
        setContentView(R.layout.activity_result)
        window.enterTransition = Explode()

        MobileAds.initialize(this) {}
        interstitialAd = InterstitialAd(this)
        interstitialAd.adUnitId = resources.getString(R.string.ad_inters1)
        interstitialAd.loadAd(AdRequest.Builder().build())

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

        tv_score.text = score.toString()
        tv_answered.text = strTotalAnswered
        tv_correct_answer.text = strTotalCorrectAnswer
        tv_wrong_answer.text = strTotalWrongAnswer

        if (questionType!="latihan") {
            tv_time.visibility = View.VISIBLE
            tv_average_time.visibility = View.VISIBLE

            tv_time.text = userTime
            tv_average_time.text = averageTime
        }

        btn_home.setOnClickListener {
            if (interstitialAd.isLoaded) interstitialAd.show()
            finish()
        }
        btn_explanation.setOnClickListener {
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