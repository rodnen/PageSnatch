package com.example.pagesnatch

import android.os.Bundle
import android.content.Context
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pagesnatch.browser.Browser
import com.example.pagesnatch.browser.SavedPagesManager
import com.example.pagesnatch.ui.SavedPageAdapter
import org.mozilla.geckoview.*
import android.util.Log
import android.view.View
import android.widget.FrameLayout

class MainActivity : AppCompatActivity() {

    private val SPAN_COUNT = 4

    private lateinit var browser: Browser
    private lateinit var pagesManager: SavedPagesManager

    // Views
    private lateinit var geckoView: GeckoView
    private lateinit var btnHome: ImageButton
    private lateinit var btnMenu: ImageButton
    private lateinit var urlInput: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var homePageContainer: FrameLayout
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupWindow()

        initViews()
        initBrowser()
        initPagesManager()
        setupListeners()

    }

    private fun setupWindow() {
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initViews() {
        geckoView = findViewById(R.id.geckoview)
        btnHome = findViewById(R.id.btnHome)
        btnMenu = findViewById(R.id.btnMenu)
        urlInput = findViewById(R.id.urlInput)
        progressBar = findViewById(R.id.progressBar)
        recyclerView = findViewById(R.id.recyclerView)
        homePageContainer = findViewById(R.id.homePageContainer)
    }

    private fun initBrowser() {
        browser = Browser(
            context = this,
            geckoView = geckoView,
            homePage = homePageContainer,
            progressBar = progressBar,
            urlInput = urlInput
        )

        browser.initialize()
    }

    private fun initPagesManager() {
        pagesManager = SavedPagesManager(
            context = this,
            recyclerView = recyclerView
        )
    }

    private fun setupRecyclerViewListener(){
        val adapter = SavedPageAdapter(
            onItemClick = { browser.loadUrl(it.url)},
            onAddClick = { /*showAddPageDialog(this) { title, url ->
                pagesManager.addPage(title, url)*/
            /*}*/
            testAddPage()}
        )

        recyclerView.layoutManager = GridLayoutManager(this, SPAN_COUNT)
        recyclerView.adapter = adapter
        adapter.submitItems(pagesManager.getItems())
    }

    private fun setupListeners() {
        setupSearchListener()
        setupHomeBtnListener()
        setupRecyclerViewListener()
        setupContentClickListener()
    }

    private fun setupSearchListener() {
        urlInput.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(urlInput.windowToken, 0)

                val query = urlInput.text.toString()
                browser.loadUrl(query)

                true
            } else {
                false
            }
        }
    }

    private fun setupHomeBtnListener() {
        btnHome.setOnClickListener {
            browser.home()
        }
    }

    private fun setupContentClickListener(){
        recyclerView.setOnClickListener{
            urlInput.clearFocus()
        }

        geckoView.setOnClickListener{
            urlInput.clearFocus()
        }
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        if (browser.canGoBack()) {
            browser.goBack()
        } else {
            super.onBackPressed() // закрити активність
        }
    }

    private fun showAddPageDialog(context: Context, onAdd: (title: String, url: String) -> Unit) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_page, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        val inputTitle = dialogView.findViewById<EditText>(R.id.inputTitle)
        val inputUrl = dialogView.findViewById<EditText>(R.id.inputUrl)
        val btnAdd = dialogView.findViewById<Button>(R.id.btnAdd)

        btnAdd.setOnClickListener {
            val title = inputTitle.text.toString().trim()
            val url = inputUrl.text.toString().trim()
            if (title.isNotEmpty() && url.isNotEmpty()) {
                onAdd(title, url)
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Заповніть усі поля", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun testAddPage(){
        pagesManager.addPage("TEST", "pixiv.net")
    }

}