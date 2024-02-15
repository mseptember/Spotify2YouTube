package com.maciej.s2y.infrastructure.Spotify

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView
import se.michaelthelin.spotify.model_objects.specification.User
import se.michaelthelin.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest

@RestController
class SpotifyController {

    @Value("\${spotify.clientid}")
    lateinit var clientID: String

    @Value("\${spotify.clientsecret}")
    lateinit var clientSecret: String

    var spotifyAuth = SpotifyAuthorization()

    @RequestMapping("/api/spotify/callback/uri")
    fun authorizationUri(): RedirectView? {
        val response = createSpotifyService().authorizationCodeUriSync(
            spotifyAuth.authorizationCodeUriRequest(
                spotifyAuth.getSpotifyBuilder(clientID, clientSecret)
            )
        )
        return RedirectView(response ?: "/error")
    }

    @RequestMapping("/auth/spotify/callback")
    //@RequestMapping("/api/spotify/token/{code}")
    fun authorizationToken(
        @RequestParam("code") code: String,
        @RequestParam("state") state: String
    ): RedirectView {
        val response = createSpotifyService()
            .authorizationCode(
                spotifyAuth.buildAuthorizationCode(
                    spotifyAuth.getSpotifyBuilder(clientID, clientSecret), code
                ),
                spotifyAuth.getSpotifyBuilder(clientID, clientSecret)
            )

        // send and set vars for both accessToken and refreshToken
        val accessToken = response?.get(0)
        val refreshToken = response?.get(1)
        spotifyAuth.tokenAuthorization(accessToken)
        spotifyAuth.setRefreshToken(refreshToken)

        // TODO change it later to a different URL, so I won't get just user data
        return RedirectView("/api/spotify/user/playlists")
    }

    @RequestMapping("/api/spotify/user/{token}")
    fun currentUserData(
        @PathVariable token: String?
    ): User? {
        val user = createSpotifyService()
            .currentUserProfile(
                token, spotifyAuth.tokenAuthorization(token)
            )

        return user
    }

    @RequestMapping("/api/spotify/user/playlists")
    fun currentUserPlaylists(): GetListOfCurrentUsersPlaylistsRequest? {
        val playlists = createSpotifyService()
            .getListOfCurrentUserPlaylists(
                spotifyAuth.getAccessToken(), spotifyAuth.tokenAuthorization(spotifyAuth.getAccessToken())
            )
        return playlists
    }


    fun createSpotifyService(): SpotifyService = SpotifyService()
}
