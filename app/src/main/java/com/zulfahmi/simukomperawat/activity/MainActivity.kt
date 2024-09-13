package com.zulfahmi.simukomperawat.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.zulfahmi.simukomperawat.R
import com.zulfahmi.simukomperawat.databinding.ActivityMainBinding
import com.zulfahmi.simukomperawat.utlis.Commons

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Commons.setFullscreenLayout(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        binding.advBanner.loadAd(adRequest)

        binding.btnLatihan.setOnClickListener(this)
        binding.btnSimulasi.setOnClickListener(this)
        binding.btnTipstrick.setOnClickListener(this)
        binding.btnForum.setOnClickListener(this)
//        btn_kamusperawat.setOnClickListener(this)

        binding.btnSendQuestions.setOnClickListener(this)
        binding.imgbtnInfo.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_latihan -> startActivity(Intent(this, PackActivity::class.java).putExtra(PackActivity.EXTRA_QUESTION_TYPE, "latihan"), Commons.setIntentTransition(this))
            R.id.btn_simulasi -> startActivity(Intent(this, PackActivity::class.java).putExtra(PackActivity.EXTRA_QUESTION_TYPE, "simulasi"), Commons.setIntentTransition(this))
            R.id.btn_tipstrick -> startActivity(Intent(this, TipsActivity::class.java))
            R.id.btn_forum -> {
                val auth = FirebaseAuth.getInstance()
                if (auth.currentUser != null)
                    startActivity(Intent(this, ForumActivity::class.java))
                else
                    startActivity(Intent(this, AuthActivity::class.java))
            }
//            R.id.btn_kamusperawat -> startActivity(Intent(this, TipsActivity::class.java))
            R.id.imgbtn_info -> {
                val message = getString(R.string.info)
                val formattedMessage = SpannableString(message)
                formattedMessage.setSpan(StyleSpan(Typeface.BOLD), 0, 15, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                formattedMessage.setSpan(StyleSpan(Typeface.BOLD), message.indexOf("Email"), message.indexOf("Email")+5, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                Commons.showAlertDialog(this, formattedMessage)
            }
            R.id.btn_send_questions -> {
                Commons.showAlertDialog(this, getString(R.string.open_email_alert_message), true){
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "text/plain"
                    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("abd.rahmanrara@gmail.com", "cbs.fahmi@gmail.com"))
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Soal Ukom Perawat")
                    intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_content))
                    try {
                        startActivity(Intent.createChooser(intent, "Kirim soal melalui email..."))
                    } catch (ex: ActivityNotFoundException) {
                        //do something else
                    }
                }
            }
        }
    }

}
