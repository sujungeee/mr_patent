package com.ssafy.mr_patent_android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ssafy.mr_patent_android.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val mainBinding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private fun openFragment(index: Int, id: String = "") {
        moveFragment(index, id)
    }

    private fun moveFragment(index: Int, id: String = "") {
        val transaction = supportFragmentManager.beginTransaction()
        when(index) {

        }

        transaction.commit()
    }
}