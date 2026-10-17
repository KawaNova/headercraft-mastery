package com.kawanova.headercraft.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kawanova.headercraft.data.*
import com.kawanova.headercraft.ui.components.SisterAppsBanner
import com.kawanova.headercraft.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderBuilderScreen(
    viewModel: HeaderViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val headers by viewModel.headers.collectAsState()
    val selectedFormat by viewModel.selectedFormat.collectAsState()
    val auditResult by viewModel.auditResult.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showCorsDialog by remember { mutableStateOf(false) }
    var showCacheDialog by remember { mutableStateOf(false) }
    var showAuditDialog by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }

    var editingHeaderIndex by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "HeaderCraft",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Slate100
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Security Grade Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when (auditResult.grade) {
                                        "A+" -> HeaderEmerald.copy(alpha = 0.2f)
                                        "A" -> HeaderEmerald.copy(alpha = 0.15f)
                                        "B" -> HeaderSky.copy(alpha = 0.2f)
                                        "C" -> HeaderAmber.copy(alpha = 0.2f)
                                        else -> HeaderRose.copy(alpha = 0.2f)
                                    }
                                )
                                .border(
                                    1.dp,
                                    when (auditResult.grade) {
                                        "A+", "A" -> HeaderEmerald
                                        "B" -> HeaderSky
                                        "C" -> HeaderAmber
                                        else -> HeaderRose
                                    },
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { showAuditDialog = true }
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🛡️ ${auditResult.grade} (${auditResult.score}点)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (auditResult.grade) {
                                        "A+", "A" -> HeaderEmerald
                                        "B" -> HeaderSky
                                        "C" -> HeaderAmber
                                        else -> HeaderRose
                                    }
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showSaveDialog = true }) {
                        Icon(Icons.Default.BookmarkAdd, contentDescription = "設定を保存", tint = HeaderSky)
                    }
                    IconButton(onClick = { viewModel.clearHeaders() }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "クリア", tint = Slate400)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate900)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = HeaderSky,
                contentColor = Slate900
            ) {
                Icon(Icons.Default.Add, contentDescription = "ヘッダー追加")
            }
        },
        containerColor = Slate900
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Quick Generators (CORS / Cache-Control buttons)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showCorsDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Text("🌐 CORS ビルダー", color = HeaderSky, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = { showCacheDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Text("⚡ Cache 制御", color = HeaderEmerald, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Header Items List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "設定中のHTTPヘッダー (${headers.size}件)",
                        color = Slate200,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "タップで編集 / チェックでON・OFF",
                        color = Slate400,
                        fontSize = 11.sp
                    )
                }
            }

            if (headers.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Slate800)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ヘッダーがありません。\n下の ＋ ボタンまたは CORS/Cache ビルダーから追加してください。",
                            color = Slate400,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                itemsIndexed(headers) { index, item ->
                    HeaderRowItem(
                        item = item,
                        onToggle = { viewModel.toggleHeader(index) },
                        onEdit = { editingHeaderIndex = index },
                        onDelete = { viewModel.removeHeader(index) }
                    )
                }
            }

            // Code Output Card Section
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate850),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "サーバー設定コード出力",
                                color = Slate100,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Row {
                                IconButton(
                                    onClick = {
                                        val output = viewModel.getFormattedOutput()
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("HTTP Headers", output))
                                        Toast.makeText(context, "コードをクリップボードにコピーしました", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "コピー", tint = HeaderSky)
                                }
                                IconButton(
                                    onClick = {
                                        val output = viewModel.getFormattedOutput()
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, output)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "ヘッダー設定を共有"))
                                    }
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "共有", tint = Slate300)
                                }
                            }
                        }

                        // Format selection chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ServerFormat.values().forEach { fmt ->
                                val isSelected = fmt == selectedFormat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) HeaderSky else Slate700)
                                        .clickable { viewModel.setFormat(fmt) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = fmt.displayName,
                                        color = if (isSelected) Slate900 else Slate200,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Code box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Slate950)
                                .border(1.dp, Slate700, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = viewModel.getFormattedOutput(),
                                color = HeaderSky,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Sister Apps Banner
            item {
                SisterAppsBanner(isJapanese = true)
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }

    // Add Custom Header Dialog
    if (showAddDialog) {
        var key by remember { mutableStateOf("") }
        var value by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("カスタムHTTPヘッダー追加", color = Slate100, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = key,
                        onValueChange = { key = it },
                        label = { Text("ヘッダー名 (例: Content-Type)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HeaderSky,
                            unfocusedBorderColor = Slate600
                        )
                    )
                    OutlinedTextField(
                        value = value,
                        onValueChange = { value = it },
                        label = { Text("値 (例: application/json)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HeaderSky,
                            unfocusedBorderColor = Slate600
                        )
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("メモ・説明 (任意)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HeaderSky,
                            unfocusedBorderColor = Slate600
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (key.isNotBlank()) {
                            viewModel.addHeader(key, value, desc)
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("追加", color = HeaderSky)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("キャンセル", color = Slate400)
                }
            },
            containerColor = Slate800
        )
    }

    // Edit Header Dialog
    editingHeaderIndex?.let { index ->
        val item = headers.getOrNull(index)
        if (item != null) {
            var key by remember { mutableStateOf(item.key) }
            var value by remember { mutableStateOf(item.value) }
            var desc by remember { mutableStateOf(item.description) }

            AlertDialog(
                onDismissRequest = { editingHeaderIndex = null },
                title = { Text("ヘッダーの編集", color = Slate100, fontSize = 16.sp) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = key,
                            onValueChange = { key = it },
                            label = { Text("ヘッダー名") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HeaderSky,
                                unfocusedBorderColor = Slate600
                            )
                        )
                        OutlinedTextField(
                            value = value,
                            onValueChange = { value = it },
                            label = { Text("値") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HeaderSky,
                                unfocusedBorderColor = Slate600
                            )
                        )
                        OutlinedTextField(
                            value = desc,
                            onValueChange = { desc = it },
                            label = { Text("説明") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HeaderSky,
                                unfocusedBorderColor = Slate600
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.updateHeader(index, key, value, desc)
                            editingHeaderIndex = null
                        }
                    ) {
                        Text("更新", color = HeaderSky)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editingHeaderIndex = null }) {
                        Text("キャンセル", color = Slate400)
                    }
                },
                containerColor = Slate800
            )
        }
    }

    // CORS Generator Dialog
    if (showCorsDialog) {
        CorsGeneratorDialog(
            viewModel = viewModel,
            onDismiss = { showCorsDialog = false }
        )
    }

    // Cache Generator Dialog
    if (showCacheDialog) {
        CacheGeneratorDialog(
            viewModel = viewModel,
            onDismiss = { showCacheDialog = false }
        )
    }

    // Security Audit Dialog
    if (showAuditDialog) {
        SecurityAuditDialog(
            auditResult = auditResult,
            onDismiss = { showAuditDialog = false }
        )
    }

    // Save Configuration Dialog
    if (showSaveDialog) {
        var title by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("現在のヘッダー設定を保存", color = Slate100, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("設定名 (例: 本番API用CORS設定)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HeaderSky,
                            unfocusedBorderColor = Slate600
                        )
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("メモ (任意)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HeaderSky,
                            unfocusedBorderColor = Slate600
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.saveCurrentConfiguration(title, note)
                            showSaveDialog = false
                            Toast.makeText(context, "設定を保存しました", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("保存", color = HeaderSky)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("キャンセル", color = Slate400)
                }
            },
            containerColor = Slate800
        )
    }
}

