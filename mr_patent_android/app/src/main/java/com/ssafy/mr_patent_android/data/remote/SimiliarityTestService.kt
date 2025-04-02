package com.ssafy.mr_patent_android.data.remote

import com.ssafy.mr_patent_android.base.BaseResponse
import com.ssafy.mr_patent_android.data.model.response.SimiliarityTestResponse
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Path

interface SimiliarityTestService {

    @POST("draft/{patent_draft_id}/similiarity-check")
    suspend fun similiarityTest(
        @Path("patent_draft_id") patentDraftId: Int,
    ): Response<BaseResponse<SimiliarityTestResponse>>

}