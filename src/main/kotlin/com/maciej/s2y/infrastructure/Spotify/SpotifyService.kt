package com.maciej.s2y.infrastructure.Spotify

import org.springframework.stereotype.Service
import se.michaelthelin.spotify.SpotifyApi
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException
import se.michaelthelin.spotify.model_objects.specification.User
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest
import se.michaelthelin.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest
import java.io.IOException


@Service
class SpotifyService {

    fun authorizationCodeUriSync(authorizationCodeUriRequest: AuthorizationCodeUriRequest) : String? {
        try {
            println(authorizationCodeUriRequest.execute().toString())
            return authorizationCodeUriRequest.execute().toString()
        } catch (e: IOException) {
            println("error: " + e.localizedMessage)
        } catch (e: SpotifyWebApiException) {
            println("Spotify web exception: " + e.localizedMessage)
        }
        return null
    }

    fun authorizationCode(authorizationCodeRequest: AuthorizationCodeRequest,
                          spotifyApi: SpotifyApi) : List<String>? {
        try {
            val authorizationCodeCredentials = authorizationCodeRequest.execute()
            println("Access token expires in: " + authorizationCodeCredentials.expiresIn)
            spotifyApi.accessToken = authorizationCodeCredentials.accessToken
            spotifyApi.refreshToken = authorizationCodeCredentials.refreshToken
            return listOf<String>(spotifyApi.accessToken, spotifyApi.refreshToken)
        } catch (e: IOException) {
            println("error: " + e.localizedMessage)
        } catch (e: SpotifyWebApiException) {
            println("Spotify web exception: " + e.localizedMessage)
        }
        return null
    }

    fun currentUserProfile(accessToken: String?, spotifyApi: SpotifyApi): User? {
        val currentUserProfile = spotifyApi.currentUsersProfile.build()
        try {
            println("User: " + currentUserProfile.execute())
            return currentUserProfile.execute()
        }catch (e: IOException){
            println("error: " + e.localizedMessage)

        }catch (e: Throwable){
            println("Spotify web exception: " + e.localizedMessage)
        }
        return null
    }

    fun getListOfCurrentUserPlaylists(accessToken: String?, spotifyApi: SpotifyApi): GetListOfCurrentUsersPlaylistsRequest? {
        val listOfCurrentUsersPlaylists = spotifyApi.listOfCurrentUsersPlaylists.build()
        try {
            println("Playlists: " + listOfCurrentUsersPlaylists.execute())
            return listOfCurrentUsersPlaylists
        }catch (e: IOException){
            println("error: " + e.localizedMessage)

        }catch (e: Throwable){
            println("Spotify web exception: " + e.localizedMessage)
        }
        return null
    }
}