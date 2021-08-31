package com.training.finalsticker2



import android.graphics.Matrix
import android.graphics.PointF
import android.util.FloatMath
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.ImageView
import java.lang.Math.sqrt
import kotlin.math.sqrt


class Touch : OnTouchListener
{
    var lastEvent: FloatArray? = null
    var d = 0f
    var newRot = 0f
    private val matrix = Matrix()
    private val savedMatrix = Matrix()
    var fileNAME: String? = null
    var framePos = 0

    private val scale = 0f
    private val newDist = 0f

    // We can be in one of these 3 states
    private val NONE = 0
    private val DRAG = 1
    private val ZOOM = 2
    private var mode = NONE

    // Remember some things for zooming
    private val start = PointF()
    private val mid = PointF()
    var oldDist = 1f

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val view = v as ImageView
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                savedMatrix.set(matrix)
                start[event.x] = event.y
                mode = DRAG
                lastEvent = null
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                oldDist = spacing(event)
                if (oldDist > 10f) {
                    savedMatrix.set(matrix)
                    midPoint(mid, event)
                    mode = ZOOM
                }
                lastEvent = FloatArray(4)
                lastEvent!![0] = event.getX(0)
                lastEvent!![1] = event.getX(1)
                lastEvent!![2] = event.getY(0)
                lastEvent!![3] = event.getY(1)
                d = rotation(event)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                mode = NONE
                lastEvent = null
            }
            MotionEvent.ACTION_MOVE -> if (mode == DRAG) {
                // ...
                matrix.set(savedMatrix)
                matrix.postTranslate(
                    event.x - start.x, event.y
                            - start.y
                )
            } else if (mode == ZOOM && event.pointerCount == 2) {
                val newDist = spacing(event)
                matrix.set(savedMatrix)
                if (newDist > 10f) {
                    val scale = newDist / oldDist
                    matrix.postScale(scale, scale, mid.x, mid.y)
                }
                if (lastEvent != null) {
                    newRot = rotation(event)
                    val r = newRot - d
                    matrix.postRotate(
                        r, (view.measuredWidth / 2).toFloat(), (
                                view.measuredHeight / 2).toFloat()
                    )
                }
            }
        }
        view.imageMatrix = matrix
        return true
    }
//For rotate image on multi-touch.

    //For rotate image on multi-touch.
    private fun rotation(event: MotionEvent): Float {
        val delta_x = (event.getX(0) - event.getX(1)).toDouble()
        val delta_y = (event.getY(0) - event.getY(1)).toDouble()
        val radians = Math.atan2(delta_y, delta_x)
        return Math.toDegrees(radians).toFloat()
    }

    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt(x * x + y * y).toFloat()
    }

    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point[x / 2] = y / 2
    }
}