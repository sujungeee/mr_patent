package com.ssafy.mr_patent_android.ui.study

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.mr_patent_android.data.model.dto.WordDto
import com.ssafy.mr_patent_android.data.remote.RetrofitUtil.Companion.studyService
import kotlinx.coroutines.launch

class StudyCardViewModel : ViewModel() {
    private val _wordList = MutableLiveData<List<WordDto.Word>>()
    val wordList: LiveData<List<WordDto.Word>>
        get() = _wordList


    fun getWordList(levelId:Int){
        viewModelScope.launch {
            runCatching {
                studyService.getWords(levelId)
            }.onSuccess { response ->
                if(response.isSuccessful){
                    response.body()?.let {
                        _wordList.value = it.data?.words
                    }
                }else{
                    _wordList.value = List(10)
                    {WordDto.Word(true, 1,"뜻", "단어")}
                    Log.d("StudyCardViewModel", "getWordList: ${response.errorBody()}")
                }
            }.onFailure {
                Log.d("StudyCardViewModel", "getWordList: ${it.message}")
            }
        }

    }

}