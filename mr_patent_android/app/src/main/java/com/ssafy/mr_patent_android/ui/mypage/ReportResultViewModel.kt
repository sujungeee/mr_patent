package com.ssafy.mr_patent_android.ui.mypage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.mr_patent_android.data.model.response.SimiliarityResultResponse
import com.ssafy.mr_patent_android.data.remote.RetrofitUtil.Companion.patentService
import kotlinx.coroutines.launch

class ReportResultViewModel : ViewModel() {
    private val _toastMsg = MutableLiveData<String>()
    val toastMsg: LiveData<String>
        get() = _toastMsg

    private val _userPatentId = MutableLiveData<Int>()
    val userPatentId: LiveData<Int>
        get() = _userPatentId

    private val _fitnessResult = MutableLiveData<String>()
    val fitnessResult: LiveData<String>
        get() = _fitnessResult

    private val _similiarityResult = MutableLiveData<List<SimiliarityResultResponse.Comparison>>()
    val similiarityResult: LiveData<List<SimiliarityResultResponse.Comparison>>
        get() = _similiarityResult

    fun setUserPatentId(patentDraftId: Int) {
        _userPatentId.value = patentDraftId
    }

    // TODO: delete
    fun setFitnessResult(fitnessResult: String) {
        _fitnessResult.value = fitnessResult
    }

    // TODO: delete
    fun setSimiliarityResult(similiarityResult: List<SimiliarityResultResponse.Comparison>) {
        _similiarityResult.value = similiarityResult
    }

    fun getFitnessResult(userPatentId: Int) {
        viewModelScope.launch {
            runCatching {
                patentService.getFitnessResult(userPatentId)
            }.onSuccess {
                if (it.isSuccessful) {
                    it.body().let { response ->
                        if (response?.data?.fitnessIsCorrected == true) {
                            _fitnessResult.value = "PASS"
                        } else {
                            _fitnessResult.value = "FAIL"
                        }
                    }
                }
            }.onFailure {

            }
        }
    }

    fun getSimiliarityResult(userPatentId: Int) {
        viewModelScope.launch {
            runCatching {
                patentService.getSimiliarityResult(userPatentId)
            }.onSuccess {
                if (it.isSuccessful) {
                    it.body().let { response ->
                        if (response?.data?.comparisons?.isNotEmpty() == true) {
                            _similiarityResult.value = response.data.comparisons
                        }
                    }
                }
            }.onFailure {

            }
        }
    }
}