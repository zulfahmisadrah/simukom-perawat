package com.zulfahmi.simukomperawat.repository

import com.zulfahmi.simukomperawat.model.Question

data class RemoteFirestoreOption(
    val id: String,
    val text: String,
)

data class RemoteFirestoreQuestion(
    val text: String,
    val options: List<RemoteFirestoreOption>,
    val correctOptionId: String,
    val explanation: String,
)

object FirestoreQuestionMapper {
    const val QUESTIONS_PER_PACK = 20

    fun toRoomQuestion(
        remote: RemoteFirestoreQuestion,
        type: String,
        pack: Int,
    ): Question {
        require(type == "latihan" || type == "simulasi")
        require(pack > 0)
        require(remote.text.isNotBlank())
        require(remote.options.size == 5)
        require(remote.options.all { it.text.isNotBlank() })

        val answerIndex = remote.options.indexOfFirst { it.id == remote.correctOptionId }
        require(answerIndex >= 0)

        return Question(
            type = type,
            pack = pack,
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

    fun toRoomQuestions(
        remoteQuestions: List<RemoteFirestoreQuestion>,
        type: String,
        pack: Int,
    ): List<Question> {
        require(remoteQuestions.size == QUESTIONS_PER_PACK)
        return remoteQuestions.map { toRoomQuestion(it, type, pack) }
    }

}
