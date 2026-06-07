package com.zulfahmi.simukomperawat.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.MobileAds
import com.zulfahmi.simukomperawat.R
import com.zulfahmi.simukomperawat.ads.AdMobManager
import com.zulfahmi.simukomperawat.ads.NativeAdPlacementPolicy
import com.zulfahmi.simukomperawat.adapter.RvAdapter
import com.zulfahmi.simukomperawat.databinding.ActivityTipsBinding
import com.zulfahmi.simukomperawat.model.Article
import com.zulfahmi.simukomperawat.model.NativeAdItem
import com.zulfahmi.simukomperawat.utlis.Commons

class TipsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTipsBinding
    private val nativeAdPlacementPolicy = NativeAdPlacementPolicy()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTipsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Tips dan Trik"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        MobileAds.initialize(this) {}
        AdMobManager.loadAdaptiveBanner(this, binding.advBanner, R.string.ad_banner_tips)

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

        val monetizedArticleList = nativeAdPlacementPolicy.withNativeAdAfterIndex(
            listArticle,
            afterIndex = 1,
            placement = NativeAdItem.Placement.TIPS_FEED
        )

        val articleAdapter = RvAdapter(monetizedArticleList) { item, _ ->
            val articleIndex = listArticle.indexOf(item as Article)
            startActivity(Intent(this, ArticleActivity::class.java).putExtra(ArticleActivity.EXTRA_INDEX, articleIndex), Commons.setIntentTransition(this))
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
