@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)
package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Path
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
import com.example.ui.components.CubeFace
import com.example.ui.components.RubikCube
import com.example.ui.components.RubikSticker
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.PuzzleViewModel
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RubiksCubeScreen(
    viewModel: PuzzleViewModel,
    modifier: Modifier = Modifier
) {
    val bitmap by viewModel.selectedBitmap.collectAsState()
    val cube by viewModel.rubikCube.collectAsState()
    val moves by viewModel.rubikMoves.collectAsState()
    val solved by viewModel.rubikSolved.collectAsState()
    val seconds by viewModel.timerSeconds.collectAsState()

    val haptic = LocalHapticFeedback.current

    // Set colors for 3D hologram representation of faces
    val faceColors = listOf(
        Color(0xFF, 0x02, 0x88, 0xD1), // FRONT: Deep Blue
        Color(0xFF, 0xC2, 0x18, 0x5B), // BACK: Rose Pink
        Color(0xFF, 0xFB, 0xC0, 0x2D), // UP: Yellow
        Color(0xFF, 0x38, 0x8E, 0x3C), // DOWN: Green
        Color(0xFF, 0x7B, 0x1F, 0xA2), // LEFT: Purple
        Color(0xFF, 0xE6, 0x4A, 0x19)  // RIGHT: Orange
    )

    Scaffold(
        containerColor = Color(0xFF, 0xFE, 0xF7, 0xFF), // Elegant background #fef7ff
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "3D RUBIK'S MAP",
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
                    IconButton(onClick = { viewModel.startRubikGame() }) {
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stats Panel (Bento white card style)
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
                        Text("ROTATIONS", fontSize = 11.sp, color = Color(0xFF, 0x49, 0x45, 0x4F), fontWeight = FontWeight.Bold)
                        Text("$moves", fontSize = 20.sp, color = Color(0xFF, 0x67, 0x50, 0xA4), fontWeight = FontWeight.Black)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ELAPSED TIME", fontSize = 11.sp, color = Color(0xFF, 0x49, 0x45, 0x4F), fontWeight = FontWeight.Bold)
                        Text(
                            formatTime(seconds),
                            fontSize = 20.sp,
                            color = Color(0xFF, 0x7D, 0x52, 0x60),
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Top: GORGEOUS HOLO-3D ISOMETRIC CUBE VIEW (Canvas rendering)
            Text(
                text = "Holographic 3D projection (showing Up, Front, Right)",
                fontSize = 11.sp,
                color = Color(0xFF, 0x49, 0x45, 0x4F),
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, Color(0xFF, 0xE6, 0xE1, 0xE5), RoundedCornerShape(20.dp))
                    .background(Color(0xFF, 0xF3, 0xED, 0xF7)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(120.dp)) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val du = 16f // scale factor of isometric edges

                    val cos30 = 0.866f
                    val sin30 = 0.5f

                    // 1. UP Face - Top Diamond
                    for (r in 0..2) {
                        for (c in 0..2) {
                            val sticker = cube.faces[2][r][c] // Up face
                            val path = Path()
                            
                            // Coordinate math
                            val p0_x = cx + (c - r) * du * cos30
                            val p0_y = cy + (r + c - 2) * du * sin30 - du * 1.5f
                            
                            val p1_x = cx + (c + 1 - r) * du * cos30
                            val p1_y = cy + (r + c + 1 - 2) * du * sin30 - du * 1.5f

                            val p2_x = cx + (c + 1 - (r + 1)) * du * cos30
                            val p2_y = cy + (r + 1 + c + 1 - 2) * du * sin30 - du * 1.5f

                            val p3_x = cx + (c - (r + 1)) * du * cos30
                            val p3_y = cy + (r + 1 + c - 2) * du * sin30 - du * 1.5f

                            path.moveTo(p0_x, p0_y)
                            path.lineTo(p1_x, p1_y)
                            path.lineTo(p2_x, p2_y)
                            path.lineTo(p3_x, p3_y)
                            path.close()

                            drawPath(
                                path = path,
                                color = faceColors[sticker.originalFace]
                            )
                            drawPath(
                                path = path,
                                color = Color.Black.copy(alpha = 0.8f),
                                style = Stroke(width = 2f)
                            )
                        }
                    }

                    // 2. FRONT Face - Left Skewed Wall
                    for (r in 0..2) {
                        for (c in 0..2) {
                            val sticker = cube.faces[0][r][c] // Front face
                            val path = Path()

                            val offset_x = -du * 1.5f * cos30
                            val offset_y = -du * 0.5f * sin30

                            val p0_x = cx + (c - 3) * du * cos30
                            val p0_y = cy + (r + c) * du * sin30 - offset_y
                            
                            val p1_x = cx + (c + 1 - 3) * du * cos30
                            val p1_y = cy + (r + c + 1) * du * sin30 - offset_y

                            val p2_x = cx + (c + 1 - 3) * du * cos30
                            val p2_y = cy + (r + 1 + c + 1) * du * sin30 - offset_y + du

                            val p3_x = cx + (c - 3) * du * cos30
                            val p3_y = cy + (r + 1 + c) * du * sin30 - offset_y + du

                            path.moveTo(p0_x, p0_y)
                            path.lineTo(p1_x, p1_y)
                            path.lineTo(p2_x, p2_y)
                            path.lineTo(p3_x, p3_y)
                            path.close()

                            drawPath(
                                path = path,
                                color = faceColors[sticker.originalFace]
                            )
                            drawPath(
                                path = path,
                                color = Color.Black.copy(alpha = 0.8f),
                                style = Stroke(width = 2f)
                            )
                        }
                    }

                    // 3. RIGHT Face - Right Skewed Wall
                    for (r in 0..2) {
                        for (c in 0..2) {
                            val sticker = cube.faces[5][r][c] // Right face
                            val path = Path()

                            val p0_x = cx + c * du * cos30
                            val p0_y = cy + (r - c) * du * sin30 + du * 0.5f
                            
                            val p1_x = cx + (c + 1) * du * cos30
                            val p1_y = cy + (r - (c + 1)) * du * sin30 + du * 0.5f

                            val p2_x = cx + (c + 1) * du * cos30
                            val p2_y = cy + (r + 1 - (c + 1)) * du * sin30 + du * 0.5f + du

                            val p3_x = cx + c * du * cos30
                            val p3_y = cy + (r + 1 - c) * du * sin30 + du * 0.5f + du

                            path.moveTo(p0_x, p0_y)
                            path.lineTo(p1_x, p1_y)
                            path.lineTo(p2_x, p2_y)
                            path.lineTo(p3_x, p3_y)
                            path.close()

                            drawPath(
                                path = path,
                                color = faceColors[sticker.originalFace]
                            )
                            drawPath(
                                path = path,
                                color = Color.Black.copy(alpha = 0.8f),
                                style = Stroke(width = 2f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Middle: FLAT IMAGE MAPPED UN-FOLDED CROSS CONTROLLER
            Text(
                text = "Interactive Image-Mapped Faces (Flat Map Mode)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Dynamic 2D Unfolded Grid Controller
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.3f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x13, 0x10, 0x24))
                    .border(2.dp, Color(0x3D, 0x30, 0x75), RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                // Layout representation of standard Rubik's cross
                // Matrix sizes: Width is 4 blocks, Height is 3 blocks
                // Block 1,0 (row 1, col 0) -> Left
                // Block 1,1 -> Front
                // Block 1,2 -> Right
                // Block 1,3 -> Back
                // Block 0,1 -> Up
                // Block 2,1 -> Down
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val bw = maxWidth / 4f
                    val bh = maxHeight / 3f
                    val subTileSize = minOf(bw, bh) // size of a 3x3 face

                    if (bitmap != null) {
                        // UP Face (row 0, col 1)
                        Box(
                            modifier = Modifier
                                .size(subTileSize)
                                .offset(x = subTileSize, y = 0.dp)
                        ) {
                            UnfoldedFaceGrid(bitmap = bitmap!!, faceIndex = 2, stickers = cube.faces[2], borderAccent = faceColors[2])
                        }

                        // LEFT Face (row 1, col 0)
                        Box(
                            modifier = Modifier
                                .size(subTileSize)
                                .offset(x = 0.dp, y = subTileSize)
                        ) {
                            UnfoldedFaceGrid(bitmap = bitmap!!, faceIndex = 4, stickers = cube.faces[4], borderAccent = faceColors[4])
                        }

                        // FRONT Face (row 1, col 1)
                        Box(
                            modifier = Modifier
                                .size(subTileSize)
                                .offset(x = subTileSize, y = subTileSize)
                        ) {
                            UnfoldedFaceGrid(bitmap = bitmap!!, faceIndex = 0, stickers = cube.faces[0], borderAccent = faceColors[0])
                        }

                        // RIGHT Face (row 1, col 2)
                        Box(
                            modifier = Modifier
                                .size(subTileSize)
                                .offset(x = subTileSize * 2, y = subTileSize)
                        ) {
                            UnfoldedFaceGrid(bitmap = bitmap!!, faceIndex = 5, stickers = cube.faces[5], borderAccent = faceColors[5])
                        }

                        // BACK Face (row 1, col 3)
                        Box(
                            modifier = Modifier
                                .size(subTileSize)
                                .offset(x = subTileSize * 3, y = subTileSize)
                        ) {
                            UnfoldedFaceGrid(bitmap = bitmap!!, faceIndex = 1, stickers = cube.faces[1], borderAccent = faceColors[1])
                        }

                        // DOWN Face (row 2, col 1)
                        Box(
                            modifier = Modifier
                                .size(subTileSize)
                                .offset(x = subTileSize, y = subTileSize * 2)
                        ) {
                            UnfoldedFaceGrid(bitmap = bitmap!!, faceIndex = 3, stickers = cube.faces[3], borderAccent = faceColors[3])
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Controller Actions Panel (Buttons to permute lines with tactile sounds)
            Text(
                text = "Layer Permuters (Swap 3-Sticker Rings)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF, 0x1D, 0x1B, 0x20),
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Rows of Permuter buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PermuteButton("Top ↻ (U)", Color(0xFF, 0x82, 0x6E, 0x00), Modifier.weight(1f)) {
                        viewModel.makeRubikMove("U")
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    PermuteButton("Top ↺ (U')", Color(0xFF, 0x82, 0x6E, 0x00), Modifier.weight(1f)) {
                        viewModel.makeRubikMove("U'")
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PermuteButton("Front ↻ (F)", Color(0xFF, 0x02, 0x88, 0xD1), Modifier.weight(1f)) {
                        viewModel.makeRubikMove("F")
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    PermuteButton("Front ↺ (F')", Color(0xFF, 0x02, 0x88, 0xD1), Modifier.weight(1f)) {
                        viewModel.makeRubikMove("F'")
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PermuteButton("Right ↻ (R)", Color(0xFF, 0xE6, 0x4A, 0x19), Modifier.weight(1f)) {
                        viewModel.makeRubikMove("R")
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    PermuteButton("Right ↺ (R')", Color(0xFF, 0xE6, 0x4A, 0x19), Modifier.weight(1f)) {
                        viewModel.makeRubikMove("R'")
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PermuteButton("Left ↻ (L)", Color(0xFF, 0x7B, 0x1F, 0xA2), Modifier.weight(1f)) {
                        viewModel.makeRubikMove("L")
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    PermuteButton("Left ↺ (L')", Color(0xFF, 0x7B, 0x1F, 0xA2), Modifier.weight(1f)) {
                        viewModel.makeRubikMove("L'")
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Perform moves to align every sticker back to its original face block. The flat layout will form the unified complete picture on every side when solved!",
                    fontSize = 11.sp,
                    color = Color(0xFF, 0x49, 0x45, 0x4F),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // WINNER CELEBRATION (Light Bento Layout)
    if (solved) {
        AlertDialog(
            onDismissRequest = { viewModel.navigateTo(AppScreen.DASHBOARD) },
            confirmButton = {
                Button(
                    onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF, 0x67, 0x50, 0xA4), contentColor = Color.White)
                ) {
                    Text("TAKE ME BACK", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.startRubikGame() },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF, 0x67, 0x50, 0xA4)),
                    border = BorderStroke(1.5.dp, Color(0xFF, 0x67, 0x50, 0xA4)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("PLAY AGAIN", fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text("RUBIK SOLVED!", fontWeight = FontWeight.Bold, color = Color(0xFF, 0x1D, 0x1B, 0x20), textAlign = TextAlign.Center)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Spectacular! You solved the multi-face image mapping puzzle!", color = Color(0xFF, 0x49, 0x45, 0x4F))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Rotations: $moves", fontWeight = FontWeight.Bold, color = Color(0xFF, 0x67, 0x50, 0xA4))
                    Text("Total time: ${formatTime(seconds)}", fontWeight = FontWeight.Bold, color = Color(0xFF, 0x7D, 0x52, 0x60))
                }
            },
            containerColor = Color(0xFF, 0xFE, 0xF7, 0xFF),
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun UnfoldedFaceGrid(
    bitmap: Bitmap,
    faceIndex: Int,
    stickers: Array<Array<RubikSticker>>,
    borderAccent: Color
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(1.dp, borderAccent, RoundedCornerShape(4.dp))
            .padding(1.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            for (r in 0..2) {
                Row(modifier = Modifier.weight(1f)) {
                    for (c in 0..2) {
                        val sticker = stickers[r][c]
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(1.dp)
                        ) {
                            // Calculate sub-tile coordinate from sticker's original position
                            // That way, we render the correct image piece!
                            val origR = sticker.row
                            val origC = sticker.col

                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Draw cropped sub-tile
                                val tileW = bitmap.width / 3
                                val tileH = bitmap.height / 3
                                val srcX = origC * tileW
                                val srcY = origR * tileH

                                drawImage(
                                    image = bitmap.asImageBitmap(),
                                    srcOffset = IntOffset(srcX, srcY),
                                    srcSize = IntSize(tileW, tileH),
                                    dstSize = IntSize(size.width.toInt(), size.height.toInt())
                                )

                                // Add border illustrating the current stickering face alignment
                                drawRect(
                                    color = Color(0xFF, 0xE6, 0xE1, 0xE5).copy(alpha = 0.5f),
                                    style = Stroke(width = 1.dp.toPx())
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PermuteButton(
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = accent
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF, 0xE6, 0xE1, 0xE5)),
        contentPadding = PaddingValues(vertical = 10.dp),
        modifier = modifier
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = accent
        )
    }
}
