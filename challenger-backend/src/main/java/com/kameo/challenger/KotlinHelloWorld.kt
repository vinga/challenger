package com.kameo.challenger

class KotlinHelloWorld {
    fun main(a: Int?,  args: Array<String>) {
        println("Hello, world!")
    }


    fun test() {
       // println(toString2());

        var kt: KotlinHelloWorld?=null;
        println(kt.toString2());
    }
}

fun Any?.toString2(): String {
    if (this == null) return "Bnull"
    // after the null check, 'this' is autocast to a non-null type, so the toString() below
    // resolves to the member function of the Any class
    return "A"+toString()
}