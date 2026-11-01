package com.abhinav.caisarra.app

import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.yalantis.ucrop.UCropActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat


class InsetAwareUCropActivity : UCropActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.rgb(8, 13, 19)
        window.navigationBarColor = Color.rgb(8, 13, 19)

        WindowCompat.getInsetsController(
            window,
            window.decorView
        ).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        val content = findViewById<View>(android.R.id.content)

        val initialLeft = content.paddingLeft
        val initialTop = content.paddingTop
        val initialRight = content.paddingRight
        val initialBottom = content.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(content) { view, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            val displayCutout = insets.getInsets(
                WindowInsetsCompat.Type.displayCutout()
            )

            view.setPadding(
                initialLeft + maxOf(systemBars.left, displayCutout.left),
                initialTop + maxOf(systemBars.top, displayCutout.top),
                initialRight + maxOf(systemBars.right, displayCutout.right),
                initialBottom + maxOf(systemBars.bottom, displayCutout.bottom)
            )

            WindowInsetsCompat.CONSUMED
        }

        ViewCompat.requestApplyInsets(content)
    }
}
