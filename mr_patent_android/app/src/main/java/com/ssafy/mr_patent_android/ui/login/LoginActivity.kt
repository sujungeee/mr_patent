package com.ssafy.mr_patent_android.ui.login

import android.content.Intent
import android.os.Bundle
import com.ssafy.mr_patent_android.MainActivity
import com.ssafy.mr_patent_android.base.BaseActivity
import com.ssafy.mr_patent_android.databinding.ActivityLoginBinding

class LoginActivity : BaseActivity<ActivityLoginBinding>(ActivityLoginBinding::inflate) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    fun navigateToMain() {
        // 메인 화면으로 이동
        val intent= Intent(this, MainActivity::class.java)
        intent.flags= Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

}