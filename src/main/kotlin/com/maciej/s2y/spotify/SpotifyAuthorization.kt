package com.maciej.s2y.spotify

import org.springframework.boot.autoconfigure.SpringBootApplication
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64.getEncoder
import java.awt.Desktop
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.*

@SpringBootApplication
class SpotifyAuthorization

fun main() {
    val clientId = "your_client_id"
    val clientSecret = "your_client_secret"

    val authHeaders = mapOf(
        "client_id" to clientId,
        "response_type" to "code",
        "redirect_uri" to "http://localhost:8080",
        "scope" to "user-library-read"
    )

    val authUrl = "https://accounts.spotify.com/authorize?" + urlencode(authHeaders)
    openWebBrowser(authUrl)

    // Assuming you manually enter the authorization code here
    val code =
        "AQApKvjpyLV9IJ7HIBexCYKyJJVRikFM9pzUNDZBtrJjbOA3OiZEqBzhzkvcEcPf2bd8QpIgJtHZfUgFyHrIuZBamnARPid148bDBxI1srjtTRDMkFJHu-Wa7IRGiJPMsiicKPs-tEf93olFlzznQlMcUGnP0jo-z5_f-8Xo_pmtlvB2JmegrZ6IHFs"

    val encodedCredentials = getEncodedCredentials(clientId, clientSecret)

    val tokenHeaders = mapOf(
        "Authorization" to "Basic $encodedCredentials",
        "Content-Type" to "application/x-www-form-urlencoded"
    )

    val tokenData = mapOf(
        "grant_type" to "authorization_code",
        "code" to code,
        "redirect_uri" to "http://localhost:8080"
    )

    val token = requestAccessToken(tokenHeaders, tokenData)

    val userHeaders = mapOf(
        "Authorization" to "Bearer $token",
        "Content-Type" to "application/json"
    )

    val userParams = mapOf(
        "limit" to "50"
    )

    val url = URL("https://api.spotify.com/v1/me/tracks?" + urlEncodeParams(userParams))
    with(url.openConnection() as HttpURLConnection) {
        requestMethod = "GET"
        setRequestProperty("Authorization", userHeaders["Authorization"])
        setRequestProperty("Content-Type", userHeaders["Content-Type"])

        val responseCode = responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val reader = BufferedReader(InputStreamReader(inputStream))
            val response = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            reader.close()

            println("User tracks: $response")
        } else {
            println("Error getting user tracks. Response code: $responseCode")
        }
    }

    println(token)
}

fun urlencode(params: Map<String, String>): String {
    return params.entries.joinToString("&") {
        "${it.key}=${
            URLEncoder.encode(
                it.value,
                StandardCharsets.UTF_8.toString()
            )
        }"
    }
}

fun openWebBrowser(url: String) {
    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
        Desktop.getDesktop().browse(URI(url))
    } else {
        println("Unable to open a web browser. Please visit the following URL manually: $url")
    }
}

fun urlEncodeParams(params: Map<String, String>): String {
    return params
        .map { (key, value) -> URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8") }
        .joinToString("&")
}

fun getEncodedCredentials(clientId: String, clientSecret: String): String {
    val credentials = "$clientId:$clientSecret"
    val encodedCredentials = getEncoder().encodeToString(credentials.toByteArray())
    return String(encodedCredentials.toByteArray(), StandardCharsets.UTF_8)
}

fun requestAccessToken(headers: Map<String, String>, data: Map<String, String>): String {
    val url = "https://accounts.spotify.com/api/token"
    val body = urlencode(data)

    val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
    connection.requestMethod = "POST"
    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
    headers.forEach { connection.setRequestProperty(it.key, it.value) }

    connection.doOutput = true
    connection.outputStream.use { it.write(body.toByteArray(StandardCharsets.UTF_8)) }

    val responseCode = connection.responseCode
    val responseBody = if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
        connection.inputStream.bufferedReader().use { it.readText() }
    } else {
        connection.errorStream.bufferedReader().use { it.readText() }
    }

    connection.disconnect()

    return responseBody
}