@Composable
fun HeaderRowItem(
    item: HeaderEntry,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.enabled,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = HeaderSky,
                    uncheckedColor = Slate500
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.key,
                    color = if (item.enabled) HeaderSky else Slate400,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = item.value,
                    color = if (item.enabled) Slate100 else Slate500,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 2
                )
                if (item.description.isNotBlank()) {
                    Text(
                        text = item.description,
                        color = Slate400,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Close, contentDescription = "削除", tint = Slate500, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun CorsGeneratorDialog(
    viewModel: HeaderViewModel,
    onDismiss: () -> Unit
) {
    val currentCorsState by viewModel.corsState.collectAsState()
    var origins by remember { mutableStateOf(currentCorsState.allowedOrigins) }
    var selectedMethods by remember { mutableStateOf(currentCorsState.allowedMethods) }
    var allowedHeaders by remember { mutableStateOf(currentCorsState.allowedHeaders) }
    var allowCredentials by remember { mutableStateOf(currentCorsState.allowCredentials) }
    var maxAge by remember { mutableStateOf(currentCorsState.maxAgeSeconds.toString()) }

    val allMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🌐 CORS (オリジン間リソース共有) 設定", color = Slate100, fontSize = 16.sp) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = origins,
                        onValueChange = { origins = it },
                        label = { Text("Allowed Origin (例: * または https://example.com)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HeaderSky,
                            unfocusedBorderColor = Slate600
                        )
                    )
                }

                item {
                    Text("許可するHTTPメソッド", color = Slate200, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                        allMethods.forEach { method ->
                            val isSelected = selectedMethods.contains(method)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedMethods = if (isSelected) {
                                        selectedMethods - method
                                    } else {
                                        selectedMethods + method
                                    }
                                },
                                label = { Text(method, fontSize = 11.sp) },
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = allowedHeaders,
                        onValueChange = { allowedHeaders = it },
                        label = { Text("Allowed Headers (カンマ区切り)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HeaderSky,
                            unfocusedBorderColor = Slate600
                        )
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Allow Credentials (Cookie許可)", color = Slate100, fontSize = 13.sp)
                            Text("CookieやAuthorizationヘッダーを許可", color = Slate400, fontSize = 11.sp)
                        }
                        Switch(
                            checked = allowCredentials,
                            onCheckedChange = { allowCredentials = it }
                        )
                    }
                    if (allowCredentials && origins.trim() == "*") {
                        Text(
                            text = "⚠️ 警告: ブラウザ仕様により、Credentials=true のときは Origin=* が拒否されます。",
                            color = HeaderAmber,
                            fontSize = 11.sp
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = maxAge,
                        onValueChange = { maxAge = it },
                        label = { Text("Max-Age (プリフライトキャッシュ秒数)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HeaderSky,
                            unfocusedBorderColor = Slate600
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newState = CorsBuilderState(
                        allowedOrigins = origins,
                        allowedMethods = selectedMethods,
                        allowedHeaders = allowedHeaders,
                        allowCredentials = allowCredentials,
                        maxAgeSeconds = maxAge.toIntOrNull() ?: 86400
                    )
                    viewModel.updateCorsState(newState)
                    viewModel.applyCorsHeaders()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = HeaderSky)
            ) {
                Text("ヘッダーに適用", color = Slate900)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル", color = Slate400)
            }
        },
        containerColor = Slate800
    )
}

@Composable
fun CacheGeneratorDialog(
    viewModel: HeaderViewModel,
    onDismiss: () -> Unit
) {
    val currentCacheState by viewModel.cacheState.collectAsState()
    var visibility by remember { mutableStateOf(currentCacheState.visibility) }
    var maxAge by remember { mutableStateOf(currentCacheState.maxAgeSeconds.toString()) }
    var immutable by remember { mutableStateOf(currentCacheState.immutable) }
    var mustRevalidate by remember { mutableStateOf(currentCacheState.mustRevalidate) }

    val options = listOf("public", "private", "no-store", "no-cache")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("⚡ Cache-Control 設定ビルダー", color = Slate100, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("キャッシュ可視性 (Visibility)", color = Slate200, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    options.forEach { opt ->
                        FilterChip(
                            selected = (visibility == opt),
                            onClick = { visibility = opt },
                            label = { Text(opt, fontSize = 11.sp) }
                        )
                    }
                }

                if (visibility != "no-store" && visibility != "no-cache") {
                    OutlinedTextField(
                        value = maxAge,
                        onValueChange = { maxAge = it },
                        label = { Text("Max-Age (キャッシュ保持秒数: 例 3600=1時間, 31536000=1年)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HeaderSky,
                            unfocusedBorderColor = Slate600
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("immutable (ビルドアセット向け不変指定)", color = Slate200, fontSize = 12.sp)
                        Switch(checked = immutable, onCheckedChange = { immutable = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("must-revalidate (期限切れ時サーバー再検証必須)", color = Slate200, fontSize = 12.sp)
                        Switch(checked = mustRevalidate, onCheckedChange = { mustRevalidate = it })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newState = CacheBuilderState(
                        visibility = visibility,
                        maxAgeSeconds = maxAge.toIntOrNull() ?: 3600,
                        immutable = immutable,
                        mustRevalidate = mustRevalidate
                    )
                    viewModel.updateCacheState(newState)
                    viewModel.applyCacheHeaders()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = HeaderEmerald)
            ) {
                Text("ヘッダーに適用", color = Slate900)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル", color = Slate400)
            }
        },
        containerColor = Slate800
    )
}

@Composable
fun SecurityAuditDialog(
    auditResult: AuditResult,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🛡️ セキュリティ診断レポート", color = Slate100, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${auditResult.grade} (${auditResult.score}点)",
                    color = when (auditResult.grade) {
                        "A+", "A" -> HeaderEmerald
                        "B" -> HeaderSky
                        "C" -> HeaderAmber
                        else -> HeaderRose
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(auditResult.issues) { _, issue ->
                    val color = when (issue.severity) {
                        AuditSeverity.CRITICAL -> HeaderRose
                        AuditSeverity.WARNING -> HeaderAmber
                        AuditSeverity.SUCCESS -> HeaderEmerald
                        AuditSeverity.INFO -> HeaderSky
                    }
                    val icon = when (issue.severity) {
                        AuditSeverity.CRITICAL -> "🚨"
                        AuditSeverity.WARNING -> "⚠️"
                        AuditSeverity.SUCCESS -> "✅"
                        AuditSeverity.INFO -> "ℹ️"
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "$icon ${issue.title}",
                                color = color,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = issue.message, color = Slate200, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "💡 対策: ${issue.recommendation}",
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("閉じる", color = HeaderSky)
            }
        },
        containerColor = Slate800
    )
}
