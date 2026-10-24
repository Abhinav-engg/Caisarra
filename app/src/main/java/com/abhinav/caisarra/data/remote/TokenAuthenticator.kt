package com.abhinav.caisarra.data.remote

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val refresher: TokenRefresher
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val path = response.request.url.encodedPath.trimStart('/')
        if (path in PUBLIC_PATHS) return null
        if (response.priorResponse != null) return null

        val sentToken = response.request.header("Authorization")?.removePrefix("Bearer ")

        return when (val result = refresher.refreshBlocking(sentToken)) {
            is RefreshResult.Success -> response.request.newBuilder()
                .header("Authorization", "Bearer ${result.accessToken}")
                .build()
            RefreshResult.Rejected -> null
            RefreshResult.Failed -> null
        }
    }
}
