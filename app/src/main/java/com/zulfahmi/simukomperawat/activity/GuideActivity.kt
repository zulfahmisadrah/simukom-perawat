package com.zulfahmi.simukomperawat.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.Explode
import android.transition.Slide
import android.view.Gravity
import com.zulfahmi.simukomperawat.R
import com.zulfahmi.simukomperawat.databinding.ActivityGuideBinding
import com.zulfahmi.simukomperawat.utlis.Commons
import java.util.Locale

class GuideActivity : AppCompatActivity() {
    companion object{
        const val EXTRA_QUESTION_TYPE = "question_type"
        const val EXTRA_QUESTION_PACK = "question_pack"
    }

    private lateinit var binding: ActivityGuideBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Commons.setFullscreenLayout(this)
        binding = ActivityGuideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val slide = Slide()
        slide.slideEdge = Gravity.END
        window.enterTransition = slide
        window.returnTransition = Explode()

        val questionType = intent.getStringExtra(EXTRA_QUESTION_TYPE) as String
        val questionPack = intent.getIntExtra(EXTRA_QUESTION_PACK, 0)
        binding.tvType.text = questionType.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.ROOT
            ) else it.toString()
        }

        val guides = if (questionType == "latihan") resources.getString(R.string.panduan_latihan) else resources.getString(R.string.panduan_simulasi)
        binding.tvGuides.text = guides

        binding.imgbtnBack.setOnClickListener { onBackPressed() }

        binding.btnStart.setOnClickListener {
            startActivity(Intent(this, QuestionActivity::class.java).putExtra(QuestionActivity.EXTRA_QUESTION_TYPE, questionType).putExtra(QuestionActivity.EXTRA_QUESTION_PACK, questionPack), Commons.setIntentTransition(this))
        }
    }
}