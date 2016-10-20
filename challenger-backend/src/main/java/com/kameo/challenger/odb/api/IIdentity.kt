package com.kameo.challenger.odb.api

import java.io.Serializable

interface IIdentity: Serializable {


    val id: Long // abstract

    fun isNew(): Boolean {
        return id < 0
    }

    companion object Meta {
        val column_id="id"

        fun compare(o1: IIdentity, o2: IIdentity): Int {
            if (o1.id > o2.id)
                return 1
            else if (o1.id < o2.id)
                return -1
            else
                return 0
        }
    }

}