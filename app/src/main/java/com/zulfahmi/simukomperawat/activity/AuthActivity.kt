package com.zulfahmi.simukomperawat.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentTransaction
import com.zulfahmi.simukomperawat.R
import com.zulfahmi.simukomperawat.databinding.ActivityAuthBinding
import com.zulfahmi.simukomperawat.interfaces.OnAuthPageListener
import com.zulfahmi.simukomperawat.fragment.LoginFragment
import com.zulfahmi.simukomperawat.fragment.RegisterFragment

class AuthActivity : AppCompatActivity(), OnAuthPageListener{
    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null)
            supportFragmentManager.beginTransaction().replace(R.id.container_auth , LoginFragment()).commit()

        binding.imgbtnBack.setOnClickListener { onBackPressed() }

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