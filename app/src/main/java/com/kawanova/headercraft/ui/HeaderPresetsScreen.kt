package com.kawanova.headercraft.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kawanova.headercraft.data.HeaderPreset
import com.kawanova.headercraft.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderPresetsScreen(
    viewModel: HeaderViewModel,
    onPresetApplied: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val presets = viewModel.presets
    var selectedPresetForDetails by remember { mutableStateOf<HeaderPreset?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ヘッダープリセット集",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Slate100
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate900)
            )
        },
        containerColor = Slate900
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "プロダクション環境で実証済みの推奨ヘッダー構成。タップして詳細を確認・ワンタップでビルダーへ展開できます。",
                    color = Slate300,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }

            items(presets) { preset ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedPresetForDetails = preset },
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(HeaderSky.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = preset.category,
                                    color = HeaderSky,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "${preset.headers.size} headers",
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = preset.nameJa,
                            color = Slate100,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = preset.descriptionJa,
                            color = Slate400,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    viewModel.loadPreset(preset)
                                    Toast.makeText(context, "${preset.nameJa} を適用しました", Toast.LENGTH_SHORT).show()
                                    onPresetApplied()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = HeaderSky),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = Slate900, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ビルダーに適用", color = Slate900, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }

    // Detail Dialog
    selectedPresetForDetails?.let { preset ->
        AlertDialog(
            onDismissRequest = { selectedPresetForDetails = null },
            title = {
                Text(preset.nameJa, color = Slate100, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(preset.descriptionJa, color = Slate300, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("含まれるヘッダー:", color = HeaderSky, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    items(preset.headers) { header ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Slate900)
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(header.key, color = HeaderSky, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(header.value, color = Slate100, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                if (header.description.isNotBlank()) {
                                    Text(header.description, color = Slate400, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.loadPreset(preset)
                        selectedPresetForDetails = null
                        Toast.makeText(context, "${preset.nameJa} を適用しました", Toast.LENGTH_SHORT).show()
                        onPresetApplied()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HeaderSky)
                ) {
                    Text("ビルダーに適用", color = Slate900)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedPresetForDetails = null }) {
                    Text("閉じる", color = Slate400)
                }
            },
            containerColor = Slate800
        )
    }
}
