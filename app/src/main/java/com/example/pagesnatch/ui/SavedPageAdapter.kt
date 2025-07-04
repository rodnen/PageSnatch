package com.example.pagesnatch.ui

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pagesnatch.R
import com.example.pagesnatch.browser.SavedPageItem

class SavedPageAdapter(
    private val onItemClick: (SavedPageItem.Page) -> Unit,
    private val onAddClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<SavedPageItem> = emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun submitItems(newItems: List<SavedPageItem>) {
        items = newItems
        notifyItemInserted(items.lastIndex)
    }

    companion object {
        private const val TYPE_PAGE = 0
        private const val TYPE_ADD = 1
    }

    inner class PageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.pageIcon)
        val title: TextView = view.findViewById(R.id.pageTitle)

        fun bind(page: SavedPageItem.Page) {
            title.text = page.title
            icon.setImageBitmap(page.favicon)
            itemView.setOnClickListener { onItemClick(page) }
        }
    }

    inner class AddViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            view.setOnClickListener { onAddClick() }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is SavedPageItem.Page -> TYPE_PAGE
            is SavedPageItem.AddButton -> TYPE_ADD
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_PAGE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_saved_page_grid, parent, false)
                PageViewHolder(view)
            }
            TYPE_ADD -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_add_button, parent, false)
                AddViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is SavedPageItem.Page -> (holder as PageViewHolder).bind(item)
            is SavedPageItem.AddButton -> {} // no binding needed
        }
    }

    override fun getItemCount(): Int = items.size
}