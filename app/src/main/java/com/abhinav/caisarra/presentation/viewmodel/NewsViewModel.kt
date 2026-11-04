package com.abhinav.caisarra.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.remote.ApiErrorInterpreter
import com.abhinav.caisarra.data.remote.api.NewsArticleDto
import com.abhinav.caisarra.data.remote.api.NewsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.ConcurrentHashMap

data class NewsUiState(
    val articles: List<NewsArticleDto> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null
)

class NewsViewModel(
    private val service: NewsService
) : ViewModel() {
    private val _state = MutableStateFlow(NewsUiState())
    val state = _state.asStateFlow()

    private var currentPage = 1
    private val httpClient = OkHttpClient()
    private val ogImageCache = ConcurrentHashMap<String, String>()

    init {
        loadNews(1)
    }

    fun loadNews(page: Int, isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _state.update { it.copy(isRefreshing = true, error = null) }
            } else if (page == 1) {
                _state.update { it.copy(isLoading = true, error = null) }
            } else {
                _state.update { it.copy(isLoadingMore = true) }
            }

            try {
                val response = service.getNews(page = page)
                if (response.success && response.data != null) {
                    val incoming = response.data.articles
                    currentPage = page
                    _state.update { cur ->
                        cur.copy(
                            articles = if (page == 1) incoming else cur.articles + incoming,
                            isLoading = false,
                            isRefreshing = false,
                            isLoadingMore = false,
                            hasMore = incoming.isNotEmpty() && incoming.size >= 20,
                            error = null
                        )
                    }
                    resolveOgImages(incoming)
                } else {
                    val err = response.error ?: response.message ?: "Failed to fetch news"
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            isLoadingMore = false,
                            error = if (it.articles.isEmpty()) err else null
                        )
                    }
                }
            } catch (e: Exception) {
                val err = ApiErrorInterpreter.toUserMessage(e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                        error = if (it.articles.isEmpty()) err else null
                    )
                }
            }
        }
    }

    fun refresh() {
        if (_state.value.isLoading || _state.value.isRefreshing) return
        loadNews(1, isRefresh = true)
    }

    fun loadMore() {
        val s = _state.value
        if (s.isLoading || s.isRefreshing || s.isLoadingMore || !s.hasMore) return
        loadNews(currentPage + 1)
    }

    private fun resolveOgImages(articles: List<NewsArticleDto>) {
        viewModelScope.launch(Dispatchers.IO) {
            articles.forEach { article ->
                if (article.imageUrl.isNullOrBlank() && article.link.isNotBlank()) {
                    val cached = ogImageCache[article.link]
                    if (cached != null) {
                        updateArticleImage(article.id, cached)
                    } else {
                        val ogImage = fetchOgImage(article.link)
                        if (!ogImage.isNullOrBlank()) {
                            ogImageCache[article.link] = ogImage
                            updateArticleImage(article.id, ogImage)
                        }
                    }
                }
            }
        }
    }

    private fun fetchOgImage(link: String): String? {
        return try {
            val request = Request.Builder()
                .url(link)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14)")
                .build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val html = response.body?.string() ?: return null
                val m = Regex("""<meta[^>]*property=["']og:image["'][^>]*content=["']([^"']+)["']""", RegexOption.IGNORE_CASE).find(html)
                    ?: Regex("""<meta[^>]*content=["']([^"']+)["'][^>]*property=["']og:image["']""", RegexOption.IGNORE_CASE).find(html)
                m?.groupValues?.get(1)?.trim()?.takeIf { it.isNotBlank() }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun updateArticleImage(articleId: Long, imageUrl: String) {
        _state.update { state ->
            state.copy(
                articles = state.articles.map {
                    if (it.id == articleId) it.copy(imageUrl = imageUrl) else it
                }
            )
        }
    }
}
