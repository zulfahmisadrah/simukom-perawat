package com.zulfahmi.simukomperawat.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentTransaction
import com.zulfahmi.simukomperawat.R
import com.zulfahmi.simukomperawat.interfaces.OnAuthPageListener
import com.zulfahmi.simukomperawat.fragment.LoginFragment
import com.zulfahmi.simukomperawat.fragment.RegisterFragment
import kotlinx.android.synthetic.main.activity_auth.*

class AuthActivity : AppCompatActivity(), OnAuthPageListener{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        if (savedInstanceState == null)
            supportFragmentManager.beginTransaction().replace(R.id.container_auth , LoginFragment()).commit()

        imgbtn_back.setOnClickListener { onBackPressed() }

    }

    override fun onLoginPage() {
        supportFragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).replace(R.id.container_auth , LoginFragment()).commit()
    }

    override fun onRegisterPage() {
        supportFragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).replace(R.id.container_auth , RegisterFragment()).commit()
    }

    override fun onAuthenticateSuccess() {
        startActivity(Intent(this, ForumActivity::class.java))
        finish()
    }
}