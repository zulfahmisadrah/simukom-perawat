package com.zulfahmi.simukomperawat.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestionModeTest {
    @Test
    fun latihanHasTwentyQuestionsWithoutTimer() {
        assertEquals(20, QuestionMode.LATIHAN.totalQuestions)
        assertFalse(QuestionMode.LATIHAN.isTimed)
    }

    @Test
    fun simulasiKeepsTimedHundredQuestionBehavior() {
        assertEquals(100, QuestionMode.SIMULASI.totalQuestions)
        assertTrue(QuestionMode.SIMULASI.isTimed)
    }
}
