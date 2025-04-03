package com.ssafy.mr_patent_android.data.model.dto

data class Question(
    val correct_option: Int,
    val options: List<Option>,
    val question_id: Int,
    val question_text: String
){
    data class Option(
        val option_id: Int,
        val option_text: String
    )
}