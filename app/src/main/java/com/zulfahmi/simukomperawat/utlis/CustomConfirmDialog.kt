package com.zulfahmi.simukomperawat.utlis

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import com.zulfahmi.simukomperawat.databinding.DialogConfirmBinding

class CustomConfirmDialog(context: Context, private val title: String, private val message: String, private val isCancelable: Boolean = true, private var btnPositiveText: String ="Ya", private var btnNegativeText: String = "Tidak", private val yesAction: () -> Unit): Dialog(context){
    private lateinit var binding: DialogConfirmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(false)
        binding = DialogConfirmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setCanceledOnTouchOutside(isCancelable)

        binding.tvTitle.text = title
        binding.tvMessage.text = message
        binding.btnNo.text = btnNegativeText
        binding.btnYes.text = btnPositiveText
        binding.btnNo.setOnClickListener { dismiss() }
        binding.btnYes.setOnClickListener {
            yesAction()
            dismiss()
        }
    }
}