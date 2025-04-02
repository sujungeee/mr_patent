package com.ssafy.mr_patent_android.ui.mypage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.mr_patent_android.data.model.dto.PatentFrameDto
import com.ssafy.mr_patent_android.data.model.response.FitnessResultResponse
import com.ssafy.mr_patent_android.data.model.response.PatentContentResponse
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

    private val _patentContent = MutableLiveData<PatentContentResponse>()
    val patentContent: LiveData<PatentContentResponse>
        get() = _patentContent

    private val _similiarityResult = MutableLiveData<List<SimiliarityResultResponse.Comparison>>()
    val similiarityResult: LiveData<List<SimiliarityResultResponse.Comparison>>
        get() = _similiarityResult

    private val _mode = MutableLiveData<String>()
    val mode: LiveData<String>
        get() = _mode

    // 초안 기준들
    private val _patentExpContents = MutableLiveData<List<PatentFrameDto>>()
    val patentExpContents: LiveData<List<PatentFrameDto>>
        get() = _patentExpContents

    private val _patentClaimContents = MutableLiveData<List<PatentFrameDto>>()
    val patentClaimContents: LiveData<List<PatentFrameDto>>
        get() = _patentClaimContents

    private val _patentSummaryContents = MutableLiveData<List<PatentFrameDto>>()
    val patentSummaryContents: LiveData<List<PatentFrameDto>>
        get() = _patentSummaryContents

    // 적합도
    private val _fitnessContents = MutableLiveData<FitnessResultResponse. FitnessContent>()
    val fitnessContents: LiveData<FitnessResultResponse. FitnessContent>
        get() = _fitnessContents

    private val _fitnessResult = MutableLiveData<String>()
    val fitnessResult: LiveData<String>
        get() = _fitnessResult


    fun setUserPatentId(patentDraftId: Int) {
        _userPatentId.value = patentDraftId
    }

    // TODO: delete
    fun setFitnessResult(fitnessResult: String) {
        _fitnessResult.value = fitnessResult
    }

    // TODO: delete
    fun setFitnessContents(fitnessContents: FitnessResultResponse. FitnessContent) {
        _fitnessContents.value = fitnessContents
    }

    // TODO: delete
    fun setSimiliarityResult(similiarityResult: List<SimiliarityResultResponse.Comparison>) {
        _similiarityResult.value = similiarityResult
    }

    fun setMode(mode: String) {
        _mode.value = mode
    }

    fun setPatentContent(patentContent: PatentContentResponse) {
        _patentContent.value = patentContent
    }

    fun setPatentExpContents(patentExpContents: List<PatentFrameDto>) {
        _patentExpContents.value = patentExpContents
    }

    fun setPatentClaimContents(patentClaimContents: List<PatentFrameDto>) {
        _patentClaimContents.value = patentClaimContents
    }

    fun setPatentSummaryContents(patentSummaryContents: List<PatentFrameDto>) {
        _patentSummaryContents.value = patentSummaryContents
    }

    fun getFitnessResult(userPatentId: Int) {
        viewModelScope.launch {
            runCatching {
                patentService.getFitnessResult(userPatentId)
            }.onSuccess {
                if (it.isSuccessful) {
                    it.body().let { response ->
                        response?.data
                        if (response?.data?.fitnessIsCorrected == 0) {
                            _fitnessResult.value = "PASS"
                        } else {
                            _fitnessResult.value = "FAIL"
                            _fitnessContents.value = response?.data?.fitnessGoodContent
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

    fun getPatentContent(userPatentId: Int) {
        viewModelScope.launch {
            runCatching {
                patentService.getPatentContent(userPatentId)
            }.onSuccess {
                if (it.isSuccessful) {
                    it.body()?.data?.let { response ->
                        _patentContent.value = response
                    }
                }
            }.onFailure {

            }
        }
    }
}