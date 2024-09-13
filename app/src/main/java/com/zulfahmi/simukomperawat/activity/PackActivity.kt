package com.zulfahmi.simukomperawat.activity

import android.content.Intent
import android.os.Bundle
import android.transition.Explode
import android.transition.Slide
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.zulfahmi.simukomperawat.adapter.RvAdapter
import com.zulfahmi.simukomperawat.databinding.ActivityPackBinding
import com.zulfahmi.simukomperawat.utlis.Commons
import java.util.Locale


class PackActivity : AppCompatActivity() {

    companion object{
        const val EXTRA_QUESTION_TYPE = "question_type"
        const val TOTAL_PACK_LATIHAN = 5
        const val TOTAL_PACK_SIMULASI = 2
    }

    private lateinit var binding: ActivityPackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Commons.setFullscreenLayout(this)
        binding = ActivityPackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val slide = Slide()
        slide.slideEdge = Gravity.END
        window.enterTransition = slide
        window.returnTransition = Explode()

        val questionType = intent.getStringExtra(EXTRA_QUESTION_TYPE) as String
        binding.tvType.text = questionType.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.ROOT
            ) else it.toString()
        }

        val listPaket = when (questionType) {
            "latihan" -> setJumlahPaket(TOTAL_PACK_LATIHAN)
            "simulasi" -> setJumlahPaket(TOTAL_PACK_SIMULASI)
            else -> throw IllegalArgumentException("Undefined type")
        }

        val paketAdapter = RvAdapter(listPaket) { _, position ->
            startActivity(Intent(this, GuideActivity::class.java).putExtra(GuideActivity.EXTRA_QUESTION_TYPE, questionType).putExtra(GuideActivity.EXTRA_QUESTION_PACK, position+1), Commons.setIntentTransition(this))
        }

        binding.recyclerview.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = paketAdapter
        }

        binding.imgbtnBack.setOnClickListener { onBackPressed() }
    }

    private fun setJumlahPaket(total: Int): List<String> {
        val listNumber = ArrayList<String>()
        for (i in 1..total) {
            listNumber.add(i.toString())
        }
        return listNumber
    }

}
