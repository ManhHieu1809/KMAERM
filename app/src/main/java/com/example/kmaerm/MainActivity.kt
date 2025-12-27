package com.example.kmaerm

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.example.kmaerm.data.api.RetrofitInstance
import com.example.kmaerm.ui.navigation.NavGraph
import com.example.kmaerm.ui.navigation.Screen
import com.example.kmaerm.ui.theme.KMAERMTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Khởi tạo RetrofitInstance với context để có thể thêm token vào header
        RetrofitInstance.initialize(this)

        enableEdgeToEdge()
        setContent {
            KMAERMTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        startDestination = Screen.Login.route
                    )
                }
            }
        }
    }
}