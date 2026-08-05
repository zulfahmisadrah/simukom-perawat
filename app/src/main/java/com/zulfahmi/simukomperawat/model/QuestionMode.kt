package com.zulfahmi.simukomperawat.model

enum class QuestionMode(
    val wireValue: String,
    val totalQuestions: Int,
    val isTimed: Boolean,
) {
    LATIHAN("latihan", 20, false),
    SIMULASI("simulasi", 100, true),
    EXPERIMENTAL("experimental", 20, false),
    ;

    companion object {
        fun fromWireValue(value: String): QuestionMode {
            return values().firstOrNull { it.wireValue == value }
                ?: throw IllegalArgumentException("Undefined question type: $value")
        }
    }
}
