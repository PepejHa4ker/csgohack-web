package com.pepej.csgohack.security.jwt

import com.pepej.csgohack.model.User

object JwtUserFactory {

    fun create(user: User): JwtUser {
        return JwtUser(
            user.hwid,
            user.username,
            user.password,
            user.ip,
            user.email,
            user.cookies
        )
    }

}