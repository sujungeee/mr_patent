package com.ssafy.mr_patent_android.data.remote

import com.ssafy.mr_patent_android.base.BaseResponse
import com.ssafy.mr_patent_android.data.model.dto.ChatMessageDto
import com.ssafy.mr_patent_android.data.model.dto.ChatRoomDto
import com.ssafy.mr_patent_android.data.model.dto.ChatRoomRequest
import com.ssafy.mr_patent_android.data.model.dto.LevelDto
import com.ssafy.mr_patent_android.data.model.dto.QuizDto
import com.ssafy.mr_patent_android.data.model.dto.QuizRequest
import com.ssafy.mr_patent_android.data.model.dto.WordDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface StudyService {
    @GET("study/levels")
    suspend fun getLevels():
            Response<BaseResponse<LevelDto>>

    @GET("study/levels/{level_id}/words")
    suspend fun getWords(
        @Path("level_id") level_id: Int):
            Response<BaseResponse<WordDto>>

    @GET("study/levels/{level_id}/quiz")
    suspend fun getQuiz(
        @Path("level_id") level_id: Int):
            Response<BaseResponse<QuizDto>>

    @POST("study/levels/{level_id}/quiz-results")
    suspend fun postQuizResult(
        @Path("level_id") level_id: Int,
        @Body quizDto: QuizDto):
            Response<BaseResponse<QuizRequest>>

    @POST("bookmarks")
    suspend fun postBookmark(
        @Query("word_id") word_id: Int):
            Response<BaseResponse<String>>

    @DELETE("bookmarks/{bookmark_id}")
    suspend fun deleteBookmark(
        @Path("bookmark_id") bookmark_id: Int):
            Response<BaseResponse<String?>>

}