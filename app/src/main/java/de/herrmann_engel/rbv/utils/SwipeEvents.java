package de.herrmann_engel.rbv.utils;

import android.view.MotionEvent;
import android.view.View;

public class SwipeEvents implements View.OnTouchListener {

    private static final int SWIPE_DISTANCE_MIN = 150;
    private static final int SWIPE_START_DISTANCE_MIN = 100;
    private static final int MOVE_DIRECTION_RIGHT = 1;
    private static final int MOVE_DIRECTION_LEFT = 2;
    private static final int MOVE_DIRECTION_BOTTOM = 3;
    private static final int MOVE_DIRECTION_TOP = 4;

    private float initialY;
    private float initialX;
    private int moveDirection;

    public SwipeEvents() {
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            initialX = event.getX();
            initialY = event.getY();
            moveDirection = 0;
            return true;
        } else if (action == MotionEvent.ACTION_MOVE) {
            float distanceX = event.getX() - initialX;
            float distanceY = event.getY() - initialY;
            if (Math.abs(distanceX) > Math.abs(distanceY)) {
                if (Math.abs(distanceX) < SWIPE_DISTANCE_MIN) {
                    if (moveDirection == 0) {
                        if (distanceX > 0 && initialX < SWIPE_START_DISTANCE_MIN) {
                            moveDirection = MOVE_DIRECTION_RIGHT;
                        } else if (initialX > v.getWidth() - SWIPE_START_DISTANCE_MIN) {
                            moveDirection = MOVE_DIRECTION_LEFT;
                        }
                        return true;
                    } else if (distanceX > 0 && moveDirection == MOVE_DIRECTION_RIGHT || distanceX < 0 && moveDirection == MOVE_DIRECTION_LEFT) {
                        onMoveX(distanceX);
                        return true;
                    }
                } else if (distanceX > 0 && moveDirection == MOVE_DIRECTION_RIGHT) {
                    moveDirection = -1;
                    onSwipeRight();
                    return true;
                } else if (distanceX < 0 && moveDirection == MOVE_DIRECTION_LEFT) {
                    moveDirection = -1;
                    onSwipeLeft();
                    return true;
                }
            } else {
                if (Math.abs(distanceY) < SWIPE_DISTANCE_MIN) {
                    if (moveDirection == 0) {
                        if (distanceY > 0 && initialY < SWIPE_START_DISTANCE_MIN) {
                            moveDirection = MOVE_DIRECTION_BOTTOM;
                        } else if (initialY > v.getHeight() - SWIPE_START_DISTANCE_MIN) {
                            moveDirection = MOVE_DIRECTION_TOP;
                        }
                        return true;
                    } else if (distanceY > 0 && moveDirection == MOVE_DIRECTION_BOTTOM || distanceY < 0 && moveDirection == MOVE_DIRECTION_TOP) {
                        onMoveY(distanceY);
                        return true;
                    }
                } else if (distanceY > 0 && moveDirection == MOVE_DIRECTION_BOTTOM) {
                    moveDirection = -1;
                    onSwipeBottom();
                    return true;
                } else if (distanceY < 0 && moveDirection == MOVE_DIRECTION_TOP) {
                    moveDirection = -1;
                    onSwipeTop();
                    return true;
                }
            }
        }
        onMoveCancel();
        return false;
    }

    public void onMoveCancel() {
        moveDirection = -1;
    }

    public void onMoveX(float distance) {
    }

    public void onMoveY(float distance) {
    }

    public void onSwipeRight() {
    }

    public void onSwipeLeft() {
    }

    public void onSwipeTop() {
    }

    public void onSwipeBottom() {
    }

}
