package com.abhinav.caisarra.features.follow.domain

import androidx.annotation.StringRes
import com.abhinav.caisarra.R

enum class FollowListTab(@StringRes val title: Int, @StringRes val empty: Int, val path: String) {
    FOLLOWERS(R.string.followers, R.string.no_followers_yet, "followers"),
    FOLLOWING(R.string.following, R.string.not_following_anyone_yet, "following")
}
