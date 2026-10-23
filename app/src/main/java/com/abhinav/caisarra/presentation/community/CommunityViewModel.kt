
package com.abhinav.caisarra.presentation.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.remote.dto.CommunityPostDto
import com.abhinav.caisarra.data.remote.dto.GameHistoryDto
import com.abhinav.caisarra.data.repository.CommunityRepository
import com.abhinav.caisarra.data.repository.RemoteGameRepository
import kotlinx.coroutines.CancellationException
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
    val shareableGames: List<GameHistoryDto> = emptyList(),
    val isLoadingGames: Boolean = false
)

class CommunityViewModel(
    private val repository: CommunityRepository,
    private val remoteGameRepository: RemoteGameRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CommunityUiState())
    val state = _state.asStateFlow()

    init {
        refresh()
        loadShareableGames()
    }

    fun refresh() {
        if (_state.value.isLoading) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    info = null
                )
            }

            try {
                val feed = repository.loadFeed()

                _state.update {
                    it.copy(
                        posts = feed.posts,
                        nextBeforeId = feed.nextBeforeId,
                        isLoading = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                            ?: "Unable to load community feed."
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
                        error = e.message
                            ?: "Unable to load completed games."
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
                        error = e.message
                            ?: "Unable to publish post."
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
                        error = e.message
                            ?: "Unable to share game."
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

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoadingMore = true,
                    error = null
                )
            }

            try {
                val feed = repository.loadFeed(cursor)

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
                        error = e.message
                            ?: "Unable to load more posts."
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
                        error = e.message
                            ?: "Unable to delete post."
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
                        error = e.message
                            ?: "Unable to save reaction."
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _state.update {
            it.copy(error = null, info = null)
        }
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
