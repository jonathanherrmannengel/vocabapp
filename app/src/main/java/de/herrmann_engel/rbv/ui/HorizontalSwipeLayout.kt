package de.herrmann_engel.rbv.ui

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.RelativeLayout
import kotlin.math.abs

/**
 * Layout that passes horizontal swipes to its SwipeEvents even when they start on a child view
 * (e.g. a scrollable text or a button), so that they do not have to start at the edges, where
 * Android's back gesture starts as well; for SwipeEvents with horizontalFromEdge = false. Taps and
 * vertical scrolling stay with the child views: a gesture is only taken over once it clearly moves
 * sideways.
 */
class HorizontalSwipeLayout(context: Context, attrs: AttributeSet?) :
    RelativeLayout(context, attrs) {
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var downX = 0f
    private var downY = 0f

    // Receives all touch events of the layout's swipes
    var swipeEvents: SwipeEvents? = null
        set(value) {
            field = value
            setOnTouchListener(value)
        }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                // The swipe events need the start of every gesture, also if a child takes it
                swipeEvents?.onTouch(this, event)
            }

            MotionEvent.ACTION_MOVE -> {
                val distanceX = abs(event.x - downX)
                if (distanceX > touchSlop && distanceX > abs(event.y - downY)) {
                    return true
                }
            }
        }
        return false
    }
}
