
package com.abhinav.caisarra.presentation.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.core.network.userMessage
import com.abhinav.caisarra.data.remote.dto.CommunityPostDto
import com.abhinav.caisarra.data.remote.dto.GameHistoryDto
import com.abhinav.caisarra.data.repository.CommunityRepository
import com.abhinav.caisarra.data.repository.RemoteGameRepository
import com.abhinav.caisarra.features.follow.data.FollowRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CommunityUiState(
    val posts: List<CommunityPostDto> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isPublishing: Boolean = false,
    val nextBeforeId: Long? = null,
    val error: String? = null,
    val info: String? = null,
    val commentsPostId: Long? = null,
    val showingBookmarks: Boolean = false,
    val isLoadingBookmarks: Boolean = false,
    val shareableGames: List<GameHistoryDto> = emptyList(),
    val isLoadingGames: Boolean = false,
    val feedTab: CommunityFeedTab = CommunityFeedTab.FOLLOWING
)

class CommunityViewModel(
    private val repository: CommunityRepository,
    private val remoteGameRepository: RemoteGameRepository,
    private val followRepository: FollowRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CommunityUiState())
    val state = _state.asStateFlow()
    private var feedJob: Job? = null

    init {
        refresh()
        loadShareableGames()
        viewModelScope.launch { markFollowing(followRepository.followNewFriends(), true) }
    }

    fun selectTab(tab: CommunityFeedTab) {
        if (_state.value.feedTab == tab) return
        _state.update { it.copy(feedTab = tab, posts = emptyList(), nextBeforeId = null, isLoading = false, isLoadingMore = false) }
        if (!_state.value.showingBookmarks) refresh()
    }

    private val followingFeed get() = _state.value.feedTab == CommunityFeedTab.FOLLOWING

    fun toggleFollow(userId: Long) {
        val follow = _state.value.posts.none { it.author.id == userId && it.author.isFollowing }
        viewModelScope.launch {
            followRepository.setFollowing(userId, follow)
                .onSuccess { markFollowing(setOf(userId), it) }
                .onFailure { e -> _state.update { it.copy(error = e.userMessage("Unable to update follow.")) } }
        }
    }

    private fun markFollowing(userIds: Set<Long>, following: Boolean) = _state.update { state ->
        val hide = !following && !state.showingBookmarks && state.feedTab == CommunityFeedTab.FOLLOWING
        state.copy(posts = state.posts.mapNotNull { post ->
            when {
                post.author.id?.let(userIds::contains) != true -> post
                hide -> null
                else -> post.copy(author = post.author.copy(isFollowing = following))
            }
        })
    }

    fun refresh() {
        if (_state.value.isLoading) return
        if (_state.value.showingBookmarks) {
            loadBookmarks()
            return
        }

        feedJob?.cancel()
        feedJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    isLoadingMore = false,
                    error = null,
                    info = null
                )
            }

            try {
                val feed = repository.loadFeed(following = followingFeed)

                _state.update {
                    it.copy(
                        posts = feed.posts,
                        nextBeforeId = feed.nextBeforeId,
                        showingBookmarks = false,
                        isLoading = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.userMessage("Unable to load community feed.")
                    )
                }
            }
        }
    }

    fun loadShareableGames() {
        if (_state.value.isLoadingGames) return

        viewModelScope.launch {
            _state.update {
                it.copy(isLoadingGames = true)
            }

            try {
                val games = remoteGameRepository
                    .getRawGameHistory()
                    .filter { game ->
                        game.status.equals(
                            "finished",
                            ignoreCase = true
                        ) && !game.result.isNullOrBlank()
                    }

                _state.update {
                    it.copy(
                        shareableGames = games,
                        isLoadingGames = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoadingGames = false,
                        error = e.userMessage("Unable to load completed games.")
                    )
                }
            }
        }
    }

    fun createPost(body: String) {
        if (_state.value.isPublishing) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isPublishing = true,
                    error = null,
                    info = null
                )
            }

            try {
                val post = repository.createPost(body)

                _state.update {
                    it.copy(
                        posts = listOf(post) +
                                it.posts.filterNot { old ->
                                    old.id == post.id
                                },
                        isPublishing = false,
                        info = "Post published successfully."
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isPublishing = false,
                        error = e.userMessage("Unable to publish post.")
                    )
                }
            }
        }
    }

    fun shareGame(gameId: String, caption: String) {
        if (_state.value.isPublishing) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isPublishing = true,
                    error = null,
                    info = null
                )
            }

            try {
                val post = repository.shareGame(
                    gameId = gameId,
                    caption = caption
                )

                _state.update {
                    it.copy(
                        posts = listOf(post) +
                                it.posts.filterNot { old ->
                                    old.id == post.id
                                },
                        isPublishing = false,
                        info = "Game shared with the community."
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isPublishing = false,
                        error = e.userMessage("Unable to share game.")
                    )
                }
            }
        }
    }

    fun openComments(postId: Long) {
        _state.update {
            it.copy(commentsPostId = postId)
        }
    }

    fun closeComments() {
        _state.update {
            it.copy(commentsPostId = null)
        }
    }

    fun loadMore() {
        val current = _state.value
        val cursor = current.nextBeforeId

        if (current.isLoading ||
            current.isLoadingMore ||
            cursor == null
        ) return

        feedJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoadingMore = true,
                    error = null
                )
            }

            try {
                val feed = if (current.showingBookmarks) repository.loadBookmarks(cursor) else repository.loadFeed(cursor, followingFeed)

                _state.update { old ->
                    old.copy(
                        posts = (old.posts + feed.posts)
                            .distinctBy { post -> post.id },
                        nextBeforeId = feed.nextBeforeId,
                        isLoadingMore = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoadingMore = false,
                        error = e.userMessage("Unable to load more posts.")
                    )
                }
            }
        }
    }

    fun deletePost(postId: Long) {
        viewModelScope.launch {
            try {
                repository.deletePost(postId)

                _state.update {
                    it.copy(
                        posts = it.posts.filterNot { post ->
                            post.id == postId
                        },
                        info = "Post deleted."
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = e.userMessage("Unable to delete post.")
                    )
                }
            }
        }
    }

    fun react(postId: Long, reaction: String) {
        viewModelScope.launch {
            try {
                val currentPost = _state.value.posts
                    .firstOrNull { it.id == postId }
                    ?: return@launch

                val result =
                    if (currentPost.myReaction == reaction) {
                        repository.removeReaction(postId)
                    } else {
                        repository.setReaction(
                            postId,
                            reaction
                        )
                    }

                _state.update { old ->
                    old.copy(
                        posts = old.posts.map { post ->
                            if (post.id == postId) {
                                post.copy(
                                    reactions = result.reactions,
                                    myReaction = result.myReaction
                                )
                            } else {
                                post
                            }
                        },
                        error = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = e.userMessage("Unable to save reaction.")
                    )
                }
            }
        }
    }

    fun loadBookmarks() {
        if (_state.value.isLoadingBookmarks) return
        viewModelScope.launch {
            _state.update { it.copy(isLoadingBookmarks = true, error = null, showingBookmarks = true) }
            try {
                val feed = repository.loadBookmarks()
                _state.update {
                    it.copy(
                        posts = feed.posts,
                        nextBeforeId = feed.nextBeforeId,
                        showingBookmarks = true,
                        isLoadingBookmarks = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoadingBookmarks = false, error = e.userMessage("Unable to load saved posts.")) }
            }
        }
    }

    fun showFeed() {
        _state.update { it.copy(showingBookmarks = false, posts = emptyList(), nextBeforeId = null) }
        refresh()
    }

    fun toggleBookmark(postId: Long) {
        viewModelScope.launch {
            val post = _state.value.posts.firstOrNull { it.id == postId } ?: return@launch
            try {
                val bookmarked = repository.setBookmark(postId, !post.isBookmarked)
                _state.update { old ->
                    val updated = old.posts.map { item ->
                        if (item.id == postId) item.copy(isBookmarked = bookmarked) else item
                    }
                    old.copy(
                        posts = if (old.showingBookmarks && !bookmarked) updated.filterNot { it.id == postId } else updated,
                        error = null,
                        info = if (bookmarked) "Post saved to bookmarks." else "Bookmark removed."
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(error = e.userMessage("Unable to update bookmark.")) }
            }
        }
    }

    fun clearError(message: String? = null) {
        _state.update {
            if (message == null || it.error == message) it.copy(error = null) else it
        }
    }

    fun clearInfo(message: String? = null) {
        _state.update {
            if (message == null || it.info == message) it.copy(info = null) else it
        }
    }

    fun clearMessages() {
        _state.update {
            it.copy(error = null, info = null)
        }
    }

    suspend fun loadPost(postId: Long): CommunityPostDto {
        val post = repository.loadPost(postId)
        _state.update { old ->
            val posts = if (old.posts.any { it.id == post.id }) {
                old.posts.map { if (it.id == post.id) post else it }
            } else {
                listOf(post) + old.posts
            }
            old.copy(posts = posts)
        }
        return post
    }

    suspend fun loadComments(
        postId: Long,
        afterId: Long? = null
    ) = repository.loadComments(
        postId,
        afterId = afterId
    )

    suspend fun addComment(
        postId: Long,
        body: String
    ) = repository.addComment(postId, body)

    suspend fun deleteComment(commentId: Long) =
        repository.deleteComment(commentId)
}
