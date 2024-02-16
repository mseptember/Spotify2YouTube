package com.maciej.s2y.infrastructure.Spotify

import org.springframework.context.annotation.Configuration
import se.michaelthelin.spotify.SpotifyApi
import se.michaelthelin.spotify.SpotifyHttpManager
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest
import java.net.URI

@Configuration
class SpotifyAuthorization {
    private val redirectUri: URI = SpotifyHttpManager.makeUri("http://localhost:3000/auth/spotify/callback")
    private var accessToken: String = "" // TODO do something more secure than that
    private var refreshToken: String = "" // TODO do something more secure than that

    fun getSpotifyBuilder(clientID: String, clientSecret: String):
            SpotifyApi = SpotifyApi.builder()
        .setClientId(clientID)
        .setClientSecret(clientSecret)
        .setRedirectUri(redirectUri)
        .build()

    fun authorizationCodeUriRequest(spotifyApi: SpotifyApi):
            AuthorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
        .state("x4xkmn9pu3j6ukrs8n")
        .scope("user-read-email")
        .show_dialog(true)
        .build()

    fun buildAuthorizationCode(spotifyApi: SpotifyApi, code: String): AuthorizationCodeRequest {
        println("Received code: $code")
        return spotifyApi.authorizationCode(code)
            .build()
    }

    fun tokenAuthorization(accessToken: String?): SpotifyApi {
        if (accessToken != null) {
            this.accessToken = accessToken
        }
        return SpotifyApi.Builder().setAccessToken(accessToken).build()
    }

    fun setRefreshToken(refreshToken: String?): SpotifyApi {
        if (refreshToken != null) {
            this.refreshToken = refreshToken
        }
        return SpotifyApi.Builder().setRefreshToken(refreshToken).build()
    }

    fun getAccessToken(): String? {
        return this.accessToken
    }

    fun getRefreshToken(): String? {
        return this.refreshToken
    }
}