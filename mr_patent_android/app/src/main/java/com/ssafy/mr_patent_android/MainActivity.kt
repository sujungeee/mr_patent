package com.ssafy.mr_patent_android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ssafy.mr_patent_android.databinding.ActivityMainBinding
import com.ssafy.mr_patent_android.ui.join.JoinApplicationFragment
import com.ssafy.mr_patent_android.ui.join.JoinBranchFragment
import com.ssafy.mr_patent_android.ui.join.JoinCompleteFragment
import com.ssafy.mr_patent_android.ui.join.JoinExpertFragment
import com.ssafy.mr_patent_android.ui.join.JoinMemberFragment
import com.ssafy.mr_patent_android.ui.join.JoinProfileFragment
import com.ssafy.mr_patent_android.ui.mypage.AddressChangeFragment
import com.ssafy.mr_patent_android.ui.mypage.AddressSearchFragment
import com.ssafy.mr_patent_android.ui.mypage.MyPageFragment
import com.ssafy.mr_patent_android.ui.mypage.PasswordChangeFragment
import com.ssafy.mr_patent_android.ui.mypage.PatentFolderFragment
import com.ssafy.mr_patent_android.ui.mypage.PatentReportFragment
import com.ssafy.mr_patent_android.ui.mypage.ProfileEditFragment
import com.ssafy.mr_patent_android.ui.mypage.ReportResultFragment
import com.ssafy.mr_patent_android.ui.mypage.UserDeleteFragment
import com.ssafy.mr_patent_android.ui.patent.FileUploadFragment
import com.ssafy.mr_patent_android.ui.patent.PatentContentFragment
import com.ssafy.mr_patent_android.ui.patent.PatentFragment
import com.ssafy.mr_patent_android.ui.patent.SimiliarityTestFragment

class MainActivity : AppCompatActivity() {

    private val mainBinding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        moveFragment(4)
    }
    private fun openFragment(index: Int, id: String = "") {
        moveFragment(index, id)
    }

    private fun moveFragment(index: Int, id: String = "") {
        val transaction = supportFragmentManager.beginTransaction()
        when(index) {
            1 -> transaction.replace(R.id.frame_layout, JoinBranchFragment())
            2 -> transaction.replace(R.id.frame_layout, JoinProfileFragment())
            3 -> transaction.replace(R.id.frame_layout, JoinMemberFragment())
            4 -> transaction.replace(R.id.frame_layout, JoinExpertFragment())
            5 -> transaction.replace(R.id.frame_layout, JoinCompleteFragment())
            6 -> transaction.replace(R.id.frame_layout, JoinApplicationFragment())

            7 -> transaction.replace(R.id.frame_layout, PatentFragment())
            8 -> transaction.replace(R.id.frame_layout, FileUploadFragment())
            9 -> transaction.replace(R.id.frame_layout, SimiliarityTestFragment())
            10 -> transaction.replace(R.id.frame_layout, PatentContentFragment())

            11 -> transaction.replace(R.id.frame_layout, MyPageFragment())
            12 -> transaction.replace(R.id.frame_layout, PatentReportFragment())
            13 -> transaction.replace(R.id.frame_layout, PasswordChangeFragment())
            14 -> transaction.replace(R.id.frame_layout, UserDeleteFragment())
            15 -> transaction.replace(R.id.frame_layout, ProfileEditFragment())
            16 -> transaction.replace(R.id.frame_layout, AddressChangeFragment())
            17 -> transaction.replace(R.id.frame_layout, AddressSearchFragment())
            18 -> transaction.replace(R.id.frame_layout, PatentFolderFragment())
            19 -> transaction.replace(R.id.frame_layout, ReportResultFragment())

        }

        transaction.commit()
    }
}