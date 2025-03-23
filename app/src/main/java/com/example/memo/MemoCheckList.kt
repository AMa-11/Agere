package com.example.memo

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// TODO: REFACTOR MEMOSCREEN
// TODO: REFACTOR ALL MODIFIER TO VARIABLES/VALUES. depends on performace vs memory
// TODO: REMOVE ALL UNUSED PASSED MODIFIER PARAMS
@Composable
fun MemoScreen (
    viewModel: MemoListViewModel,
    modifier: Modifier = Modifier
) {
    val memos = viewModel.memos
    val listAnimatable = remember { MutableList(memos.size){Animatable(0f)} }
    val editMode = remember {mutableStateOf(true)}
    val toggleEditMode = {editMode.value = !editMode.value}
    val addItemAnimatable = {text:String ->
        (viewModel::addItem)(text)
        listAnimatable.add(Animatable(0f))
        Unit
    }
    val composableScope = rememberCoroutineScope()
    val resetItemAnimatable = {start: Int, end: Int ->
        composableScope.launch {
            for (animatable in listAnimatable) {
                animatable.snapTo(0f)
            }
        }
    }
    Scaffold(modifier.background(MaterialTheme.colorScheme.surface),
        topBar = {
            TopBar(toggleEditMode)
        },
        bottomBar = {
            BottomBar(addItemAnimatable)
        }) { paddingValues ->
            MemoListDisplay(memos, listAnimatable, editMode, viewModel::updateCheckedItem ,viewModel::removeItem, viewModel::swapItemRange, resetItemAnimatable, paddingValues)
    }
}



@Composable
fun TopBar(toggleEditMode: () -> Unit ) {
    Box (modifier = Modifier.fillMaxWidth()
        .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Text(
            text = "ListName",
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.displayMedium,
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
        Button(onClick = { toggleEditMode() },
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Image(
                painter = painterResource(R.drawable.edit),
                contentDescription = "EditButton"
            )
        }
    }
}

@Composable
fun BottomBar(onAddItem: (text: String) -> Unit) {
    Surface(modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        var textInput by remember { mutableStateOf("Add") }
        Row {
            TextField(
                modifier = Modifier.padding(horizontal = 16.dp),
                value = textInput,
                onValueChange = { textInput = it },
            )
            //TODO: CHANGE TO A PLUS ICON
            Button(enabled = true, onClick = {onAddItem(textInput)} ) {
                Text("Add Item")
            }
        }
    }
}

@Composable
fun MemoListDisplay (
    items: List<Memo>,
    listAnimatable: MutableList<Animatable<Float, AnimationVector1D>>,
    editMode: MutableState<Boolean>,
    onCheckItem: (index: Int, checked: Boolean) -> Unit,
    onRemoveItem: (index: Int) -> Unit,
    onSwapItems: (start: Int, end: Int) -> Unit,
    resetItemAnimatable: (start: Int, end:Int) -> Job,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {

    // remember the list state for dragging logic
    // and to control scrolling
    val listState = rememberLazyListState()
    val onDragSwap = { composableScope: CoroutineScope, targetIndex: Int, targetLocation: Int  ->
        // bounds check
        if (targetIndex >= 0 && targetIndex <= items.size-1 ) {
            composableScope.launch {
                listAnimatable[targetIndex].animateTo(
                    targetLocation.toFloat(),
                    animationSpec = tween(60)
                )
            }
        } else {
            composableScope.launch{}
        }
    }
    // create the lazy column
    val lazyColumnModifier = Modifier.padding(paddingValues)
        .fillMaxWidth()
        .fillMaxHeight()
        .clip(RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp))
        .clipToBounds()
    LazyColumn (
        state = listState,
        modifier = lazyColumnModifier
    ) {
        itemsIndexed(
            items,
        ) {  index , item ->
            MemoItemDisplay(
                memoName = item.memoText,
                memoChecked = item.checked.value,
                editMode = editMode,
                index = index,
                offsetY = listAnimatable[index],
                onCheckItem,
                onRemoveItem,
                onSwapItems,
                onDragSwap = onDragSwap,
                onResetAnimatable =resetItemAnimatable,
                modifier = Modifier)
        }
        Log.d("CALLED FROM (END OF)", "MemosCheckListDisplay" )
    }
}


@Composable
fun MemoItemDisplay(
    memoName: String,
    memoChecked: Boolean,
    editMode: MutableState<Boolean>,
    index: Int,
    offsetY: Animatable<Float, AnimationVector1D>,
    onCheckItem: (index: Int, checked: Boolean) -> Unit,
    onRemoveItem: (index : Int) -> Unit,
    onSwapItems: (start: Int, end: Int) -> Unit,
    onDragSwap: (composableScope: CoroutineScope, targetIndex: Int, targetLocation: Int) -> Job,
    onResetAnimatable: (start: Int, end: Int) -> Job,
    modifier: Modifier
) {
    // LOG COMPOSITION/RECOMPOSITION
    Log.d("CALLED FROM: ", "MemoItemDisplay" )

    //Modifier Values
    val paddingVal = 16.dp
    val focus = remember { mutableStateOf(false) }
    val onChangeFocus = {focus.value = !focus.value}
    var height by remember { mutableStateOf(1f) }
    var yPos by remember { mutableStateOf(2000f) }
    val itemModifier = Modifier.padding(horizontal = paddingVal)
        .fillMaxWidth()
        .graphicsLayer { translationY = offsetY.value}//if(yPos>=0) offsetY.value else 0f }
        .clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 16.dp))
        .background(MaterialTheme.colorScheme.primaryContainer)
        .zIndex(zIndex = if(focus.value) 1f else 0f)
        .onPlaced { coord ->
            height = coord.size.height.toFloat()
            yPos = coord.positionInParent().y
        }

    Row (
        verticalAlignment = Alignment.CenterVertically,
        modifier = itemModifier
    ) {
        Text(
            text = memoName,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.weight(1f).padding(start = 12.dp),
        )
        Checkbox(
            checked = memoChecked,
            onCheckedChange = {onCheckItem(index, !memoChecked)}
        )
        Button(
            onClick = {onRemoveItem(index)},
        ) {
            Text("remove")
        }

        if (editMode.value == true) {
            val animate = { cScope: CoroutineScope, target: Float  -> cScope.launch {

                    offsetY.animateTo(target, animationSpec = tween(100))

            }}
            val printy = { -> Log.d("yAtEnd:", yPos.toString())
                Unit}
            ReorderIcon(
                index,
                height,
                printy,
                onSwapItems,
                onDragSwap,
                onResetAnimatable,
                animate,
                onChangeFocus
            )
        }
    }
}

