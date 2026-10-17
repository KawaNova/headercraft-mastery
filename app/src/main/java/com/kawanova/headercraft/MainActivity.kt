package com.kawanova.headercraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.MobileAds
import com.kawanova.headercraft.ui.HeaderViewModel
import com.kawanova.headercraft.ui.components.AdaptiveBannerAd
import com.kawanova.headercraft.ui.navigation.AppNavigation
import com.kawanova.headercraft.ui.theme.HeaderCraftTheme
import com.kawanova.headercraft.ui.theme.Slate900

class MainActivity : ComponentActivity() {

    private val viewModel: HeaderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Google Mobile Ads SDK
        MobileAds.initialize(this) {}

        setContent {
            HeaderCraftTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Slate900
                ) {
                    val navController = rememberNavController()
                    Column(modifier = Modifier.fillMaxSize()) {
                        AppNavigation(
                            navController = navController,
                            viewModel = viewModel,
                            modifier = Modifier.weight(1f)
                        )
                        AdaptiveBannerAd()
                    }
                }
            }
        }
    }
}
