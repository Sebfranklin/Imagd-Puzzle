package com.example.ui.viewmodel

enum class AppScreen {
    DASHBOARD,
    SLIDING_GAME,
    RUBIK_GAME,
    JIGSAW_GAME,
    SCORE_BOARD
}

data class JigsawPiece(
    val id: Int,
    val row: Int,
    val col: Int,
    val targetX: Float,  // Normalized 0.0f..1.0f on the grid
    val targetY: Float,  // Normalized 0.0f..1.0f on the grid
    var currentX: Float, // Dynamic percentage 0.0f..1.0f in sandbox
    var currentY: Float, // Dynamic percentage 0.0f..1.0f in sandbox
    val topEdge: Int,    // 0 = Flat, 1 = Tab, -1 = Blank
    val rightEdge: Int,
    val bottomEdge: Int,
    val leftEdge: Int,
    var isSnapped: Boolean = false,
    var rotationDegrees: Float = 0f // 0, 90, 180, 270 for hard mode
)
