package com.john.rose.presentation.reader

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.AbsListView
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.doOnNextLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.john.rose.BaseActivity
import com.john.rose.R
import com.john.rose.databinding.ActivityReaderBinding
import com.john.rose.presentation.reader.components.FontsLoader
import com.john.rose.presentation.theme.RoseTheme
import com.john.rose.presentation.utils.ExtraBoolean
import com.john.rose.presentation.utils.ExtraString
import com.john.rose.presentation.utils.dpToPx
import com.john.rose.presentation.utils.fadeIn
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ReaderActivity : BaseActivity() {
    class IntentData : Intent, ReaderStateBundle {
        override var bookUrl by ExtraString()
        override var chapterUrl by ExtraString()

        constructor(intent: Intent) : super(intent)
        constructor(
            ctx: Context,
            bookUrl: String,
            chapterUrl: String,
        ) : super(
            ctx,
            ReaderActivity::class.java
        ) {
            this.bookUrl = bookUrl
            this.chapterUrl = chapterUrl
        }
    }

    private var listIsScrolling = false
    private val fadeInTextLiveData = MutableLiveData<Boolean>(false)
    private val viewModel by viewModels<ReaderViewModel>()
    private val viewBind by lazy { ActivityReaderBinding.inflate(layoutInflater) }
    private val fontsLoader = FontsLoader()

    private val viewAdapter = object {
        val listView by lazy {
            ReaderItemAdapter(
                this@ReaderActivity,
                viewModel.items,
                viewModel.bookUrl,
                currentFontSize = { viewModel.readerPreferences.value.fontSize },
                currentTypeface = { fontsLoader.getTypeFaceNORMAL(viewModel.readerPreferences.value.fontFamily) },
                currentTypefaceBold = { fontsLoader.getTypeFaceBOLD(viewModel.readerPreferences.value.fontFamily) },
                onChapterStartVisible = viewModel::markChapterStartAsSeen,
                onChapterEndVisible = viewModel::markChapterEndAsSeen,
                onReloadReader = viewModel::reloadReader,
                onClick = {
                    viewModel.state.showReaderInfo.value = !viewModel.state.showReaderInfo.value
                },
            )
        }
    }

    override fun onBackPressed() {
        viewModel.onCloseManually()
        super.onBackPressed()
    }

    override fun onDestroy() {
        viewModel.onViewDestroyed()
        super.onDestroy()
    }

    private fun indexOfReaderItem(
        list: List<ReaderItem>,
        chapterIndex: Int,
        chapterItemPosition: Int
    ): Int {
        return list.indexOfFirst { item ->
            item is ReaderItem.Position &&
            item.chapterIndex == chapterIndex &&
            item.chapterItemPosition == chapterItemPosition
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBind.listView.adapter = viewAdapter.listView

        fadeInTextLiveData.distinctUntilChanged().observe(this) {
            if (it) {
                viewBind.listView.fadeIn(durationMillis = 150)
            }
        }

        viewModel.forceUpdateListViewState = {
            withContext(Dispatchers.Main.immediate) {
                viewAdapter.listView.notifyDataSetChanged()
            }
        }

        viewModel.maintainStartPosition = {
            withContext(Dispatchers.Main.immediate) {
                it()
                val titleIndex = (0..viewAdapter.listView.count)
                    .indexOfFirst { viewAdapter.listView.getItem(it) is ReaderItem.Title }

                if (titleIndex != -1) {
                    viewBind.listView.setSelection(titleIndex)
                }
            }
        }

        viewModel.setInitialPosition = {
            withContext(Dispatchers.Main.immediate) {
                initialScrollToChapterItemPosition(
                    chapterIndex = it.chapterIndex,
                    chapterItemPosition = it.chapterItemPosition,
                    offset = it.chapterItemOffset
                )
            }
        }

        viewModel.maintainLastVisiblePosition = {
            withContext(Dispatchers.Main.immediate) {
                val oldSize = viewAdapter.listView.count
                val position = viewBind.listView.lastVisiblePosition
                val positionView = position - viewBind.listView.firstVisiblePosition
                val top = viewBind.listView.getChildAt(positionView).run { top - paddingTop }
                it()
                val displacement = viewAdapter.listView.count - oldSize
                viewBind.listView.setSelectionFromTop(position + displacement, top)
            }
        }

        setContent {
            RoseTheme {
                ReaderScreen(
                    state = viewModel.state,
                    onTextFontChanged = { viewModel.readerPreferences.value.fontFamily },
                    onTextSizeChanged = { viewModel.readerPreferences.value.fontSize },
                    onKeepScreenOn = true,
                    onPressBack = {
                        viewModel.onCloseManually()
                        finish()
                    },
                    readerContent = {
                        AndroidView(factory = { viewBind.root })
                    },
                )

                if (viewModel.state.showInvalidChapterDialog.value) {
                    AlertDialog(
                        onDismissRequest = {
                            viewModel.state.showInvalidChapterDialog.value = false
                        }
                    ) {
                        Text(stringResource(id = R.string.chapter_error))
                    }
                }
            }
        }

        setupListViewListeners()
        setupWindowBehavior()
        initializeView()
    }

    private fun setupListViewListeners() {
        viewBind.listView.setOnItemClickListener { _, _, _, _ ->
            viewModel.state.showReaderInfo.value = !viewModel.state.showReaderInfo.value
        }

        viewBind.listView.setOnScrollListener(
            object : AbsListView.OnScrollListener {
                override fun onScroll(
                    view: AbsListView?,
                    firstVisibleItem: Int,
                    visibleItemCount: Int,
                    totalItemCount: Int
                ) {
                    updateCurrentReadingPosSavingState(
                        firstVisibleItemIndex = viewAdapter.listView.fromPositionToIndex(
                            viewBind.listView.firstVisiblePosition
                        )
                    )
                    updateInfoView()
                    updateReadingState()
                }

                override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {
                    listIsScrolling = scrollState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                }
            })
    }

    private fun setupWindowBehavior() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.displayCutout())
        controller.hide(WindowInsetsCompat.Type.systemBars())

        snapshotFlow { viewModel.state.showReaderInfo.value }
            .asLiveData()
            .observe(this) { show ->
                if (show) controller.show(WindowInsetsCompat.Type.statusBars())
                else controller.hide(WindowInsetsCompat.Type.statusBars())
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }

    private fun initializeView() {
        viewAdapter.listView.notifyDataSetChanged()
        lifecycleScope.launch {
            delay(200)
            fadeInTextLiveData.postValue(true)
        }

        if (viewModel.introScrollToCurrentChapter) {
            viewModel.introScrollToCurrentChapter = false
            val chapterState = viewModel.readingCurrentChapter
            val chapterStats =
                viewModel.chaptersLoader.chaptersStats[chapterState.chapterUrl] ?: return
            initialScrollToChapterItemPosition(
                chapterIndex = chapterStats.orderedChaptersIndex,
                chapterItemPosition = chapterState.chapterItemPosition,
                offset = chapterState.offset
            )
        }
    }

    private fun updateReadingState() {
        val firstVisibleItem = viewBind.listView.firstVisiblePosition
        val lastVisibleItem = viewBind.listView.lastVisiblePosition
        val totalItemCount = viewAdapter.listView.count
        val visibleItemCount =
            if (totalItemCount == 0) 0 else (lastVisibleItem - firstVisibleItem + 1)

        val isTop = visibleItemCount != 0 && firstVisibleItem <= 1
        val isBottom =
            visibleItemCount != 0 && (firstVisibleItem + visibleItemCount) >= totalItemCount - 1

        when (viewModel.chaptersLoader.readerState) {
            ReaderState.IDLE -> {
                if (isBottom) {
                    viewModel.chaptersLoader.tryLoadNext()
                }
                if (isTop) {
                    viewModel.chaptersLoader.tryLoadPrevious()
                }
            }
            ReaderState.LOADING -> run {}
            ReaderState.INITIAL_LOAD -> run {}
        }
    }

    private fun initialScrollToChapterItemPosition(
        chapterIndex: Int,
        chapterItemPosition: Int,
        offset: Int
    ) {
        val index = indexOfReaderItem(
            list = viewModel.items,
            chapterIndex = chapterIndex,
            chapterItemPosition = chapterItemPosition
        )
        val position = viewAdapter.listView.fromIndexToPosition(index)
        if (index != -1) {
            viewBind.listView.setSelectionFromTop(position, offset)
        }
        fadeInTextLiveData.postValue(true)
        viewBind.listView.doOnNextLayout { updateReadingState() }
    }

    private fun updateInfoView() {
        val lastVisiblePosition = viewBind.listView.lastVisiblePosition
        val itemIndex = viewAdapter.listView.fromPositionToIndex(lastVisiblePosition)
        viewModel.updateInfoViewTo(itemIndex)
    }

    override fun onPause() {
        updateCurrentReadingPosSavingState(
            firstVisibleItemIndex = viewAdapter.listView.fromPositionToIndex(
                viewBind.listView.firstVisiblePosition
            )
        )
        super.onPause()
    }

    private fun updateCurrentReadingPosSavingState(firstVisibleItemIndex: Int) {
        val item = viewModel.items.getOrNull(firstVisibleItemIndex) ?: return
        if (item !is ReaderItem.Position) return

        val offset = viewBind.listView.run { getChildAt(0).top - paddingTop }
        viewModel.readingCurrentChapter = ChapterState(
            chapterUrl = item.chapterUrl,
            chapterItemPosition = item.chapterItemPosition,
            offset = offset
        )
    }
}
