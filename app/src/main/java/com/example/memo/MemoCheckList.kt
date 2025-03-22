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
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
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
    val editMode = remember {mutableStateOf(true)}
    val toggleEditMode = {editMode.value = !editMode.value}

    Scaffold(modifier.background(MaterialTheme.colorScheme.surface),
        topBar = {
            TopBar(toggleEditMode)
        },
        bottomBar = {
            BottomBar(viewModel::addItem)
        }) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MemoListDisplay(memos, editMode, viewModel::updateCheckedItem ,viewModel::removeItem)
        }
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
        ButtonAddItem(onAddItem)
    }
}
// TODO: Consider moving this to BottomBar
@Composable
fun ButtonAddItem (onAddItem: (text: String) -> Unit) {
    var textInput by remember { mutableStateOf("Add") }

    Log.d("CALLED FROM:", "ButtonAddItem")
    Log.d("VARIABLE TEXT:", textInput)

    Row {
        TextField(
            modifier = Modifier.padding(horizontal = 16.dp),
            value = textInput,
            onValueChange = { textInput = it },
        )
        //TODO: CHANGE TO A PLUS
        Button(enabled = true, onClick = {onAddItem(textInput)} ) {
            Text("Add Item")
        }
    }
}

@Composable
fun MemoListDisplay (
    items: List<Memo>,
    editMode: MutableState<Boolean>,
    onCheckItem: (index: Int, checked: Boolean) -> Unit,
    onRemoveItem: (index: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // remember the index that is being dragged and the offset
    val dragged_index = remember { mutableStateOf(-1) }
    val offsetY = remember { Animatable(0f) }

    // remember the list state for dragging logic
    // and to control scrolling
    val listState = rememberLazyListState()

    // create the lazy column
    val lazyColumnModifier = Modifier.fillMaxWidth()
        .fillMaxHeight()
        .clip(RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp))
    LazyColumn (
        state = listState,
        modifier = lazyColumnModifier
    ) {
        itemsIndexed(
            items,
            key = { _, item -> item.idKey }
        ) {  index , item ->
            val updateDraggedIndex = {idx: Int -> dragged_index.value = idx}
            MemoItemDisplay(
                memoName = item.memoText,
                memoChecked = item.checked.value,
                editMode = editMode,
                index = index,
                dragged_index.value,
                updateDraggedIndex,
                offsetY,
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
    editMode: MutableState<Boolean>,
    index: Int,
    draggedIndex: Int,
    updateDraggedIndex: (index: Int) -> Unit,
    offsetY: Animatable<Float, AnimationVector1D>,
    onCheckItem: (index: Int, checked: Boolean) -> Unit,
    onRemoveItem: (index : Int) -> Unit,
    modifier: Modifier
) {
    // LOG COMPOSITION/RECOMPOSITION
    Log.d("CALLED FROM: ", "MemoItemDisplay" )

    val offsetY = remember { Animatable(0f) }

    //Modifier Values
    val paddingVal = 16.dp
    val itemModifier = Modifier.padding(horizontal = paddingVal, vertical = paddingVal/4)
        .fillMaxWidth()
        .graphicsLayer { translationY = offsetY.value }
        .clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 16.dp))
        .background(MaterialTheme.colorScheme.primaryContainer)
        //.zIndex(zIndex = zIndex)

    Row (
        verticalAlignment = Alignment.CenterVertically,
        modifier = itemModifier/*.onGloballyPositioned { coordinates ->
            x.value = coordinates.positionInParent().x
            y.value = coordinates.positionInParent().y
            height.value = coordinates.size.height.toFloat()
        }*/
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
        val composableScope = rememberCoroutineScope()

        if (editMode.value == true) {
            val updateOffset = {offsetChange: Float -> }
            val animate = { cScope: CoroutineScope, target: Float  -> cScope.launch {
                offsetY.animateTo(target, animationSpec = tween(60))
            }}
            ReorderIcon(
                composableScope,
                index,
                { _ -> Unit},
                updateOffset,
                animate
            )
        }
    }
}

@Composable
fun ReorderIcon(
    composableScope: CoroutineScope,
    index: Int,
    updateDraggedIndex: (index: Int) -> Unit,
    updateOffset: (offsetChange: Float) -> Unit,
    animate: (composableScope: CoroutineScope, target: Float) -> Job
) {
    val offsetLocalY = remember { mutableStateOf(0f) }
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
                        updateDraggedIndex(index)
                    },
                    onDragEnd = {
                        offsetLocalY.value = 0f
                        animate(composableScope, 0f)
                        updateDraggedIndex(-1)
                        updateOffset(0f)
                    },
                    onDrag = { change, dragAmount ->
                        offsetLocalY.value += dragAmount.y
                        updateOffset(dragAmount.y)
                        animate(composableScope, offsetLocalY.value)
                        change.consume()
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
    val draggedindex = 0
    val updateDraggedIndex = {idx: Int -> Unit}
    val offsetY = remember { Animatable(0f) }
    val listState = rememberLazyListState()
    MemoItemDisplay(memoName="MagikMemo",
        memoChecked=true,
        editMode=editMode,
        index=0,
        draggedIndex = draggedindex,
        updateDraggedIndex = updateDraggedIndex,
        offsetY=offsetY,
        { _: Int, _: Boolean -> },
        {},
        modifier = Modifier)
}
