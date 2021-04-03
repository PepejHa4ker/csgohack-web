package com.pepej.csgohack.model

import kotlinx.serialization.Serializable
import java.time.Duration
import kotlin.time.ExperimentalTime

@Serializable
data class Key(val holder: User? = null, val content: String, val expiringTime: Long)
