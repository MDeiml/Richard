package com.mdeiml.richard
import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import java.util.ArrayList
import android.util.Log
import java.util.Locale

class ChartView(context: Context, attr: AttributeSet) : View(context, attr) {

    enum class Type {
        WIN, IMPORTANCE, IMPORTANCE_WIN
    }

    private val IMPORTANCE_WIN_PARTS = 6
   
    private val bluePaint: Paint
    private val redPaint: Paint
    private val axisPaint: Paint
    private val indicatorPaint: Paint
    private val textPaint: Paint
    private val scalePaint: Paint
    private val imp = IntArray(IMPORTANCE_WIN_PARTS)
    private val ns = IntArray(IMPORTANCE_WIN_PARTS)
    var labelA = ""
    var labelB = ""
    var match: Match? = null
    var type = Type.WIN
    
    init {
        val res = context.resources
        val met = res.displayMetrics
        val dpr = met.densityDpi / DisplayMetrics.DENSITY_DEFAULT

        bluePaint = Paint()
        bluePaint.color = res.getColor(R.color.primaryLight)
        bluePaint.strokeWidth = 2.0f * dpr
        bluePaint.setAntiAlias(true)
        bluePaint.strokeCap = Paint.Cap.ROUND

        redPaint = Paint()
        redPaint.color = res.getColor(R.color.primaryRedLight)
        redPaint.strokeWidth = 2.0f * dpr
        redPaint.setAntiAlias(true)
        redPaint.strokeCap = Paint.Cap.ROUND
        
        axisPaint = Paint()
        axisPaint.color = res.getColor(R.color.textContent)
        axisPaint.strokeWidth = 0.0f
        axisPaint.style = Paint.Style.STROKE

        
        indicatorPaint = Paint()
        indicatorPaint.color = 0xffdddddd.toInt()
        indicatorPaint.strokeWidth = 0.0f
        indicatorPaint.style = Paint.Style.STROKE
        
        textPaint = Paint()
        textPaint.color = res.getColor(R.color.textContent)
        textPaint.setAntiAlias(true)
        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.textSize = 14.0f *dpr
        
        scalePaint = Paint()
        scalePaint.color = res.getColor(R.color.textContent)
        scalePaint.setAntiAlias(true)
        scalePaint.textAlign = Paint.Align.CENTER
        scalePaint.textSize = 14.0f * dpr
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val locale = context.resources.configuration.locales[0];
        val res = context.resources
        val met = res.displayMetrics
        val dpr = met.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT.toFloat()
        
        val w = canvas.width - this.paddingLeft - this.paddingRight - dpr*15
        val h = canvas.height - this.paddingTop - this.paddingBottom.toFloat()
        val xs = this.paddingLeft.toFloat()
        val ys = this.paddingRight.toFloat()

        val ls = xs + dpr*20
        val halfText = dpr*7

        when (type) {
            Type.WIN -> {
                canvas.drawLine(ls, ys+h/2, ls+w, ys+h/2, axisPaint)
                canvas.drawText("50% ", ls, ys+h/2+halfText, textPaint)
                val n = 2
                for (i in 1..n) {
                    val y = i*h/n/2
                    canvas.drawLine(ls, ys+h/2+y, ls+w, ys+h/2+y, indicatorPaint)
                    canvas.drawText(String.format(locale, "%d%% ", 50*i/n+50), ls, ys+h/2+y+halfText, textPaint)
                    canvas.drawLine(ls, ys+h/2-y, ls+w, ys+h/2-y, indicatorPaint)
                    canvas.drawText(String.format(locale, "%d%% ", 50*i/n+50), ls, ys+h/2-y+halfText, textPaint)
                }
            }
            Type.IMPORTANCE -> {
                canvas.drawLine(ls, ys+h, ls+w, ys+h, axisPaint)
                canvas.drawText("0% ", ls, ys+h+halfText, textPaint)
                val n = 4
                for (i in 1..n) {
                    val y = i*h/n
                    canvas.drawLine(ls, ys+h-y, ls+w, ys+h-y, indicatorPaint)
                    canvas.drawText(String.format(locale, "%d%% ", 20*i/n), ls, ys+h-y+halfText, textPaint)
                }
            }
            Type.IMPORTANCE_WIN -> {}
        }

        match?.let { match ->
            if (type == Type.WIN || type == Type.IMPORTANCE) {
                val numEntries = (match.sets.sumBy { it.games.sumBy { it.points.size }} / 50 + 1) * 50
                val entryWidth = w.toFloat() / numEntries.toFloat()
                var lastVal = 0.0f
                var index = 0
                var points = match.sets.flatMap { it.games.flatMap { it.points } }

                for (point in points) {
                    val v = when (type) {
                        Type.WIN -> point.winProb
                        Type.IMPORTANCE -> point.importance * 5
                        else -> TODO()
                    }
                    if(index != 0) {
                        val x0 = ((index - 1) * entryWidth).toInt()
                        val y0 = ((1 - lastVal) * h).toInt()
                        val x1 = (index * entryWidth).toInt()
                        val y1 = ((1 - v) * h).toInt()
                        canvas.drawLine(ls + x0, ys + y0, ls + x1, ys + y1, bluePaint)
                    }
                    lastVal = v
                    index++
                }
            } else if (type == Type.IMPORTANCE_WIN) {
                val points = match.sets.flatMap { it.games.flatMap { it.points } }.filter { it.winner != null }
                val pointsImp = points.map { it.importance }
                val n = Math.min(points.count(), IMPORTANCE_WIN_PARTS)

                if (n > 0) {
                    val minImp = pointsImp.min()!!
                    val maxImp = pointsImp.max()!!
                    val scale = if (n == 1) 1.0f else (maxImp - minImp) / (n - 1)
                    var maxN = 0
                    for (i in 0..IMPORTANCE_WIN_PARTS-1) {
                        ns[i] = 0
                        imp[i] = 0
                    }
                    for (point in points) {
                        if(point.winner != null) {
                            val i = Math.round((point.importance - minImp) / scale)
                            if (point.winner == Match.Player.PLAYER_A) imp[i]++
                            ns[i]++
                            maxN = Math.max(Math.max(maxN, imp[i]), ns[i] - imp[i])
                        }
                    }
                    val nlines = 4
                    canvas.drawLine(ls, ys+h, ls+w, ys+h, axisPaint)
                    canvas.drawText("0", ls, ys+h+halfText, textPaint)
                    for(i in 1..nlines) {
                        val v = maxN*i/nlines
                        val y = v*h/maxN
                        canvas.drawLine(ls, ys+h-y, ls+w, ys+h-y, indicatorPaint)
                        canvas.drawText(String.format(locale, "%d", v), ls, ys+h-y+halfText, textPaint)
                    }
                    val entryWidth = w.toFloat() / n.toFloat()
                    for(i in 0..n-1) {
                        val val1 = imp[i].toFloat() / maxN.toFloat()
                        val val2 = (ns[i] - imp[i]).toFloat() / maxN.toFloat()
                        val x0 = (entryWidth * (i + 0.15)).toInt()
                        val x1 = (entryWidth * (i + 0.45)).toInt()
                        val xt = (entryWidth * (i + 0.5)).toInt()
                        val x2 = (entryWidth * (i + 0.55)).toInt()
                        val x3 = (entryWidth * (i + 0.85)).toInt()
                        val y1 = Math.max(2, (val1 * h).toInt())
                        val y2 = Math.max(2, (val2 * h).toInt())
                        canvas.drawRect(ls + x0, ys + h, ls + x1, ys + h - y1, redPaint)
                        canvas.drawRect(ls + x2, ys + h, ls + x3, ys + h - y2, bluePaint)
                        canvas.drawText(String.format(locale, "%.1f%%", (minImp + scale * i) * 100), ls + xt, ys + h + 2 * halfText, scalePaint)
                    }
                } else {
                    val nlines = 4
                    canvas.drawLine(ls, ys+h, ls+w, ys+h, axisPaint)
                    canvas.drawText("0", ls, ys+h+halfText, textPaint)
                    for(i in 1..nlines) {
                        val v = i
                        val y = v*h/nlines
                        canvas.drawLine(ls, ys+h-y, ls+w, ys+h-y, indicatorPaint)
                        canvas.drawText(String.format(locale, "%d ", i), ls, ys+h-y+halfText, textPaint)
                    }
                }
            }
        }
        
        canvas.drawText(labelA, xs+5*dpr, ys+15*dpr, textPaint)
        canvas.drawText(labelB, xs+5*dpr, ys+h-5*dpr, textPaint)
    }
    
    fun drawMatch(match: Match) {
        this.match = match
        invalidate()
    }

}
