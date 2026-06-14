package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PuzzleDatabase
import com.example.data.PuzzleRepository
import com.example.data.PuzzleScore
import com.example.ui.components.BitmapUtils
import com.example.ui.components.ProceduralGenerator
import com.example.ui.components.RubikCube
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PuzzleViewModel(application: Application) : AndroidViewModel(application) {

    private val db = PuzzleDatabase.getDatabase(application)
    private val repository = PuzzleRepository(db.puzzleDao())

    // UI screen state
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Loaded image bitmap
    private val _selectedBitmap = MutableStateFlow<Bitmap?>(null)
    val selectedBitmap: StateFlow<Bitmap?> = _selectedBitmap.asStateFlow()

    // Track active image preset index (0..3) if custom is false
    private val _proceduralIndex = MutableStateFlow(0)
    val proceduralIndex: StateFlow<Int> = _proceduralIndex.asStateFlow()

    private val _isCustomSelected = MutableStateFlow(false)
    val isCustomSelected: StateFlow<Boolean> = _isCustomSelected.asStateFlow()

    // Game timer and moves count
    private val _timerSeconds = MutableStateFlow(0)
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()
    private var timerJob: Job? = null

    // Score Board flow
    val allScores: StateFlow<List<PuzzleScore>> = repository.allScores
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Load default procedural image index 0 initially
        loadProceduralPreset(0)
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
        if (screen == AppScreen.DASHBOARD || screen == AppScreen.SCORE_BOARD) {
            stopTimer()
        }
    }

    fun loadProceduralPreset(index: Int) {
        _proceduralIndex.value = index
        _isCustomSelected.value = false
        _selectedBitmap.value = ProceduralGenerator.generate(index)
    }

    fun loadCustomImage(uri: Uri) {
        viewModelScope.launch {
            val bitmap = BitmapUtils.loadFromUri(getApplication(), uri)
            if (bitmap != null) {
                _selectedBitmap.value = bitmap
                _isCustomSelected.value = true
            }
        }
    }

    private fun startTimer() {
        stopTimer()
        _timerSeconds.value = 0
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _timerSeconds.value += 1
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun saveScore(mode: String, difficulty: String, dimensions: String, moves: Int) {
        val seconds = _timerSeconds.value
        viewModelScope.launch {
            repository.saveScore(
                PuzzleScore(
                    puzzleMode = mode,
                    difficulty = difficulty,
                    dimensions = dimensions,
                    timeInSeconds = seconds,
                    moves = moves
                )
            )
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearScores()
        }
    }

    // ==========================================
    // 1. SLIDING BLOCK PUZZLE ENGINE
    // ==========================================
    private val _slidingGrid = MutableStateFlow<List<Int>>(emptyList())
    val slidingGrid: StateFlow<List<Int>> = _slidingGrid.asStateFlow()

    private val _slidingSize = MutableStateFlow(3) // Default 3x3
    val slidingSize: StateFlow<Int> = _slidingSize.asStateFlow()

    private val _slidingMoves = MutableStateFlow(0)
    val slidingMoves: StateFlow<Int> = _slidingMoves.asStateFlow()

    private val _slidingSolved = MutableStateFlow(false)
    val slidingSolved: StateFlow<Boolean> = _slidingSolved.asStateFlow()

    fun startSlidingGame(size: Int) {
        _slidingSize.value = size
        _slidingMoves.value = 0
        _slidingSolved.value = false
        
        // Generate grid: 0 until size*size
        val tileCount = size * size
        val list = (0 until tileCount).toMutableList()
        
        // Scramble grid by executing random sliding moves starting from solved state
        // This guarantees a mathematically solvable sequence
        var emptyIndex = tileCount - 1
        for (step in 0..150) {
            val adjacent = getAdjacentIndices(emptyIndex, size)
            val swapWith = adjacent.random()
            
            // Swap
            val temp = list[emptyIndex]
            list[emptyIndex] = list[swapWith]
            list[swapWith] = temp
            emptyIndex = swapWith
        }

        _slidingGrid.value = list
        startTimer()
    }

    private fun getAdjacentIndices(index: Int, size: Int): List<Int> {
        val row = index / size
        val col = index % size
        val adj = mutableListOf<Int>()
        if (row > 0) adj.add((row - 1) * size + col)
        if (row < size - 1) adj.add((row + 1) * size + col)
        if (col > 0) adj.add(row * size + (col - 1))
        if (col < size - 1) adj.add(row * size + (col + 1))
        return adj
    }

    fun slideTile(clickedIndex: Int): Boolean {
        if (_slidingSolved.value) return false
        
        val grid = _slidingGrid.value.toMutableList()
        val size = _slidingSize.value
        val emptyIndex = grid.indexOf(size * size - 1)
        
        // Check adjacency
        val rowClick = clickedIndex / size
        val colClick = clickedIndex % size
        val rowEmpty = emptyIndex / size
        val colEmpty = emptyIndex % size

        val isAdjacent = (Math.abs(rowClick - rowEmpty) == 1 && colClick == colEmpty) ||
                         (Math.abs(colClick - colEmpty) == 1 && rowClick == rowEmpty)

        if (isAdjacent) {
            // Swap clicked with empty
            grid[emptyIndex] = grid[clickedIndex]
            grid[clickedIndex] = size * size - 1
            _slidingGrid.value = grid
            _slidingMoves.value += 1

            // Verify if completed
            checkSlidingSolved()
            return true
        }
        return false
    }

    private fun checkSlidingSolved() {
        val grid = _slidingGrid.value
        val size = _slidingSize.value
        val tileCount = size * size
        
        var solved = true
        for (i in 0 until tileCount) {
            if (grid[i] != i) {
                solved = false
                break
            }
        }
        if (solved) {
            _slidingSolved.value = true
            stopTimer()
            saveScore(
                mode = "SLIDING",
                difficulty = if (size == 3) "EASY" else "MEDIUM",
                dimensions = "${size}x${size}",
                moves = _slidingMoves.value
            )
        }
    }


    // ==========================================
    // 2. RUBIK'S CUBE ENGINES
    // ==========================================
    private val _rubikCube = MutableStateFlow(RubikCube())
    val rubikCube: StateFlow<RubikCube> = _rubikCube.asStateFlow()

    private val _rubikSolved = MutableStateFlow(false)
    val rubikSolved: StateFlow<Boolean> = _rubikSolved.asStateFlow()

    private val _rubikMoves = MutableStateFlow(0)
    val rubikMoves: StateFlow<Int> = _rubikMoves.asStateFlow()

    fun startRubikGame() {
        val cube = RubikCube()
        cube.scramble(20)
        _rubikCube.value = cube
        _rubikMoves.value = 0
        _rubikSolved.value = false
        startTimer()
    }

    fun makeRubikMove(move: String) {
        if (_rubikSolved.value) return
        
        val cube = _rubikCube.value
        cube.applyMove(move)
        _rubikMoves.value = cube.movesCount
        
        // Trigger flow update
        _rubikCube.value = RubikCube().apply {
            this.faces = cube.faces.clone()
            this.movesCount = cube.movesCount
        }

        if (cube.isSolved()) {
            _rubikSolved.value = true
            stopTimer()
            saveScore(
                mode = "RUBIK",
                difficulty = "MEDIUM",
                dimensions = "3x3 Map",
                moves = _rubikMoves.value
            )
        }
    }


    // ==========================================
    // 3. JIGSAW PUZZLE ENGINES
    // ==========================================
    private val _jigsawPieces = MutableStateFlow<List<JigsawPiece>>(emptyList())
    val jigsawPieces: StateFlow<List<JigsawPiece>> = _jigsawPieces.asStateFlow()

    private val _jigsawSize = MutableStateFlow(3) // 3x3 = 9 pieces default
    val jigsawSize: StateFlow<Int> = _jigsawSize.asStateFlow()

    private val _jigsawDifficulty = MutableStateFlow("Medium")
    val jigsawDifficulty: StateFlow<String> = _jigsawDifficulty.asStateFlow()

    private val _jigsawSolved = MutableStateFlow(false)
    val jigsawSolved: StateFlow<Boolean> = _jigsawSolved.asStateFlow()

    private val _jigsawMoves = MutableStateFlow(0)
    val jigsawMoves: StateFlow<Int> = _jigsawMoves.asStateFlow()

    fun startJigsawGame(gridSize: Int, difficulty: String) {
        _jigsawSize.value = gridSize
        _jigsawDifficulty.value = difficulty
        _jigsawSolved.value = false
        _jigsawMoves.value = 0

        val totalPieces = gridSize * gridSize
        val list = mutableListOf<JigsawPiece>()

        // Generate edge cuts randomly and consistently
        // Horizontal boundaries inside the grid
        val horizCuts = Array(gridSize - 1) { IntArray(gridSize) }
        for (r in 0 until gridSize - 1) {
            for (c in 0 until gridSize) {
                horizCuts[r][c] = if (Math.random() < 0.5) 1 else -1
            }
        }
        // Vertical boundaries inside the grid
        val vertCuts = Array(gridSize) { IntArray(gridSize - 1) }
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize - 1) {
                vertCuts[r][c] = if (Math.random() < 0.5) 1 else -1
            }
        }

        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                val targetX = c / gridSize.toFloat()
                val targetY = r / gridSize.toFloat()

                // Neighbors cuts mapping
                val topEdge = if (r == 0) 0 else -horizCuts[r - 1][c]
                val bottomEdge = if (r == gridSize - 1) 0 else horizCuts[r][c]
                val leftEdge = if (c == 0) 0 else -vertCuts[r][c - 1]
                val rightEdge = if (c == gridSize - 1) 0 else vertCuts[r][c]

                // Scatter positions randomly in sandbox canvas [0.0..0.8]
                val scatterX = (Math.random() * 0.7f).toFloat()
                val scatterY = (Math.random() * 0.7f).toFloat()

                // Scramble rotation if Hard difficulty
                val rot = if (difficulty == "Hard") {
                    listOf(0f, 90f, 180f, 270f).random()
                } else {
                    0f
                }

                list.add(
                    JigsawPiece(
                        id = r * gridSize + c,
                        row = r,
                        col = c,
                        targetX = targetX,
                        targetY = targetY,
                        currentX = scatterX,
                        currentY = scatterY,
                        topEdge = topEdge,
                        rightEdge = rightEdge,
                        bottomEdge = bottomEdge,
                        leftEdge = leftEdge,
                        rotationDegrees = rot,
                        isSnapped = false
                    )
                )
            }
        }

        _jigsawPieces.value = list
        startTimer()
    }

    fun updateJigsawPosition(pieceId: Int, dx: Float, dy: Float, containerWidth: Float, containerHeight: Float) {
        if (_jigsawSolved.value) return
        
        val list = _jigsawPieces.value.map { piece ->
            if (piece.id == pieceId && !piece.isSnapped) {
                // Adjust normalized percents based on move
                val nx = (piece.currentX + dx / containerWidth).coerceIn(0.0f, 0.9f)
                val ny = (piece.currentY + dy / containerHeight).coerceIn(0.0f, 0.9f)
                piece.copy(currentX = nx, currentY = ny)
            } else {
                piece
            }
        }
        _jigsawPieces.value = list
    }

    fun rotateJigsawPiece(pieceId: Int): Boolean {
        if (_jigsawSolved.value) return false
        var mutated = false
        
        val list = _jigsawPieces.value.map { piece ->
            if (piece.id == pieceId && !piece.isSnapped) {
                val currentRot = piece.rotationDegrees
                val nextRot = (currentRot + 90f) % 360f
                mutated = true
                piece.copy(rotationDegrees = nextRot)
            } else {
                piece
            }
        }
        if (mutated) {
            _jigsawPieces.value = list
            _jigsawMoves.value += 1
        }
        return mutated
    }

    fun attemptSnapJigsawPiece(pieceId: Int): Boolean {
        if (_jigsawSolved.value) return false
        var snapped = false
        
        val list = _jigsawPieces.value.map { piece ->
            if (piece.id == pieceId && !piece.isSnapped) {
                // Match criteria:
                // 1. Coordinates close to targets
                // 2. Rotation matches 0f
                val dx = Math.abs(piece.currentX - piece.targetX)
                val dy = Math.abs(piece.currentY - piece.targetY)
                val matchCoord = dx < 0.08f && dy < 0.08f
                val matchRotation = piece.rotationDegrees == 0f

                if (matchCoord && matchRotation) {
                    snapped = true
                    piece.copy(
                        currentX = piece.targetX,
                        currentY = piece.targetY,
                        isSnapped = true
                    )
                } else {
                    piece
                }
            } else {
                piece
            }
        }

        if (snapped) {
            _jigsawPieces.value = list
            _jigsawMoves.value += 1
            checkJigsawSolved()
        }
        return snapped
    }

    private fun checkJigsawSolved() {
        val pieces = _jigsawPieces.value
        val allSnapped = pieces.none { !it.isSnapped }
        if (allSnapped) {
            _jigsawSolved.value = true
            stopTimer()
            saveScore(
                mode = "JIGSAW",
                difficulty = _jigsawDifficulty.value.uppercase(),
                dimensions = "${_jigsawSize.value}x${_jigsawSize.value}",
                moves = _jigsawMoves.value
            )
        }
    }
}
