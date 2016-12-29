package com.kameo.challenger.domain.accounts

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


object PasswordUtil {

    private fun randomInt(min: Int, max: Int): Int {
        return (Math.random() * (max - min) + min).toInt()
    }

    private fun randomString(min: Int, max: Int): String {
        val num = randomInt(min, max)
        val b = ByteArray(num)
        for (i in 0..num - 1)
            b[i] = randomInt('a'.toInt(), 'z'.toInt()).toByte()
        return String(b)
    }


    fun createSalt(): String {
        return randomString(6, 6)
    }

    fun getPasswordHash(pass: String, salt: String): String {

        val md: MessageDigest
        val sb = StringBuilder()
        try {
            md = MessageDigest.getInstance("SHA-256")
            md.update((salt + pass + salt).toByteArray())
            val byteData = md.digest()

            // convert the byte to hex format method 1

            for (element in byteData) {
                sb.append(Integer.toString((element.toInt() and 0xff) + 0x100, 16)
                        .substring(1))
            }

        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        return sb.toString()
    }
}
