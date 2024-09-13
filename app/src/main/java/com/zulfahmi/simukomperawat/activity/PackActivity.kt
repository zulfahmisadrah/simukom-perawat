package com.zulfahmi.simukomperawat.activity

import android.content.Intent
import android.os.Bundle
import android.transition.Explode
import android.transition.Slide
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.zulfahmi.simukomperawat.R
import com.zulfahmi.simukomperawat.adapter.RvAdapter
import com.zulfahmi.simukomperawat.utlis.Commons
import kotlinx.android.synthetic.main.activity_pack.*


class PackActivity : AppCompatActivity() {

    companion object{
        const val EXTRA_QUESTION_TYPE = "question_type"
        const val TOTAL_PACK_LATIHAN = 5
        const val TOTAL_PACK_SIMULASI = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Commons.setFullscreenLayout(this)
        setContentView(R.layout.activity_pack)
        val slide = Slide()
        slide.slideEdge = Gravity.END
        window.enterTransition = slide
        window.returnTransition = Explode()

        val questionType = intent.getStringExtra(EXTRA_QUESTION_TYPE) as String
        tv_type.text = questionType.capitalize()

        val listPaket = when (questionType) {
            "latihan" -> setJumlahPaket(TOTAL_PACK_LATIHAN)
            "simulasi" -> setJumlahPaket(TOTAL_PACK_SIMULASI)
            else -> throw IllegalArgumentException("Undefined type")
        }

        val paketAdapter = RvAdapter(listPaket) { _, position ->
            startActivity(Intent(this, GuideActivity::class.java).putExtra(GuideActivity.EXTRA_QUESTION_TYPE, questionType).putExtra(GuideActivity.EXTRA_QUESTION_PACK, position+1), Commons.setIntentTransition(this))
        }

        recyclerview.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = paketAdapter
        }

        imgbtn_back.setOnClickListener { onBackPressed() }
    }

    private fun setJumlahPaket(total: Int): List<String> {
        val listNumber = ArrayList<String>()
        for (i in 1..total) {
            listNumber.add(i.toString())
        }
        return listNumber
    }

}
