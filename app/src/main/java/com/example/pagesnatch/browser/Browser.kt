package com.example.pagesnatch.browser

import android.content.Context
import android.net.Uri
import android.util.Log
import android.util.Patterns
import android.util.Base64
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ProgressBar
import org.mozilla.geckoview.AllowOrDeny
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSession.*
import org.mozilla.geckoview.GeckoSessionSettings
import org.mozilla.geckoview.GeckoView
import java.util.concurrent.CopyOnWriteArrayList


class Browser (
    context: Context,
    private val geckoView: GeckoView,
    private val homePage: FrameLayout,
    private val progressBar : ProgressBar,
    private val urlInput : EditText)
{

    private val appContext = context.applicationContext
    private lateinit var runtime: GeckoRuntime

    data class BrowserTab(
        val session: GeckoSession,
        val history: MutableList<String> = mutableListOf(),
        var historyIndex: Int = -1,
        var isHomePage: Boolean = true
    )

    private val tabs = CopyOnWriteArrayList<BrowserTab>()
    private var currentTabIndex = 0

    private val currentTab: BrowserTab?
        get() = tabs.getOrNull(currentTabIndex)

    fun initialize() {
        runtime = GeckoRuntime.create(appContext)
        createNewTab()
    }

    fun createNewTab(initialUrl: String? = null): GeckoSession {
        val session = GeckoSession()
        val settings = session.settings

        settings.apply {
            suspendMediaWhenInactive = true // Призупиняти медіа, коли сесія неактивна
            userAgentMode = GeckoSessionSettings.USER_AGENT_MODE_MOBILE // Змінити User-Agent
            displayMode = GeckoSessionSettings.DISPLAY_MODE_FULLSCREEN // Режим відображення (наприклад, для PWA)
            viewportMode = GeckoSessionSettings.VIEWPORT_MODE_MOBILE // Адаптація під мобільний/десктопний viewport
        }

        setSessionDelegates(session)

        session.open(runtime)
        geckoView.setSession(session)

        val tab = BrowserTab(session)
        tabs.add(tab)
        currentTabIndex = tabs.lastIndex

        loadPage(initialUrl, session)

        return session
    }

    private fun loadPage(initialUrl : String?, session: GeckoSession) {
        // ⏱️ МОМЕНТАЛЬНО: одразу вставляємо HTML з assets
        if (initialUrl == null || initialUrl == "about:newtab" || initialUrl == "about:blank") {
            home()
        } else {
            loadUrl(initialUrl)
        }
    }

    private fun setSessionDelegates(session: GeckoSession) {
        // Делегати
        session.navigationDelegate = object : NavigationDelegate {
            override fun onLoadRequest(
                session: GeckoSession,
                request: NavigationDelegate.LoadRequest
            ): GeckoResult<AllowOrDeny>? {
                Log.d("REQUEST", request.uri)

                return GeckoResult.fromValue(AllowOrDeny.ALLOW)
            }

            override fun onLocationChange(
                session: GeckoSession,
                url: String?,
                permissions: MutableList<PermissionDelegate.ContentPermission>,
                isSameDocument: Boolean
            ) {
                url?.let {
                    if(it == "about:blank")
                        return

                    Log.d("NAVIGATION", "URL змінено на: $it")
                    urlInput.setText(it)
                }
            }
        }

        session.contentDelegate = object : ContentDelegate {}

        session.progressDelegate = object : ProgressDelegate {
            override fun onPageStart(session: GeckoSession, url: String) {
                Log.d("PROGRESS", "Сторінка почала завантаження: $url")
                // Показати індикатор завантаження
            }

            override fun onPageStop(session: GeckoSession, success: Boolean) {
                Log.d("PROGRESS", "Сторінка завершила завантаження. Успішно: $success")
                progressBar.progress = 0
            }

            override fun onProgressChange(session: GeckoSession, progress: Int) {
                Log.d("PROGRESS", "Прогрес: $progress%")
                // Оновити ProgressBar
                progressBar.progress = progress
                hideHomePage()
                showGeckoView()
            }
        }
    }

    private fun loadHtmlFromAssets(session: GeckoSession, fileName: String) {
        val inputStream = appContext.assets.open(fileName)
        val html = inputStream.bufferedReader().use { it.readText() }
        val encodedHtml = Base64.encodeToString(html.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        val dataUri = "data:text/html;base64,$encodedHtml"
        session.loadUri(dataUri)
    }

    fun loadUrl(query: String) {
        val url = if (Patterns.WEB_URL.matcher(query).matches()) {
            if (!query.startsWith("http")) "https://$query" else query
        } else {
            "https://www.google.com/search?q=" + Uri.encode(query)
        }

        currentTab?.let {
            // Trim forward history
            if (it.historyIndex < it.history.size - 1) {
                it.history.subList(it.historyIndex + 1, it.history.size).clear()
            }
            it.history.add(url)
            it.historyIndex++
            it.session.loadUri(url)
            it.isHomePage = false
        }
    }

    fun home() {
        currentTab?.let {
            it.isHomePage = true
        }

        urlInput.setText("")
        urlInput.clearFocus()


        showHomePage()
        hideGeckoView()
    }

    fun canGoForward(): Boolean {
        return currentTab?.let { it.historyIndex < it.history.size - 1 } ?: false
    }

    fun canGoBack(): Boolean {
        return (currentTab?.historyIndex ?: -1) > 0
    }

    fun goBack() {
        currentTab?.let {
            if (it.historyIndex > 0) {
                it.historyIndex--
                it.session.loadUri(it.history[it.historyIndex])
            }
        }
    }

    fun goForward() {
        currentTab?.let {
            if (it.historyIndex < it.history.size - 1) {
                it.historyIndex++
                it.session.loadUri(it.history[it.historyIndex])
            }
        }
    }

    fun closeCurrentTab() {
        if (tabs.size <= 1) return
        val removed = tabs.removeAt(currentTabIndex)
        removed.session.close()
        currentTabIndex = (currentTabIndex - 1).coerceAtLeast(0)
        geckoView.setSession(tabs[currentTabIndex].session)
    }

    fun switchToTab(index: Int): GeckoSession? {
        if (index in tabs.indices) {
            currentTabIndex = index
            geckoView.setSession(tabs[index].session)
            return tabs[index].session
        }
        return null
    }

    fun getAllTabs(): List<GeckoSession> = tabs.map { it.session }

    fun getCurrentTabIndex(): Int = currentTabIndex

    private fun showHomePage(){
        homePage.visibility = View.VISIBLE
    }

    private fun hideHomePage(){
        homePage.visibility = View.GONE
    }

    private fun showGeckoView(){
        geckoView.visibility = View.VISIBLE
    }

    private fun hideGeckoView(){
        geckoView.visibility = View.GONE
    }

}
