package com.anwesh.uiprojects.hortoverlineview

/**
 * Created by anweshmishra on 19/09/18.
 */

import android.app.Activity
import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF

val nodes : Int = 5

val color : Int = Color.parseColor("#4527A0")

val strokeWidthFactor : Int = 60

fun Canvas.drawHTVNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / (nodes + 1)
    paint.strokeWidth = Math.min(w, h) / strokeWidthFactor
    paint.strokeCap = Paint.Cap.ROUND
    paint.color = color
    save()
    translate(gap * i + gap, h/2)
    for (j in 0..1) {
        val sf : Float = 1f - 2 * j
        val sc : Float = Math.min(0.5f, Math.max(0f, scale - j * 0.5f)) * 2
        save()
        rotate(90f * sc)
        drawLine(0f, 0f, (gap/2) * sf, 0f, paint)
        restore()
        val size : Float = gap / 8
        val y : Float = -0.4f * h * (sf)
        drawRect(RectF(-gap/2, y - size, -gap/2 + gap * sc, y + size), paint)
    }
    restore()
}

class HorToVerLineView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += 0.05f * this.dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class HTVNode(var i : Int, val state : State = State()) {
        private var next : HTVNode? = null
        private var prev : HTVNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = HTVNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawHTVNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : HTVNode {
            var curr : HTVNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class HorToVerLine(var i : Int) {
        private var root : HTVNode = HTVNode(0)
        private var curr : HTVNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : HorToVerLineView) {

        private val hvl : HorToVerLine = HorToVerLine(0)

        private val animator : Animator = Animator(view)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            hvl.draw(canvas, paint)
            animator.animate {
                hvl.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            hvl.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        private var view : HorToVerLineView? = null
        fun create(activity : Activity) : HorToVerLineView? {
            if (view == null) {
                view = HorToVerLineView(activity)
                activity.setContentView(view)
            }
            return view
        }
    }
}