package com.abhinav.caisarra.core.ui.glance

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.glance.action.Action
import androidx.glance.appwidget.action.actionStartActivity
import com.abhinav.caisarra.app.MainActivity

fun widgetDeepLink(context: Context, path: String): Action =
    actionStartActivity(Intent(Intent.ACTION_VIEW, Uri.parse("caisarra://$path"), context, MainActivity::class.java))
