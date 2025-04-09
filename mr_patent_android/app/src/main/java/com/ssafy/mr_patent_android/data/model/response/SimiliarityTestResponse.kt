package com.ssafy.mr_patent_android.data.model.response

import com.google.gson.annotations.SerializedName

data class SimiliarityTestResponse(
    @SerializedName("similiarity_id") val similiarityId: Int
    , @SerializedName("status") val status: String
    , @SerializedName("search_method") val searchMethod: String
)
