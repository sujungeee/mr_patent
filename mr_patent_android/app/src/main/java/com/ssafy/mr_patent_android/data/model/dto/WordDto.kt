package com.ssafy.mr_patent_android.data.model.dto

import com.google.gson.annotations.SerializedName

data class WordDto(
    @SerializedName("level_id")val level_id: Int,
    @SerializedName("level_name")val level_name: String,
    @SerializedName("words")val words: List<Word>
){
    data class Word(
        @SerializedName("is_bookmarked")val is_bookmarked: Boolean,
        @SerializedName("word_id")val word_id: Int,
        @SerializedName("word_mean")val word_mean: String,
        @SerializedName("word_name")val word_name: String,
        @SerializedName("bookmark_id")val bookmark_id: Int
    ){
        constructor(is_bookmarked: Boolean, word_id: Int, word_mean: String, word_name: String):this(is_bookmarked, word_id, word_mean, word_name, 1)
    }
}