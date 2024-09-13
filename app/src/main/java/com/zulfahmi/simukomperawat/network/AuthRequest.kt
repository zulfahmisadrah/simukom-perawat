package com.zulfahmi.simukomperawat.network

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.zulfahmi.simukomperawat.interfaces.OnAuthRequestListener
import com.zulfahmi.simukomperawat.utlis.MyApplication
import com.zulfahmi.simukomperawat.model.User
import com.zulfahmi.simukomperawat.utlis.Commons
import com.zulfahmi.simukomperawat.utlis.PreferencesManager

class AuthRequest {
    companion object{
        private lateinit var databaseReference: DatabaseReference
        private var valueEventListener: ValueEventListener? = null

        private var username = ""
        private var email = ""
        private var password = ""

        fun field(username: String, email: String, password: String): AuthRequest {
            this.username = username
            this.email = email
            this.password = password
            return AuthRequest()
        }

        fun field(email: String, password: String): AuthRequest {
            this.email = email
            this.password = password
            return AuthRequest()
        }

        fun removeSignInListener() {
            if (valueEventListener!=null)
                databaseReference.removeEventListener(valueEventListener!!)
        }
    }

    fun registerUser(listener: OnAuthRequestListener) {
        val auth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                val userId = it.result?.user?.uid as String
                val user = User(
                    username = username,
                    email = email,
                    userId = userId,
                    loginTime = Commons.getTime()
                )

                PreferencesManager.initPreferences().setUserInfo(user)
                MyApplication.getFirebaseDatabaseReferences("user").child(userId).setValue(user)

                listener.onAuthSuccess()
            }else {
                listener.onAuthFailled(it.exception?.message)
                Log.d("RegisterRequest", "onComplete: " + it.exception?.message)
            }
        }
    }

    fun loginUser(listener: OnAuthRequestListener) {
        val auth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener{
            if (it.isSuccessful) {
                val userId = it.result?.user?.uid as String

                databaseReference = MyApplication.getFirebaseDatabaseReferences("user").child(userId)
                databaseReference.addListenerForSingleValueEvent(valueEventListener(listener, userId))
            } else
                listener.onAuthFailled(it.exception?.message)
        }
    }

    private fun valueEventListener(listener: OnAuthRequestListener, userId: String): ValueEventListener {
        return object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) { }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)

                if (user != null) {
                    user.loginTime = Commons.getTime()
                    MyApplication.getFirebaseDatabaseReferences("user").child(userId).setValue(user)
                    PreferencesManager.initPreferences().setUserInfo(user)

                    listener.onAuthSuccess()
                }
            }
        }
    }

}