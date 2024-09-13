package com.zulfahmi.simukomperawat.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.Explode
import android.transition.Slide
import android.view.Gravity
import com.zulfahmi.simukomperawat.R
import com.zulfahmi.simukomperawat.utlis.Commons
import kotlinx.android.synthetic.main.activity_guide.*

class GuideActivity : AppCompatActivity() {
    companion object{
        const val EXTRA_QUESTION_TYPE = "question_type"
        const val EXTRA_QUESTION_PACK = "question_pack"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Commons.setFullscreenLayout(this)
        setContentView(R.layout.activity_guide)
        val slide = Slide()
        slide.slideEdge = Gravity.END
        window.enterTransition = slide
        window.returnTransition = Explode()

        val questionType = intent.getStringExtra(EXTRA_QUESTION_TYPE) as String
        val questionPack = intent.getIntExtra(EXTRA_QUESTION_PACK, 0)
        tv_type.text = questionType.capitalize()

        val guides = if (questionType == "latihan") resources.getString(R.string.panduan_latihan) else resources.getString(R.string.panduan_simulasi)
        tv_guides.text = guides

        imgbtn_back.setOnClickListener { onBackPressed() }

        btn_start.setOnClickListener {
            startActivity(Intent(this, QuestionActivity::class.java).putExtra(QuestionActivity.EXTRA_QUESTION_TYPE, questionType).putExtra(QuestionActivity.EXTRA_QUESTION_PACK, questionPack), Commons.setIntentTransition(this))
        }
    }
}