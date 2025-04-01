package com.ssafy.mr_patent_android.data.model.response

import com.google.gson.annotations.SerializedName

data class PatentListResponse (
    @SerializedName("patents") val patents: List<PatentSummaryInfo>
) {
    data class PatentSummaryInfo(
        @SerializedName("patent_draft_id") val patentDraftId: Int,
        @SerializedName("patent_draft_title") val patentDraftTitle: String,
        @SerializedName("patent_similiarity_result") val patentSimiliarityResult: String,
        @SerializedName("patent_similiarity_result_score") val patentSimiliarityResultScore: Int,
        @SerializedName("created_at") val createdAt: String
    )
}