package com.zulfahmi.simukomperawat.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.zulfahmi.simukomperawat.R
import com.zulfahmi.simukomperawat.adapter.RvAdapter
import com.zulfahmi.simukomperawat.utlis.MyApplication
import com.zulfahmi.simukomperawat.model.Chat
import com.zulfahmi.simukomperawat.network.ChatRequest
import com.zulfahmi.simukomperawat.utlis.*
import kotlinx.android.synthetic.main.activity_forum.*
import kotlinx.android.synthetic.main.navigation_layout.*

class ForumActivity : AppCompatActivity() {
    private lateinit var interstitialAd: InterstitialAd

    private var listChat = ArrayList<Chat>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forum)
        toolbar.title = "Forum"

        MobileAds.initialize(this) {}
        interstitialAd = InterstitialAd(this)
        interstitialAd.adUnitId = resources.getString(R.string.ad_inters2)
        interstitialAd.loadAd(AdRequest.Builder().build())

        rv_chat.setHasFixedSize(true)
        rv_chat.layoutManager = LinearLayoutManager(this)

        val user = PreferencesManager.initPreferences().getUserInfo()

        nav_user.text = user.username
        nav_email.text = user.email

        val toggle = ActionBarDrawerToggle(this, layout_drawer, toolbar, R.string.app_name, R.string.app_name )
        layout_drawer.addDrawerListener(toggle)
        toggle.syncState()

        input_message.addTextChangedListener(InputTextListener(btn_send))

        btn_send.setOnClickListener {
            val username = MyApplication.getInstance().getSharedPreferences().getString(Constants.PREF_USERNAME, "user") as String
            val message = input_message.text.toString()
            val time = Commons.getTime()

            val chat = Chat(
                user = username,
                message = message,
                time = time
            )

            ChatRequest.postMessage(chat)
            input_message.setText("")
            MyApplication.hideSoftInput(this, input_message)
        }

        btn_sign_out.setOnClickListener { signOut() }

        getChat()
    }

    private fun getChat() {
        ChatRequest.getChat(object : ChatRequest.OnChatRequest{
            override fun result(chat: Chat) {
                listChat.add(chat)

                if (listChat.size > 100)
                    listChat.removeAt(0)

                val chatAdapter = RvAdapter(listChat) { _, _ ->}
                chatAdapter.notifyDataSetChanged()

                rv_chat.adapter = chatAdapter
            }
        })
    }

    private fun signOut() {
        CustomConfirmDialog(this, "Sign Out", "Anda yakin ingin keluar?") {
            val auth = FirebaseAuth.getInstance()
            auth.signOut()

            startActivity(Intent(this, AuthActivity::class.java))
            if (interstitialAd.isLoaded) interstitialAd.show()
            finish()
        }.show()

        layout_drawer.closeDrawers()
    }
}
