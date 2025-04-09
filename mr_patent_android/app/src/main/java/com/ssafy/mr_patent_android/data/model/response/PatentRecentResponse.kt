package com.ssafy.mr_patent_android.data.model.response

import com.google.gson.annotations.SerializedName

data class PatentRecentResponse(
    @SerializedName("patent_drafts") val patentDrafts: List<PatentDraft>
) {
    data class PatentDraft(
       @SerializedName("patent_draft_id") val id: Int
        , @SerializedName("user_patent_folder_id") val userPatentFolderId: Int
        , @SerializedName("patent_draft_title") val title: String
        , @SerializedName("patent_draft_technical_field") val technicalField: String
        , @SerializedName("patent_draft_background") val background: String
        , @SerializedName("patent_draft_problem") val problem: String
        , @SerializedName("patent_draft_solution") val solution: String
        , @SerializedName("patent_draft_effect") val effect: String
        , @SerializedName("patent_draft_detailed") val detailed: String
        , @SerializedName("patent_draft_summary") val summary: String
        , @SerializedName("patent_draft_claim") val claim: String
        , @SerializedName("created_at") val createdAt: String
        , @SerializedName("updated_at") val updatedAt: String
    )
}