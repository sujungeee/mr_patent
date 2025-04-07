package com.ssafy.mr_patent_android.ui.study

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.mr_patent_android.data.model.dto.AnswerDto
import com.ssafy.mr_patent_android.data.model.dto.Question
import com.ssafy.mr_patent_android.data.model.dto.QuizDto
import com.ssafy.mr_patent_android.data.model.dto.QuizRequest
import com.ssafy.mr_patent_android.data.model.dto.WordDto
import com.ssafy.mr_patent_android.data.remote.RetrofitUtil.Companion.studyService
import kotlinx.coroutines.launch

class QuizResultViewModel : ViewModel() {
    private val _resultData = MutableLiveData<WordDto>()
    val resultData: LiveData<WordDto> get() = _resultData


    fun getQuizResult(levelId:Int,wrongList: List<Int>){
        viewModelScope.launch {
            runCatching {
                val answerDto = AnswerDto(
                    answers = wrongList.map { AnswerDto.WordId(it) }
                )

                studyService.postQuizResult(levelId,answerDto)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    response.body()?.let { result ->
                        _resultData.value = result.data ?: WordDto()
                    }
                }
            }.onFailure { error ->
                // Handle error
                error.printStackTrace()
            }
        }
    }


}
