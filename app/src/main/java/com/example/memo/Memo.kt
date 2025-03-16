package com.example.memo

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

data class Memo (
    val memoText: String,
    val order: Int,
    var memoChecked : Boolean = false
) {
    var memoCheckedState = mutableStateOf(memoChecked)

    fun updateChecked() {
        memoChecked = !memoChecked
        memoCheckedState.value = memoChecked }
}