@Composable
fun ReorderIcon(
    index: Int,
    height: Float,
    printy: () -> Unit,
    onSwapItems: (start: Int, end: Int) -> Unit,
    onDragSwap: (composableScope: CoroutineScope, targetIndex: Int, targetLocation: Int) -> Job,
    onResetAnimatable: (start: Int, end: Int) -> Job,
    animate: (composableScope: CoroutineScope, target: Float) -> Job,
    onChangeFocus: () -> Unit
) {
    Log.d("CALLED FROM:","ReorderIcon")
    val offsetLocalY = remember { mutableStateOf(0f) }
    val composableScope = rememberCoroutineScope()
    Image(
        painterResource(id = R.drawable.reorder),
        contentDescription = "reorder icon",
        modifier = Modifier
            .padding(end = 12.dp)
            .clip(shape= CircleShape)
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .pointerInput(Unit) {
                Log.d("Pointer Input Detected:", "InputDetected")
                detectDragGestures(
                    onDragStart = { offset: Offset ->
                        onChangeFocus()
                    },
                    onDragEnd = {
                        printy()
                        val size = height.toInt()
                        val remainder = offsetLocalY.value.toInt() / size
                        val targetIndexOffset =  if (offsetLocalY.value >= 0) {1} else {-1}
                        val targetIndex = index + remainder + targetIndexOffset
                        if (targetIndex != index) {onSwapItems(index, targetIndex)}
                        // cleanup
                        onResetAnimatable(0,0)
                        offsetLocalY.value = 0f
                        onChangeFocus()
                    },
                    onDrag = { change, dragAmount ->
                        offsetLocalY.value += dragAmount.y

                        animate(composableScope, offsetLocalY.value)
                        change.consume()
                        // Swapping Logic
                        val size = height.toInt()
                        val remainder = offsetLocalY.value.toInt() / size
                        val mod = (offsetLocalY.value % size)
                        val targetIndexOffset = if (offsetLocalY.value >= 0) { 1 } else { -1 }
                        val targetIndex = index + remainder + targetIndexOffset
                        val targetLocation = -mod
                        //Log.d("Height:", height.toString())
                        Log.d("Drag Offset:", offsetLocalY.value.toString())
                        Log.d("Remainder:", remainder.toString())
                        Log.d("Mod:", mod.toString())
                        Log.d("TargetIndex:", targetIndex.toString())

                        if (targetIndex != index) {
                            onDragSwap(composableScope, targetIndex, targetLocation.toInt())
                        }

                    }
                )
            }
    )
}

@Preview
@Composable
fun ListScreenPreview() {
    var list = mutableListOf(
        Memo("Memo1", 1),
        Memo("Memo2" ,  2),
        Memo("Memo3", 3),
        Memo("Memo4",  4)
    )
    val viewModel = remember { MemoListViewModel(list, "HELL LIST") }
    MemoScreen(viewModel)
}


@Preview(showBackground = true)
@SuppressLint("UnrememberedMutableState")
@Composable
fun MemoPreview() {
    val editMode = remember { mutableStateOf(true) }
    val offsetY = remember { Animatable(0f) }
    val cScope = rememberCoroutineScope()
    MemoItemDisplay(memoName="MagikMemo",
        memoChecked=true,
        editMode=editMode,
        index=0,
        offsetY=offsetY,
        { _: Int, _: Boolean -> },
        {},
        { _: Int, _: Int ->},
        { c: CoroutineScope, _: Int, _: Int -> c.launch{} },
        { _: Int, _: Int -> cScope.launch{} },
        modifier = Modifier)
}
