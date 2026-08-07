package com.zulfahmi.simukomperawat.repository

import android.content.Context

class RemotePackKeyAllocator(
    private val keys: MutableMap<String, Int> = mutableMapOf(),
    firstKey: Int = FIRST_REMOTE_ROOM_PACK,
) {
    private var nextKey = firstKey

    fun allocate(firestoreId: String): Int = keys.getOrPut(firestoreId) { nextKey++ }

    companion object {
        const val FIRST_REMOTE_ROOM_PACK = 10_000
    }
}

class RemotePackKeyStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    @Synchronized
    fun roomPackFor(firestoreId: String): Int {
        val existing = preferences.getInt(firestoreId, 0)
        if (existing != 0) return existing

        val allocated = preferences.getInt(
            NEXT_KEY,
            RemotePackKeyAllocator.FIRST_REMOTE_ROOM_PACK,
        )
        preferences.edit()
            .putInt(firestoreId, allocated)
            .putInt(NEXT_KEY, allocated + 1)
            .commit()
        return allocated
    }

    private companion object {
        const val PREFERENCES_NAME = "remote_pack_keys"
        const val NEXT_KEY = "next_key"
    }
}
