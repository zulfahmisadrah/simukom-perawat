package com.zulfahmi.simukomperawat.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestionModeTest {
    @Test
    fun experimentalHasLatihanTimingAndQuestionCount() {
        assertEquals(20, QuestionMode.EXPERIMENTAL.totalQuestions)
        assertFalse(QuestionMode.EXPERIMENTAL.isTimed)
    }

    @Test
    fun simulasiKeepsTimedHundredQuestionBehavior() {
        assertEquals(100, QuestionMode.SIMULASI.totalQuestions)
        assertTrue(QuestionMode.SIMULASI.isTimed)
    }
}
