package com.ssafy.mr_patent_android

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.fragment.app.findFragment
import androidx.navigation.NavHostController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
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


private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private val mainBinding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mainBinding.root)
        initNavigation()
    }

    private fun openFragment(index: Int, id: String = "") {
        moveFragment(index, id)
    }

    private fun moveFragment(index: Int, id: String = "") {
        val transaction = supportFragmentManager.beginTransaction()
        when (index) {
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

    private fun initNavigation() {
        val navController = supportFragmentManager.findFragmentById(R.id.frame_layout)
            ?.findNavController() as NavHostController

        mainBinding.bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            // 바텀 네비게이션이 표시되는 Fragment
            if (destination.id == R.id.nav_fragment_home || destination.id == R.id.nav_fragment_patent
                || destination.id == R.id.nav_fragment_chat || destination.id == R.id.nav_fragment_mypage || destination.id == R.id.nav_fragment_study
            ) {
                mainBinding.bottomNavigationView.visibility = View.VISIBLE
            }
            // 바텀 네비게이션이 표시되지 않는 Fragment
            else {
                mainBinding.bottomNavigationView.visibility = View.GONE
            }
        }
    }


}