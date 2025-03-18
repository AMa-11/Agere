package com.example.memo

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import java.util.UUID

data class Memo (
    val memoText: String,
    val order: Int,
    val checked : MutableState<Boolean> = mutableStateOf(false),
    //TODO: CONSIDER CASE IF GENERATOR   UUID IS NOT UNIQUE IN THE LIST
    val idKey: UUID = UUID.randomUUID()
) {}
