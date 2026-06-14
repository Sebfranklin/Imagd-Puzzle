@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)
package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.JigsawPath
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.JigsawPiece
import com.example.ui.viewmodel.PuzzleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JigsawPuzzleScreen(
    viewModel: PuzzleViewModel,
    modifier: Modifier = Modifier
) {
    val bitmap by viewModel.selectedBitmap.collectAsState()
    val pieces by viewModel.jigsawPieces.collectAsState()
    val gridSize by viewModel.jigsawSize.collectAsState()
    val difficulty by viewModel.jigsawDifficulty.collectAsState()
    val solved by viewModel.jigsawSolved.collectAsState()
    val seconds by viewModel.timerSeconds.collectAsState()
    val moves by viewModel.jigsawMoves.collectAsState()

    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current.density

    Scaffold(
        containerColor = Color(0xFF, 0xFE, 0xF7, 0xFF), // Elegant background #fef7ff
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "VECTOR JIGSAW",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color(0xFF, 0x1D, 0x1B, 0x20)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF, 0x1D, 0x1B, 0x20))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.startJigsawGame(gridSize, difficulty) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Scramble", tint = Color(0xFF, 0x1D, 0x1B, 0x20))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF, 0xFE, 0xF7, 0xFF)
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // High level Jigsaw stats bar (Bento white card style)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFF, 0xE6, 0xE1, 0xE5)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PIECES", fontSize = 11.sp, color = Color(0xFF, 0x49, 0x45, 0x4F), fontWeight = FontWeight.Bold)
                        Text("${gridSize * gridSize}", fontSize = 20.sp, color = Color(0xFF, 0x67, 0x50, 0xA4), fontWeight = FontWeight.Black)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("DIFFICULTY", fontSize = 11.sp, color = Color(0xFF, 0x49, 0x45, 0x4F), fontWeight = FontWeight.Bold)
                        Text(difficulty.uppercase(), fontSize = 15.sp, color = Color(0xFF, 0x1D, 0x1B, 0x20), fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TIME", fontSize = 11.sp, color = Color(0xFF, 0x49, 0x45, 0x4F), fontWeight = FontWeight.Bold)
                        Text(formatTime(seconds), fontSize = 20.sp, color = Color(0xFF, 0x7D, 0x52, 0x60), fontWeight = FontWeight.Black)
                    }
                }
            }

            if (difficulty == "Hard") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF, 0x67, 0x50, 0xA4), modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("HARD: Pieces are rotated! Tap once to rotate by 90°.", fontSize = 11.sp, color = Color(0xFF, 0x67, 0x50, 0xA4))
                }
            }

            // SANDBOX INTERACTIVE PLAY BOARD (Bento Style Canvas)
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF, 0xF3, 0xED, 0xF7))
                    .border(1.dp, Color(0xFF, 0xE6, 0xE1, 0xE5), RoundedCornerShape(24.dp))
            ) {
                val areaWidth = constraints.maxWidth.toFloat()
                val areaHeight = constraints.maxHeight.toFloat()

                // Calculate the actual Jigsaw grid dimensions (keep it proportional and centered)
                val boardDim = minOf(areaWidth, areaHeight) * 0.7f // 70% of board size
                val startX = (areaWidth - boardDim) / 2f
                val startY = (areaHeight - boardDim) / 2f

                if (bitmap != null && pieces.isNotEmpty()) {
                    
                    // A. DRAW BACKGROUND METRICS / GHOST IMAGE UNDERNEATH IF EASY
                    if (difficulty == "Easy") {
                        Box(
                            modifier = Modifier
                                .absoluteOffset(
                                    x = (startX / density).dp,
                                    y = (startY / density).dp
                                )
                                .size((boardDim / density).dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawImage(
                                    image = bitmap!!.asImageBitmap(),
                                    dstSize = IntSize(size.width.toInt(), size.height.toInt()),
                                    alpha = 0.2f // Faint ghost guideline on light screen
                                )
                            }
                        }
                    } else {
                        // Drawing subtle dashed frames on Medium/Hard
                        Box(
                            modifier = Modifier
                                .absoluteOffset(
                                    x = (startX / density).dp,
                                    y = (startY / density).dp
                                )
                                .size((boardDim / density).dp)
                                .border(1.dp, Color(0xFF, 0x67, 0x50, 0xA4).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        )
                    }

                    // B. DRAW RETAINED JIGSAW PIECES
                    // Renting correct order: snapped pieces first (as background), then draggable floating pieces on top
                    val sortedPieces = pieces.sortedBy { it.isSnapped }

                    sortedPieces.forEach { piece ->
                        val pieceWidth = boardDim / gridSize
                        val pieceHeight = boardDim / gridSize

                        // Targets pixels
                        val targetPxX = startX + piece.targetX * boardDim
                        val targetPxY = startY + piece.targetY * boardDim

                        // Current pixel coordinate calculations
                        val cellPxX = if (piece.isSnapped) targetPxX else (piece.currentX * (areaWidth - pieceWidth))
                        val cellPxY = if (piece.isSnapped) targetPxY else (piece.currentY * (areaHeight - pieceHeight))

                        Box(
                            modifier = Modifier
                                .absoluteOffset(
                                    x = (cellPxX / density).dp,
                                    y = (cellPxY / density).dp
                                )
                                .size(
                                    width = (pieceWidth / density).dp,
                                    height = (pieceHeight / density).dp
                                )
                                .graphicsLayer {
                                    // Rotate dynamically if difficult
                                    rotationZ = piece.rotationDegrees
                                }
                                .pointerInput(piece.id, piece.isSnapped) {
                                    if (!piece.isSnapped) {
                                        detectDragGestures(
                                            onDragEnd = {
                                                // Check for snap trigger
                                                // Convert normalized positions
                                                val normX = (cellPxX - startX) / boardDim
                                                val normY = (cellPxY - startY) / boardDim
                                                val snapped = viewModel.attemptSnapJigsawPiece(piece.id)
                                                if (snapped) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                }
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                viewModel.updateJigsawPosition(
                                                    pieceId = piece.id,
                                                    dx = dragAmount.x,
                                                    dy = dragAmount.y,
                                                    containerWidth = areaWidth - pieceWidth,
                                                    containerHeight = areaHeight - pieceHeight
                                                )
                                            }
                                        )
                                    }
                                }
                                .pointerInput(piece.id, piece.isSnapped) {
                                    if (!piece.isSnapped) {
                                        detectTapGestures(
                                            onTap = {
                                                if (difficulty == "Hard") {
                                                    viewModel.rotateJigsawPiece(piece.id)
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                }
                                            }
                                        )
                                    }
                                }
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Cut bounds
                                val left = 0f
                                val top = 0f
                                val right = size.width
                                val bottom = size.height

                                // Draw modern interlocking vectors
                                val piecePath = JigsawPath.createPiecePath(
                                    left = left,
                                    top = top,
                                    right = right,
                                    bottom = bottom,
                                    topEdge = piece.topEdge,
                                    rightEdge = piece.rightEdge,
                                    bottomEdge = piece.bottomEdge,
                                    leftEdge = piece.leftEdge
                                )

                                clipPath(piecePath) {
                                    // Crop pixels corresponding to this piece row and column
                                    val bitmapW = bitmap!!.width
                                    val bitmapH = bitmap!!.height
                                    val tileW = bitmapW / gridSize
                                    val tileH = bitmapH / gridSize
                                    
                                    val srcX = piece.col * tileW
                                    val srcY = piece.row * tileH

                                    drawImage(
                                        image = bitmap!!.asImageBitmap(),
                                        srcOffset = IntOffset(srcX, srcY),
                                        srcSize = IntSize(tileW, tileH),
                                        dstSize = IntSize(size.width.toInt(), size.height.toInt())
                                    )
                                }

                                // Border line
                                drawPath(
                                    path = piecePath,
                                    color = if (piece.isSnapped) Color(0x67, 0x50, 0xA4).copy(alpha = 0.7f) else Color(0x49, 0x45, 0x4F).copy(alpha = 0.4f),
                                    style = Stroke(width = if (piece.isSnapped) 1.5.dp.toPx() else 1.dp.toPx())
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Drag interlocking blocks. Once a tile lands adjacent to its correct grid position, it clicks and snaps into place forever. Align all blocks to finish.",
                fontSize = 11.sp,
                color = Color(0xFF, 0x49, 0x45, 0x4F),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }

    // WIN CELEBRATION MODAL (Light Bento Layout)
    if (solved) {
        AlertDialog(
            onDismissRequest = { viewModel.navigateTo(AppScreen.DASHBOARD) },
            confirmButton = {
                Button(
                    onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF, 0x67, 0x50, 0xA4), contentColor = Color.White)
                ) {
                    Text("TAKE BACK HOME", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.startJigsawGame(gridSize, difficulty) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF, 0x67, 0x50, 0xA4)),
                    border = BorderStroke(1.5.dp, Color(0xFF, 0x67, 0x50, 0xA4)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("PLAY AGAIN", fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text("JIGSAW COMPLETED!", fontWeight = FontWeight.Bold, color = Color(0xFF, 0x1D, 0x1B, 0x20), textAlign = TextAlign.Center)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Magnificent resolution! Every sector locked perfectly.", color = Color(0xFF, 0x49, 0x45, 0x4F))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Total placements: $moves", fontWeight = FontWeight.Bold, color = Color(0xFF, 0x67, 0x50, 0xA4))
                    Text("Duration: ${formatTime(seconds)}", fontWeight = FontWeight.Bold, color = Color(0xFF, 0x7D, 0x52, 0x60))
                }
            },
            containerColor = Color(0xFF, 0xFE, 0xF7, 0xFF),
            shape = RoundedCornerShape(24.dp)
        )
    }
}
