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
}
