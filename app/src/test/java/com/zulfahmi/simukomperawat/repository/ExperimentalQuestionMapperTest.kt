package com.zulfahmi.simukomperawat.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class ExperimentalQuestionMapperTest {
    @Test
    fun mapsFiveFirestoreOptionsAndCorrectOptionIdToRoomQuestion() {
        val remote = RemoteExperimentalQuestion(
            id = "q-1",
            packId = "pack-a",
            text = "Pertanyaan",
            options = listOf(
                RemoteOption("a", "A"),
                RemoteOption("b", "B"),
                RemoteOption("c", "C"),
                RemoteOption("d", "D"),
                RemoteOption("e", "E"),
            ),
            correctOptionId = "c",
            explanation = "Karena C",
        )

        val question = ExperimentalQuestionMapper.toRoomQuestion(remote)

        assertEquals("c", question.answer)
        assertEquals("C", question.optionC)
        assertEquals("experimental", question.type)
        assertEquals(1, question.pack)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsQuestionWithFewerThanFiveOptions() {
        ExperimentalQuestionMapper.toRoomQuestion(
            RemoteExperimentalQuestion(
                id = "q-1",
                packId = "pack-a",
                text = "Pertanyaan",
                options = emptyList(),
                correctOptionId = "a",
                explanation = "",
            ),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsExperimentalBatchWithFewerThanTwentyQuestions() {
        ExperimentalQuestionMapper.toRoomQuestions(List(19) { index ->
            RemoteExperimentalQuestion(
                id = "q-$index",
                packId = "pack-a",
                text = "Pertanyaan $index",
                options = listOf(
                    RemoteOption("a", "A"),
                    RemoteOption("b", "B"),
                    RemoteOption("c", "C"),
                    RemoteOption("d", "D"),
                    RemoteOption("e", "E"),
                ),
                correctOptionId = "a",
                explanation = "",
            )
        })
    }

    @Test
    fun mapsQuestionIntoSelectedPacksRoomCacheSlot() {
        val remote = RemoteExperimentalQuestion(
            id = "q-1",
            packId = "pack-7",
            text = "Pertanyaan",
            options = listOf(
                RemoteOption("a", "A"),
                RemoteOption("b", "B"),
                RemoteOption("c", "C"),
                RemoteOption("d", "D"),
                RemoteOption("e", "E"),
            ),
            correctOptionId = "a",
            explanation = "",
        )

        val question = ExperimentalQuestionMapper.toRoomQuestion(remote, roomPack = 7)

        assertEquals(7, question.pack)
    }
}
