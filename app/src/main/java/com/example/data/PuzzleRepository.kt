package com.example.data

import kotlinx.coroutines.flow.Flow

class PuzzleRepository(private val puzzleDao: PuzzleDao) {
    val allScores: Flow<List<PuzzleScore>> = puzzleDao.getAllScores()

    fun getBestScores(mode: String): Flow<List<PuzzleScore>> = puzzleDao.getBestScoresForMode(mode)

    suspend fun saveScore(score: PuzzleScore) {
        puzzleDao.insertScore(score)
    }

    suspend fun clearScores() {
        puzzleDao.clearAllScores()
    }
}
