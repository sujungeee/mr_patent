package com.ssafy.mr_patent_android

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavHostController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.ssafy.mr_patent_android.base.BaseActivity
import com.ssafy.mr_patent_android.databinding.ActivityMainBinding
import com.ssafy.mr_patent_android.ui.home.HomeFragmentDirections
import com.ssafy.mr_patent_android.util.ConnectionStateMonitor
import com.ssafy.mr_patent_android.ui.patent.PatentFragmentDirections
import com.ssafy.mr_patent_android.ui.patent.SimiliarityTestViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private const val TAG = "MainActivity_Mr_Patent"
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate)  {
    val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initNavigation()
        initConnection()
    }

    fun initConnection(){
        ConnectionStateMonitor(this, {
            viewModel.setNetworkState(true)
        }, {
            viewModel.setNetworkState(false)
        })
    }

    fun initObserver(){
        viewModel.networkState.observe(this){
            Log.d(TAG, "initObserver: $it")
            if(it == true){
                binding.appbar.visibility = View.GONE
            }
            else{
                binding.appbar.visibility = View.VISIBLE
            }
        }
    }


    private fun initNavigation() {
        Log.d(TAG, "initNavigation: ${intent.extras}")
        Log.d(TAG, "initNavigation: ${intent.data}")
        Log.d(TAG, "initNavigation: ${intent.getStringExtra("user_id")}")


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

        if (intent.getStringExtra("roomId") != null) {
            val userId = (intent.getStringExtra("userId")?:"-1").toInt()
            val expertId = (intent.getStringExtra("expertId")?:"-1").toInt()
            val roomId = intent.getStringExtra("roomId")!!
            val userName = intent.getStringExtra("userName")?: "이름없음"
            val userImage = intent.getStringExtra("userImage")?:""
            val action = HomeFragmentDirections.actionNavFragmentHomeToChatFragment(userId,expertId,roomId,userName,userImage)
            navController.navigate(action)
        }

        if (intent.getStringExtra("type") == "SIMILARITY_TEST") {
            navController.navigate(
                PatentFragmentDirections.actionNavFragmentPatentToSimiliarityTestFragment("finished")
            )
        }
    }

}