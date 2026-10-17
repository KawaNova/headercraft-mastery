package com.kawanova.headercraft.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
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
import com.kawanova.headercraft.data.GlossaryItem
import com.kawanova.headercraft.data.HeaderType
import com.kawanova.headercraft.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderGlossaryScreen(
    viewModel: HeaderViewModel,
    onHeaderAddedToBuilder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allGlossary = viewModel.glossary
    val searchQuery by viewModel.glossaryQuery.collectAsState()
    val selectedType by viewModel.selectedGlossaryType.collectAsState()

    val filteredList = remember(searchQuery, selectedType, allGlossary) {
        allGlossary.filter { item ->
            val matchesQuery = searchQuery.isBlank() ||
                    item.name.contains(searchQuery, ignoreCase = true) ||
                    item.summaryJa.contains(searchQuery, ignoreCase = true) ||
                    item.detailJa.contains(searchQuery, ignoreCase = true)
            val matchesType = selectedType == null || item.type == selectedType
            matchesQuery && matchesType
        }
    }

    var expandedItemName by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "HTTPヘッダー辞典",
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setGlossaryQuery(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("ヘッダー名や役割を検索 (例: CORS, HSTS, ETag)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate400) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HeaderSky,
                    unfocusedBorderColor = Slate700,
                    focusedContainerColor = Slate800,
                    unfocusedContainerColor = Slate800
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = (selectedType == null),
                    onClick = { viewModel.setGlossaryTypeFilter(null) },
                    label = { Text("すべて (${allGlossary.size})", fontSize = 11.sp) }
                )
                HeaderType.values().forEach { type ->
                    val isSelected = (selectedType == type)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setGlossaryTypeFilter(if (isSelected) null else type) },
                        label = { Text(type.labelJa, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Glossary List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredList) { item ->
                    val isExpanded = expandedItemName == item.name
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedItemName = if (isExpanded) null else item.name
                            },
                        colors = CardDefaults.cardColors(containerColor = Slate800),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.name,
                                    color = HeaderSky,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            when (item.type) {
                                                HeaderType.SECURITY -> HeaderEmerald.copy(alpha = 0.2f)
                                                HeaderType.CORS -> HeaderPurple.copy(alpha = 0.2f)
                                                HeaderType.CACHE -> HeaderAmber.copy(alpha = 0.2f)
                                                HeaderType.AUTH -> HeaderRose.copy(alpha = 0.2f)
                                                else -> Slate700
                                            }
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = item.type.labelJa,
                                        color = when (item.type) {
                                            HeaderType.SECURITY -> HeaderEmerald
                                            HeaderType.CORS -> HeaderPurple
                                            HeaderType.CACHE -> HeaderAmber
                                            HeaderType.AUTH -> HeaderRose
                                            else -> Slate300
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = item.summaryJa,
                                color = Slate300,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )

                            AnimatedVisibility(visible = isExpanded) {
                                Column(modifier = Modifier.padding(top = 10.dp)) {
                                    Divider(color = Slate700)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "📖 詳細解説",
                                        color = Slate100,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.detailJa,
                                        color = Slate200,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "💻 設定例",
                                        color = Slate100,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Slate950)
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = item.example,
                                            color = HeaderEmerald,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = {
                                            // Split example into Key: Value
                                            val parts = item.example.split(":", limit = 2)
                                            if (parts.size == 2) {
                                                viewModel.addHeader(parts[0].trim(), parts[1].trim(), item.summaryJa)
                                                Toast.makeText(context, "${item.name} をビルダーに追加しました", Toast.LENGTH_SHORT).show()
                                                onHeaderAddedToBuilder()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = HeaderSky),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = Slate900, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("このヘッダーを現在の設定に追加", color = Slate900, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
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
    }
}
