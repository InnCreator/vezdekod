package ru.inncreator.vezdekod.utils

import android.R.attr.*
import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import timber.log.Timber
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign


@SuppressLint("ClickableViewAccessibility")
class SelectedScaleView(private val view: View, private val container: View) : ScaleGestureDetector.OnScaleGestureListener{

    var isSelected = false


    private val MIN_ZOOM = 1.0f
    private val MAX_ZOOM = 4.0f

    private var mode: Mode = Mode.NONE
    private var scale = 1.0f
    private var lastScaleFactor = 0f

    // Where the finger first  touches the screen
    private var startX = 0f
    private var startY = 0f

    // How much to translate the canvas
    private var dx = 0f
    private var dy = 0f
    private var prevDx = 0f
    private var prevDy = 0f

    init {

        val scaleDetector = ScaleGestureDetector(container.context, this)

        Timber.d("Init")

        container.setOnTouchListener { _, motionEvent ->

            Timber.v("touch")
            if (isSelected){
                when (motionEvent.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_DOWN -> {
                        Timber.v("DOWN")
                        if (scale > MIN_ZOOM) {
                            mode = Mode.DRAG
                            startX = motionEvent.x - prevDx
                            startY = motionEvent.y - prevDy
                        }

                    }
                    MotionEvent.ACTION_MOVE -> if (mode == Mode.DRAG) {
                        dx = motionEvent.x - startX
                        dy = motionEvent.y - startY
                    }
                    MotionEvent.ACTION_POINTER_DOWN -> mode = Mode.ZOOM
                    MotionEvent.ACTION_POINTER_UP -> mode = Mode.NONE
                    MotionEvent.ACTION_UP -> {
                        Timber.v("UP")
                        mode = Mode.NONE
                        prevDx = dx
                        prevDy = dy
                    }
                    else -> {}



                }

                scaleDetector.onTouchEvent(motionEvent)

                if (mode === Mode.DRAG && scale >= MIN_ZOOM || mode === Mode.ZOOM) {
                    container.parent
                        .requestDisallowInterceptTouchEvent(true)
                    val maxDx: Float = (child().width - child().width / scale) / 2 * scale
                    val maxDy: Float = (child().height - child().height / scale) / 2 * scale
                    dx = min(max(dx, -maxDx), maxDx)
                    dy = min(max(dy, -maxDy), maxDy)
                    Timber.v(
                        "Width: " + child().width
                            .toString() + ", scale " + scale.toString() + ", dx " + dx
                            .toString() + ", max " + maxDx
                    )
                    applyScaleAndTranslation()
                }
            }

            return@setOnTouchListener true
        }
    }

    private fun applyScaleAndTranslation() {
        child().scaleX = scale
        child().scaleY = scale
        child().translationX = dx
        child().translationY = dy
    }

    private fun child(): View {
//        return getChildAt(0)
        return view
    }




    // ScaleGestureDetector

    override fun onScale(p0: ScaleGestureDetector): Boolean {
        val scaleFactor: Float = p0.scaleFactor
        Timber.v("mode:" + this.mode + ", onScale:" + scaleFactor)
        if (lastScaleFactor == 0.0f || sign(scaleFactor) == sign(
                lastScaleFactor
            )
        ) {
            scale *= scaleFactor
            scale = max(MIN_ZOOM, min(scale, MAX_ZOOM))
            lastScaleFactor = scaleFactor
        } else {
            lastScaleFactor = 0.0f
        }
        if (container != null) {
            val orgWidth = container.width
            val _width = (orgWidth.toFloat() * scale).toInt()
            val _height = (container.height.toFloat() * scale).toInt()
            val params: ViewGroup.LayoutParams = container.layoutParams as ViewGroup.LayoutParams
            params.height = _height
            params.width = _width
            this.container.layoutParams = params
            child().scaleX = scale
            child().scaleY = scale
        }
        return true
    }

    override fun onScaleBegin(p0: ScaleGestureDetector?): Boolean {
        Timber.v("onScaleBegin")
        return true
    }

    override fun onScaleEnd(p0: ScaleGestureDetector?) {
        Timber.v("onScaleEnd")
    }


    private enum class Mode {
        NONE, DRAG, ZOOM
    }

}