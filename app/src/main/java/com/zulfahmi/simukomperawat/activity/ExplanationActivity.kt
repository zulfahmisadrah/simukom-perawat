package com.zulfahmi.simukomperawat.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.zulfahmi.simukomperawat.R
import com.zulfahmi.simukomperawat.R.drawable
import com.zulfahmi.simukomperawat.model.Question
import com.zulfahmi.simukomperawat.utlis.Commons
import com.zulfahmi.simukomperawat.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_explanation.*
import kotlinx.android.synthetic.main.activity_explanation.adv_banner

class ExplanationActivity : AppCompatActivity(), View.OnClickListener  {

    companion object{
        const val EXTRA_QUESTION_TYPE = "extra_question_type"
        const val EXTRA_QUESTION_PACK = "extra_question_pack"
        const val EXTRA_USER_ANSWER = "extra_user_answer"
        const val EXTRA_ANSWER = "extra_answer"
    }

    private lateinit var mainViewModel: MainViewModel
    private lateinit var listAnswer: Array<String>
    private lateinit var listUserAnswer: Array<String>
    private lateinit var listQuestionImage: IntArray
    private lateinit var listExplanationImage: IntArray

    private var listQuestions = ArrayList<Question>()

    private var questionType = ""
    private var totalQuestions = 0
    private var questionPack = 0

    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Commons.setFullscreenLayout(this)
        setContentView(R.layout.activity_explanation)

        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        adv_banner.loadAd(adRequest)

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        questionPack = intent.getIntExtra(EXTRA_QUESTION_PACK, 0)
        questionType = intent.getStringExtra(EXTRA_QUESTION_TYPE) as String
        listUserAnswer = intent.getStringArrayExtra(EXTRA_USER_ANSWER) as Array<String>
        listAnswer = intent.getStringArrayExtra(EXTRA_ANSWER) as Array<String>

        totalQuestions = if (questionType=="latihan") 20 else 100

        listQuestionImage = IntArray(totalQuestions){0}
        listExplanationImage = IntArray(totalQuestions){0}

        observeData(questionType, questionPack)

        btn_finish.setOnClickListener(this)
        imgbtn_next.setOnClickListener(this)
        imgbtn_prev.setOnClickListener(this)
        input_number.setOnClickListener(this)

        imgbtn_explanation.setOnClickListener(this)
        imgbtn_question.setOnClickListener(this)
    }

    private fun observeData(type: String, pack: Int){
        mainViewModel.getSoal(type, pack)?.observe(this, Observer {
            listQuestions.addAll(it)
            bindQuestionsExplanation()
        })

    }

    private fun bindQuestionsExplanation() {
        val number = currentIndex+1
        input_number.setText(number.toString())

        val question = "$number. ${listQuestions[currentIndex].question}"
        val userAnswer = getOptionsText(listUserAnswer[currentIndex])
        val correctAnswer = getOptionsText(listAnswer[currentIndex])

        var strUserAnswer = "Jawaban Anda: $userAnswer"
        val strCorrectAnswer = "Jawaban benar: $correctAnswer"

        val answerStatus = if(userAnswer!="-" && userAnswer==correctAnswer) "(BENAR)" else if (userAnswer!="-" && userAnswer!=correctAnswer) "(SALAH)" else ""
        strUserAnswer = "$strUserAnswer $answerStatus"

        tv_question.text = question
        tv_user_answer.text = Commons.setBoldAtSpanString(strUserAnswer, 0, 12)
        tv_correct_answer.text = Commons.setBoldAtSpanString(strCorrectAnswer, 0, 13)
        tv_explanation.text = listQuestions[currentIndex].explanation

        val imageQuestion = listQuestions[currentIndex].image
        if (imageQuestion!=null) {
            imgbtn_question.visibility = View.VISIBLE
            listQuestionImage[currentIndex] = Commons.getResourceId( imageQuestion, drawable::class.java)
            imgbtn_question.setImageResource(listQuestionImage[currentIndex])
        }else
            imgbtn_question.visibility = View.GONE

        val imageExplanation = listQuestions[currentIndex].imageExplanation
        if (imageExplanation!=null) {
            imgbtn_explanation.visibility = View.VISIBLE
            listExplanationImage[currentIndex] = Commons.getResourceId( imageExplanation, drawable::class.java)
            imgbtn_explanation.setImageResource(listExplanationImage[currentIndex])
        }else
            imgbtn_explanation.visibility = View.GONE

    }

    private fun getOptionsText(options: String): String{
        return when(options){
            "a" -> "A. ${listQuestions[currentIndex].optionA}"
            "b" -> "B. ${listQuestions[currentIndex].optionB}"
            "c" -> "C. ${listQuestions[currentIndex].optionC}"
            "d" -> "D. ${listQuestions[currentIndex].optionD}"
            "e" -> "E. ${listQuestions[currentIndex].optionE}"
            "" -> "-"
            else -> throw IllegalArgumentException("Invalid options")
        }

    }

    // 0 = previous, 1 = next
    private fun changeQuestion(type: Int) {
        if (type==0 && currentIndex>0){
            currentIndex--
        } else if (type==1 && currentIndex < totalQuestions-1) {
            currentIndex++
        } else
            return
        bindQuestionsExplanation()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgbtn_prev -> changeQuestion(0)
            R.id.imgbtn_next -> changeQuestion(1)
            R.id.input_number -> {
                val numbers = Array(totalQuestions) { i -> (i+1).toString()}
                Commons.showSelector(this, "Pilih nomor soal", numbers) { _, i ->
                    currentIndex = i
                    bindQuestionsExplanation()
                }
            }
            R.id.btn_finish -> {
                onBackPressed()
            }
            R.id.imgbtn_question -> {
                Commons.zoomImageFromThumb(container, v, listQuestionImage[currentIndex], resources.getInteger(android.R.integer.config_shortAnimTime))
            }
            R.id.imgbtn_explanation -> {
                Commons.zoomImageFromThumb(container, v, listExplanationImage[currentIndex], resources.getInteger(android.R.integer.config_shortAnimTime))
            }
        }
    }
}