package com.example.memo

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

//TODO: CONSIDER CASE IF GENERATORED UUID OF A MEMO IS NOT UNIQUE IN THE LIST
// ^^^^ copied in Memo.kt
class MemoCheckListViewModel (list: MutableList<Memo>, listName: String)
    : ViewModel() {
    //State remains private while discrete values are attainable
    //through the public values
    // _ for private
    private var _name = mutableStateOf(listName)
    private var _memos = list.toMutableStateList()
    val memos: List<Memo>
        get() = _memos

    fun addItem(text: String) {
        Log.d("CALLED FROM:" ,"addItem(text:String)")
        Log.d("VARIABLE TEXT", text)
        //Maintain Order
        _memos.add(Memo(text, _memos.last().order + 1))
        Log.d("VARIABLE DATA:",_memos.toString())
    }

    fun removeItem(index: Int) {
        Log.d("CALLED FROM:" ,"removeItem(index: Int)")
        Log.d("VARIABLE INDEX", index.toString())
        _memos.removeAt(index)
        Log.d("VARIABLE DATA:",_memos.toString())
    }

    fun removeItem(item: Memo){
        _memos.remove(item)
    }

    fun updateCheckedItem(index: Int, checked: Boolean) {
        _memos[index].checked.value = checked
    }
}

// TODO: REFACTOR MEMOSCREEN
@Composable
fun MemoScreen (
    viewModel: MemoCheckListViewModel,
    modifier: Modifier = Modifier
) {
    val memos = viewModel.memos
    val editMode = remember {mutableStateOf(false)}

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
                Button(onClick = {},
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Image(
                        painter = painterResource(R.drawable.menu),
                        contentDescription = "buttonText",
                    )
                }
                Button(onClick = { editMode.value = !editMode.value},
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Image(
                        painter = painterResource(R.drawable.menu),
                        contentDescription = "EditButton"
                    )
                }
            }
        },
        bottomBar = {
            Surface(modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondary
            ) {
                ButtonAddItem(viewModel::addItem)
            }
        }) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MemoListDisplay(memos, viewModel::updateCheckedItem ,viewModel::removeItem)
        }
    }
}

@Composable
fun MemoListDisplay (
    items: List<Memo>,
    onCheckItem: (index: Int, checked: Boolean) -> Unit,
    onRemoveItem: (index: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val lazyColumnModifier = Modifier.fillMaxWidth()
        .fillMaxHeight()
        .background(MaterialTheme.colorScheme.onBackground)
        .clip(RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp))
        .background(MaterialTheme.colorScheme.secondary)
    LazyColumn (
        modifier = lazyColumnModifier) {
        itemsIndexed(
            items,
            key = { _, item -> item.idKey }
        ) {  index , item ->
            MemoItemDisplay(
                memoName = item.memoText,
                memoChecked = item.checked.value,
                index = index,
                onCheckItem,
                onRemoveItem,
                modifier = Modifier)
        }
        Log.d("CALLED FROM (END OF)", "MemosCheckListDisplay" )
    }
}


@Composable
fun MemoItemDisplay(
    memoName: String,
    memoChecked: Boolean,
    index: Int,
    onCheckItem: (index: Int, checked: Boolean) -> Unit,
    onRemoveItem: (index : Int) -> Unit,
    modifier: Modifier
) {
    val paddingVal = 16.dp
    Row (
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = paddingVal, vertical = paddingVal/4)
            .fillMaxWidth()
    ) {
        Log.d("CALLED FROM: ", "MemoItemDisplay" )
        Text(
            text = memoName,
            modifier = Modifier.weight(1f)
        )
        Checkbox(
            checked = memoChecked,
            onCheckedChange = {onCheckItem(index, !memoChecked)}
        )
        Button(onClick = {onRemoveItem(index)},
        ) {
            Text("remove")
        }
    }
}


@Composable
fun ButtonAddItem (onAddItem: (text: String) -> Unit) {
    var textInput by remember { mutableStateOf("Add") }

    Log.d("CALLED FROM:", "ButtonAddItem")
    Log.d("VARIABLE TEXT:", textInput)

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
fun ListScreenPreview() {
    var list = mutableListOf(
        Memo("Memo1",  1, ),
        Memo("Memo2" ,  2),
        Memo("Memo3", 3),
        Memo("Memo4",  4)
    )
    val viewModel = remember { MemoCheckListViewModel(list, "HELL LIST") }
    MemoScreen(viewModel)
}


@Preview(showBackground = true)
@SuppressLint("UnrememberedMutableState")
@Composable
fun MemoPreview() {
    MemoItemDisplay("MagikMemo", true,0, { _: Int, _: Boolean -> }, {}, modifier = Modifier)
}
