package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

data class RubikSticker(
    val originalFace: Int, // 0..5
    val row: Int,          // 0..2
    val col: Int           // 0..2
)

enum class CubeFace(val index: Int) {
    FRONT(0),
    BACK(1),
    UP(2),
    DOWN(3),
    LEFT(4),
    RIGHT(5)
}

class RubikCube {
    // 6 faces, each is 3x3 grid of Stickers
    var faces: Array<Array<Array<RubikSticker>>> = Array(6) { faceIndex ->
        Array(3) { r ->
            Array(3) { c ->
                RubikSticker(faceIndex, r, c)
            }
        }
    }

    var movesCount = 0

    init {
        reset()
    }

    fun reset() {
        faces = Array(6) { faceIndex ->
            Array(3) { r ->
                Array(3) { c ->
                    RubikSticker(faceIndex, r, c)
                }
            }
        }
        movesCount = 0
    }

    fun isSolved(): Boolean {
        // Solved if every face has stickers matching the face index
        for (f in 0..5) {
            val targetFace = f
            for (r in 0..2) {
                for (c in 0..2) {
                    if (faces[f][r][c].originalFace != targetFace) return false
                }
            }
        }
        return true
    }

    // Helper to rotate a 3x3 face clockwise
    private fun rotateFaceClockwise(f: Int) {
        val temp = Array(3) { r -> Array(3) { c -> faces[f][r][c] } }
        for (r in 0..2) {
            for (c in 0..2) {
                faces[f][r][c] = temp[2 - c][r]
            }
        }
    }

    // Helper to rotate a 3x3 face counter-clockwise
    private fun rotateFaceCounterClockwise(f: Int) {
        val temp = Array(3) { r -> Array(3) { c -> faces[f][r][c] } }
        for (r in 0..2) {
            for (c in 0..2) {
                faces[f][r][c] = temp[c][2 - r]
            }
        }
    }

    // Apply standard moves
    fun applyMove(moveName: String, recordMove: Boolean = true) {
        if (recordMove) movesCount++
        when (moveName) {
            "U" -> rotateU()
            "U'" -> { rotateU(); rotateU(); rotateU() }
            "D" -> rotateD()
            "D'" -> { rotateD(); rotateD(); rotateD() }
            "L" -> rotateL()
            "L'" -> { rotateL(); rotateL(); rotateL() }
            "R" -> rotateR()
            "R'" -> { rotateR(); rotateR(); rotateR() }
            "F" -> rotateF()
            "F'" -> { rotateF(); rotateF(); rotateF() }
            "B" -> rotateB()
            "B'" -> { rotateB(); rotateB(); rotateB() }
        }
    }

    fun scramble(steps: Int = 15) {
        val possibleMoves = listOf("U", "U'", "D", "D'", "L", "L'", "R", "R'", "F", "F'", "B", "B'")
        for (i in 0 until steps) {
            val m = possibleMoves.random()
            applyMove(m, recordMove = false)
        }
        movesCount = 0
    }

    // --- MOVE IMPLEMENTATIONS ---

    private fun rotateU() {
        rotateFaceClockwise(2) // UP is index 2
        // Adjacent elements: row 0 of L(4), F(0), R(5), B(1)
        val temp = Array(3) { faces[4][0][it] } // Left row 0
        for (i in 0..2) faces[4][0][i] = faces[0][0][i] // Left row 0 = Front row 0
        for (i in 0..2) faces[0][0][i] = faces[5][0][i] // Front row 0 = Right row 0
        for (i in 0..2) faces[5][0][i] = faces[1][0][i] // Right row 0 = Back row 0
        for (i in 0..2) faces[1][0][i] = temp[i]         // Back row 0 = old Left row 0
    }

