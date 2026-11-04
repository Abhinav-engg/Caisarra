package com.abhinav.caisarra.presentation.screens

import android.content.Intent
import android.net.Uri
import android.text.Html
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.abhinav.caisarra.R
import com.abhinav.caisarra.core.ui.components.CaisaaraTopBar
import com.abhinav.caisarra.core.ui.components.GeneralButton
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.core.ui.theme.StatsScreenPadding
import com.abhinav.caisarra.data.remote.api.NewsArticleDto
import com.abhinav.caisarra.presentation.common.singleClickable
import com.abhinav.caisarra.presentation.viewmodel.NewsViewModel
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.StatsCardBg
import com.caisaara.ui.theme.StatsCardBorder
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsRed
import com.caisaara.ui.theme.StatsTextMuted
import com.caisaara.ui.theme.StatsTextWhite
import com.caisaara.ui.theme.TextSizeCaption
import java.time.Duration
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    viewModel: NewsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val pullToRefreshState = rememberPullToRefreshState()
    val context = LocalContext.current

    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleItem >= totalItems - 4
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && state.hasMore && !state.isLoadingMore) {
            viewModel.loadMore()
        }
    }

    BackHandler {
        onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StatsBg)
            .statusBarsPadding()
    ) {
        CaisaaraTopBar(
            title = "NEWS",
            onBack = onBack
        )

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.refresh() },
            state = pullToRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullToRefreshState,
                    isRefreshing = state.isRefreshing,
                    modifier = Modifier.align(Alignment.TopCenter),
                    containerColor = StatsCardBg,
                    color = StatsCyan
                )
            }
        ) {
            when {
                state.isLoading && state.articles.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = StatsCyan,
                            modifier = Modifier.size(36.dp),
                            strokeWidth = 3.dp
                        )
                    }
                }
                state.error != null && state.articles.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Unable to Load News",
                                fontFamily = JetBrainsMono,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatsRed
                            )
                            Text(
                                text = state.error.orEmpty(),
                                fontFamily = JetBrainsMono,
                                fontSize = TextSizeCaption,
                                color = StatsTextMuted,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            GeneralButton(
                                text = "RETRY",
                                onClick = { viewModel.loadNews(1) }
                            )
                        }
                    }
                }
                state.articles.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No news available",
                            fontFamily = JetBrainsMono,
                            fontSize = 14.sp,
                            color = StatsTextMuted
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(StatsScreenPadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.articles, key = { it.id }) { article ->
                            NewsArticleItem(
                                article = article,
                                onClick = {
                                    if (article.link.isNotBlank()) {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.link))
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            )
                        }
                        if (state.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = StatsCyan,
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NewsArticleItem(
    article: NewsArticleDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageRequest = remember(article.imageUrl) {
        if (!article.imageUrl.isNullOrBlank()) {
            ImageRequest.Builder(context)
                .data(article.imageUrl)
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 14)")
                .crossfade(true)
                .build()
        } else {
            null
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(StatsCardBg)
            .border(1.dp, StatsCardBorder, RoundedCornerShape(12.dp))
            .singleClickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(Color(0xFF101923)),
            contentAlignment = Alignment.Center
        ) {
            if (imageRequest != null) {
                SubcomposeAsyncImage(
                    model = imageRequest,
                    contentDescription = article.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = { NewsPlaceholder(article.source) },
                    error = { NewsPlaceholder(article.source) }
                )
            } else {
                NewsPlaceholder(article.source)
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = article.source.uppercase(),
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = StatsCyan,
                    modifier = Modifier
                        .background(StatsCyan.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                val relativeTime = formatRelativeTime(article.publishedAt)
                if (relativeTime.isNotBlank()) {
                    Text(
                        text = relativeTime,
                        fontFamily = JetBrainsMono,
                        fontSize = 11.sp,
                        color = StatsTextMuted
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = article.title,
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = StatsTextWhite,
                lineHeight = 20.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            val summary = remember(article.summary) { stripHtml(article.summary) }
            if (summary.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = summary,
                    fontFamily = JetBrainsMono,
                    fontSize = 12.sp,
                    color = StatsTextMuted,
                    lineHeight = 17.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun NewsPlaceholder(source: String) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF0D1722),
                        Color(0xFF142433),
                        Color(0xFF0D1722)
                    )
                )
            )
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = source.uppercase(),
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = StatsCyan
            )
            Text(
                text = "CHESS NEWS",
                fontFamily = JetBrainsMono,
                fontSize = 11.sp,
                color = StatsTextMuted
            )
        }
        Image(
            painter = painterResource(R.drawable.challenge_pieces),
            contentDescription = null,
            modifier = Modifier
                .size(76.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Fit
        )
    }
}

private fun formatRelativeTime(isoString: String): String {
    if (isoString.isBlank()) return ""
    return try {
        val instant = Instant.parse(isoString)
        val duration = Duration.between(instant, Instant.now())
        val seconds = duration.seconds
        when {
            seconds < 60 -> "Just now"
            seconds < 3600 -> "${(seconds / 60).coerceAtLeast(1)}m ago"
            seconds < 86400 -> "${seconds / 3600}h ago"
            seconds < 172800 -> "Yesterday"
            else -> "${seconds / 86400}d ago"
        }
    } catch (_: Exception) {
        isoString.take(10)
    }
}

private fun stripHtml(html: String?): String {
    if (html.isNullOrBlank()) return ""
    return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString().trim()
}
