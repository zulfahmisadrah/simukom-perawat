package com.zulfahmi.simukomperawat.repository

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.zulfahmi.simukomperawat.database.AppDatabase
import com.zulfahmi.simukomperawat.model.ExperimentalPack
import java.util.concurrent.Executors

class ExperimentalPackRepository(
    context: Context,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    private val database = AppDatabase.getDatabase(context.applicationContext)
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun fetchPacks(
        onSuccess: (List<ExperimentalPack>) -> Unit,
        onError: (String) -> Unit,
    ) {
        firestore.collection(PACKS_COLLECTION)
            .whereEqualTo("type", ExperimentalQuestionMapper.TYPE)
            .whereEqualTo("isPublished", true)
            .get()
            .addOnSuccessListener { snapshot ->
                onSuccess(
                    snapshot.documents.mapNotNull(::toExperimentalPack)
                        .sortedWith(compareBy<ExperimentalPack> { it.packNumber }.thenBy { it.id }),
                )
            }
            .addOnFailureListener { error -> onError(error.message ?: "Tidak dapat memuat paket Experimental.") }
    }

    fun syncPack(
        pack: ExperimentalPack,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        firestore.collection(QUESTIONS_COLLECTION)
            .whereEqualTo("packId", pack.id)
            .get()
            .addOnSuccessListener { snapshot ->
                val remoteQuestions = try {
                    snapshot.documents.sortedBy { it.id }.map(::toRemoteQuestion)
                } catch (error: IllegalArgumentException) {
                    openCachedPackOrReportError(
                        pack.roomPackNumber,
                        error.message ?: "Data soal Experimental tidak valid.",
                        onSuccess,
                        onError,
                    )
                    return@addOnSuccessListener
                }

                val roomQuestions = try {
                    ExperimentalQuestionMapper.toRoomQuestions(remoteQuestions, pack.roomPackNumber)
                } catch (error: IllegalArgumentException) {
                    openCachedPackOrReportError(
                        pack.roomPackNumber,
                        error.message ?: "Paket Experimental harus berisi 20 soal valid.",
                        onSuccess,
                        onError,
                    )
                    return@addOnSuccessListener
                }

                executor.execute {
                    try {
                        database.ukomDao().replaceQuestions(
                            ExperimentalQuestionMapper.TYPE,
                            pack.roomPackNumber,
                            roomQuestions,
                        )
                        mainHandler.post(onSuccess)
                    } catch (error: Exception) {
                        mainHandler.post {
                            onError(error.message ?: "Tidak dapat menyimpan paket Experimental.")
                        }
                    }
                }
            }
            .addOnFailureListener { error ->
                openCachedPackOrReportError(
                    pack.roomPackNumber,
                    error.message ?: "Tidak dapat memuat soal Experimental.",
                    onSuccess,
                    onError,
                )
            }
    }

    private fun openCachedPackOrReportError(
        roomPack: Int,
        fallbackMessage: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        executor.execute {
            val hasCachedQuestions = database.ukomDao().countByTypeAndPack(
                ExperimentalQuestionMapper.TYPE,
                roomPack,
            ) == 20
            mainHandler.post {
                if (hasCachedQuestions) onSuccess() else onError(fallbackMessage)
            }
        }
    }

    private fun toExperimentalPack(document: DocumentSnapshot): ExperimentalPack? {
        val title = document.getString("title")?.trim().orEmpty()
        val packNumber = document.getLong("packNumber")
        return if (title.isBlank() || packNumber == null) null
        else ExperimentalPack(document.id, title, packNumber)
    }

    private fun toRemoteQuestion(document: DocumentSnapshot): RemoteExperimentalQuestion {
        val content = document.get("content") as? Map<*, *>
            ?: throw IllegalArgumentException("Konten soal Experimental tidak tersedia.")
        val text = content["text"] as? String
            ?: throw IllegalArgumentException("Teks soal Experimental tidak tersedia.")
        val rawOptions = document.get("options") as? List<*>
            ?: throw IllegalArgumentException("Pilihan soal Experimental tidak tersedia.")
        val options = rawOptions.map { rawOption ->
            val option = rawOption as? Map<*, *>
                ?: throw IllegalArgumentException("Format pilihan soal Experimental tidak valid.")
            val id = option["id"] as? String
                ?: throw IllegalArgumentException("ID pilihan soal Experimental tidak tersedia.")
            val optionText = option["text"] as? String
                ?: throw IllegalArgumentException("Teks pilihan soal Experimental tidak tersedia.")
            RemoteOption(id, optionText)
        }
        val correctOptionId = document.getString("correctOptionId")
            ?: throw IllegalArgumentException("Kunci jawaban Experimental tidak tersedia.")
        val explanation = (document.get("explanation") as? Map<*, *>)
            ?.get("text") as? String ?: ""

        return RemoteExperimentalQuestion(
            id = document.id,
            packId = document.getString("packId").orEmpty(),
            text = text,
            options = options,
            correctOptionId = correctOptionId,
            explanation = explanation,
        )
    }

    private companion object {
        const val PACKS_COLLECTION = "packs"
        const val QUESTIONS_COLLECTION = "questions"
    }
}