    private fun rotateD() {
        rotateFaceClockwise(3) // DOWN is index 3
        // Adjacent elements: row 2 of L(4), B(1), R(5), F(0)
        val temp = Array(3) { faces[4][2][it] } // Left row 2
        for (i in 0..2) faces[4][2][i] = faces[1][2][i] // Left row 2 = Back row 2
        for (i in 0..2) faces[1][2][i] = faces[5][2][i] // Back row 2 = Right row 2
        for (i in 0..2) faces[5][2][i] = faces[0][2][i] // Right row 2 = Front row 2
        for (i in 0..2) faces[0][2][i] = temp[i]         // Front row 2 = old Left row 2
    }

    private fun rotateF() {
        rotateFaceClockwise(0) // FRONT is index 0
        // Adjacent: Bottom row of UP(2), Left col of RIGHT(5), Top row of DOWN(3), Right col of LEFT(4)
        val temp = Array(3) { faces[2][2][it] } // UP bottom row (row 2)
        
        // UP bottom row = LEFT right col (col 2), reversed ordering
        for (i in 0..2) faces[2][2][i] = faces[4][2 - i][2]
        
        // LEFT right col = DOWN top row (row 0), same order mapping
        for (i in 0..2) faces[4][i][2] = faces[3][0][i]
        
        // DOWN top row = RIGHT left col (col 0), reversed order mapping
        for (i in 0..2) faces[3][0][2 - i] = faces[5][i][0]
        
        // RIGHT left col = old UP bottom row
        for (i in 0..2) faces[5][i][0] = temp[i]
    }

    private fun rotateB() {
        rotateFaceClockwise(1) // BACK is index 1
        // Adjacent: Top row of UP(2), Left col of LEFT(4), Bottom row of DOWN(3), Right col of RIGHT(5)
        val temp = Array(3) { faces[2][0][it] } // UP top row (row 0)
        
        // UP top row = RIGHT right col (col 2), same order mapping
        for (i in 0..2) faces[2][0][i] = faces[5][i][2]
        
        // RIGHT right col = DOWN bottom row (row 2), reversed order mapping
        for (i in 0..2) faces[5][2 - i][2] = faces[3][2][i]
        
        // DOWN bottom row = LEFT left col (col 0), same order mapping
        for (i in 0..2) faces[3][2][i] = faces[4][i][0]
        
        // LEFT left col = old UP top row, reversed order modeling
        for (i in 0..2) faces[4][2 - i][0] = temp[i]
    }

    private fun rotateL() {
        rotateFaceClockwise(4) // LEFT is index 4
        // Adjacent: Left col of UP(2), F(0), DOWN(3), BACK(1) (with back inverted)
        val temp = Array(3) { faces[2][it][0] } // UP left col (col 0)
        
        // UP left col = BACK right col (col 2) inverted
        for (i in 0..2) faces[2][i][0] = faces[1][2 - i][2]
        
        // BACK right col = DOWN left col (col 0) inverted
        for (i in 0..2) faces[1][i][2] = faces[3][2 - i][0]
        
        // DOWN left col = FRONT left col (col 0)
        for (i in 0..2) faces[3][i][0] = faces[0][i][0]
        
        // FRONT left col = old UP left col
        for (i in 0..2) faces[0][i][0] = temp[i]
    }

    private fun rotateR() {
        rotateFaceClockwise(5) // RIGHT is index 5
        // Adjacent: Right col of UP(2), BACK(1) inverted, DOWN(3) inverted, F(0)
        val temp = Array(3) { faces[2][it][2] } // UP right col (col 2)
        
        // UP right col = FRONT right col (col 2)
        for (i in 0..2) faces[2][i][2] = faces[0][i][2]
        
        // FRONT right col = DOWN right col (col 2)
        for (i in 0..2) faces[0][i][2] = faces[3][i][2]
        
        // DOWN right col = BACK left col (col 0) inverted
        for (i in 0..2) faces[3][i][2] = faces[1][2 - i][0]
        
        // BACK left col = old UP right col inverted
        for (i in 0..2) faces[1][i][0] = temp[2 - i]
    }
}
