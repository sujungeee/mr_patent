package com.ssafy.mr_patent_android.data.model.response

import com.google.gson.annotations.SerializedName

data class FitnessResultResponse (
    @SerializedName("fitness_id") val fitnessResult: String,
    @SerializedName("fitness_good_content") val fitnessGoodContent: FitnessContent,
    @SerializedName("fitness_is_corrected") val fitnessIsCorrected: Int
) {
    data class FitnessContent(
        @SerializedName("patent_draft_title") val patentDraftTitle: Boolean,
        @SerializedName("patent_draft_technical_field") val patentDraftTechnicalField: Boolean,
        @SerializedName("patent_draft_background") val patentDraftBackground: Boolean,
        @SerializedName("patent_draft_problem") val patentDraftProblem: Boolean,
        @SerializedName("patent_draft_solution") val patentDraftSolution: Boolean,
        @SerializedName("patent_draft_effect") val patentDraftEffect: Boolean,
        @SerializedName("patent_draft_detailed") val patentDraftDetailed: Boolean,
        @SerializedName("patent_draft_summary") val patentDraftSummary: Boolean,
        @SerializedName("patent_draft_claim") val patentDraftClaim: Boolean,
    )
}