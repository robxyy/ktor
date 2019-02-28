package io.ktor.client.tests.utils

import ch.qos.logback.classic.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.jetty.*
import io.ktor.websocket.*
import org.slf4j.*
import org.slf4j.Logger

private val DEFAULT_PORT: Int = 8080

internal fun startServer(): ApplicationEngine {
    println("START SERVER")
    val logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as ch.qos.logback.classic.Logger
    logger.level = Level.WARN

    return embeddedServer(Jetty, DEFAULT_PORT) {
        routing {
            post("/echo") {
                val response = call.receiveText()
                call.respond(response)
            }
            get("/bytes") {
                val size = call.request.queryParameters["size"]!!.toInt()
                call.respondBytes(makeArray(size))
            }
            route("/json") {
                get("/users") {
                    call.respondText("[{id: 42, login: 'TestLogin'}]", contentType = ContentType.Application.Json)
                }
                get("/photos") {
                    call.respondText("[{id: 4242, path: 'cat.jpg'}]", contentType = ContentType.Application.Json)
                }
            }
            route("/benchmarks") {
                val testData = "{'message': 'Hello World'}"

                /**
                 * Receive json data-class.
                 */
                get("/json") {
                    call.respondText(testData, ContentType.Application.Json)
                }

                /**
                 * Send json data-class.
                 */
                post("/json") {
                    val request = call.receiveText()
                    check(testData == request)
                    call.respond(HttpStatusCode.OK, "OK")
                }

                /**
                 * Submit url form.
                 */
                get("/form-url") {
                }

                /**
                 * Submit body form.
                 */
                post("/form-body") {
                }

                /**
                 * Download file.
                 */
                get("/file-download") {
                }

                /**
                 * Upload file
                 */
                post("/file-upload") {
                }

                route("/websockets") {
                    webSocket("/get/{count}") {
                        println("connected")
                        val count = call.parameters["count"]!!.toInt()

                        repeat(count) {
                            send("$it")
                        }
                    }
                }

            }

        }
    }.start()
}
