package com.pepej.csgohack.security.jwt

import java.util.*


class JwtTokenProvider(var secret: String, val validateMillis: Long) {

    init {
        secret = Base64.getEncoder().encodeToString(secret.toByteArray())
    }

    fun createToken(username: String): String {
        return ""
    }

    fun getUsername(token: String): String {
        return ""
    }

    fun validateToken(token: String): Boolean {
        return true
    }
}