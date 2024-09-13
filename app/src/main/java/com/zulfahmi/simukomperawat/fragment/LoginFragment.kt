package com.zulfahmi.simukomperawat.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import com.zulfahmi.simukomperawat.databinding.FragmentLoginBinding
import com.zulfahmi.simukomperawat.interfaces.OnAuthPageListener
import com.zulfahmi.simukomperawat.interfaces.OnAuthRequestListener
import com.zulfahmi.simukomperawat.utlis.MyApplication
import com.zulfahmi.simukomperawat.network.AuthRequest
import com.zulfahmi.simukomperawat.utlis.Commons
import com.zulfahmi.simukomperawat.utlis.Constants
import com.zulfahmi.simukomperawat.utlis.InputTextListener

class LoginFragment : Fragment() {
    private lateinit var registerPage: OnAuthPageListener

    private var _binding: FragmentLoginBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.inputEmail.addTextChangedListener(InputTextListener(binding.layoutEmail))
        binding.inputPassword.addTextChangedListener(InputTextListener(binding.layoutPassword))

        binding.tvSignUp.setOnClickListener { registerPage.onRegisterPage() }
        binding.btnSignIn.setOnClickListener {
            val email = binding.inputEmail.text.toString()
            val password = binding.inputPassword.text.toString()

            if (email.isEmpty()) {
                binding.layoutEmail.error = "Masukkan email"
                binding.inputEmail.requestFocus()
                return@setOnClickListener
            }

            if (!Commons.isEmailValid(email)) {
                binding.layoutEmail.error = "Email tidak valid"
                binding.inputEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.layoutPassword.error = "Masukkan password"
                binding.inputPassword.requestFocus()
                return@setOnClickListener
            }

            MyApplication.hideSoftInput(requireContext(), binding.inputEmail)

            view.isEnabled = false
            setInputTextEnabled(false)

            AuthRequest.field(email, password).loginUser(object : OnAuthRequestListener{
                override fun onAuthFailled(error: String?) {
                    view.isEnabled = true
                    setInputTextEnabled(true)

                    when (error) {
                        Constants.ERR_EMAIL_NOT_EXISTS -> {
                            binding.layoutEmail.error = "Email belum terdaftar"
                            binding.inputEmail.requestFocus()
                        }
                        Constants.ERR_PASS -> {
                            binding.layoutPassword.error = "Password salah"
                            binding.inputPassword.requestFocus()
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
        binding.layoutEmail.isEnabled = state
        binding.layoutPassword.isEnabled = state

        if (state) binding.progressbar.visibility = View.GONE else binding.progressbar.visibility = View.VISIBLE
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.container, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        AuthRequest.removeSignInListener()
    }
}