package com.abhinav.caisarra.presentation.community

import androidx.annotation.StringRes
import com.abhinav.caisarra.R

enum class CommunityFeedTab(@StringRes val title: Int) {
    FOLLOWING(R.string.following),
    ALL(R.string.all_posts)
}
