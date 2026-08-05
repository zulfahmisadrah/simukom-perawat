package com.zulfahmi.simukomperawat.repository

import com.zulfahmi.simukomperawat.model.Question

data class RemoteOption(
    val id: String,
    val text: String,
)

data class RemoteExperimentalQuestion(
    val id: String,
    val packId: String,
    val text: String,
    val options: List<RemoteOption>,
    val correctOptionId: String,
    val explanation: String,
)

object ExperimentalQuestionMapper {
    const val TYPE = "experimental"
    const val PACK = 1

    fun toRoomQuestions(remoteQuestions: List<RemoteExperimentalQuestion>): List<Question> {
        require(remoteQuestions.size == 20)
        return remoteQuestions.map(::toRoomQuestion)
    }

    fun toRoomQuestion(remote: RemoteExperimentalQuestion): Question {
        require(remote.text.isNotBlank())
        require(remote.options.size == 5)
        require(remote.options.all { it.text.isNotBlank() })

        val answerIndex = remote.options.indexOfFirst { it.id == remote.correctOptionId }
        require(answerIndex >= 0)

        return Question(
            type = TYPE,
            pack = PACK,
            question = remote.text,
            optionA = remote.options[0].text,
            optionB = remote.options[1].text,
            optionC = remote.options[2].text,
            optionD = remote.options[3].text,
            optionE = remote.options[4].text,
            answer = ('a'.code + answerIndex).toChar().toString(),
            explanation = remote.explanation,
        )
    }
}
