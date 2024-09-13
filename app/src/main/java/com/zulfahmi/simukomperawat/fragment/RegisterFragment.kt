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
import kotlinx.android.synthetic.main.fragment_register.*

class RegisterFragment : Fragment() {
    private lateinit var loginPage: OnAuthPageListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        input_username.addTextChangedListener(InputTextListener(layout_username))
        input_email.addTextChangedListener(InputTextListener(layout_email))
        input_password.addTextChangedListener(InputTextListener(layout_password))
        input_password_confirm.addTextChangedListener(InputTextListener(layout_password_confirm))

        tv_sign_in.setOnClickListener { loginPage.onLoginPage() }
        btn_sign_up.setOnClickListener { view ->
            val username = input_username.text.toString()
            val email = input_email.text.toString()
            val password = input_password.text.toString()
            val passwordConfirm = input_password_confirm.text.toString()

            if (username.isEmpty()) {
                layout_username.error = "Masukkan username"
                input_username.requestFocus()
                return@setOnClickListener
            }

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

            if (password.length < 6) {
                layout_password.error = "Password minimal 6 karakter"
                input_password.requestFocus()
                return@setOnClickListener
            }

            if (passwordConfirm.isEmpty()) {
                layout_password_confirm.error = "Konfirmasi password Anda"
                input_password_confirm.requestFocus()
                return@setOnClickListener
            }

            if (passwordConfirm!=password) {
                layout_password_confirm.error = "Password tidak sama"
                input_password_confirm.requestFocus()
                return@setOnClickListener
            }

            MyApplication.hideSoftInput(requireContext(), input_username)
            view.isEnabled = false
            setInputTextEnabled(false)

            AuthRequest.field(username, email, passwordConfirm).registerUser(object : OnAuthRequestListener{
                override fun onAuthFailled(error: String?) {
                    view.isEnabled = true
                    setInputTextEnabled(true)

                    when (error) {
                        Constants.ERR_EMAIL_EXISTS -> {
                            layout_email.error = "Email yang Anda masukkan telah digunakan"
                            input_email.requestFocus()
                        }
                        else -> showSnackbar(error!!)
                    }
                }

                override fun onAuthSuccess() {
                    loginPage.onAuthenticateSuccess()
                }

            })
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        loginPage = context as OnAuthPageListener
    }

    fun setInputTextEnabled(state: Boolean) {
        layout_email.isEnabled = state
        layout_username.isEnabled = state
        layout_password.isEnabled = state
        layout_password_confirm.isEnabled = state

        if (state) progressbar.visibility = View.GONE else progressbar.visibility = View.VISIBLE
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(container, message, Snackbar.LENGTH_SHORT).show()
    }

}