package com.zulfahmi.simukomperawat.activity

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.transition.Fade
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.zulfahmi.simukomperawat.R
import com.zulfahmi.simukomperawat.databinding.ActivityQuestionBinding
import com.zulfahmi.simukomperawat.model.Question
import com.zulfahmi.simukomperawat.utlis.Commons
import com.zulfahmi.simukomperawat.utlis.CustomConfirmDialog
import com.zulfahmi.simukomperawat.viewmodel.MainViewModel
import java.util.*
import kotlin.collections.ArrayList

class QuestionActivity : AppCompatActivity(), View.OnClickListener {

    companion object{
        const val EXTRA_QUESTION_TYPE = "extra_question_type"
        const val EXTRA_QUESTION_PACK = "extra_question_pack"
    }

    private lateinit var mainViewModel: MainViewModel
    private lateinit var listSelectedRadioButtonId: IntArray
    private lateinit var listAnswer: Array<String>
    private lateinit var listUserAnswer: Array<String>
    private lateinit var listQuestionImage: IntArray
    private lateinit var binding: ActivityQuestionBinding

    private lateinit var mCountDownTimer: CountDownTimer
    private val handler = Handler()

    private val startTimeInMillis: Long = 6000000
    private var timeLeftInMillis = startTimeInMillis

    private var listQuestions = ArrayList<Question>()
    private var questionType = ""
    private var totalQuestions = 0
    private var questionPack = 0
    private var totalAnswered = 0

    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Commons.setFullscreenLayout(this)
        binding = ActivityQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.enterTransition = Fade()

        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        binding.advBanner.loadAd(adRequest)

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        questionType = intent.getStringExtra(EXTRA_QUESTION_TYPE) as String
        questionPack = intent.getIntExtra(EXTRA_QUESTION_PACK, 0)

        totalQuestions = if (questionType=="latihan") 20 else 100

        listQuestionImage = IntArray(totalQuestions){0}

        listSelectedRadioButtonId = IntArray(totalQuestions){0}
        listAnswer = Array(totalQuestions){""}
        listUserAnswer = Array(totalQuestions){""}

        if (questionType != "latihan") {
            binding.timerContainer.visibility = View.VISIBLE
            startTimer()
            handler.post(object : Runnable {
                override fun run() {
                    handler.postDelayed(this, 1000)
                    updateCountDownText()
                }
            })
//            updateCountDownText()
//            startTimer()
        }
        
        observeData(questionType, questionPack)
        updateAnsweredQuestion()

        binding.imgbtnNext.setOnClickListener(this)
        binding.imgbtnPrev.setOnClickListener(this)
        binding.imgbtnQuestion.setOnClickListener(this)
        binding.inputNumber.setOnClickListener(this)
        binding.btnFinish.setOnClickListener(this)
        binding.btnTotalAnswered.setOnClickListener(this)
        binding.btnPause.setOnClickListener(this)
        binding.btnContinue.setOnClickListener(this)

