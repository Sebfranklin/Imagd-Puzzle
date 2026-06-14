package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PuzzleDao {
    @Query("SELECT * FROM puzzle_scores ORDER BY timestamp DESC")
    fun getAllScores(): Flow<List<PuzzleScore>>

    @Query("SELECT * FROM puzzle_scores WHERE puzzleMode = :mode ORDER BY timeInSeconds ASC LIMIT 5")
    fun getBestScoresForMode(mode: String): Flow<List<PuzzleScore>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: PuzzleScore)

    @Query("DELETE FROM puzzle_scores")
    suspend fun clearAllScores()
}
