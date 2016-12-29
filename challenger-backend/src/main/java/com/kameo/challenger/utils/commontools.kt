package com.kameo.challenger.utils

import java.time.LocalDate


fun <T> MutableList<T>.synchCopyThenClear() :List<T> {
    return synchronized(this) {
        val newList=this.toList()
        this.clear()
        newList
    }
}

inline fun <reified T : Any> Any.getThroughReflection(propertyName: String): T? {
    val getterName = "get" + propertyName.capitalize()
    return try {
        javaClass.getMethod(getterName).invoke(this) as? T
    } catch (e: NoSuchMethodException) {
        throw IllegalArgumentException(e)
    }
}
inline fun <reified T : Any> Any.setThroughReflection(propertyName: String, t: T?) {
    val setterName = "set" + propertyName.capitalize()
    try {
        javaClass.getMethod(setterName).invoke(this, t)
    } catch (e: NoSuchMethodException) {
       throw IllegalArgumentException(e)
    }
}

operator fun ClosedRange<LocalDate>.iterator() : Iterator<LocalDate>{
    return object: Iterator<LocalDate> {
        private var next = this@iterator.start
        private val finalElement = this@iterator.endInclusive
        private var hasNext = !next.isAfter(this@iterator.endInclusive)

        override fun hasNext(): Boolean = hasNext

        override fun next(): LocalDate {
            val value = next
            if(value == finalElement) {
                hasNext = false
            }
            else {
                next = next.plusDays(1)
            }
            return value
        }
    }
}