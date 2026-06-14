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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.PuzzleViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SlidingPuzzleScreen(
    viewModel: PuzzleViewModel,
    modifier: Modifier = Modifier
) {
    val bitmap by viewModel.selectedBitmap.collectAsState()
    val grid by viewModel.slidingGrid.collectAsState()
    val gridSize by viewModel.slidingSize.collectAsState()
    val moves by viewModel.slidingMoves.collectAsState()
    val solved by viewModel.slidingSolved.collectAsState()
    val seconds by viewModel.timerSeconds.collectAsState()

    val haptic = LocalHapticFeedback.current

    Scaffold(
        containerColor = Color(0xFF, 0xFE, 0xF7, 0xFF), // Elegant background #fef7ff
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "SLIDING BLOCK",
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
                    IconButton(onClick = { viewModel.startSlidingGame(gridSize) }) {
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Stats Panel (Bento Style white card)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFF, 0xE6, 0xE1, 0xE5)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("MOVES", fontSize = 11.sp, color = Color(0xFF, 0x49, 0x45, 0x4F), fontWeight = FontWeight.Bold)
                        Text("$moves", fontSize = 24.sp, color = Color(0xFF, 0x67, 0x50, 0xA4), fontWeight = FontWeight.Black)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TIME ELAPSED", fontSize = 11.sp, color = Color(0xFF, 0x49, 0x45, 0x4F), fontWeight = FontWeight.Bold)
                        Text(
                            formatTime(seconds),
                            fontSize = 24.sp,
                            color = Color(0xFF, 0x70, 0x5D, 0x00),
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // The Sliding Game Board (Square view, style rounded-[28px])
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF, 0xF3, 0xED, 0xF7))
                    .border(1.dp, Color(0xFF, 0xE6, 0xE1, 0xE5), RoundedCornerShape(28.dp))
                    .padding(8.dp)
            ) {
                val boardSize = maxWidth
                val cellSize = boardSize / gridSize

                if (bitmap != null && grid.isNotEmpty()) {
                    // Draw cells
                    Column(modifier = Modifier.fillMaxSize()) {
                        for (r in 0 until gridSize) {
                            Row(modifier = Modifier.weight(1f)) {
                                for (c in 0 until gridSize) {
                                    val index = r * gridSize + c
                                    val tileValue = grid[index]

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                    ) {
                                        if (tileValue != gridSize * gridSize - 1) {
                                            // Tile chunk content
                                            val srcRow = tileValue / gridSize
                                            val srcCol = tileValue % gridSize

                                            Canvas(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable {
                                                        // Play tactile click
                                                        if (viewModel.slideTile(index)) {
                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        }
                                                    }
                                            ) {
                                                val tileW = bitmap!!.width / gridSize
                                                val tileH = bitmap!!.height / gridSize
                                                val srcX = srcCol * tileW
                                                val srcY = srcRow * tileH

                                                drawImage(
                                                    image = bitmap!!.asImageBitmap(),
                                                    srcOffset = IntOffset(srcX, srcY),
                                                    srcSize = IntSize(tileW, tileH),
                                                    dstSize = IntSize(size.width.toInt(), size.height.toInt())
                                                )

                                                // Inner bento purple highlight border
                                                drawRect(
                                                    color = Color(0x67, 0x50, 0xA4).copy(alpha = 0.25f),
                                                    style = Stroke(width = 3.dp.toPx())
                                                )
                                            }
                                        } else {
                                            // Empty cell indicator (Bento Light Grey placeholder)
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color(0xFF, 0xE6, 0xE1, 0xE5).copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Helper Hint info
            Text(
                text = "Tap any highlighted tile adjacent to the empty cell to slide it. Reassemble the image in the correct grid structure to resolve.",
                fontSize = 11.sp,
                color = Color(0xFF, 0x49, 0x45, 0x4F),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }

    // CELEBRATION SOLVED OVERLAY DIALOG (Light Bento Layout)
    if (solved) {
        AlertDialog(
            onDismissRequest = { viewModel.navigateTo(AppScreen.DASHBOARD) },
            confirmButton = {
                Button(
                    onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF, 0x67, 0x50, 0xA4), contentColor = Color.White)
                ) {
                    Text("BACK TO STUDIO", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.startSlidingGame(gridSize) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF, 0x67, 0x50, 0xA4)),
                    border = BorderStroke(1.5.dp, Color(0xFF, 0x67, 0x50, 0xA4)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("PLAY AGAIN", fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text("PUZZLE RESOLVED!", fontWeight = FontWeight.Bold, color = Color(0xFF, 0x1D, 0x1B, 0x20), textAlign = TextAlign.Center)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Congratulations! Beautiful alignment.", color = Color(0xFF, 0x49, 0x45, 0x4F))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Total moves: $moves", fontWeight = FontWeight.Bold, color = Color(0xFF, 0x67, 0x50, 0xA4))
                    Text("Total time: ${formatTime(seconds)}", fontWeight = FontWeight.Bold, color = Color(0xFF, 0x70, 0x5D, 0x00))
                }
            },
            containerColor = Color(0xFF, 0xFE, 0xF7, 0xFF),
            shape = RoundedCornerShape(24.dp)
        )
    }
}

// Global clock formatter helper
fun formatTime(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return String.format("%02d:%02d", m, s)
}
