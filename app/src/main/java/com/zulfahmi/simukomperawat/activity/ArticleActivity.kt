package com.zulfahmi.simukomperawat.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.Explode
import android.view.Gravity
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.zulfahmi.simukomperawat.R
import com.zulfahmi.simukomperawat.databinding.ActivityArticleBinding
import com.zulfahmi.simukomperawat.utlis.Commons
import java.lang.IllegalArgumentException

class ArticleActivity : AppCompatActivity() {

    companion object{
        const val EXTRA_INDEX = "extra_index"
    }

    private lateinit var binding: ActivityArticleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.enterTransition = Explode()
        window.returnTransition = Explode()

        supportActionBar?.title = "Tips dan Trik"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        binding.advBanner.loadAd(adRequest)

        val index = intent.getIntExtra(EXTRA_INDEX, 0)

        val listImages = resources.obtainTypedArray(R.array.article_images)

        val title = resources.getStringArray(R.array.article_titles)[index]
        binding.tvTitle.text = title

        val article = getArticle(index)
        article.forEachIndexed { _, item ->
            if (item.contains("#")) {
                val imageIndex = item.substring(1, item.length).toInt()
                val imageButton = ImageButton(this)
                val images = listImages.getResourceId(imageIndex, -1)

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    500
                )
                params.setMargins(32,2,32,2)
                params.gravity = Gravity.CENTER_HORIZONTAL
                imageButton.apply{
                    layoutParams = params
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setImageResource(images)
                    setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                    setOnClickListener { Commons.zoomImageFromThumb(binding.container, imageButton, images, resources.getInteger(android.R.integer.config_shortAnimTime))
                    }
                }
                binding.containerArticle.addView(imageButton)

            } else{
                val strParagraph = if (item[1]==')' || item[1]=='.') item else "\t\t\t\t\t$item"
                val textView  = TextView(this)

                val font = ResourcesCompat.getFont(this, R.font.source_sans_regular)
                val params = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                params.setMargins(0,10,0,0)

                if(item[1]!='.' && item[1]!=')'){
                    textView.apply{
                        layoutParams = params
                        textSize = 18F
                        typeface = font
                        setTextColor(ContextCompat.getColor(context, R.color.colorDarkGray))
                        text = strParagraph
                    }
                    binding.containerArticle.addView(textView)
                } else {
                    val number = strParagraph.substring(0, 3)
                    val newParagraph = strParagraph.substring(3, strParagraph.length)

                    val textNumber  = TextView(this)
                    val linearLayout = LinearLayout(this)

                    val linParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )

                    if (number[1]=='.') {
                        linParams.setMargins(32,0,0,0)
                    } else {
                        linParams.setMargins(75,0,0,0)
                    }

                    linearLayout.apply {
                        layoutParams = linParams
                        addView(Commons.setTextViewAttributes(textNumber, params, font!!, number))
                        addView(Commons.setTextViewAttributes(textView, params, font, newParagraph))
                    }

                    binding.containerArticle.addView(linearLayout)
                }
            }
        }
    }

    private fun getArticle(index: Int): Array<String> {
        return when (index) {
            0 -> resources.getStringArray(R.array.article_1)
            1 -> resources.getStringArray(R.array.article_2)
            2 -> resources.getStringArray(R.array.article_3)
            3 -> resources.getStringArray(R.array.article_4)
            4 -> resources.getStringArray(R.array.article_5)
            5 -> resources.getStringArray(R.array.article_6)
            else -> throw IllegalArgumentException("Undefined Article")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}