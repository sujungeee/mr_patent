package com.ssafy.mr_patent_android.data.model.response

import com.google.gson.annotations.SerializedName

data class FitnessResultResponse (
    @SerializedName("fitness_id") val fitnessResult: String,
    @SerializedName("fitness_good_content") val fitnessGoodContent: List<FitnessContent>,
    @SerializedName("fitness_is_corrected") val fitnessIsCorrected: Boolean
) {
    data class FitnessContent(
        @SerializedName("patent_draft_title") val patentDraftTitle: Boolean,
        @SerializedName("patent_draft_claim") val patentDraftClaim: Boolean
    )
}