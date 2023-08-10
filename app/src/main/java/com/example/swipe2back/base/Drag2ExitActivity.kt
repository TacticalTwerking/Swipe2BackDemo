package com.example.swipe2back.base

import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.app.ActivityCompat
import com.example.swipe2back.R
import kotlin.math.abs




open abstract class Drag2ExitActivity : AppCompatActivity() {

    private val RETURN_FACTOR = 250
    private val ANIMATION_DURATION = 200L
    private var mLastMotionY: Float? = null
    private var mLastMotionX: Float? = null
    private var mDragMode: Boolean = false
    private var mScrollMode:Boolean = false

    private var mDownY: Float? = null
    private var mDownX: Float? = null
    private var mAnimationRunning = false
    private lateinit var mContentView:View


    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        init()
    }

    private fun init(){
        mContentView = this.window.decorView.findViewById(android.R.id.content)
        setUpTransitionAnimationDuration()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                hideViews()
                finishWithAnimation()
            }
        })
    }

    open fun enableDrag():Boolean{
        return false
    }

    open fun ignoredRegion(): Rect? {
        return null
    }


    private fun setUpTransitionAnimationDuration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.sharedElementEnterTransition.duration = ANIMATION_DURATION
            window.sharedElementReturnTransition.duration = ANIMATION_DURATION
        }
    }

    /**
     * Hide these views to make transition animation smoother
     */
    private fun hideViews() {
        mContentView.findViewById<View>(R.id.ll_actionbar).visibility = View.GONE
        mContentView.findViewById<View>(R.id.ll_comment).visibility = View.GONE
    }

    /**
     * See if we can scroll right because there might be some scrollable views(horizontal) that we don't want to interrupt.
     * to make sure any scrollable views inside of this activity were currently on the rightest
     */
    private fun canScrollRight(): Boolean {
        //eg: return mHorizontalScrollView.canScrollHorizontally(-1)
        return true
    }

//    private fun canScrollRight(): Boolean {
//        return true
//    }
//
//    private fun canScrollDown(): Boolean {
//        return mScrollView.canScrollVertically(1)
//    }
//
//    private fun canScrollUp(): Boolean {
//        return mScrollView.canScrollVertically(-1)
//    }

    /**
     * Finish activity with transition animation running
     */
    private fun finishWithAnimation() {
        ActivityCompat.finishAfterTransition(this@Drag2ExitActivity)
    }


    private fun exitIfNeed() {
        mDragMode = false
        mAnimationRunning = true
        var exit = false
        if (null != mDownX && null != mLastMotionX) {
            if (abs(mDownX!! - mLastMotionX!!) > RETURN_FACTOR) {
                exit = true
            }
        }
        //Comment out these code because we don't need vertical drag
//        if (null != mDownY && null != mLastMotionY) {
//            if (abs(mDownY!! - mLastMotionY!!) > RETURN_FACTOR) {
//                exit = true
//            }
//        }
        if (exit) {
            hideViews()
        }
        val va = ValueAnimator.ofFloat(1F, 0F)
        va.interpolator = AccelerateDecelerateInterpolator()
        va.duration = ANIMATION_DURATION //in millis
        va.addUpdateListener { animation ->
            val av = animation.animatedValue as Float
            mContentView.translationX = mContentView.translationX * av
            mContentView.translationY = mContentView.translationY * av
            if (exit) {
                window.setBackgroundDrawable(ColorDrawable(Color.parseColor("#7f000000")).apply {
                    this.alpha = (255 * av).toInt()
                })
            }
        }
        va.doOnEnd {
            mAnimationRunning = false
            mLastMotionY = null
            mLastMotionX = null
        }
        va.start()
        if (exit) {
            finishWithAnimation()
        }
    }


    /**
     * Transit contentview's offset to make it following the touching position
     */
    private fun mkTransition(ev: MotionEvent) {
        var offsetY = 0F
        var offsetX = 0F
        if (ev?.x != null && null != mLastMotionX) {
            offsetX = mLastMotionX!! - ev.x
        }
        if (ev?.x != null && null != mLastMotionY) {
            offsetY = mLastMotionY!! - ev.y
        }
        mContentView.translationX = mContentView.translationX - offsetX
        mContentView.translationY = mContentView.translationY - offsetY
    }


    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {

        if (!mAnimationRunning && enableDrag()) {
            if (ev?.action == MotionEvent.ACTION_DOWN) {
                mLastMotionY = ev?.y
                mLastMotionX = ev?.x
                mDownX = ev?.x
                mDownY = ev?.y
                ignoredRegion()?.let{
                    println("mDownY $mDownY ${it.top} ${it.bottom+it.top}")
                    if(null!=mDownY && mDownY!! > it.top && mDownY!!< (it.bottom+it.top)){
                        mScrollMode = true
                    }
                }

            }

            if (ev?.action == MotionEvent.ACTION_MOVE) {
                if (mDragMode) {
                    mkTransition(ev)
                }
                //Set to drag mode if distance of right-swipe reaching 100 pixels
                if (canScrollRight() && !mScrollMode && null != mLastMotionX && null != mDownX && mLastMotionX!! - mDownX!! > 100) {
                    mDragMode = true
                }
                //Prevent get into the drag mode when scrolling
                if (null != mLastMotionY && null != mDownY && mLastMotionY!! - mDownY!! > 100) {
                    mScrollMode = true
                }
            }

            if (ev?.action == MotionEvent.ACTION_UP) {
                mScrollMode = false

                if (mDragMode) {
                    exitIfNeed()
                }
            }

            mLastMotionY = ev?.y
            mLastMotionX = ev?.x
            return if (mDragMode) false else super.dispatchTouchEvent(ev)
        } else {
            return super.dispatchTouchEvent(ev)
        }

    }


}