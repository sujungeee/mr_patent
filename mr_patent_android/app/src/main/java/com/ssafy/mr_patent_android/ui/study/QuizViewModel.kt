package com.ssafy.mr_patent_android.ui.study

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.mr_patent_android.data.model.dto.AnswerDto
import com.ssafy.mr_patent_android.data.model.dto.Question
import com.ssafy.mr_patent_android.data.model.dto.QuizDto
import com.ssafy.mr_patent_android.data.remote.RetrofitUtil.Companion.studyService
import kotlinx.coroutines.launch

class QuizViewModel : ViewModel() {
    private val _quizData = MutableLiveData<QuizDto>()
    val quizData: LiveData<QuizDto> get() = _quizData

    private val _wrongAnswers = MutableLiveData<ArrayList<Int>>()
    val wrongAnswers: LiveData<ArrayList<Int>> get() = _wrongAnswers

    fun addWrongQuiz(wordId: Int) {
        _wrongAnswers.value = (_wrongAnswers.value ?: ArrayList()).apply {
            add(wordId)
        }
    }



    fun getQuiz(levelId: Int) {
        viewModelScope.launch {
            runCatching {
                studyService.getQuiz(levelId)
            }.onSuccess {
                if (it.isSuccessful) {
                    it.body()?.data?.let { it1 ->
                        _quizData.value = it1
                    }
                }else{
                    _quizData.value = QuizDto( listOf(
                        Question(1,  listOf(
                            Question.Option(1, "답1"),
                            Question.Option(2, "답2"),
                            Question.Option(3, "답3"),
                            Question.Option(4, "답4")
                        ), 1,"문제1"),
                        Question(2, listOf(
                            Question.Option(1, "답1"),
                            Question.Option(2, "답2"),
                            Question.Option(3, "답3"),
                            Question.Option(4, "답4")
                        ), 2, "문제2"),
                        Question(3, listOf(
                            Question.Option(1, "답1"),
                            Question.Option(2, "답2"),
                            Question.Option(3, "답3"),
                            Question.Option(4, "답4")
                        ), 3, "문제3"),
                        Question(4, listOf(
                            Question.Option(1, "답1"),
                            Question.Option(2, "답2"),
                            Question.Option(3, "답3"),
                            Question.Option(4, "답4")
                        ), 4, "문제4"),
                        Question(5, listOf(
                            Question.Option(1, "답1"),
                            Question.Option(2, "답2"),
                            Question.Option(3, "답3"),
                            Question.Option(4, "답4")
                        ), 1, "문제5")
                    ))
                }
            }.onFailure {
                // mock data

            }
        }
    }


}
