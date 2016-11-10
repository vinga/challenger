package com.kameo.challenger.utils

fun <T> MutableList<T>.synchCopyThenClear() :List<T> {
    return synchronized(this) {
        var newList=this.toList()
        this.clear();
        newList
    }
}
