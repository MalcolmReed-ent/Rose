package com.john.rose.presentation.onboarding

import androidx.annotation.DrawableRes
import com.john.rose.R


data class Page(
    val title: String,
    val description: String,
    @DrawableRes val image: Int?
)

val pages = listOf(
    Page(
        title = "Welcome to Rose",
        description = "A simple EPUB reader. No ads, no trackers—just you and your books.",
        image = null
    ),
    Page(
        title = "Disclaimer",
        description = "Where your books come from is your business. Enjoy, but don’t blame us for lost hours!",
        image = R.drawable.disclaimer
    ),
    Page(
        title = "Get Started",
        description = "Open a book and lose yourself. The world can wait—your story begins now.",
        image = null
    )
)
