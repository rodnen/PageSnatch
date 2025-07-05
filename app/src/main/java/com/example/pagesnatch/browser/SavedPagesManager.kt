package com.example.pagesnatch.browser

import android.content.Context
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import android.graphics.Bitmap
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

    private fun loadFavicon(
        url: String,
        onReady: (Bitmap) -> Unit,
        onFailed: () -> Unit,
        onCleared: () -> Unit = {}
    ){
        val faviconUrl = "https://www.google.com/s2/favicons?sz=128&domain=$url"

        Glide.with(context)
            .asBitmap()
            .load(faviconUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    onReady(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    onCleared()
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    // Якщо не вдалося — додаємо без іконки
                    onFailed()
                }
            })
    }

    fun addPage(title: String, url: String) {
        loadFavicon(
            url = url,
            onReady = { bitmap ->
                savedPages.add(SavedPageItem.Page(title, url, bitmap))
                (recyclerView.adapter as? SavedPageAdapter)?.submitItems(getItems())
            },
            onFailed = {
                savedPages.add(SavedPageItem.Page(title, url, null))
                (recyclerView.adapter as? SavedPageAdapter)?.submitItems(getItems())
            },
            onCleared = {
                // Опціонально — наприклад, очистити або поставити заглушку
            }
        )
    }

    fun editPage(item: SavedPageItem.Page, title: String, url: String) {
        val index = savedPages.indexOf(item)
        if (index != -1) {

            loadFavicon(
                url = url,
                onReady = { bitmap ->
                    val updatedPage = SavedPageItem.Page(title, url ,bitmap)
                    savedPages[index] = updatedPage
                    (recyclerView.adapter as? SavedPageAdapter)?.submitItems(getItems(), index)
                },
                onFailed = {
                    val updatedPage = SavedPageItem.Page(title, url ,null)
                    savedPages[index] = updatedPage
                    (recyclerView.adapter as? SavedPageAdapter)?.submitItems(getItems(), index)
                },
                onCleared = {
                    // Опціонально — наприклад, очистити або поставити заглушку
                }
            )
        }
    }

    fun removePage(index: Int){
        savedPages.removeAt(index)
        recyclerView.adapter?.notifyItemRemoved(index)
    }

    fun removePage(item: SavedPageItem.Page){
        val index = savedPages.indexOf(item)
        savedPages.removeAt(index)
        recyclerView.adapter?.notifyItemRemoved(index)
    }

}