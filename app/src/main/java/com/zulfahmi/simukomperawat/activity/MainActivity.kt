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
import com.zulfahmi.simukomperawat.utlis.Commons
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Commons.setFullscreenLayout(this)
        setContentView(R.layout.activity_main)

        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        adv_banner.loadAd(adRequest)

        btn_latihan.setOnClickListener(this)
        btn_simulasi.setOnClickListener(this)
        btn_tipstrick.setOnClickListener(this)
        btn_forum.setOnClickListener(this)
//        btn_kamusperawat.setOnClickListener(this)

        btn_send_questions.setOnClickListener(this)
        imgbtn_info.setOnClickListener(this)
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
