package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import kotlin.math.cos
import kotlin.math.sin

object ProceduralGenerator {
    
    val presets = listOf(
        "Neon Cyber Grid" to "Glowing hot pink lines and neon cyan circles on an obsidian background.",
        "Pastel Aurora" to "A serene blend of swirling lavender, magenta, and solar orange gradients.",
        "Celestial Portal" to "Starry depths featuring concentric galactic geometry and a golden core.",
        "Geometric Prism" to "Sharp abstract triangles, grids, and shards in high-contrast primary hues."
    )

    fun generate(presetIndex: Int, width: Int = 600, height: Int = 600): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        when (presetIndex) {
            0 -> drawCyberGrid(canvas, paint, width, height)
            1 -> drawPastelAurora(canvas, paint, width, height)
            2 -> drawCelestialPortal(canvas, paint, width, height)
            else -> drawGeometricPrism(canvas, paint, width, height)
        }

        return bitmap
    }

    private fun drawCyberGrid(canvas: Canvas, paint: Paint, w: Int, h: Int) {
        // Background
        canvas.drawColor(Color.parseColor("#0F0C1B")) // Deep cyber space

        // Radiant glow from center
        val centerGlow = RadialGradient(
            w / 2f, h / 2f, w * 0.7f,
            Color.parseColor("#3300FF99"),
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        paint.shader = centerGlow
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Grid lines
        paint.strokeWidth = 3f
        paint.style = Paint.Style.STROKE
        val gridSpacing = w / 12f
        
        // Vertical perspective-like lines
        for (i in 0..12) {
            val x = i * gridSpacing
            paint.color = Color.parseColor("#FF007F") // Neon Pink
            paint.alpha = 150
            canvas.drawLine(x, 0f, x, h.toFloat(), paint)
            
            // Horizontal lines
            paint.color = Color.parseColor("#00E5FF") // Neon Cyan
            canvas.drawLine(0f, x, w.toFloat(), x, paint)
        }

        // Draw concentric neon rings in the center
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f
        val centerColors = intArrayOf(Color.parseColor("#FF007F"), Color.parseColor("#00E5FF"), Color.parseColor("#FFFF00"))
        
        for (i in 1..4) {
            paint.color = centerColors[i % 3]
            paint.alpha = 200 - i * 35
            canvas.drawCircle(w / 2f, h / 2f, (w * 0.12f) * i, paint)
        }

        // Star-like particles
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        val rawCoords = listOf(
            0.15f to 0.25f, 0.82f to 0.15f, 0.75f to 0.85f, 0.22f to 0.78f,
            0.50f to 0.10f, 0.90f to 0.50f, 0.10f to 0.55f, 0.45f to 0.90f
        )
        for (coord in rawCoords) {
            paint.alpha = 255
            canvas.drawCircle(coord.first * w, coord.second * h, 6f, paint)
            // Draw cross-hair for glow
            paint.color = Color.parseColor("#00E5FF")
            paint.strokeWidth = 2f
            val cx = coord.first * w
            val cy = coord.second * h
            canvas.drawLine(cx - 15f, cy, cx + 15f, cy, paint)
            canvas.drawLine(cx, cy - 15f, cx, cy + 15f, paint)
            paint.color = Color.WHITE
        }
    }

    private fun drawPastelAurora(canvas: Canvas, paint: Paint, w: Int, h: Int) {
        // Deep indigo night sky
        canvas.drawColor(Color.parseColor("#12075C"))

        // Add broad linear shift
        val skyGradient = LinearGradient(
            0f, 0f, w.toFloat(), h.toFloat(),
            Color.parseColor("#2C1075"),
            Color.parseColor("#0F0326"),
            Shader.TileMode.CLAMP
        )
        paint.shader = skyGradient
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)

        // Draw swirling luminous nodes (Aurora waves)
        val colors = listOf(
            Color.parseColor("#CC14E3"), // Vibrant violet
            Color.parseColor("#FF2E93"), // Bright Magenta
            Color.parseColor("#FF8E53")  // Warm Orange
        )

        // Draw overlapping circular glows mimicking plasma
        val centers = listOf(
            0.3f to 0.4f, 0.7f to 0.6f, 0.5f to 0.8f, 0.8f to 0.2f
        )
        
        for (idx in centers.indices) {
            val center = centers[idx]
            val color = colors[idx % colors.size]
            val radius = w * 0.45f
            
            val circGlow = RadialGradient(
                center.first * w, center.second * h, radius,
                color,
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
            paint.shader = circGlow
            canvas.drawCircle(center.first * w, center.second * h, radius, paint)
        }

        // Add abstract curved overlays
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 10f
        paint.color = Color.WHITE
        paint.alpha = 60

        val wavePath = Path()
        wavePath.moveTo(0f, h * 0.5f)
        wavePath.cubicTo(w * 0.25f, h * 0.3f, w * 0.75f, h * 0.7f, w.toFloat(), h * 0.4f)
        canvas.drawPath(wavePath, paint)

        wavePath.reset()
        wavePath.moveTo(0f, h * 0.7f)
        wavePath.cubicTo(w * 0.3f, h * 0.85f, w * 0.6f, h * 0.45f, w.toFloat(), h * 0.65f)
        canvas.drawPath(wavePath, paint)
    }

    private fun drawCelestialPortal(canvas: Canvas, paint: Paint, w: Int, h: Int) {
        // Space
        canvas.drawColor(Color.parseColor("#050811"))

        // Giant dark-blue galactic corona
        val bgGlow = RadialGradient(
            w / 2f, h / 2f, w / 2f,
            Color.parseColor("#1B2244"),
            Color.parseColor("#050811"),
            Shader.TileMode.CLAMP
        )
        paint.shader = bgGlow
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Draw background stars
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        for (i in 0..40) {
            val sx = (Math.random() * w).toFloat()
            val sy = (Math.random() * h).toFloat()
            paint.alpha = 80 + (Math.random() * 175).toInt()
            val size = 2f + (Math.random() * 4f).toFloat()
            canvas.drawCircle(sx, sy, size, paint)
        }

        // Cosmic central rings (Golden portal style)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        for (r in 1..6) {
            paint.color = Color.parseColor("#FFD700") // Gold
            paint.alpha = 250 - r * 35
            canvas.drawCircle(w / 2f, h / 2f, r * 45f, paint)
        }

        // Draw central radiant solar flare
        val sunGlow = RadialGradient(
            w / 2f, h / 2f, 40f,
            Color.parseColor("#FFFFFF"),
            Color.parseColor("#FFA500"), // Orange
            Shader.TileMode.CLAMP
        )
        paint.style = Paint.Style.FILL
        paint.shader = sunGlow
        canvas.drawCircle(w / 2f, h / 2f, 42f, paint)
        paint.shader = null

        // Constellation lines
        paint.color = Color.parseColor("#00FFCC")
        paint.alpha = 100
        paint.strokeWidth = 2f
        paint.style = Paint.Style.STROKE
        val cx = w / 2f
        val cy = h / 2f
        for (angle in 0 until 360 step 30) {
            val rads = Math.toRadians(angle.toDouble())
            val px = cx + cos(rads) * 260
            val py = cy + sin(rads) * 260
            canvas.drawLine(cx, cy, px.toFloat(), py.toFloat(), paint)
        }
    }

    private fun drawGeometricPrism(canvas: Canvas, paint: Paint, w: Int, h: Int) {
        // Dark energetic slate
        canvas.drawColor(Color.parseColor("#1E2022"))

        // Add geometric diagonal slices
        paint.style = Paint.Style.FILL
        
        // Piece 1: Warm Red triangle
        paint.color = Color.parseColor("#D92B5F")
        val path1 = Path()
        path1.moveTo(0f, 0f)
        path1.lineTo(w * 0.75f, 0f)
        path1.lineTo(0f, h * 0.55f)
        path1.close()
        canvas.drawPath(path1, paint)

        // Piece 2: Cool Teal triangle
        paint.color = Color.parseColor("#1BCBB6")
        val path2 = Path()
        path2.moveTo(w.toFloat(), h.toFloat())
        path2.lineTo(w.toFloat(), h * 0.4f)
        path2.lineTo(w * 0.25f, h.toFloat())
        path2.close()
        canvas.drawPath(path2, paint)

        // Piece 3: Emerald Accent
        paint.color = Color.parseColor("#44D242")
        paint.alpha = 180
        val path3 = Path()
        path3.moveTo(w * 0.5f, h * 0.3f)
        path3.lineTo(w * 0.85f, h * 0.65f)
        path3.lineTo(w * 0.4f, h * 0.9f)
        path3.close()
        canvas.drawPath(path3, paint)

        // Grid overlap overlay
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = Color.WHITE
        paint.alpha = 60
        val gap = w / 15f
        for (i in 0..15) {
            canvas.drawLine(0f, i * gap, w.toFloat(), i * gap, paint)
            canvas.drawLine(i * gap, 0f, i * gap, h.toFloat(), paint)
        }
    }
}
