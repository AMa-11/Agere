package com.example.memo

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

data class Memo (
    val memoText: String,
    val order: Int,
    var checked : Boolean = false
) {}