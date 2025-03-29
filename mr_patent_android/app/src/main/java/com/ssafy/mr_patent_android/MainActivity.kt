package com.ssafy.mr_patent_android

import android.os.Bundle
import android.view.View
import androidx.navigation.NavHostController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.ssafy.mr_patent_android.base.BaseActivity
import com.ssafy.mr_patent_android.databinding.ActivityMainBinding

private const val TAG = "MainActivity_Mr_Patent"
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate)  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initNavigation()
    }

    private fun initNavigation() {
        val navController = supportFragmentManager.findFragmentById(R.id.frame_layout)
            ?.findNavController() as NavHostController

        binding.bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            // 바텀 네비게이션이 표시되는 Fragment
            if (destination.id == R.id.nav_fragment_home || destination.id == R.id.nav_fragment_patent
                || destination.id == R.id.nav_fragment_chat || destination.id == R.id.nav_fragment_mypage || destination.id == R.id.nav_fragment_study
            ) {
                binding.bottomNavigationView.visibility = View.VISIBLE
            }
            // 바텀 네비게이션이 표시되지 않는 Fragment
            else {
                binding.bottomNavigationView.visibility = View.GONE
            }
        }
    }
}