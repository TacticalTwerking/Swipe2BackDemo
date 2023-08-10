package com.example.swipe2back.widgets

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.swipe2back.R

class ScreenSlidePageFragment(val position: Int) : Fragment() {

    val mColors = arrayOf(Color.parseColor("#FFEAEAEA"), Color.parseColor("#7F0000FF"), Color.parseColor("#7FFF0000"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.it_viewpager, container, false)
        v.findViewById<ImageView>(R.id.iv).setBackgroundColor(mColors[position])
        return v
    }

}

