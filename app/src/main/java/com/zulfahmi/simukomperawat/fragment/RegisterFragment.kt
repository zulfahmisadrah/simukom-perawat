package com.zulfahmi.simukomperawat.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import com.zulfahmi.simukomperawat.databinding.FragmentRegisterBinding
import com.zulfahmi.simukomperawat.interfaces.OnAuthPageListener
import com.zulfahmi.simukomperawat.interfaces.OnAuthRequestListener
import com.zulfahmi.simukomperawat.utlis.MyApplication
import com.zulfahmi.simukomperawat.network.AuthRequest
import com.zulfahmi.simukomperawat.utlis.Commons
import com.zulfahmi.simukomperawat.utlis.Constants
import com.zulfahmi.simukomperawat.utlis.InputTextListener

class RegisterFragment : Fragment() {
    private lateinit var loginPage: OnAuthPageListener

    private var _binding: FragmentRegisterBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.inputUsername.addTextChangedListener(InputTextListener(binding.layoutUsername))
        binding.inputEmail.addTextChangedListener(InputTextListener(binding.layoutEmail))
        binding.inputPassword.addTextChangedListener(InputTextListener(binding.layoutPassword))
        binding.inputPasswordConfirm.addTextChangedListener(InputTextListener(binding.layoutPasswordConfirm))

        binding.tvSignIn.setOnClickListener { loginPage.onLoginPage() }
        binding.btnSignUp.setOnClickListener { view ->
            val username = binding.inputUsername.text.toString()
            val email = binding.inputEmail.text.toString()
            val password = binding.inputPassword.text.toString()
            val passwordConfirm = binding.inputPasswordConfirm.text.toString()

            if (username.isEmpty()) {
                binding.layoutUsername.error = "Masukkan username"
                binding.inputUsername.requestFocus()
                return@setOnClickListener
            }

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

            if (password.length < 6) {
                binding.layoutPassword.error = "Password minimal 6 karakter"
                binding.inputPassword.requestFocus()
                return@setOnClickListener
            }

            if (passwordConfirm.isEmpty()) {
                binding.layoutPasswordConfirm.error = "Konfirmasi password Anda"
                binding.inputPasswordConfirm.requestFocus()
                return@setOnClickListener
            }

            if (passwordConfirm!=password) {
                binding.layoutPasswordConfirm.error = "Password tidak sama"
                binding.inputPasswordConfirm.requestFocus()
                return@setOnClickListener
            }

            MyApplication.hideSoftInput(requireContext(), binding.inputUsername)
            view.isEnabled = false
            setInputTextEnabled(false)

            AuthRequest.field(username, email, passwordConfirm).registerUser(object : OnAuthRequestListener{
                override fun onAuthFailled(error: String?) {
                    view.isEnabled = true
                    setInputTextEnabled(true)

                    when (error) {
                        Constants.ERR_EMAIL_EXISTS -> {
                            binding.layoutEmail.error = "Email yang Anda masukkan telah digunakan"
                            binding.inputEmail.requestFocus()
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
        binding.layoutEmail.isEnabled = state
        binding.layoutUsername.isEnabled = state
        binding.layoutPassword.isEnabled = state
        binding.layoutPasswordConfirm.isEnabled = state

        if (state) binding.progressbar.visibility = View.GONE else binding.progressbar.visibility = View.VISIBLE
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.container, message, Snackbar.LENGTH_SHORT).show()
    }

}