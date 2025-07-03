package com.example.pagesnatch.browser

import android.content.Context
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import android.graphics.Bitmap
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.pagesnatch.ui.SavedPageAdapter

class SavedPagesManager (
    private val context: Context,
    private val recyclerView: RecyclerView
){
    private val savedPages = mutableListOf<SavedPageItem.Page>()

    fun getItems(): List<SavedPageItem> {
        return savedPages + SavedPageItem.AddButton
    }

    fun addPage(title: String, url: String) {
        val faviconUrl = "https://www.google.com/s2/favicons?sz=128&domain=$url"

        Glide.with(context)
            .asBitmap()
            .load(faviconUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    savedPages.add(SavedPageItem.Page(title, url, resource))
                    (recyclerView.adapter as? SavedPageAdapter)?.submitItems(getItems())
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Опціонально: очищення ресурсів
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    // Якщо не вдалося — додаємо без іконки
                    savedPages.add(SavedPageItem.Page(title, url, null))
                    (recyclerView.adapter as? SavedPageAdapter)?.submitItems(getItems())
                }
            })
    }

    fun removePage(index: Int){
        savedPages.removeAt(index)
        recyclerView.adapter?.notifyItemRemoved(index)
    }

    fun removePage(page: SavedPageItem.Page){
        val index = savedPages.indexOf(page)
        savedPages.removeAt(index)
        recyclerView.adapter?.notifyItemRemoved(index)
    }

}