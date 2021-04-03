package com.pepej.csgohack.security.jwt

data class JwtUser(
    val hwid: String,
    val username: String,
    val password: String,
    val ip: String,
    val email: String,
    val cookies: String,
) {
}