package com.kawanova.headercraft.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kawanova.headercraft.ui.theme.*

data class SisterApp(
    val nameJa: String,
    val nameEn: String,
    val descJa: String,
    val descEn: String,
    val icon: String,
    val packageName: String
)

val sisterApps = listOf(
    SisterApp(
        nameJa = "JWTインスペクター",
        nameEn = "JWT Inspector",
        descJa = "トークンデコーダ ＆ クレーム検証",
        descEn = "JWT Token Decoder & Claims Analyzer",
        icon = "🔐",
        packageName = "com.kawanova.jwtinspector"
    ),
    SisterApp(
        nameJa = "モバイルAPIテスター",
        nameEn = "API Tester",
        descJa = "スマホで即時REST APIテスト・JSON整形",
        descEn = "Mobile REST Client & JSON Viewer",
        icon = "⚡",
        packageName = "com.kawanova.apitester"
    ),
    SisterApp(
        nameJa = "Cron式ビルダー",
        nameEn = "CronGen",
        descJa = "スケジュール生成・実行日時シミュレータ",
        descEn = "Cron Expression Builder & Schedule Simulator",
        icon = "⏱️",
        packageName = "com.kawanova.crongen"
    ),
    SisterApp(
        nameJa = "情報セキュリティマネジメント超入門",
        nameEn = "SG Drill",
        descJa = "認証・暗号化・OAuth攻撃防御フロー",
        descEn = "OIDC, Crypto & Security Management",
        icon = "🛡️",
        packageName = "com.kawanova.sgdrill"
    ),
    SisterApp(
        nameJa = "Linuxコマンド超入門",
        nameEn = "Linux Drill",
        descJa = "シェル特訓・パーミッション・現場必須",
        descEn = "Linux Shell, Permissions & Admin Drill",
        icon = "🐧",
        packageName = "com.kawanova.linuxdrill"
    ),
    SisterApp(
        nameJa = "MarkNote",
        nameEn = "MarkNote",
        descJa = "超高速モバイルMarkdownエディタ ＆ プレビュー",
        descEn = "Mobile Markdown Editor & GFM Preview",
        icon = "📝",
        packageName = "com.kawanova.marknote"
    )
)

@Composable
fun SisterAppsBanner(
    modifier: Modifier = Modifier,
    isJapanese: Boolean = true
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Slate800)
            .border(1.dp, Slate700, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isJapanese) "🛠️ 開発者・エンジニア必携シリーズ" else "🛠️ Developer Ecosystem Apps",
                color = Slate200,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isJapanese) "Google Playで見る" else "Google Play",
                color = HeaderSky,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            sisterApps.take(4).forEach { app ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate900.copy(alpha = 0.6f))
                        .clickable {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=${app.packageName}")
                            ).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val webIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=${app.packageName}")
                                ).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(webIntent)
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = app.icon, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isJapanese) app.nameJa else app.nameEn,
                            color = Slate100,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isJapanese) app.descJa else app.descEn,
                            color = Slate400,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = "GET",
                        color = HeaderSky,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(HeaderSky.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
