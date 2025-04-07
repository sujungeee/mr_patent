package com.ssafy.mr_patent_android.data.model.dto

import com.google.gson.annotations.SerializedName

data class WordDto(
    @SerializedName("level_id")val level_id: Int,
    @SerializedName("level_name")val level_name: String,
    @SerializedName("words")val words: List<Word>,
    @SerializedName("score")val score: Int,
    @SerializedName("wrong_answers")val wrong_answers: List<Word>,
    @SerializedName("total")val total: Int
){
    data class Word(
        @SerializedName("bookmarked")val is_bookmarked: Boolean,
        @SerializedName("word_id")val word_id: Int,
        @SerializedName("word_mean")val word_mean: String,
        @SerializedName("word_name")val word_name: String,
        @SerializedName("bookmark_id")val bookmark_id: Int
    ){
        constructor(is_bookmarked: Boolean, word_id: Int, word_mean: String, word_name: String):this(is_bookmarked, word_id, word_mean, word_name, 1)
    }

    constructor() : this(0, "", emptyList(), 0, emptyList(),0)
}