        binding.rbOptionGroup.setOnCheckedChangeListener { _, checkedId ->
            if(checkedId!=-1){
                val selectedOption = findViewById<RadioButton>(checkedId)
                listSelectedRadioButtonId[currentIndex] = checkedId
                listUserAnswer[currentIndex] = selectedOption.text.toString().lowercase(Locale.ROOT)
            }
            updateAnsweredQuestion()
        }


    }

    private fun observeData(type: String, pack: Int){
        mainViewModel.getSoal(type, pack)?.observe(this, Observer {
            listQuestions.addAll(it)
            bindQuestions()
        })

    }

    private fun getCorrectAnswer() {
        listQuestions.forEachIndexed{ i, question ->
            listAnswer[i] = question.answer
        }
    }

    private fun bindQuestions() {
        val number = currentIndex+1
        binding.inputNumber.setText(number.toString())

        val question = "$number. ${listQuestions[currentIndex].question}"
        val optionA = "A. ${listQuestions[currentIndex].optionA}"
        val optionB = "B. ${listQuestions[currentIndex].optionB}"
        val optionC = "C. ${listQuestions[currentIndex].optionC}"
        val optionD = "D. ${listQuestions[currentIndex].optionD}"
        val optionE = "E. ${listQuestions[currentIndex].optionE}"

        if(listSelectedRadioButtonId[currentIndex]!=0)
            binding.rbOptionGroup.check(listSelectedRadioButtonId[currentIndex])

        binding.tvQuestion.text = question
        binding.tvOptionA.text = optionA
        binding.tvOptionB.text = optionB
        binding.tvOptionC.text = optionC
        binding.tvOptionD.text = optionD
        binding.tvOptionE.text = optionE

        val imageQuestion = listQuestions[currentIndex].image
        if (imageQuestion!=null) {
            binding.imgbtnQuestion.visibility = View.VISIBLE
            listQuestionImage[currentIndex] = Commons.getResourceId( imageQuestion, R.drawable::class.java)
            binding.imgbtnQuestion.setImageResource(listQuestionImage[currentIndex])
        }else
            binding.imgbtnQuestion.visibility = View.GONE
    }

    // 0 = previous, 1 = next, 2 = jump
    private fun changeQuestion(type: Int) {
        if (type==0 && currentIndex>0){
            binding.rbOptionGroup.clearCheck()
            currentIndex--
        } else if (type==1 && currentIndex < totalQuestions-1) {
            binding.rbOptionGroup.clearCheck()
            currentIndex++
        } else if (type==2){
            bindQuestions()
            return
        } else
            return
        bindQuestions()
    }

    private fun showUnansweredQuestion() {
        var unansweredQuestion = ""
        var isFirstOccur = true
        listUserAnswer.forEachIndexed { index, answer ->
            if (answer == "")
                if (isFirstOccur) {
                    isFirstOccur = false
                   unansweredQuestion += index+1
                }
                else
                    unansweredQuestion +=", ${index+1}"
        }

        val addText = "Belum dijawab: \n $unansweredQuestion"
        val strMessage = Commons.setBoldAtSpanString(addText, 0, 13)
        Commons.showAlertDialog(this, strMessage)
    }

    private fun updateAnsweredQuestion() {
        totalAnswered = Commons.countFilledArray(listUserAnswer)
        val strAnswered = "Terjawab: $totalAnswered/$totalQuestions"
        binding.btnTotalAnswered.text = strAnswered
    }

    private fun startTimer() {
        mCountDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()
            }

            override fun onFinish() {
            }
        }.start()

    }

    private fun pauseTimer() {
        mCountDownTimer.cancel()
        binding.containerPause.visibility = View.VISIBLE
    }

    private fun updateCountDownText() {
        val minutes = (timeLeftInMillis / 1000).toInt() / 60
        val seconds = (timeLeftInMillis / 1000).toInt() % 60
        val minutesInHour = minutes - 60
        val hour = 1
        val timeLeftFormatted = String.format(
            Locale.getDefault(),
            "%02d:%02d:%02d",
            hour,
            minutesInHour,
            seconds
        )
        binding.tvCountdownTimer.text = timeLeftFormatted
    }

    override fun onBackPressed() {
        Toast.makeText(this, "Tekan tombol SELESAI untuk keluar", Toast.LENGTH_SHORT).show()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgbtn_prev -> changeQuestion(0)
            R.id.imgbtn_next -> changeQuestion(1)
            R.id.input_number -> {
                val numbers = Array(totalQuestions) { i -> (i+1).toString()}
                Commons.showSelector(this, "Pilih nomor soal", numbers) { _, i ->
                    binding.rbOptionGroup.clearCheck()
                    currentIndex = i
                    changeQuestion(2)
                }
            }
            R.id.btn_finish -> {
                CustomConfirmDialog(this,"Selesai","Anda yakin ingin menyelesaikan $questionType ini?") {
                    getCorrectAnswer()
                    val userTime: Long = startTimeInMillis - timeLeftInMillis
                    val minutes = (userTime / 1000).toInt() / 60
                    val seconds = (userTime / 1000).toInt() % 60
                    val strUserTime = if (minutes==0) "Waktu pengerjaan: $seconds detik" else "Waktu pengerjaan: $minutes menit $seconds detik"
                    val average: Long = if (totalAnswered!=0) userTime / totalAnswered else 0
                    val averageMinutes = (average / 1000).toInt() / 60
                    val averageSeconds = (average / 1000).toInt() % 60
                    val strAverageTime = if (average==0L) "Rata-rata waktu/soal: -" else if (averageMinutes==0) "Rata-rata waktu/soal: $averageSeconds detik" else "Rata-rata waktu/soal: $averageMinutes menit $averageSeconds detik"
                    startActivity(
                        Intent(this, ResultActivity::class.java)
                            .putExtra(ResultActivity.EXTRA_USER_ANSWER, listUserAnswer)
                            .putExtra(ResultActivity.EXTRA_ANSWER, listAnswer)
                            .putExtra(ResultActivity.EXTRA_USER_TIME, strUserTime)
                            .putExtra(ResultActivity.EXTRA_TIME_PER_QUESTION, strAverageTime)
                            .putExtra(ResultActivity.EXTRA_QUESTION_PACK, questionPack)
                            .putExtra(ResultActivity.EXTRA_QUESTION_TYPE, questionType)
                            .putExtra(ResultActivity.EXTRA_TOTAL_QUESTIONS, totalQuestions),
                        Commons.setIntentTransition(this)
                    )
                }.show()
            }
            R.id.btn_total_answered -> {
                showUnansweredQuestion()
            }
            R.id.btn_pause -> pauseTimer()
            R.id.btn_continue -> {
                binding.containerPause.visibility = View.GONE
                startTimer()
            }
            R.id.imgbtn_question -> {
                Commons.zoomImageFromThumb(binding.container, v, listQuestionImage[currentIndex], resources.getInteger(android.R.integer.config_shortAnimTime))
            }
        }
    }
}
