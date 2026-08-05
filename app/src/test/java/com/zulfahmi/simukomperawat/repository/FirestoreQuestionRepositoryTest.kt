package com.zulfahmi.simukomperawat.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class FirestoreQuestionRepositoryTest {
    @Test
    fun buildsFirestoreIdForSqliteLatihanPackage() {
        assertEquals(
            "legacy_latihan_paket_3",
            FirestoreQuestionRepository.firestorePackId("latihan", 3),
        )
    }

    @Test
    fun mapsDownloadedQuestionToSelectedLatihanPackage() {
        val remote = RemoteFirestoreQuestion(
            text = "Pertanyaan",
            options = listOf(
                RemoteFirestoreOption("a", "A"),
                RemoteFirestoreOption("b", "B"),
                RemoteFirestoreOption("c", "C"),
                RemoteFirestoreOption("d", "D"),
                RemoteFirestoreOption("e", "E"),
            ),
            correctOptionId = "c",
            explanation = "Karena C",
        )

        val question = FirestoreQuestionMapper.toRoomQuestion(remote, "latihan", 3)

        assertEquals("latihan", question.type)
        assertEquals(3, question.pack)
        assertEquals("c", question.answer)
    }

    @Test
    fun rejectsRefreshWithActionableQuestionCount() {
        val remote = RemoteFirestoreQuestion(
            text = "Pertanyaan",
            options = listOf(
                RemoteFirestoreOption("a", "A"),
                RemoteFirestoreOption("b", "B"),
                RemoteFirestoreOption("c", "C"),
                RemoteFirestoreOption("d", "D"),
                RemoteFirestoreOption("e", "E"),
            ),
            correctOptionId = "a",
            explanation = "",
        )

        try {
            FirestoreQuestionMapper.toRoomQuestions(List(19) { remote }, "latihan", 2)
            fail("Expected the incomplete package to be rejected")
        } catch (error: IllegalArgumentException) {
            assertEquals(
                "Paket latihan harus berisi tepat 20 soal. Ditemukan 19.",
                error.message,
            )
        }
    }

    @Test
    fun usesSuppliedFirestorePackId() {
        assertEquals(
            "new-pack",
            FirestoreQuestionRepository.resolvedPackId("latihan", 6, "new-pack"),
        )
    }
}
