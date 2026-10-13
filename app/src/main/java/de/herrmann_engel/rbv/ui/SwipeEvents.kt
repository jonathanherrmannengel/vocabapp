package de.herrmann_engel.rbv.ui

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

/**
 * Touch listener detecting swipes from the edges of a view. Subclasses override the callbacks.
 *
 * A swipe has to start within SWIPE_START_DISTANCE_MAX pixels of the edge it starts from.
 * While moving, onMoveX/onMoveY report the distance; once the distance reaches SWIPE_DISTANCE_MIN,
 * the matching onSwipe* callback is called once.
 */
open class SwipeEvents : View.OnTouchListener {
    private var initialY = 0f
    private var initialX = 0f
    // 0: no direction yet, -1: gesture finished or cancelled, else one of the MOVE_DIRECTION_* constants
    private var moveDirection = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val action = event.actionMasked
        if (action == MotionEvent.ACTION_DOWN) {
            initialX = event.x
            initialY = event.y
            moveDirection = 0
            return true
        } else if (action == MotionEvent.ACTION_MOVE) {
            val distanceX = event.x - initialX
            val distanceY = event.y - initialY
            // Horizontal movement
            if (abs(distanceX) > abs(distanceY)) {
                if (abs(distanceX) < SWIPE_DISTANCE_MIN) {
                    // Determine the direction once, only if the gesture started near the corresponding edge
                    if (moveDirection == 0) {
                        if (distanceX > 0 && initialX < SWIPE_START_DISTANCE_MAX) {
                            moveDirection = MOVE_DIRECTION_RIGHT
                        } else if (distanceX < 0 && initialX > v.width - SWIPE_START_DISTANCE_MAX) {
                            moveDirection = MOVE_DIRECTION_LEFT
                        }
                        return true
                    } else if (distanceX > 0 && moveDirection == MOVE_DIRECTION_RIGHT || distanceX < 0 && moveDirection == MOVE_DIRECTION_LEFT) {
                        onMoveX(distanceX)
                        return true
                    }
                } else if (distanceX > 0 && moveDirection == MOVE_DIRECTION_RIGHT) {
                    moveDirection = -1
                    onSwipeRight()
                    return true
                } else if (distanceX < 0 && moveDirection == MOVE_DIRECTION_LEFT) {
                    moveDirection = -1
                    onSwipeLeft()
                    return true
                }
            } else {
                if (abs(distanceY) < SWIPE_DISTANCE_MIN) {
                    if (moveDirection == 0) {
                        if (distanceY > 0 && initialY < SWIPE_START_DISTANCE_MAX) {
                            moveDirection = MOVE_DIRECTION_BOTTOM
                        } else if (distanceY < 0 && initialY > v.height - SWIPE_START_DISTANCE_MAX) {
                            moveDirection = MOVE_DIRECTION_TOP
                        }
                        return true
                    } else if (distanceY > 0 && moveDirection == MOVE_DIRECTION_BOTTOM || distanceY < 0 && moveDirection == MOVE_DIRECTION_TOP) {
                        onMoveY(distanceY)
                        return true
                    }
                } else if (distanceY > 0 && moveDirection == MOVE_DIRECTION_BOTTOM) {
                    moveDirection = -1
                    onSwipeBottom()
                    return true
                } else if (distanceY < 0 && moveDirection == MOVE_DIRECTION_TOP) {
                    moveDirection = -1
                    onSwipeTop()
                    return true
                }
            }
        }
        onMoveCancel()
        return false
    }

    // Called when the gesture ends without a swipe
    open fun onMoveCancel() {
        moveDirection = -1
    }

    open fun onMoveX(distance: Float) {}
    open fun onMoveY(distance: Float) {}
    open fun onSwipeRight() {}
    open fun onSwipeLeft() {}
    open fun onSwipeTop() {}
    open fun onSwipeBottom() {}

    companion object {
        private const val SWIPE_DISTANCE_MIN = 150
        private const val SWIPE_START_DISTANCE_MAX = 150
        private const val MOVE_DIRECTION_RIGHT = 1
        private const val MOVE_DIRECTION_LEFT = 2
        private const val MOVE_DIRECTION_BOTTOM = 3
        private const val MOVE_DIRECTION_TOP = 4
    }
}
