package com.example.ui.components

import androidx.compose.ui.graphics.Path

object JigsawPath {

    /**
     * Builds a closed Path for a jigsaw piece at specified rectangle boundaries
     * with custom interlocking edge configurations (0 = flat, 1 = tab, -1 = blank).
     */
    fun createPiecePath(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        topEdge: Int,
        rightEdge: Int,
        bottomEdge: Int,
        leftEdge: Int
    ): Path {
        val path = Path()
        path.moveTo(left, top)

        // 1. TOP EDGE (Left to Right)
        appendInterlockingEdge(
            path = path,
            x1 = left, y1 = top,
            x2 = right, y2 = top,
            edgeType = topEdge,
            isVertical = false
        )

        // 2. RIGHT EDGE (Top to Bottom)
        appendInterlockingEdge(
            path = path,
            x1 = right, y1 = top,
            x2 = right, y2 = bottom,
            edgeType = rightEdge,
            isVertical = true
        )

        // 3. BOTTOM EDGE (Right to Left)
        appendInterlockingEdge(
            path = path,
            x1 = right, y1 = bottom,
            x2 = left, y2 = bottom,
            edgeType = bottomEdge,
            isVertical = false
        )

        // 4. LEFT EDGE (Bottom to Top)
        appendInterlockingEdge(
            path = path,
            x1 = left, y1 = bottom,
            x2 = left, y2 = top,
            edgeType = leftEdge,
            isVertical = true
        )

        path.close()
        return path
    }

    private fun appendInterlockingEdge(
        path: Path,
        x1: Float, y1: Float,
        x2: Float, y2: Float,
        edgeType: Int,
        isVertical: Boolean
    ) {
        if (edgeType == 0) {
            path.lineTo(x2, y2)
            return
        }

        val dx = x2 - x1
        val dy = y2 - y1
        val length = Math.hypot(dx.toDouble(), dy.toDouble()).toFloat()

        if (length == 0f) return

        // Under normal orientation (e.g. going top left to right):
        // Tan vector is in direction of movement.
        // Normal vector points outward.
        val tx = dx / length
        val ty = dy / length

        // Normal vector (outward) depends on travel direction
        // For classic piece rectangles:
        // Top edge: Left->Right. Outward normal is (0, -1) (upward).
        // Right edge: Top->Bottom. Outward normal is (1, 0) (rightward).
        // Bottom edge: Right->Left. Outward normal is (0, 1) (downward).
        // Left edge: Bottom->Top. Outward normal is (-1, 0) (leftward).
        // We can get outward normal by taking (-ty, tx)
        val nx = -ty * edgeType
        val ny = tx * edgeType

        // Tab proportions
        val tabDepth = length * 0.17f
        val tabNeckWidth = length * 0.14f
        val tabHeadWidth = length * 0.22f

        // Divide the edge into portions
        // Midpoint
        val mx = (x1 + x2) / 2f
        val my = (y1 + y2) / 2f

        // Transition points
        val pA_x = x1 + dx * 0.38f
        val pA_y = y1 + dy * 0.38f

        val pB_x = x2 - dx * 0.38f
        val pB_y = y2 - dy * 0.38f

        // Neck bases (narrower)
        val neck1_x = mx - tx * (tabNeckWidth / 2f)
        val neck1_y = my - ty * (tabNeckWidth / 2f)

        val neck2_x = mx + tx * (tabNeckWidth / 2f)
        val neck2_y = my + ty * (tabNeckWidth / 2f)

        // Bulb peak
        val peak_x = mx + nx * tabDepth
        val peak_y = my + ny * tabDepth

        // Bulb ears (wider)
        val ear1_x = peak_x - tx * (tabHeadWidth / 2f)
        val ear1_y = peak_y - ty * (tabHeadWidth / 2f)

        val ear2_x = peak_x + tx * (tabHeadWidth / 2f)
        val ear2_y = peak_y + ty * (tabHeadWidth / 2f)

        // Draw a smooth interlocking puzzle connector
        path.lineTo(pA_x, pA_y)
        
        // Curved transition from straight line into the narrow neck:
        path.quadraticTo(
            neck1_x - nx * (tabDepth * 0.2f), neck1_y - ny * (tabDepth * 0.2f),
            neck1_x, neck1_y
        )

        // Curved transition spreading out into bulb ear 1:
        path.cubicTo(
            neck1_x + nx * (tabDepth * 0.5f), neck1_y + ny * (tabDepth * 0.5f),
            ear1_x - tx * (tabHeadWidth * 0.1f), ear1_y - ty * (tabHeadWidth * 0.1f),
            ear1_x, ear1_y
        )

        // Curve shaping across the top of the bulb/protrusion:
        path.quadraticTo(
            peak_x, peak_y,
            ear2_x, ear2_y
        )

        // Curved transition from ear 2 down to the narrow neck 2:
        path.cubicTo(
            ear2_x + tx * (tabHeadWidth * 0.1f), ear2_y + ty * (tabHeadWidth * 0.1f),
            neck2_x + nx * (tabDepth * 0.5f), neck2_y + ny * (tabDepth * 0.5f),
            neck2_x, neck2_y
        )

        // Curved transition back to the main straight edge line:
        path.quadraticTo(
            neck2_x - nx * (tabDepth * 0.2f), neck2_y - ny * (tabDepth * 0.2f),
            pB_x, pB_y
        )

        path.lineTo(x2, y2)
    }
}
