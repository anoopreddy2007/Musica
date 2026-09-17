package com.musica.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.musica.app.navigation.MusicaNavigation
import com.musica.app.ui.theme.MusicaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            MusicaTheme {
                MusicaNavigation()
            }
        }
    }
}