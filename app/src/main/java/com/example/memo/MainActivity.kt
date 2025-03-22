package com.example.memo

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.example.compose.AppTheme
//import com.example.memo.ui.theme.MemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, true)
        super.onCreate(savedInstanceState)
        setContent {
            val list1: MutableList<Memo> = remember {
                mutableListOf(
                    Memo("Memo1",  1),
                    Memo("Memo2", 2),
                    Memo("Memo3",  3),
                    Memo("Memo4",  4),
                    Memo("Memo5",  5),
                    Memo("Memo6",  6),
                    Memo("Memo7",  7),
                    Memo("Memo8",  8)
                )
            }
            AppTheme {
                Box(Modifier.safeDrawingPadding()) {
                    val viewModel = remember { MemoListViewModel(list = list1, listName = "Hell") }
                    MemoScreen(viewModel)
                }
            }
        }
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "DefaultPreviewDark"
)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "DefaultPreviewLight"
)
@Composable
fun AppPreview() {
    val list1: MutableList<Memo> = remember {
        mutableListOf(
            Memo("Memo1",  1),
            Memo("Memo2", 2),
            Memo("Memo3",  3),
            Memo("Memo4",  4),
            Memo("Memo5",  5),
            Memo("Memo6",  6),
            Memo("Memo7",  7),
            Memo("Memo8",  8)
        )
    }
    AppTheme {
        Box(Modifier.safeDrawingPadding()) {
            val viewModel = remember { MemoListViewModel(list = list1, listName = "Hell") }
            MemoScreen(viewModel)
        }
    }
}
