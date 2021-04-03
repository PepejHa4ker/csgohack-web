package com.pepej.csgohack.security

import com.pepej.csgohack.model.Key
import org.apache.commons.lang3.RandomStringUtils
import java.time.Duration


interface KeyGenerator {

    fun generate(length: Int = 20, expiringTime: Long = Duration.ofDays(1).toMillis()): Key

}


class KeyGeneratorImpl : KeyGenerator {
    override fun generate(length: Int, expiringTime: Long): Key {
        val generatedString = RandomStringUtils.random(length)
        return Key(null, generatedString, expiringTime)
    }

}