package com.zulfahmi.simukomperawat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zulfahmi.simukomperawat.R
import com.zulfahmi.simukomperawat.utlis.MyApplication
import com.zulfahmi.simukomperawat.model.Article
import com.zulfahmi.simukomperawat.model.Chat
import com.zulfahmi.simukomperawat.utlis.Constants
import kotlinx.android.synthetic.main.item_article.view.*
import kotlinx.android.synthetic.main.item_chat.view.*
import kotlinx.android.synthetic.main.item_empty.view.*
import kotlinx.android.synthetic.main.item_pack.view.*

class RvAdapter(private val listData: List<Any>, private val listener: (Any, Int) -> Unit ): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val VIEW_TYPE_EMPTY = 0
    private val VIEW_TYPE_PACK = 1
    private val VIEW_TYPE_ARTICLE = 2
    private val VIEW_TYPE_CHAT = 3

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_PACK -> PackViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_pack, parent, false))
            VIEW_TYPE_ARTICLE -> ArticleViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_article, parent, false))
            VIEW_TYPE_CHAT -> ChatViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false))
            VIEW_TYPE_EMPTY -> EmptyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_empty, parent, false))
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

    class PackViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bindItem(item: String, listener: (Any, Int) -> Unit) {
            itemView.tv_paket.text = item

            itemView.setOnClickListener{
                listener(item, layoutPosition)
            }
        }
    }

    class ArticleViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bindItem(item: Article, listener: (Any, Int) -> Unit) {
            itemView.tv_title.text = item.title
            itemView.tv_content.text = item.firstParagraph

            itemView.setOnClickListener{
                listener(item, layoutPosition)
            }
        }
    }

    class ChatViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bindItem(item: Chat) {
            val user = item.user
            val currentUser = MyApplication.getInstance().getSharedPreferences().getString(Constants.PREF_USERNAME, "user")

            if (user == currentUser) {
                itemView.card_to.visibility = View.VISIBLE
                itemView.card_from.visibility = View.GONE

                itemView.tv_username_to.text = item.user
                itemView.tv_message_to.text = item.message
                itemView.tv_time_to.text = item.time
            } else {
                itemView.card_from.visibility = View.VISIBLE
                itemView.card_to.visibility = View.GONE

                itemView.tv_username_from.text = item.user
                itemView.tv_message_from.text = item.message
                itemView.tv_time_from.text = item.time
            }
        }
    }

    class EmptyViewHolder (itemView: View): RecyclerView.ViewHolder(itemView){
        fun bindItem(){
            itemView.tv_empty.text = "No data found"
        }
    }

}