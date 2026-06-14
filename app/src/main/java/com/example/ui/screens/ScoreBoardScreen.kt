@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)
package com.example.ui.screens

import android.text.format.DateFormat
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PuzzleScore
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.PuzzleViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreBoardScreen(
    viewModel: PuzzleViewModel,
    modifier: Modifier = Modifier
) {
    val scores by viewModel.allScores.collectAsState()
    var isClearConfirmOpen by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFF, 0xFE, 0xF7, 0xFF), // Elegant background #fef7ff
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "LEADERBOARD",
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
                    if (scores.isNotEmpty()) {
                        IconButton(onClick = { isClearConfirmOpen = true }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear History", tint = Color(0xFF, 0x67, 0x50, 0xA4))
                        }
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (scores.isEmpty()) {
                // Empty state view (Bento Style)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color(0xFF, 0xF3, 0xED, 0xF7), RoundedCornerShape(20.dp))
                            .border(1.dp, Color(0xFF, 0xE6, 0xE1, 0xE5), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Leaderboard,
                            contentDescription = null,
                            tint = Color(0xFF, 0x67, 0x50, 0xA4),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "NO COMPLETED CODES",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF, 0x1D, 0x1B, 0x20)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Your completed high scores, movements, and best times will list here offline.",
                        fontSize = 12.sp,
                        color = Color(0xFF, 0x49, 0x45, 0x4F),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            } else {
                // Scrollable logs list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(scores) { score ->
                        ScoreLogCard(score = score)
                    }
                }
            }
        }
    }

    // CONFIRM CLEAR DIALOG (Light Bento Layout)
    if (isClearConfirmOpen) {
        AlertDialog(
            onDismissRequest = { isClearConfirmOpen = false },
            confirmButton = {
                Button(
                    onClick = {
                        isClearConfirmOpen = false
                        viewModel.clearAllHistory()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF, 0x67, 0x50, 0xA4))
                ) {
                    Text("YES, CLEAR ALL", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { isClearConfirmOpen = false }) {
                    Text("Cancel", color = Color(0xFF, 0x67, 0x50, 0xA4))
                }
            },
            title = {
                Text("Clear History?", fontWeight = FontWeight.Bold, color = Color(0xFF, 0x1D, 0x1B, 0x20))
            },
            text = {
                Text("This will permanently purge your complete solved records catalog from local offline storage. This action is irreversible.", color = Color(0xFF, 0x49, 0x45, 0x4F), fontSize = 13.sp)
            },
            containerColor = Color(0xFF, 0xFE, 0xF7, 0xFF),
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun ScoreLogCard(score: PuzzleScore) {
    val dateStr = try {
        val cal = Calendar.getInstance().apply { timeInMillis = score.timestamp }
        DateFormat.format("yyyy-MM-dd HH:mm", cal).toString()
    } catch (e: Exception) {
        ""
    }

    val modeLabel = when (score.puzzleMode) {
        "SLIDING" -> "SLIDING BLOCK"
        "RUBIK" -> "RUBIK'S CUBE"
        else -> "JIGSAW PUZZLE"
    }

    val themeColor = when (score.puzzleMode) {
        "SLIDING" -> Color(0xFF, 0x67, 0x50, 0xA4) // Premium purple
        "RUBIK" -> Color(0xFF, 0x7D, 0x52, 0x60) // Cube burgundy
        else -> Color(0xFF, 0x70, 0x5D, 0x00) // Sliding yellow-ochre
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFF, 0xE6, 0xE1, 0xE5)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Mode and Date row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = modeLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColor
                )
                Text(
                    text = dateStr,
                    fontSize = 11.sp,
                    color = Color(0xFF, 0x49, 0x45, 0x4F).copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Score details row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Difficulty Node
                Column(modifier = Modifier.weight(1f)) {
                    Text("DIFFICULTY", fontSize = 9.sp, color = Color(0xFF, 0x49, 0x45, 0x4F), fontWeight = FontWeight.Bold)
                    Text(score.difficulty, fontSize = 13.sp, color = Color(0xFF, 0x1D, 0x1B, 0x20), fontWeight = FontWeight.SemiBold)
                }

                // Grid stats Node
                Column(modifier = Modifier.weight(1f)) {
                    Text("GEOMETRY", fontSize = 9.sp, color = Color(0xFF, 0x49, 0x45, 0x4F), fontWeight = FontWeight.Bold)
                    Text(score.dimensions, fontSize = 13.sp, color = Color(0xFF, 0x1D, 0x1B, 0x20), fontWeight = FontWeight.SemiBold)
                }

                // Moves Node
                Column(modifier = Modifier.weight(1f)) {
                    Text("ACTIONS", fontSize = 9.sp, color = Color(0xFF, 0x49, 0x45, 0x4F), fontWeight = FontWeight.Bold)
                    Text("${score.moves} moves", fontSize = 13.sp, color = Color(0xFF, 0x1D, 0x1B, 0x20), fontWeight = FontWeight.SemiBold)
                }

                // Speed Time Node
                Column(modifier = Modifier.weight(1.2f)) {
                    Text("BEST TIME", fontSize = 9.sp, color = Color(0xFF, 0x49, 0x45, 0x4F), fontWeight = FontWeight.Bold)
                    Text(formatTime(score.timeInSeconds), fontSize = 13.sp, color = themeColor, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
