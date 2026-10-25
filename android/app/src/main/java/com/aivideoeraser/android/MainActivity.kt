package com.aivideoeraser.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.aivideoeraser.android.ui.navigation.NavGraph
import com.aivideoeraser.android.ui.theme.AiVideoEraserTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AiVideoEraserTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
