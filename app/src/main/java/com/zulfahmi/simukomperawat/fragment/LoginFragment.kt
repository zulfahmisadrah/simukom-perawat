package com.zulfahmi.simukomperawat.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import com.zulfahmi.simukomperawat.R
import com.zulfahmi.simukomperawat.interfaces.OnAuthPageListener
import com.zulfahmi.simukomperawat.interfaces.OnAuthRequestListener
import com.zulfahmi.simukomperawat.utlis.MyApplication
import com.zulfahmi.simukomperawat.network.AuthRequest
import com.zulfahmi.simukomperawat.utlis.Commons
import com.zulfahmi.simukomperawat.utlis.Constants
import com.zulfahmi.simukomperawat.utlis.InputTextListener
import kotlinx.android.synthetic.main.fragment_login.*

class LoginFragment : Fragment() {
    private lateinit var registerPage: OnAuthPageListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        input_email.addTextChangedListener(InputTextListener(layout_email))
        input_password.addTextChangedListener(InputTextListener(layout_password))

        tv_sign_up.setOnClickListener { registerPage.onRegisterPage() }
        btn_sign_in.setOnClickListener {
            val email = input_email.text.toString()
            val password = input_password.text.toString()

            if (email.isEmpty()) {
                layout_email.error = "Masukkan email"
                input_email.requestFocus()
                return@setOnClickListener
            }

            if (!Commons.isEmailValid(email)) {
                layout_email.error = "Email tidak valid"
                input_email.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                layout_password.error = "Masukkan password"
                input_password.requestFocus()
                return@setOnClickListener
            }

            MyApplication.hideSoftInput(requireContext(), input_email)

            view.isEnabled = false
            setInputTextEnabled(false)

            AuthRequest.field(email, password).loginUser(object : OnAuthRequestListener{
                override fun onAuthFailled(error: String?) {
                    view.isEnabled = true
                    setInputTextEnabled(true)

                    when (error) {
                        Constants.ERR_EMAIL_NOT_EXISTS -> {
                            layout_email.error = "Email belum terdaftar"
                            input_email.requestFocus()
                        }
                        Constants.ERR_PASS -> {
                            layout_password.error = "Password salah"
                            input_password.requestFocus()
                        }
                        else -> showSnackbar(error!!)
                    }
                }

                override fun onAuthSuccess() {
                    registerPage.onAuthenticateSuccess()
                }

            })
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        registerPage = context as OnAuthPageListener
    }

    fun setInputTextEnabled(state: Boolean) {
        layout_email.isEnabled = state
        layout_password.isEnabled = state

        if (state) progressbar.visibility = View.GONE else progressbar.visibility = View.VISIBLE
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(container, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        AuthRequest.removeSignInListener()
    }
}