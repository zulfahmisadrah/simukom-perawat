package com.zulfahmi.simukomperawat.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.zulfahmi.simukomperawat.R
import com.zulfahmi.simukomperawat.adapter.RvAdapter
import com.zulfahmi.simukomperawat.databinding.ActivityTipsBinding
import com.zulfahmi.simukomperawat.model.Article
import com.zulfahmi.simukomperawat.utlis.Commons

class TipsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTipsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTipsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Tips dan Trik"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        binding.advBanner.loadAd(adRequest)

        val listTitle = resources.getStringArray(R.array.article_titles)
        val listFirstParagraph = resources.getStringArray(R.array.article_first_paragraph)

        val listArticle = ArrayList<Article>()
        for (i in 0..listTitle.lastIndex) {
            val article = Article(
                title = listTitle[i],
                firstParagraph = listFirstParagraph[i]
            )
            listArticle.add(article)
        }

        val articleAdapter = RvAdapter(listArticle) { _, position ->
            startActivity(Intent(this, ArticleActivity::class.java).putExtra(ArticleActivity.EXTRA_INDEX, position), Commons.setIntentTransition(this))
        }

        binding.recyclerview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = articleAdapter
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
