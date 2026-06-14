@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)
package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ProceduralGenerator
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.PuzzleViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: PuzzleViewModel,
    modifier: Modifier = Modifier
) {
    val selectedBitmap by viewModel.selectedBitmap.collectAsState()
    val isCustomSelected by viewModel.isCustomSelected.collectAsState()
    val activePresetIndex by viewModel.proceduralIndex.collectAsState()

    // Mode options states
    var isJigsawModalOpen by remember { mutableStateOf(false) }
    var chosenJigsawGridSize by remember { mutableStateOf(3) } // 3x3 default
    var chosenJigsawDifficulty by remember { mutableStateOf("Medium") }

    var isSlidingModalOpen by remember { mutableStateOf(false) }
    var chosenSlidingGridSize by remember { mutableStateOf(3) } // 3x3 default

    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.loadCustomImage(uri)
        }
    }

    Scaffold(
        containerColor = Color(0xFF, 0xFE, 0xF7, 0xFF), // Elegant background #fef7ff
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF, 0x67, 0x50, 0xA4), RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Extension,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "PixelPuzzler",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF, 0x1D, 0x1B, 0x20),
                            letterSpacing = (-0.5).sp
                        )
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier
                            .background(Color(0xFF, 0xE8, 0xDE, 0xF8), RoundedCornerShape(100.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "Offline Mode",
                            tint = Color(0xFF, 0x49, 0x45, 0x4F),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "OFFLINE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF, 0x49, 0x45, 0x4F)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.SCORE_BOARD) }) {
                        Icon(
                            imageVector = Icons.Default.Leaderboard,
                            contentDescription = "Leaderboard Scores",
                            tint = Color(0xFF, 0x67, 0x50, 0xA4)
                        )
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
            // Section 1: ACTIVE IMAGE PREVIEW & SELECTING MEDIA (Bento Box #d3e3fd)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF, 0xD3, 0xE3, 0xFD)),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, Color(0xFF, 0xC2, 0xE7, 0xFF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Image",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF, 0x04, 0x1E, 0x49)
                        )
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.40f), RoundedCornerShape(100.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ACTIVE CANVAS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF, 0x04, 0x1E, 0x49)
                            )
                        }
                    }
                    
                    Text(
                        text = "Convert any photo into an offline custom-cut grid puzzle",
                        color = Color(0xFF, 0x04, 0x1E, 0x49).copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(top = 4.dp, bottom = 16.dp)
                    )

                    // Image viewport
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White)
                            .border(3.dp, Color.White, RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedBitmap != null) {
                            Image(
                                bitmap = selectedBitmap!!.asImageBitmap(),
                                contentDescription = "Active puzzle image source",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            CircularProgressIndicator(color = Color(0xFF, 0x04, 0x1E, 0x49))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons to change/import
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF, 0x04, 0x1E, 0x49),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1.1f)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import Photo", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        if (isCustomSelected) {
                            OutlinedButton(
                                onClick = { viewModel.loadProceduralPreset(0) },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF, 0x04, 0x1E, 0x49)),
                                border = BorderStroke(1.5.dp, Color(0xFF, 0x04, 0x1E, 0x49).copy(alpha = 0.4f)),
                                modifier = Modifier.weight(0.9f)
                            ) {
                                Text("Reset canvas", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Or choose a beautiful procedural style:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF, 0x04, 0x1E, 0x49).copy(alpha = 0.7f),
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Procedural Options Grid
                    AdaptiveRowLayout(spacing = 6.dp) {
                        ProceduralGenerator.presets.forEachIndexed { index, pair ->
                            val isActive = !isCustomSelected && activePresetIndex == index
                            Box(
                                modifier = Modifier
                                    .height(34.dp)
                                    .border(
                                        width = if (isActive) 1.5.dp else 1.dp,
                                        color = if (isActive) Color(0xFF, 0x04, 0x1E, 0x49) else Color(0xFF, 0x04, 0x1E, 0x49).copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .background(
                                        color = if (isActive) Color(0xFF, 0x04, 0x1E, 0x49).copy(alpha = 0.12f) else Color.White.copy(alpha = 0.35f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { viewModel.loadProceduralPreset(index) }
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = pair.first,
                                    fontSize = 11.sp,
                                    fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = Color(0xFF, 0x04, 0x1E, 0x49)
                                )
                            }
                        }
                    }
                }
            }

            // Section 2: THE ADVENTURE PLATFORM (BENTO GRID PATTERNS)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Left box: Jigsaw Game (bg-[#f3edf7], rounded-[28px], span 3x3 equivalent)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF, 0xF3, 0xED, 0xF7)),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, Color(0xFF, 0xE6, 0xE1, 0xE5)),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { isJigsawModalOpen = true }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🧩",
                            fontSize = 32.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Column {
                            Text(
                                text = "Jigsaw",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                color = Color(0xFF, 0x1D, 0x1B, 0x20)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Organic vector puzzle tabs & difficulty guides.",
                                color = Color(0xFF, 0x49, 0x45, 0x4F),
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFF, 0xCA, 0xC4, 0xD0), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "12-256 PCS",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF, 0x49, 0x45, 0x4F)
                                )
                            }
                        }
                    }
                }

                // Right column containing stacked cards
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Top: 3D Cube Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, Color(0xFF, 0xE6, 0xE1, 0xE5)),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clickable {
                                viewModel.startRubikGame()
                                viewModel.navigateTo(AppScreen.RUBIK_GAME)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(Color(0xFF, 0xF7, 0xD8, 0xFF), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ViewInAr,
                                    contentDescription = null,
                                    tint = Color(0xFF, 0x7D, 0x52, 0x60),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "3D Cube",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF, 0x1D, 0x1B, 0x20)
                                )
                                Text(
                                    text = "3D Image Map",
                                    color = Color(0xFF, 0x49, 0x45, 0x4F),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // Bottom: Sliding Block Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, Color(0xFF, 0xE6, 0xE1, 0xE5)),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clickable { isSlidingModalOpen = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(Color(0xFF, 0xFF, 0xF1, 0xBD), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GridView,
                                    contentDescription = null,
                                    tint = Color(0xFF, 0x70, 0x5D, 0x00),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Sliding",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF, 0x1D, 0x1B, 0x20)
                                )
                                Text(
                                    text = "Classic Slider",
                                    color = Color(0xFF, 0x49, 0x45, 0x4F),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            // Haptic Feedback Banner (dark charcoal contrast #1d1b20, rounded-[24px])
            var hapticFeedbackEnabled by remember { mutableStateOf(true) }
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF, 0x1D, 0x1B, 0x20)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clickable { hapticFeedbackEnabled = !hapticFeedbackEnabled }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = null,
                            tint = Color(0xFF, 0xD0, 0xBC, 0xFF),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Haptic Feedback",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Switch(
                        checked = hapticFeedbackEnabled,
                        onCheckedChange = { hapticFeedbackEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF, 0x38, 0x1E, 0x72),
                            checkedTrackColor = Color(0xFF, 0xD0, 0xBC, 0xFF),
                            uncheckedThumbColor = Color(0xFF, 0x49, 0x45, 0x4F),
                            uncheckedTrackColor = Color.Black.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    }

    // SLIDING PUZZLE DIFFICULTY OPTIONS MODAL (Light bento layout)
    if (isSlidingModalOpen) {
        AlertDialog(
            onDismissRequest = { isSlidingModalOpen = false },
            confirmButton = {
                Button(
                    onClick = {
                        isSlidingModalOpen = false
                        viewModel.startSlidingGame(chosenSlidingGridSize)
                        viewModel.navigateTo(AppScreen.SLIDING_GAME)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF, 0x67, 0x50, 0xA4))
                ) {
                    Text("START ADVENTURE", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { isSlidingModalOpen = false }) {
                    Text("Cancel", color = Color(0xFF, 0x67, 0x50, 0xA4))
                }
            },
            title = {
                Text("Configure Sliding Block", fontWeight = FontWeight.Bold, color = Color(0xFF, 0x1D, 0x1B, 0x20))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Select grid arrangement size:", color = Color(0xFF, 0x49, 0x45, 0x4F), fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(3 to "3 x 3\n(Easy)", 4 to "4 x 4\n(Medium)", 5 to "5 x 5\n(Hard)").forEach { (size, label) ->
                            val active = chosenSlidingGridSize == size
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        width = if (active) 1.5.dp else 1.dp,
                                        color = if (active) Color(0xFF, 0x67, 0x50, 0xA4) else Color(0xFF, 0xE6, 0xE1, 0xE5),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .background(
                                        color = if (active) Color(0xFF, 0xE8, 0xDE, 0xF8) else Color.White,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { chosenSlidingGridSize = size }
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                    color = if (active) Color(0xFF, 0x1D, 0x1B, 0x20) else Color(0xFF, 0x49, 0x45, 0x4F),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            },
            containerColor = Color(0xFF, 0xFE, 0xF7, 0xFF),
            shape = RoundedCornerShape(24.dp)
        )
    }

    // JIGSAW OPTIONS MODAL (Light bento layout)
    if (isJigsawModalOpen) {
        AlertDialog(
            onDismissRequest = { isJigsawModalOpen = false },
            confirmButton = {
                Button(
                    onClick = {
                        isJigsawModalOpen = false
                        viewModel.startJigsawGame(chosenJigsawGridSize, chosenJigsawDifficulty)
                        viewModel.navigateTo(AppScreen.JIGSAW_GAME)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF, 0x67, 0x50, 0xA4))
                ) {
                    Text("START GAME", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { isJigsawModalOpen = false }) {
                    Text("Cancel", color = Color(0xFF, 0x67, 0x50, 0xA4))
                }
            },
            title = {
                Text("Configure Jigsaw", fontWeight = FontWeight.Bold, color = Color(0xFF, 0x1D, 0x1B, 0x20))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("1. Piece Density:", color = Color(0xFF, 0x49, 0x45, 0x4F), fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(2 to "4 Pcs", 3 to "9 Pcs", 4 to "16 Pcs", 5 to "25 Pcs").forEach { (size, label) ->
                            val active = chosenJigsawGridSize == size
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        width = if (active) 1.5.dp else 1.dp,
                                        color = if (active) Color(0xFF, 0x67, 0x50, 0xA4) else Color(0xFF, 0xE6, 0xE1, 0xE5),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .background(
                                        color = if (active) Color(0xFF, 0xE8, 0xDE, 0xF8) else Color.White,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { chosenJigsawGridSize = size }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                    color = if (active) Color(0xFF, 0x1D, 0x1B, 0x20) else Color(0xFF, 0x49, 0x45, 0x4F)
                                )
                            }
                        }
                    }

                    Text("2. Difficulty Mode:", color = Color(0xFF, 0x49, 0x45, 0x4F), fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Easy", "Medium", "Hard").forEach { diff ->
                            val active = chosenJigsawDifficulty == diff
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        width = if (active) 1.5.dp else 1.dp,
                                        color = if (active) Color(0xFF, 0x67, 0x50, 0xA4) else Color(0xFF, 0xE6, 0xE1, 0xE5),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .background(
                                        color = if (active) Color(0xFF, 0xE8, 0xDE, 0xF8) else Color.White,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { chosenJigsawDifficulty = diff }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = diff,
                                        fontSize = 11.sp,
                                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                        color = if (active) Color(0xFF, 0x1D, 0x1B, 0x20) else Color(0xFF, 0x49, 0x45, 0x4F)
                                    )
                                    Text(
                                        text = when(diff) {
                                            "Easy" -> "Ghost Guide"
                                            "Medium" -> "Blank Grid"
                                            else -> "Rotated Tiles"
                                        },
                                        fontSize = 8.sp,
                                        color = Color(0xFF, 0x49, 0x45, 0x4F).copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            containerColor = Color(0xFF, 0xFE, 0xF7, 0xFF),
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun GameModeCard(
    title: String,
    description: String,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0x14, 0x11, 0x25)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0x34, 0x2C, 0x56)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(accentColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = "Open Mode Settings",
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Super lightweight helper layer grouping items horizontally or feeding lines
 * depending on orientation constraints.
 */
@Composable
fun AdaptiveRowLayout(
    spacing: androidx.compose.ui.unit.Dp,
    content: @Composable () -> Unit
) {
    // FlowRow is perfect here to lay items adjacent fluidly
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing),
        modifier = Modifier.fillMaxWidth()
    ) {
        content()
    }
}

// Inline helper for mutal StateFlow Compose variables
@Composable
fun <T> rememberStateOf(initial: T): MutableState<T> = remember { mutableStateOf(initial) }
