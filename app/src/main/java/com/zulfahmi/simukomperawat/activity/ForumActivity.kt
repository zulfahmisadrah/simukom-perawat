package com.zulfahmi.simukomperawat.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.auth.FirebaseAuth
import com.zulfahmi.simukomperawat.R
import com.zulfahmi.simukomperawat.adapter.RvAdapter
import com.zulfahmi.simukomperawat.databinding.ActivityForumBinding
import com.zulfahmi.simukomperawat.databinding.NavigationLayoutBinding
import com.zulfahmi.simukomperawat.utlis.MyApplication
import com.zulfahmi.simukomperawat.model.Chat
import com.zulfahmi.simukomperawat.network.ChatRequest
import com.zulfahmi.simukomperawat.utlis.*


class ForumActivity : AppCompatActivity() {
    companion object {
        const val TAG = "ForumActivity"
    }

    private lateinit var binding: ActivityForumBinding
    private lateinit var navLayoutBinding: NavigationLayoutBinding

    private var mInterstitialAd: InterstitialAd? = null
    private var listChat = ArrayList<Chat>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForumBinding.inflate(layoutInflater)
        navLayoutBinding = binding.layoutNavigation
        setContentView(binding.root)
        binding.toolbar.title = "Forum"

        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this, resources.getString(R.string.ad_inters2), adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError.toString())
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "Ad was loaded.")
                mInterstitialAd = interstitialAd
            }
        })

        binding.rvChat.setHasFixedSize(true)
        binding.rvChat.layoutManager = LinearLayoutManager(this)

        val user = PreferencesManager.initPreferences().getUserInfo()

        navLayoutBinding.navUser.text = user.username
        navLayoutBinding.navEmail.text = user.email

        val toggle = ActionBarDrawerToggle(this, binding.layoutDrawer, binding.toolbar, R.string.app_name, R.string.app_name )
        binding.layoutDrawer.addDrawerListener(toggle)
        toggle.syncState()

        binding.inputMessage.addTextChangedListener(InputTextListener(binding.btnSend))

        binding.btnSend.setOnClickListener {
            val username = MyApplication.getInstance().getSharedPreferences().getString(Constants.PREF_USERNAME, "user") as String
            val message = binding.inputMessage.text.toString()
            val time = Commons.getTime()

            val chat = Chat(
                user = username,
                message = message,
                time = time
            )

            ChatRequest.postMessage(chat)
            binding.inputMessage.setText("")
            MyApplication.hideSoftInput(this, binding.inputMessage)
        }

        navLayoutBinding.btnSignOut.setOnClickListener { signOut() }

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

                binding.rvChat.adapter = chatAdapter
            }
        })
    }

    private fun signOut() {
        CustomConfirmDialog(this, "Sign Out", "Anda yakin ingin keluar?") {
            val auth = FirebaseAuth.getInstance()
            auth.signOut()

            startActivity(Intent(this, AuthActivity::class.java))
            if (mInterstitialAd != null) mInterstitialAd?.show(this)
            finish()
        }.show()

        binding.layoutDrawer.closeDrawers()
    }
}
