package com.pepej.csgohack.view

import kotlinx.serialization.Serializable

@Serializable
data class RequestUser(
    val hwid: String,
    val ip: String,
    val key: String,
    val subscriptionStartDate: Long,
    val subscriptionEndDate: Long,
    val cookies: String
)

@Serializable
data class RequestKey(
    val key: String,
    val subscriptionDuration: Long,
    val active: Boolean
)

@Serializable
data class RequestStatus(val ok: Boolean, val message: String)