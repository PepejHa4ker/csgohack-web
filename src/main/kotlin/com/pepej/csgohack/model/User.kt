package com.pepej.csgohack.model

import kotlinx.serialization.Serializable

@Serializable
data class User(val hwid: String, val username: String, val password: String, val ip: String, val email: String, val cookies: String)
