package com.example.memo

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel


class MemoListViewModel (list: MutableList<Memo>, listName: String)
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