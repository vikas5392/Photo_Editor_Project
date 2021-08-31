package com.training.sticker


import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.View

class StickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {
    private val mScaleDetector: ScaleGestureDetector
    var mIconWidth = 0.0f
    var mIconHeight = 0.0f
    var mMatrix: Matrix = Matrix()
    private var mScaleFactor = 1f
    private val mIcon: Drawable
    private var mPosX = 0f
    private var mPosY = 0f
    private var mLastTouchX = 0f
    private var mLastTouchY = 0f

    private inner class ScaleListener : SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            mScaleFactor *= detector.scaleFactor
            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f))
            invalidate()
            return true
        }
    }

    // The ‘active pointer’ is the one currently moving our object.
    private var mActivePointerId = INVALID_POINTER_ID
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        // Let the ScaleGestureDetector inspect all events.
        mScaleDetector.onTouchEvent(ev)
        val action = ev.action
        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                val x = ev.x
                val y = ev.y
                mLastTouchX = x
                mLastTouchY = y
                mActivePointerId = ev.getPointerId(0)
            }
            MotionEvent.ACTION_MOVE -> {
                val pointerIndex = ev.findPointerIndex(mActivePointerId)
                val x = ev.getX(pointerIndex)
                val y = ev.getY(pointerIndex)

                // Only move if the ScaleGestureDetector isn't processing a gesture.
                if (!mScaleDetector.isInProgress) {
                    val dx = x - mLastTouchX
                    val dy = y - mLastTouchY
                    mPosX += dx
                    mPosY += dy
                    invalidate()
                }
                mLastTouchX = x
                mLastTouchY = y
            }
            MotionEvent.ACTION_UP -> {
                mActivePointerId = INVALID_POINTER_ID
            }
            MotionEvent.ACTION_CANCEL -> {
                mActivePointerId = INVALID_POINTER_ID
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val pointerIndex =
                    (ev.action and MotionEvent.ACTION_POINTER_INDEX_MASK           //to know the id of the finger that went up
                            shr MotionEvent.ACTION_POINTER_INDEX_SHIFT)
                val pointerId = ev.getPointerId(pointerIndex)
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    val newPointerIndex = if (pointerIndex == 0) 1 else 0
                    mLastTouchX = ev.getX(newPointerIndex)
                    mLastTouchY = ev.getY(newPointerIndex)
                    mActivePointerId = ev.getPointerId(newPointerIndex)
                }
            }
        }
        return true
    }

    companion object {
        private const val INVALID_POINTER_ID = -1
    }

    init {
        mIcon = context.resources.getDrawable(R.drawable.like_image)
        mIconWidth = mIcon.intrinsicWidth.toFloat()
        mIconHeight = mIcon.intrinsicHeight.toFloat()
        mIcon.setBounds(0, 0, mIconWidth.toInt(), mIconHeight.toInt())
        // Create our ScaleGestureDetector
        mScaleDetector = ScaleGestureDetector(context, ScaleListener())
    }

    public override fun onDraw(canvas: Canvas) {
        val density = resources.displayMetrics.density
        val widthDp = width / density
        val heightDp = width / density
        super.onDraw(canvas)
        canvas.save()
        canvas.translate(mPosX, mPosY)
        canvas.scale(
            mScaleFactor,
            mScaleFactor,
            widthDp / 2 - (x / density),
            heightDp / 2 - (y / density)
        )
        mIcon.draw(canvas)
        canvas.restore()
    }

}