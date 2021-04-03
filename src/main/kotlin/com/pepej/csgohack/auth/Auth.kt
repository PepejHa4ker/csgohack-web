package com.pepej.csgohack.auth

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.locations.get
import io.ktor.locations.head
import io.ktor.request.*
import io.ktor.routing.*
import kotlinx.html.*

@KtorExperimentalLocationsAPI
@Location("/")
class Index

@KtorExperimentalLocationsAPI
@Location("/login/{type?}")
class Login(val type: String = "")

val loginProviders = listOf(
    OAuthServerSettings.OAuth2ServerSettings(
        name = "vk",
        authorizeUrl = "https://oauth.vk.com/authorize",
        accessTokenUrl = "https://oauth.vk.com/access_token",
        clientId = "7760908",
        clientSecret = "8R2zSfzhD7rWQk4XgBaG"
    ),
).associateBy { it.name }


@KtorExperimentalLocationsAPI
fun Application.OAuthLoginApplication(oauthHttpClient: HttpClient) {
    val authOauthForLogin = "authOauthForLogin"

    install(DefaultHeaders)
    install(CallLogging)
    install(Locations)
    install(Authentication) {
        oauth(authOauthForLogin) {
            client = oauthHttpClient
            providerLookup = {
                loginProviders[application.locations.resolve<Login>(Login::class, this).type]
            }
            urlProvider = { p -> redirectUrl(Login(p.name), false) }
        }
    }


    routing {
        get<Index> {
            call.respondHtml {
                head {
                    title { +"index page" }
                }
                body {
                    h1 {
                        +"Try to login"
                    }
                    p {
                        a(href = locations.href(Login())) {
                            +"Login"
                        }
                    }
                }
            }
        }


        authenticate(authOauthForLogin) {
            location<Login> {
                param("error") {
                    handle {
                        call.loginFailedPage(call.parameters.getAll("error").orEmpty())
                    }
                }

                handle {
                    val principal = call.authentication.principal<OAuthAccessTokenResponse>()
                    if (principal != null) {
                        call.loggedInSuccessResponse(principal)
                    } else {
                        call.loginPage()
                    }
                }
            }
        }
    }
}

@KtorExperimentalLocationsAPI
private fun <T : Any> ApplicationCall.redirectUrl(t: T, secure: Boolean = true): String {
    val hostPort = request.host() + request.port().let { port -> if (port == 80) "" else ":$port" }
    val protocol = when {
        secure -> "https"
        else -> "http"
    }
    val redirect = "$protocol://$hostPort${application.locations.href(t)}"
    println(redirect)
    return redirect
}

@KtorExperimentalLocationsAPI
private suspend fun ApplicationCall.loginPage() {
    respondHtml {
        head {
            title { +"Login with" }
        }
        body {
            h1 {
                +"Login with:"
            }

            for (p in loginProviders) {
                p {
                    a(href = application.locations.href(Login(p.key))) {
                        +p.key
                    }
                }
            }
        }
    }
}


private suspend fun ApplicationCall.loginFailedPage(errors: List<String>) {
    respondHtml {
        head {
            title { +"Login with" }
        }
        body {
            h1 {
                +"Login error"
            }

            for (e in errors) {
                p {
                    +e
                }
            }
        }
    }
}

private suspend fun ApplicationCall.loggedInSuccessResponse(callback: OAuthAccessTokenResponse) {
    respondHtml {
        head {
            title { +"Logged in" }
        }
        body {
            h1 {
                +"You are logged in"
            }
            p {
                +"Your token is $callback"
            }
        }
    }
}