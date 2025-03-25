package com.ssafy.mr_patent_android.ui.study

import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GestureDetectorCompat
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.databinding.FragmentQuizResultBinding

private const val TAG = "QuizResultFragment"
class QuizResultFragment : Fragment() {
    lateinit var binding: FragmentQuizResultBinding
    private lateinit var gestureDetector: GestureDetectorCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentQuizResultBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val slidePanel = binding.suplQuizResult

        slidePanel.isTouchEnabled = true// SlidingUpPanel

        gestureDetector = GestureDetectorCompat(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                slidePanel.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
                return super.onFling(e1, e2, velocityX, velocityY)
            }
        })
        binding.tvWrongList.setOnTouchListener { v, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        binding.tvQuizResult.text = getString(R.string.quiz_result, 8)
    }


}