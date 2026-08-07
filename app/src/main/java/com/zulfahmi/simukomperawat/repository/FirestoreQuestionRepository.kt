package com.zulfahmi.simukomperawat.repository

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.zulfahmi.simukomperawat.database.AppDatabase
import com.zulfahmi.simukomperawat.model.LatihanPack
import com.zulfahmi.simukomperawat.model.PackAccessType
import java.util.concurrent.Executors

class FirestoreQuestionRepository(
    context: Context,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    private val database = AppDatabase.getDatabase(context.applicationContext)
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val remotePackKeyStore = RemotePackKeyStore(context.applicationContext)

    fun refreshPackage(
        type: String,
        pack: Int,
        firestorePackId: String,
        onReady: () -> Unit,
        onError: (String) -> Unit,
    ) {
        firestore.collection(QUESTIONS_COLLECTION)
            .whereEqualTo("packId", resolvedPackId(type, pack, firestorePackId))
            .get()
            .addOnSuccessListener { snapshot ->
                val remoteQuestions = try {
                    snapshot.documents.sortedBy { it.id }.map(::toRemoteQuestion)
                } catch (error: IllegalArgumentException) {
                    openCachedPackageOrReportError(type, pack, error.message ?: INVALID_DATA_MESSAGE, onReady, onError)
                    return@addOnSuccessListener
                }

                val roomQuestions = try {
                    FirestoreQuestionMapper.toRoomQuestions(remoteQuestions, type, pack)
                } catch (error: IllegalArgumentException) {
                    openCachedPackageOrReportError(type, pack, error.message ?: INVALID_DATA_MESSAGE, onReady, onError)
                    return@addOnSuccessListener
                }

                executor.execute {
                    try {
                        database.ukomDao().replaceQuestions(type, pack, roomQuestions)
                        mainHandler.post(onReady)
                    } catch (error: Exception) {
                        mainHandler.post { onError(error.message ?: SAVE_ERROR_MESSAGE) }
                    }
                }
            }
            .addOnFailureListener { error ->
                openCachedPackageOrReportError(type, pack, error.message ?: FETCH_ERROR_MESSAGE, onReady, onError)
            }
    }

    fun fetchPublishedLatihanPacks(
        onSuccess: (List<LatihanPack>) -> Unit,
        onError: (String) -> Unit,
    ) {
        firestore.collection(PACKS_COLLECTION)
            .whereEqualTo("type", "latihan")
            .whereEqualTo("isPublished", true)
            .get()
            .addOnSuccessListener { snapshot ->
                firestore.collection(CATEGORIES_COLLECTION)
                    .get()
                    .addOnSuccessListener { categorySnapshot ->
                        val categoryNames = categorySnapshot.documents.associate { document ->
                            document.id to document.getString("name").orEmpty()
                        }
                        val packs = snapshot.documents.mapNotNull { document ->
                            val packNumber = document.getLong("packNumber")?.toInt()
                                ?: return@mapNotNull null
                            if (packNumber <= 0) return@mapNotNull null

                            toLatihanPack(
                                firestoreId = document.id,
                                title = document.getString("title").orEmpty(),
                                categoryId = document.getString("categoryId").orEmpty(),
                                categoryName = categoryNames[document.getString("categoryId")].orEmpty(),
                                packNumber = packNumber,
                                accessType = document.getString("accessType"),
                                roomPack = remotePackKeyStore.roomPackFor(document.id),
                            )
                        }.sortedWith(compareBy<LatihanPack> { it.categoryName }.thenBy { it.title })
                        onSuccess(packs)
                    }
                    .addOnFailureListener { error -> onError(error.message ?: PACK_FETCH_ERROR_MESSAGE) }
            }
            .addOnFailureListener { error -> onError(error.message ?: PACK_FETCH_ERROR_MESSAGE) }
    }

    private fun openCachedPackageOrReportError(
        type: String,
        pack: Int,
        fallbackMessage: String,
        onReady: () -> Unit,
        onError: (String) -> Unit,
    ) {
        executor.execute {
            val hasCachedQuestions = database.ukomDao().countByTypeAndPack(type, pack) == FirestoreQuestionMapper.QUESTIONS_PER_PACK
            mainHandler.post {
                if (hasCachedQuestions) onReady() else onError(fallbackMessage)
            }
        }
    }

    private fun toRemoteQuestion(document: DocumentSnapshot): RemoteFirestoreQuestion {
        val content = document.get("content") as? Map<*, *>
            ?: throw IllegalArgumentException("Konten soal tidak tersedia.")
        val text = content["text"] as? String
            ?: throw IllegalArgumentException("Teks soal tidak tersedia.")
        val rawOptions = document.get("options") as? List<*>
            ?: throw IllegalArgumentException("Pilihan soal tidak tersedia.")
        val options = rawOptions.map { rawOption ->
            val option = rawOption as? Map<*, *>
                ?: throw IllegalArgumentException("Format pilihan soal tidak valid.")
            val id = option["id"] as? String
                ?: throw IllegalArgumentException("ID pilihan soal tidak tersedia.")
            val optionText = option["text"] as? String
                ?: throw IllegalArgumentException("Teks pilihan soal tidak tersedia.")
            RemoteFirestoreOption(id, optionText)
        }
        val correctOptionId = document.getString("correctOptionId")
            ?: throw IllegalArgumentException("Kunci jawaban tidak tersedia.")
        val explanation = (document.get("explanation") as? Map<*, *>)?.get("text") as? String ?: ""

        return RemoteFirestoreQuestion(text, options, correctOptionId, explanation)
    }

    companion object {
        fun toLatihanPack(
            firestoreId: String,
            title: String,
            categoryId: String,
            categoryName: String,
            packNumber: Int,
            accessType: String?,
            roomPack: Int,
        ): LatihanPack {
            val resolvedCategoryId = categoryId.ifBlank { "uncategorized" }
            return LatihanPack.remote(
                roomPack = roomPack,
                firestoreId = firestoreId,
                title = title.ifBlank { "Paket $packNumber" },
                categoryId = resolvedCategoryId,
                categoryName = categoryName.ifBlank { "Kategori Lainnya" },
                displayNumber = packNumber,
                accessType = PackAccessType.fromWireValue(accessType),
            )
        }

        fun firestorePackId(type: String, pack: Int): String {
            require(type == "latihan" || type == "simulasi")
            require(pack > 0)
            return "legacy_${type}_paket_$pack"
        }

        fun resolvedPackId(type: String, pack: Int, firestorePackId: String): String {
            firestorePackId(type, pack)
            require(firestorePackId.isNotBlank())
            return firestorePackId
        }

        private const val PACKS_COLLECTION = "packs"
        private const val CATEGORIES_COLLECTION = "categories"
        private const val QUESTIONS_COLLECTION = "questions"
        private const val PACK_FETCH_ERROR_MESSAGE = "Tidak dapat memuat paket dari server."
        private const val INVALID_DATA_MESSAGE = "Data soal dari server tidak valid."
        private const val FETCH_ERROR_MESSAGE = "Tidak dapat mengunduh soal dari server."
        private const val SAVE_ERROR_MESSAGE = "Tidak dapat menyimpan soal ke perangkat."
    }
}
