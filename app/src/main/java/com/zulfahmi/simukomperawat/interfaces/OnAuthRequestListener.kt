package com.zulfahmi.simukomperawat.interfaces

interface OnAuthRequestListener{
    fun onAuthFailled(error: String?)
    fun onAuthSuccess()
}