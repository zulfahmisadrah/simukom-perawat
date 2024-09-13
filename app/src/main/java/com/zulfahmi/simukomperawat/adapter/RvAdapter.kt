package com.zulfahmi.simukomperawat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zulfahmi.simukomperawat.databinding.ItemArticleBinding
import com.zulfahmi.simukomperawat.databinding.ItemChatBinding
import com.zulfahmi.simukomperawat.databinding.ItemEmptyBinding
import com.zulfahmi.simukomperawat.databinding.ItemPackBinding
import com.zulfahmi.simukomperawat.utlis.MyApplication
import com.zulfahmi.simukomperawat.model.Article
import com.zulfahmi.simukomperawat.model.Chat
import com.zulfahmi.simukomperawat.utlis.Constants


class RvAdapter(private val listData: List<Any>, private val listener: (Any, Int) -> Unit ): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val VIEW_TYPE_EMPTY = 0
    private val VIEW_TYPE_PACK = 1
    private val VIEW_TYPE_ARTICLE = 2
    private val VIEW_TYPE_CHAT = 3

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_PACK -> PackViewHolder(ItemPackBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            VIEW_TYPE_ARTICLE -> ArticleViewHolder(ItemArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            VIEW_TYPE_CHAT -> ChatViewHolder(ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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
        }
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

    class EmptyViewHolder (private val itemBinding: ItemEmptyBinding): RecyclerView.ViewHolder(itemBinding.root){
        fun bindItem(){
            itemBinding.tvEmpty.text = "No data found"
        }
    }

}