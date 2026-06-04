package pro.moreira.catapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import pro.moreira.catapp.core.ui.theme.CatAppTheme
import pro.moreira.catapp.navigation.CatAppNavHost

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CatAppTheme {
                CatAppNavHost()
            }
        }
    }
}
