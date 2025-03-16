package com.example.memo

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.roundToInt


//Log.d("CALLED FROM: ", "" )
class MemoCheckListViewModel (list: List<Memo>, name: String)
    : ViewModel() {
    private var _name = mutableStateOf(name)
    private var _data = MutableStateFlow(list)
    var displayedItems: MutableStateFlow<List<Memo>> = _data

    fun addItem(text: String) {
        Log.d("CALLED FROM:" ,"addItem(text:String)")
        Log.d("VARIABLE TEXT", text)

        //_data = _data.add(Memo(text))
        //_data.value += Memo(text)
        //displayedItems.value += Memo(text, mutableStateOf(false), displayedItems.value.size + 1)
        displayedItems.value += Memo(text,  displayedItems.value.size + 1)
        Log.d("VARIABLE DATA:",_data.toString())
        Log.d("VARIABLE DATA:",displayedItems.toString())
    }

    fun removeItem() {

    }

    fun removeItem(index: Int) {
        //_displayedItems.value = _displayedItems.value.drop(index)
    }

    fun resetOrder() {
        //_displayedItems.value = _data.filter { it in _displayedItems.value }
    }

    fun sortAlphabetically() {
        //_displayedItems.value = _displayedItems.value.sortedBy { it }
    }

    fun sortByLength() {
        //_displayedItems.value = _displayedItems.value.sortedBy { it.memoText.length }
    }

    fun verifyList() {

    }
}

@Composable
fun MemoScreen (
    viewModel: MemoCheckListViewModel,
    modifier: Modifier = Modifier
) {
    val displayedItems by viewModel.displayedItems.collectAsStateWithLifecycle()

    MemoAllScreen(
        displayedItems,
        onAddItem = viewModel::addItem,
        onRemoveItem = viewModel::removeItem,
        resetOrder = viewModel::resetOrder,
        onSortAlphabetically = viewModel::sortAlphabetically,
        onSortByLength = viewModel::sortByLength,
        modifier = modifier
    )
}

@Composable
fun MemoAllScreen (
    data: List<Memo>,
    modifier: Modifier = Modifier,
    onAddItem: (text: String) -> Unit = {},
    onRemoveItem: () -> Unit = {},
    resetOrder: () -> Unit = {},
    onSortAlphabetically: () -> Unit = {},
    onSortByLength: () -> Unit = {},
) {
    Scaffold(modifier,
        topBar = {
            //TODO:: TRY OUT USING ROW INSTEAD
            Box (modifier = Modifier.fillMaxWidth()
                .background(MaterialTheme.colorScheme.onBackground)
            ) {
                Text(
                    text = "ListName",
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.displayLarge,
                    textAlign = TextAlign.Center,
                )
                Button(onClick = {}, modifier = Modifier.align(Alignment.CenterStart)){
                    Image(
                        painter = painterResource(R.drawable.menu),
                        contentDescription = "buttonText",
                    )
                }
            }
        },
        bottomBar = {
            Surface(modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondary
            ) {
                ButtonAddItem { onAddItem(it) }
            }
        }) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MemosCheckListDisplay(data)
        }
    }
}

@Composable
fun MemosCheckListDisplay (
    items: List<Memo>,
    modifier: Modifier = Modifier
) {
    var listState = LazyListState()
    LazyColumn (
        modifier = Modifier.fillMaxWidth()
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.onBackground)
            .clip(RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp))
            .background(MaterialTheme.colorScheme.secondary),
        ) {
        Log.d("CALLED FROM: ", "MemosCheckListDisplay" )
        items(items, key = {item -> item.order}) {
                item ->
            MemoItemDisplay(
                memo = item
            )
        }
    }
}


@Composable
fun MemoItemDisplay(memo: Memo) {
    val paddingVal = 16.dp
    var offsetY by remember { mutableStateOf(0f) }
    Row (
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = paddingVal, vertical = paddingVal/4)
            .fillMaxWidth()
            .offset {IntOffset(0, offsetY.roundToInt())}
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    offsetY += delta
                }
            ),
    ) {
        Log.d("CALLED FROM: ", "MemoItemDisplay" )
        Text(
            text = memo.memoText,
            modifier = Modifier.weight(1f)
        )
        Checkbox(
            checked = memo.memoCheckedState.value,
            onCheckedChange = {memo.updateChecked()}
        )
    }
}


@Composable
fun ButtonAddItem (onAddItem: (text: String) -> Unit) {
    var textInput by remember { mutableStateOf("Add") }
    var checked by remember { mutableStateOf(false) }
    Log.d("CALLED FROM:", "ButtonAddItem")
    Log.d("VARIABLE TEXT:", textInput)
    Log.d("VARIABLE CHECKED:", checked.toString())
    Row {
        TextField(
            value = textInput,
            onValueChange = { textInput = it },
        )

        Button(enabled = true, onClick = {onAddItem(textInput)} ) {
            Text("Add Item")
        }
    }
}


@Preview
@Composable
fun AnimatedOrderedListScreenPreview() {
    var list = listOf(
        Memo("Memo1",  1, true),
        Memo("Memo2" ,  2),
        Memo("Memo3", 3),
        Memo("Memo4",  4)
    )
    val viewModel = remember { MemoCheckListViewModel(list, "HELL LIST") }
    MemoScreen(viewModel)
}

//Preview(showBackground = true)
@Composable
fun MemosCheckListPreview() {
    val list: MutableList<Memo> = remember {
        mutableListOf(
            Memo("Memo1",  1),
            Memo("Memo2" ,  2),
            Memo("Memo3",  3),
            Memo("Memo4",  4)
            /*
            Memo("Memo1", mutableStateOf(false), 1),
            Memo("Memo2", mutableStateOf(false), 2 ),
            Memo("Memo3" , mutableStateOf(false), 3),
            Memo("Memo4", mutableStateOf(false), 4 )
            Memo("Memo"),
            Memo("Mememo" ),
            Memo("Memomo" ),
            Memo("Mememomo" )
             */
        )
    }
    MemosCheckListDisplay(list)
}

//@Preview(showBackground = true)
@SuppressLint("UnrememberedMutableState")
@Composable
fun MemoPreview() {
    val memo: Memo = Memo("hell",  0 )
    MemoItemDisplay(memo)
}

