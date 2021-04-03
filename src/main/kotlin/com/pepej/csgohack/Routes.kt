package com.pepej.csgohack

import com.pepej.csgohack.view.RequestKey
import com.pepej.csgohack.view.RequestStatus
import com.pepej.csgohack.view.RequestUser
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.license() {
    post("/licenses") {
        val user = call.receive<RequestUser>()
        if (userManager.userExists(user.hwid)) {
            call.respond(HttpStatusCode.OK, RequestStatus(false, "Пользователь уже с таким hwid уже существует"))
            return@post
        }

        val created = userManager.createUser(user.hwid,
            user.key,
            user.ip,
            user.subscriptionStartDate,
            user.subscriptionEndDate,
            user.cookies)

        call.respond(HttpStatusCode.OK, RequestStatus(true, "Пользователь $created успешно создан"))

    }

    post("/keys/generate") {
        val key = call.receive<RequestKey>()
        if (keyManager.keyExist(key.key)) {
            call.respond(HttpStatusCode.OK, RequestStatus(false, "Такой ключ уже существует"))
            return@post
        }
        val generated = keyManager.generateKey(key.key, key.subscriptionDuration)
        call.respond(HttpStatusCode.OK, RequestStatus(true, "Ключ $generated успешно создан"))

    }

    get("/users/hwid/{hwid}") {
        val hwid = call.parameters["hwid"] ?: return@get call.respond(
            HttpStatusCode.BadRequest,
            RequestStatus(false, "Неверные параметры запроса")
        )

        val user = userManager.getUserByHwid(hwid) ?: return@get call.respond(
            HttpStatusCode.NotFound,
            RequestStatus(false, "Пользователь с текущим hwid не найден"))

        call.respond(user.toRequestUser())
    }

    get("/users/ip/{ip}") {
        val ip = call.parameters["ip"] ?: return@get call.respond(
            HttpStatusCode.BadRequest,
            RequestStatus(false, "Неверные параметры запроса")
        )

        val user = userManager.getUsersByIp(ip).filterNotNull()

        call.respond(user.map { it.toRequestUser() })
    }
}