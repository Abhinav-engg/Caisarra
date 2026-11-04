package com.abhinav.caisarra.data.remote.api

import com.abhinav.caisarra.data.remote.dto.ApiResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsService {
    @GET("api/news")
    suspend fun getNews(
        @Query("page") page: Int = 1
    ): ApiResponse<NewsDataDto>
}

@Serializable
data class NewsDataDto(
    val articles: List<NewsArticleDto> = emptyList()
)

@Serializable
data class NewsArticleDto(
    val id: Long = 0L,
    val title: String = "",
    val link: String = "",
    val summary: String? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    val source: String = "",
    @SerialName("published_at")
    val publishedAt: String = ""
)
