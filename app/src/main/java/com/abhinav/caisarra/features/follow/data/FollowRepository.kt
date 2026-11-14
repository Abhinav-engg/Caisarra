package com.abhinav.caisarra.features.follow.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.local.settingsDataStore
import com.abhinav.caisarra.data.remote.RetrofitInstance
import com.abhinav.caisarra.data.repository.FriendsRepository
import com.abhinav.caisarra.data.repository.apiCall
import com.abhinav.caisarra.features.follow.domain.FollowListTab
import kotlinx.coroutines.flow.first

private val AUTO_FOLLOWED_FRIENDS_KEY = stringSetPreferencesKey("auto_followed_friends")

class FollowRepository(context: Context) {
    private val service = RetrofitInstance.createFollowService(TokenManager.get(context.applicationContext))
    private val friends = FriendsRepository(context)
    private val settings = context.applicationContext.settingsDataStore

    suspend fun setFollowing(userId: Long, follow: Boolean) =
        apiCall { if (follow) service.follow(userId) else service.unfollow(userId) }.map { it.isFollowing }

    suspend fun isFollowing(userId: Long) = apiCall { service.status(userId) }.map { it.isFollowing }

    suspend fun counts(userId: Long) = apiCall { service.counts(userId) }

    suspend fun users(userId: Long, tab: FollowListTab, beforeId: Long?) = apiCall { service.users(userId, tab.path, beforeId) }

    suspend fun followNewFriends(): Set<Long> {
        val done = settings.data.first()[AUTO_FOLLOWED_FRIENDS_KEY].orEmpty()
        val followed = friends.getFriends().getOrNull().orEmpty().map { it.userId }
            .filter { it.toString() !in done && setFollowing(it, true).isSuccess }
        if (followed.isNotEmpty()) settings.edit { it[AUTO_FOLLOWED_FRIENDS_KEY] = done + followed.map(Long::toString) }
        return followed.toSet()
    }
}
