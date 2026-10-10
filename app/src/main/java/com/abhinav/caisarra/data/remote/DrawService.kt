package com.abhinav.caisarra.data.remote

import retrofit2.http.POST
import retrofit2.http.Path

interface DrawService {

    @POST("api/games/{gameId}/draw/offer")
    suspend fun offerDraw(@Path("gameId") gameId: String)

    @POST("api/games/{gameId}/draw/accept")
    suspend fun acceptDraw(@Path("gameId") gameId: String)

    @POST("api/games/{gameId}/draw/decline")
    suspend fun declineDraw(@Path("gameId") gameId: String)
}