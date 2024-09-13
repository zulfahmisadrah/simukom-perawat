package com.zulfahmi.simukomperawat.utlis

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import com.google.android.material.textfield.TextInputLayout

class InputTextListener (private val view: View) : TextWatcher {

    override fun afterTextChanged(s: Editable?) {    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        when (view) {
            is Button -> if (s?.length == 0) view.visibility = View.GONE else view.visibility = View.VISIBLE
            is TextInputLayout -> {
                view.apply {
                    error = ""
                    isErrorEnabled = false
                }
            }
        }
    }
}