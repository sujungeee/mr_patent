package com.ssafy.mr_patent_android.data.model.response

import com.google.gson.annotations.SerializedName

data class PatentRecentResponse(
    @SerializedName("patent_drafts") val patentDrafts: List<PatentDraft>
) {
    data class PatentDraft(
        @SerializedName("parent_draft_id") val parentDraftId: Int,
        @SerializedName("patent_draft_title") val patentDraftTitle: String,
        @SerializedName("patent_draft_summary") val patentDraftSummary: String,
        @SerializedName("updated_at") val updatedAt: String
    )
}