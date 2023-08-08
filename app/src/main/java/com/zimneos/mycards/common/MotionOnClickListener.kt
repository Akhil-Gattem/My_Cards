package com.zimneos.mycards.common

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import com.zimneos.mycards.R

class MotionOnClickListener(private val context: Context?, private val onClick: (view: View) -> Unit) : View.OnTouchListener {

    private var lastClickTimestamp = 0L

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                view?.isPressed = true
                animator(view, R.animator.btn_animator_scale_down)
                view?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
            MotionEvent.ACTION_UP -> {
                when {
                    view?.let { isViewContains(it, event.rawX, event.rawY) } == true -> view.postDelayed({
                        if (triggerAction()) {
                            onClick(view)
                        }
                    }, animator(view, R.animator.btn_animator_scale_up))
                    else -> animator(view, R.animator.btn_animator_scale_up)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                when {
                    view?.let { isViewContains(it, event.rawX, event.rawY) } == false -> animator(view, R.animator.btn_animator_scale_up)
                    else -> animator(view, R.animator.btn_animator_scale_up)
                }
            }
        }
        return false
    }

    private fun triggerAction(): Boolean {
        val currentTimestamp = SystemClock.elapsedRealtime()
        val shouldTrigger = currentTimestamp - lastClickTimestamp > VIEW_CLICK_DEBOUNCE_TIMESPAN_MILLSEC
        lastClickTimestamp = currentTimestamp

        return shouldTrigger
    }


    @RequiresApi(Build.VERSION_CODES.N)
    private fun animator(view: View?, animator: Int): Long {
        val anim = AnimatorInflater.loadAnimator(context, animator) as AnimatorSet
        anim.setTarget(view)
        anim.start()
        return anim.totalDuration
    }

    private fun isViewContains(view: View, touchX: Float, touchY: Float): Boolean {
        val coordinates = IntArray(2)
        view.getLocationOnScreen(coordinates)
        val viewCoordinatesX = coordinates[0]
        val viewCoordinatesY = coordinates[1]
        val viewWidth = view.width
        val viewHeight = view.height
        return !(touchX < viewCoordinatesX || touchX > viewCoordinatesX + viewWidth || touchY < viewCoordinatesY || touchY > viewCoordinatesY + viewHeight)
    }

    companion object {
        const val VIEW_CLICK_DEBOUNCE_TIMESPAN_MILLSEC = 1000L
    }
}