package com.zulfahmi.simukomperawat.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.zulfahmi.simukomperawat.R
import com.zulfahmi.simukomperawat.databinding.ItemArticleBinding
import com.zulfahmi.simukomperawat.databinding.ItemChatBinding
import com.zulfahmi.simukomperawat.databinding.ItemEmptyBinding
import com.zulfahmi.simukomperawat.databinding.ItemNativeAdBinding
import com.zulfahmi.simukomperawat.databinding.ItemPackBinding
import com.zulfahmi.simukomperawat.utlis.MyApplication
import com.zulfahmi.simukomperawat.model.Article
import com.zulfahmi.simukomperawat.model.Chat
import com.zulfahmi.simukomperawat.model.NativeAdItem
import com.zulfahmi.simukomperawat.utlis.Constants


class RvAdapter(private val listData: List<Any>, private val listener: (Any, Int) -> Unit ): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val TAG = "RvAdapter"
    }

    private val VIEW_TYPE_EMPTY = 0
    private val VIEW_TYPE_PACK = 1
    private val VIEW_TYPE_ARTICLE = 2
    private val VIEW_TYPE_CHAT = 3
    private val VIEW_TYPE_NATIVE_AD = 4

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_PACK -> PackViewHolder(ItemPackBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            VIEW_TYPE_ARTICLE -> ArticleViewHolder(ItemArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            VIEW_TYPE_CHAT -> ChatViewHolder(ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            VIEW_TYPE_NATIVE_AD -> NativeAdViewHolder(ItemNativeAdBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            VIEW_TYPE_EMPTY -> EmptyViewHolder(ItemEmptyBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> throw IllegalArgumentException("Undefined view type")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (listData.isEmpty())
            VIEW_TYPE_EMPTY
        else{
            when (listData[position]) {
                is String -> VIEW_TYPE_PACK
                is Article -> VIEW_TYPE_ARTICLE
                is Chat -> VIEW_TYPE_CHAT
                is NativeAdItem -> VIEW_TYPE_NATIVE_AD
                else -> throw IllegalArgumentException("Undefined type")
            }
        }
    }


    override fun getItemCount(): Int = if (listData.isEmpty()) 1 else listData.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            VIEW_TYPE_EMPTY -> {
                val emptyHolder = holder as EmptyViewHolder
                emptyHolder.bindItem()
            }
            VIEW_TYPE_PACK -> {
                val packHolder = holder as PackViewHolder
                packHolder.bindItem(listData[position] as String, listener)
            }
            VIEW_TYPE_ARTICLE -> {
                val articleHolder = holder as ArticleViewHolder
                articleHolder.bindItem(listData[position] as Article, listener)
            }
            VIEW_TYPE_CHAT -> {
                val chatHolder = holder as ChatViewHolder
                chatHolder.bindItem(listData[position] as Chat)
            }
            VIEW_TYPE_NATIVE_AD -> {
                val nativeAdHolder = holder as NativeAdViewHolder
                nativeAdHolder.bindItem(listData[position] as NativeAdItem)
            }
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is NativeAdViewHolder) {
            holder.destroyNativeAd()
        }
        super.onViewRecycled(holder)
    }

    class PackViewHolder(private val itemBinding: ItemPackBinding): RecyclerView.ViewHolder(itemBinding.root) {
        fun bindItem(item: String, listener: (Any, Int) -> Unit) {
            itemBinding.tvPaket.text = item

            itemBinding.root.setOnClickListener{
                listener(item, layoutPosition)
            }
        }
    }

    class ArticleViewHolder(private val itemBinding: ItemArticleBinding): RecyclerView.ViewHolder(itemBinding.root) {
        fun bindItem(item: Article, listener: (Any, Int) -> Unit) {
            itemBinding.tvTitle.text = item.title
            itemBinding.tvContent.text = item.firstParagraph

            itemBinding.root.setOnClickListener{
                listener(item, layoutPosition)
            }
        }
    }

    class ChatViewHolder(private val itemBinding: ItemChatBinding): RecyclerView.ViewHolder(itemBinding.root) {
        fun bindItem(item: Chat) {
            val user = item.user
            val currentUser = MyApplication.getInstance().getSharedPreferences().getString(Constants.PREF_USERNAME, "user")

            if (user == currentUser) {
                itemBinding.cardTo.visibility = View.VISIBLE
                itemBinding.cardFrom.visibility = View.GONE

                itemBinding.tvUsernameTo.text = item.user
                itemBinding.tvMessageTo.text = item.message
                itemBinding.tvTimeTo.text = item.time
            } else {
                itemBinding.cardFrom.visibility = View.VISIBLE
                itemBinding.cardTo.visibility = View.GONE

                itemBinding.tvUsernameFrom.text = item.user
                itemBinding.tvMessageFrom.text = item.message
                itemBinding.tvTimeFrom.text = item.time
            }
        }
    }

    class NativeAdViewHolder(private val itemBinding: ItemNativeAdBinding): RecyclerView.ViewHolder(itemBinding.root) {
        private var nativeAd: NativeAd? = null

        fun bindItem(item: NativeAdItem) {
            itemBinding.nativeAdCard.visibility = View.GONE
            destroyNativeAd()

            val context = itemBinding.root.context
            val adUnitId = when (item.placement) {
                NativeAdItem.Placement.TIPS_FEED -> context.getString(R.string.ad_native_tips_feed)
                NativeAdItem.Placement.FORUM_FEED -> context.getString(R.string.ad_native_forum_feed)
            }

            AdLoader.Builder(context, adUnitId)
                .forNativeAd { loadedNativeAd ->
                    nativeAd = loadedNativeAd
                    populateNativeAdView(loadedNativeAd)
                    itemBinding.nativeAdCard.visibility = View.VISIBLE
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.d(TAG, adError.toString())
                        itemBinding.nativeAdCard.visibility = View.GONE
                    }
                })
                .build()
                .loadAd(AdRequest.Builder().build())
        }

        fun destroyNativeAd() {
            nativeAd?.destroy()
            nativeAd = null
        }

        private fun populateNativeAdView(ad: NativeAd) {
            itemBinding.nativeAdView.headlineView = itemBinding.adHeadline
            itemBinding.nativeAdView.bodyView = itemBinding.adBody
            itemBinding.nativeAdView.callToActionView = itemBinding.adCallToAction

            itemBinding.adHeadline.text = ad.headline

            itemBinding.adBody.visibility = if (ad.body == null) View.GONE else View.VISIBLE
            itemBinding.adBody.text = ad.body

            itemBinding.adCallToAction.visibility = if (ad.callToAction == null) View.GONE else View.VISIBLE
            itemBinding.adCallToAction.text = ad.callToAction

            itemBinding.nativeAdView.setNativeAd(ad)
        }
    }

    class EmptyViewHolder (private val itemBinding: ItemEmptyBinding): RecyclerView.ViewHolder(itemBinding.root){
        fun bindItem(){
            itemBinding.tvEmpty.text = "No data found"
        }
    }

}
