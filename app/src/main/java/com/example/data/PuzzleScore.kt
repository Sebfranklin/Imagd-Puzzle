package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "puzzle_scores")
data class PuzzleScore(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val puzzleMode: String, // "SLIDING", "RUBIK", "JIGSAW"
    val difficulty: String, // "EASY", "MEDIUM", "HARD"
    val dimensions: String, // e.g. "3x3", "4x4", "9 pieces"
    val timeInSeconds: Int,
    val moves: Int,
    val timestamp: Long = System.currentTimeMillis()
)
