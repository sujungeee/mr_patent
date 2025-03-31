package com.ssafy.mr_patent_android.data.model.response

import com.google.gson.annotations.SerializedName

data class SimiliarityResultResponse (
    @SerializedName("comparisons") val comparisons: List<Comparison>
){
    data class Comparison(
        @SerializedName("comparisons_id") val comparisonsId: Int,
        @SerializedName("similarity_patent_id") val similarityPatentId: Int,
        @SerializedName("detailed_comparison_total_score") val detailedComparisonTotalScore: Double,
        @SerializedName("comparison_contexts") val comparisonContexts: List<SimiliarContext>
    ) {
        data class SimiliarContext(
            @SerializedName("user_section") val userSection: String,
            @SerializedName("patent_section") val patentSection: String,
            @SerializedName("user_text") val userText: String,
            @SerializedName("patent_text") val patentText: String,
            @SerializedName("similarity_score") val similarityScore: Double
        )
    }
}