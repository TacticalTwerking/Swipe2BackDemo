package com.example.swipe2back.base

import android.animation.ValueAnimator
import android.content.res.Resources
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

    data class IgnoredRegionExampt(var ltr:Boolean = false,var rtl:Boolean = false)

    private val RETURN_FACTOR = 300
    private val ANIMATION_DURATION = 200L
    private var mLastMotionY: Float? = null
    private var mLastMotionX: Float? = null
    private var mDragLTRMode: Boolean = false
    private var mSubContentDragMode: Boolean = false
    private var mScrollMode:Boolean = false

    private var mDownY: Float? = null
    private var mDownX: Float? = null
    private var mAnimationRunning = false
    private var mSubContentAnimationRunning = false
    private lateinit var mContentView:View
    private var mSubContentView:View? = null
    private var mSubContentViewExpanded = false
    private var mScreenWidth:Int = 0
    private var mPressedInIgnore = false


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
        mScreenWidth = Resources.getSystem().displayMetrics.widthPixels
    }


    fun setSubContentView(v:View){
        mSubContentView = v
        mSubContentView!!.translationX = mScreenWidth.toFloat()
    }

    open fun enableDrag():Boolean{
        return true
    }

    open fun ignoredRegion(): Rect? {
        return null
    }


    open fun ignoredExempt():IgnoredRegionExampt?{
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
//    open fun canScrollRight(): Boolean {
//        //eg: return mHorizontalScrollView.canScrollHorizontally(-1)
//        return true
//    }
//
//    open fun canScrollLeft(): Boolean {
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
        mDragLTRMode = false
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


    private fun snapToPosition() {
        if (null == mSubContentView || null == mLastMotionX){
            return
        }
        mSubContentDragMode = false
        mSubContentAnimationRunning = true

        println(">>> ${mLastMotionX!!} ${mDownX!!} ")
        if (mSubContentViewExpanded) {
            //exit subContent
            if (mLastMotionX!! - mDownX!! > RETURN_FACTOR) {
                mSubContentViewExpanded = false
            }
        } else {
            //enter subContent
            if (mDownX!! - mLastMotionX!! > RETURN_FACTOR) {
                mSubContentViewExpanded = true
            }
        }

        var toggle = false
        if (abs(mDownX!! - mLastMotionX!!) > RETURN_FACTOR) {
            toggle =true
        }

        if(toggle){
            mSubContentViewExpanded = !mSubContentViewExpanded
        }

        val va = ValueAnimator.ofFloat(
            mSubContentView!!.translationX,
            if (mSubContentViewExpanded) 0F else mScreenWidth.toFloat()
        )
        va.interpolator = AccelerateDecelerateInterpolator()
        va.duration = ANIMATION_DURATION //in millis
        va.addUpdateListener { animation ->
            val av = animation.animatedValue as Float
            mSubContentView!!.translationX = av
        }
        va.doOnEnd {
            mSubContentAnimationRunning = false
            mDragLTRMode = false
            mSubContentDragMode = false
            mLastMotionY = null
            mLastMotionX = null
        }
        va.start()
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

    private fun mkSubContentTransition(ev: MotionEvent) {
        if (null==mSubContentView){
            return
        }
        var offsetX = 0F
        if (ev?.x != null && null != mLastMotionX) {
            offsetX = mLastMotionX!! - ev.x
        }
        mSubContentView!!.translationX = mSubContentView!!.translationX - offsetX
    }


    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {

        if (!mAnimationRunning && !mSubContentAnimationRunning) {
            if (ev?.action == MotionEvent.ACTION_DOWN) {
                mLastMotionY = ev?.y
                mLastMotionX = ev?.x
                mDownX = ev?.x
                mDownY = ev?.y
                ignoredRegion()?.let{
                    if(null==mDownX || null == mDownY){
                        return@let
                    }
                    if(mDownY!! > it.top && mDownY!!< (it.bottom+it.top) && mDownX!! > it.left && mDownX!!< (it.left+it.right)){
                        mPressedInIgnore = true
                    }
                }
            }

            if (ev?.action == MotionEvent.ACTION_MOVE) {
                if(!mSubContentViewExpanded){
                    if (mDragLTRMode) {
                        mkTransition(ev)
                    }else if(mSubContentDragMode){
                        mkSubContentTransition(ev)
                    }
                }else{
                    if(mDragLTRMode){
                        mkSubContentTransition(ev)
                    }
                }


                if (null != mLastMotionX && null != mDownX && !mScrollMode) {
                    //Set to drag mode if distance of right-swipe reaching 100 pixels
                    if (mLastMotionX!! - mDownX!! > 150) {
                        //LTR
                        if (!mPressedInIgnore || (mPressedInIgnore && ignoredExempt()?.ltr == true)) {
                            mDragLTRMode = true
                        }
                    }


                    if (mLastMotionX!! - mDownX!! < -150) {
                        //RTL
                        if (!mPressedInIgnore || (mPressedInIgnore && ignoredExempt()?.rtl == true)) {
                            mSubContentDragMode = true
                        }
                    }
                }


                //Prevent get into the drag mode when scrolling
                if (null != mLastMotionY && null != mDownY && abs(mLastMotionY!! - mDownY!!) > 100) {
                    mScrollMode = true
                }
            }

            if (ev?.action == MotionEvent.ACTION_UP) {
                mScrollMode = false
                mPressedInIgnore = false

                if (mDragLTRMode && !mSubContentViewExpanded) {
                    exitIfNeed()
                }

                if(mSubContentDragMode||mSubContentViewExpanded){
                    snapToPosition()
                }
            }

            mLastMotionY = ev?.y
            mLastMotionX = ev?.x
            return if (mDragLTRMode || mSubContentDragMode) false else super.dispatchTouchEvent(ev)
        } else {
            return super.dispatchTouchEvent(ev)
        }

    }

    private fun enableSubContentDrag() {

    }


}