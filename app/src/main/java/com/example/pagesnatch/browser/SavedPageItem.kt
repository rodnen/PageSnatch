package com.example.pagesnatch.browser

import android.graphics.Bitmap

sealed class SavedPageItem {
    data class Page(val title: String, val url: String, val favicon: Bitmap?) : SavedPageItem()
    data object AddButton : SavedPageItem()
}