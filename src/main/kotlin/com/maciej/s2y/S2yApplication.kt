package com.maciej.s2y

import com.maciej.s2y.spotify.SpotifyAuthorization
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class S2yApplication

fun main(args: Array<String>) {
	runApplication<SpotifyAuthorization>(*args)
}
