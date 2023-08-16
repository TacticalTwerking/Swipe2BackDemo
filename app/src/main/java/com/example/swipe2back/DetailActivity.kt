package com.example.swipe2back

import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.core.view.GravityCompat
import androidx.core.view.size
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.swipe2back.base.Drag2ExitActivity
import com.example.swipe2back.plaid.InkPageIndicator
import com.example.swipe2back.widgets.ScreenSlidePageFragment
import com.google.android.material.navigation.NavigationView


class DetailActivity : Drag2ExitActivity() {


//    private lateinit var nv:NavigationView
//    private lateinit var dl:DrawerLayout
    private lateinit var vp2:ViewPager2
    private lateinit var ivClose: View
    private lateinit var ipi:InkPageIndicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ac_detail)
//        nv = findViewById(R.id.nv)
//        dl = findViewById(R.id.dl)
//        dl.setScrimColor(Color.TRANSPARENT)
        vp2 = findViewById(R.id.vp2)
        ipi = findViewById(R.id.indicator)

        vp2.adapter =ScreenSlidePagerAdapter(this)
        ivClose = findViewById(R.id.iv_close)
        ipi.setViewPager(vp2)

        setSubContentView(findViewById(R.id.ll_usr_detail))

    }



    override fun ignoredRegion(): Rect? {
        val loc = IntArray(2)
        vp2.getLocationOnScreen(loc)
        return Rect(loc[0],loc[1],vp2.width,vp2.height)
    }

    override fun ignoredExempt(): IgnoredRegionExampt? {
        return IgnoredRegionExampt(vp2.currentItem == 0,vp2.currentItem == vp2.adapter!!.itemCount - 1)
    }

    override fun enableDrag() = true


    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment = ScreenSlidePageFragment(position)
    }
}