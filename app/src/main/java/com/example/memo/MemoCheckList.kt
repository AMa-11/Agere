package com.example.memo

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.Animatable
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
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.gestures.verticalDrag
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


// TODO: REFACTOR MEMOSCREEN
@Composable
fun MemoScreen (
    viewModel: MemoListViewModel,
    modifier: Modifier = Modifier
) {
    val memos = viewModel.memos
    val editMode = remember {mutableStateOf(false)}
    val toggleEditMode = {editMode.value = !editMode.value}

    Scaffold(modifier,
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
            MemoListDisplay(memos, viewModel::updateCheckedItem ,viewModel::removeItem)
        }
    }
}
@Composable
fun TopBar(toggleEditMode: () -> Unit ) {
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
        Button(onClick = { toggleEditMode() },
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Image(
                painter = painterResource(R.drawable.menu),
                contentDescription = "EditButton"
            )
        }
    }
}

@Composable
fun BottomBar(onAddItem: (text: String) -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondary
    ) {
        ButtonAddItem(onAddItem)
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

@Composable
fun MemoListDisplay (
    items: List<Memo>,
    onCheckItem: (index: Int, checked: Boolean) -> Unit,
    onRemoveItem: (index: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val dragged_index = remember { mutableStateOf(-1) }
    val offsetY = remember { Animatable(0f) }

    val memoListState = LazyListState()

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
                dragged_index,
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
    index: Int,
    draggedIndex: MutableState<Int>,
    offsetY: Animatable<Float, AnimationVector1D>,
    onCheckItem: (index: Int, checked: Boolean) -> Unit,
    onRemoveItem: (index : Int) -> Unit,
    modifier: Modifier
) {
    val paddingVal = 16.dp
    var offset: Float
    if(index == draggedIndex.value) {
        offset = offsetY.value
    } else { offset = 0f
    }
    val itemModifier = Modifier.padding(horizontal = paddingVal, vertical = paddingVal/4)
        .fillMaxWidth()
        .graphicsLayer { translationY = offset }

    Log.d("index:", index.toString())
    Log.d("Draged index received:", draggedIndex.toString())

    val offsetFloat = remember { mutableStateOf(0f) }
    Row (
        verticalAlignment = Alignment.CenterVertically,
        modifier = itemModifier
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
        Button(
            onClick = {onRemoveItem(index)},
        ) {
            Text("remove")
        }
        val composableScope = rememberCoroutineScope()
        Image (
            painterResource(id = R.drawable.reorder),
            contentDescription = "reorder icon",
            modifier = Modifier.pointerInput(Unit) {
                Log.d("Pointer Input Detected:", "InputDetected")
                detectDragGestures(
                    onDragStart = { offset: Offset ->

                        draggedIndex.value = index
                        offsetFloat.value += offset.y
                        composableScope.launch {
                            offsetY.animateTo(offsetFloat.value, tween(60))
                        }
                        Log.d("START DRAG Offset: ", offsetY.value.toString())
                        Log.d("START DRAG Dragged Index: ", draggedIndex.value.toString())


                    },
                    onDragEnd = {
                        composableScope.launch {
                            offsetY.animateTo(0f, animationSpec = tween(60))
                        }
                        draggedIndex.value = -1
                        offsetFloat.value = 0f
                        Log.d("END DRAG Offset: ", offsetY.value.toString())
                        Log.d("END DRAG Dragged Index: ", draggedIndex.value.toString())
                    },
                    onDrag = {change, dragAmount ->
                        offsetFloat.value += dragAmount.y
                        composableScope.launch {
                            offsetY.animateTo(offsetFloat.value, tween(60))
                        }
                        Log.d("DRAG Offset: ", offsetY.value.toString())
                        Log.d("DRAG Dragged Index: ", draggedIndex.value.toString())
                        change.consume()
                    }
                )
            }
                /*
            .transformable(
                state = rememberTransformableState {
                    _: Float, panChange: Offset, _: Float ->
                    if (index == draggedIndex.value) {
                        offsetY.value += panChange.y
                        Log.d("Offset: ", offsetY.value.toString())
                        draggedIndex.value = index
                        Log.d("Dragged Index: ", draggedIndex.value.toString())
                    }
                }*/
        )

    }
}

// FROM https://developer.android.com/codelabs/jetpack-compose-animation#7
/*
private fun Modifier.drag(
    onDismissed: () -> Unit,
    draggedIndex: MutableState<Int>,
    offsetY: MutableState<Float>
): Modifier = composed {

    pointerInput(Unit) {
        // Used to calculate a settling position of a fling animation.
        val decay = splineBasedDecay<Float>(this)
        // Wrap in a coroutine scope to use suspend functions for touch events and animation.
        coroutineScope {
            while (true) {
                // Wait for a touch down event.
                val pointerId = awaitPointerEventScope { awaitFirstDown().id }
                // Interrupt any ongoing animation.

                // Wait for drag events.
                awaitPointerEventScope {
                    verticalDrag(pointerId) { change ->
                        // Record the position after offset
                        val verticalDragOffset = offsetY.value + change.positionChange().y
                        launch {
                            // Overwrite the Animatable value while the element is dragged.
                            offsetY.snapTo(verticalDragOffset)
                        }

                        // Consume the gesture event, not passed to external
                        change.consumePositionChange()
                    }
                }

                // The animation should end as soon as it reaches these bounds.
                offsetY.updateBounds(
                    lowerBound = -size.width.toFloat(),
                    upperBound = size.width.toFloat()
                )
                launch {
                    if (targetOffsetX.absoluteValue <= size.width) {
                        // Not enough velocity; Slide back to the default position.
                        offsetY.animateTo(targetValue = 0f, initialVelocity = velocity)
                    } else {
                        // Enough velocity to slide away the element to the edge.
                        offsetY.animateDecay(velocity, decay)
                        // The element was swiped away.
                        onDismissed()
                    }
                }
            }
        }
    }
        // Apply the horizontal offset to the element.
        .offset { IntOffset(offsetX.value.roundToInt(), 0) }
}
*/



@Preview
@Composable
fun ListScreenPreview() {
    var list = mutableListOf(
        Memo("Memo1",  1, ),
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
    val dragged_index = remember { mutableStateOf(0) }
    val offsetY = remember { Animatable(0f) }
    MemoItemDisplay("MagikMemo", true,0, dragged_index, offsetY, { _: Int, _: Boolean -> }, {}, modifier = Modifier)
}
