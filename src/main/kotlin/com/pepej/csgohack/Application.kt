package com.pepej.csgohack

import com.pepej.csgohack.auth.OAuthLoginApplication
import com.pepej.csgohack.database.KeyManager
import com.pepej.csgohack.database.SQLManager
import com.pepej.csgohack.database.UserManager
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.json.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.locations.*
import io.ktor.routing.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

lateinit var sql: SQLManager
lateinit var userManager: UserManager
lateinit var keyManager: KeyManager

@KtorExperimentalLocationsAPI
@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    sql = SQLManager()
    userManager = UserManager()
    keyManager = KeyManager()
    val client = HttpClient(OkHttp) {
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
    }
    client.apply {
        environment.monitor.subscribe(ApplicationStopping) {
            close()
        }

    }
    install(Routing) {
        license()
    }
    OAuthLoginApplication(oauthHttpClient = client)
    install(ContentNegotiation) {
        gson {}
    }
}


