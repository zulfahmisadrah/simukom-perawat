package com.zulfahmi.simukomperawat.network

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.zulfahmi.simukomperawat.utlis.MyApplication
import com.zulfahmi.simukomperawat.model.Chat

class ChatRequest {
    companion object{
        fun getChat(chatResult: OnChatRequest){
            val databaseReference = MyApplication.getFirebaseDatabaseReferences("chat")
            databaseReference.addChildEventListener(object : ChildEventListener {
                override fun onCancelled(p0: DatabaseError) {}

                override fun onChildMoved(p0: DataSnapshot, p1: String?) {}

                override fun onChildChanged(p0: DataSnapshot, p1: String?) {}

                override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                    val chat = dataSnapshot.getValue(Chat::class.java) as Chat
                    chatResult.result(chat)
                }

                override fun onChildRemoved(p0: DataSnapshot) {}
            })
        }

        fun postMessage(chat: Chat){
            val databaseReference = MyApplication.getFirebaseDatabaseReferences("chat")
            val chatKey = databaseReference.push().key as String
            databaseReference.child(chatKey).setValue(chat)
        }
    }

    interface OnChatRequest {
        fun result(chat: Chat)
    }
}