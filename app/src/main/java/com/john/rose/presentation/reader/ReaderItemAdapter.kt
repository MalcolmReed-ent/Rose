package com.john.rose.presentation.reader

import android.content.Context
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnNextLayout
import androidx.core.view.updateLayoutParams
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.john.rose.R
import com.john.rose.databinding.ActivityReaderListItemBodyBinding
import com.john.rose.databinding.ActivityReaderListItemDividerBinding
import com.john.rose.databinding.ActivityReaderListItemErrorBinding
import com.john.rose.databinding.ActivityReaderListItemImageBinding
import com.john.rose.databinding.ActivityReaderListItemPaddingBinding
import com.john.rose.databinding.ActivityReaderListItemProgressBarBinding
import com.john.rose.databinding.ActivityReaderListItemSpecialTitleBinding
import com.john.rose.databinding.ActivityReaderListItemTitleBinding
import com.john.rose.repository.AppFileResolver
import com.john.rose.utils.inflater

class ReaderItemAdapter(
    private val ctx: Context,
    list: List<ReaderItem>,
    private val bookUrl: String,
    private val currentFontSize: () -> Float,
    private val currentTypeface: () -> Typeface,
    private val currentTypefaceBold: () -> Typeface,
    private val onChapterStartVisible: (chapterUrl: String) -> Unit,
    private val onChapterEndVisible: (chapterUrl: String) -> Unit,
    private val onReloadReader: () -> Unit,
    private val onClick: () -> Unit,
) : ArrayAdapter<ReaderItem>(ctx, 0, list) {
    val appFileResolver = AppFileResolver(ctx)
    override fun getCount() = super.getCount() + 2
    override fun getItem(position: Int): ReaderItem = when (position) {
        0 -> topPadding
        count - 1 -> bottomPadding
        else -> super.getItem(position - 1)!!
    }

    fun getFirstVisibleItemIndexGivenPosition(firstVisiblePosition: Int): Int =
        when (firstVisiblePosition) {
            in 1 until (count - 1) -> firstVisiblePosition - 1
            0 -> 0
            count - 1 -> count - 1
            else -> -1
        }

    fun fromPositionToIndex(position: Int): Int = when (position) {
        in 1 until (count - 1) -> position - 1
        else -> -1
    }

    fun fromIndexToPosition(index: Int): Int = when (index) {
        in 0 until super.getCount() -> index + 1
        else -> -1
    }

    private val topPadding = ReaderItem.Padding(chapterIndex = Int.MIN_VALUE)
    private val bottomPadding = ReaderItem.Padding(chapterIndex = Int.MAX_VALUE)

    override fun getViewTypeCount(): Int = 9
    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is ReaderItem.Body -> 0
        is ReaderItem.Image -> 1
        is ReaderItem.BookEnd -> 2
        is ReaderItem.BookStart -> 3
        is ReaderItem.Divider -> 4
        is ReaderItem.Error -> 5
        is ReaderItem.Padding -> 6
        is ReaderItem.Progressbar -> 7
        is ReaderItem.Title -> 8
    }

    private fun viewBody(item: ReaderItem.Body, convertView: View?, parent: ViewGroup): View {
        val bind = when (convertView) {
            null -> ActivityReaderListItemBodyBinding.inflate(parent.inflater, parent, false)
                .also { it.root.tag = it }
            else -> ActivityReaderListItemBodyBinding.bind(convertView)
        }

        bind.body.updateTextSelectability()
        val paragraph = item.textToDisplay + "\n"
        bind.body.text = paragraph
        bind.body.textSize = currentFontSize()
        bind.body.typeface = currentTypeface()

        when (item.location) {
            ReaderItem.Location.FIRST -> onChapterStartVisible(item.chapterUrl)
            ReaderItem.Location.LAST -> onChapterEndVisible(item.chapterUrl)
            else -> run {}
        }
        return bind.root
    }

    private fun viewImage(
        item: ReaderItem.Image,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val bind = when (convertView) {
            null -> ActivityReaderListItemImageBinding.inflate(parent.inflater, parent, false)
                .also { it.root.tag = it }
            else -> ActivityReaderListItemImageBinding.bind(convertView)
        }

        bind.image.updateLayoutParams<ConstraintLayout.LayoutParams> {
            dimensionRatio = "1:${item.image.yrel}"
        }

        val imageModel = appFileResolver.resolvedBookImagePath(
            bookUrl = bookUrl,
            imagePath = item.image.path
        )

        bind.imageContainer.doOnNextLayout {
            Glide.with(ctx)
                .load(imageModel)
                .fitCenter()
                .error(R.drawable.cover_error)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(bind.image)
        }

        when (item.location) {
            ReaderItem.Location.FIRST -> onChapterStartVisible(item.chapterUrl)
            ReaderItem.Location.LAST -> onChapterEndVisible(item.chapterUrl)
            else -> run {}
        }

        return bind.root
    }

    private fun viewBookEnd(
        item: ReaderItem.BookEnd,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val bind = when (convertView) {
            null -> ActivityReaderListItemSpecialTitleBinding.inflate(
                parent.inflater,
                parent,
                false
            ).also { it.root.tag = it }
            else -> ActivityReaderListItemSpecialTitleBinding.bind(convertView)
        }

        bind.specialTitle.updateTextSelectability()
        bind.specialTitle.text = ctx.getString(R.string.no_next_chapter)
        bind.specialTitle.typeface = currentTypefaceBold()
        return bind.root
    }

    private fun viewBookStart(
        item: ReaderItem.BookStart,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val bind = when (convertView) {
            null -> ActivityReaderListItemSpecialTitleBinding.inflate(
                parent.inflater,
                parent,
                false
            ).also { it.root.tag = it }
            else -> ActivityReaderListItemSpecialTitleBinding.bind(convertView)
        }

        bind.specialTitle.updateTextSelectability()
        bind.specialTitle.text = ctx.getString(R.string.reader_first_chapter)
        bind.specialTitle.typeface = currentTypefaceBold()
        return bind.root
    }

    private fun viewProgressbar(
        item: ReaderItem.Progressbar,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val bind = when (convertView) {
            null -> ActivityReaderListItemProgressBarBinding.inflate(parent.inflater, parent, false)
                .also { it.root.tag = it }
            else -> ActivityReaderListItemProgressBarBinding.bind(convertView)
        }
        return bind.root
    }

    private fun viewDivider(item: ReaderItem.Divider, convertView: View?, parent: ViewGroup): View {
        val bind = when (convertView) {
            null -> ActivityReaderListItemDividerBinding.inflate(parent.inflater, parent, false)
                .also { it.root.tag = it }
            else -> ActivityReaderListItemDividerBinding.bind(convertView)
        }
        return bind.root
    }

    private fun viewError(item: ReaderItem.Error, convertView: View?, parent: ViewGroup): View {
        val bind = when (convertView) {
            null -> ActivityReaderListItemErrorBinding.inflate(parent.inflater, parent, false)
                .also { it.root.tag = it }
            else -> ActivityReaderListItemErrorBinding.bind(convertView)
        }

        bind.error.updateTextSelectability()
        bind.reloadButton.setOnClickListener { onReloadReader() }
        bind.error.text = item.text
        return bind.root
    }

    private fun viewPadding(item: ReaderItem.Padding, convertView: View?, parent: ViewGroup): View {
        val bind = when (convertView) {
            null -> ActivityReaderListItemPaddingBinding.inflate(parent.inflater, parent, false)
                .also { it.root.tag = it }
            else -> ActivityReaderListItemPaddingBinding.bind(convertView)
        }
        return bind.root
    }

    private fun viewTitle(item: ReaderItem.Title, convertView: View?, parent: ViewGroup): View {
        val bind = when (convertView) {
            null -> ActivityReaderListItemTitleBinding.inflate(parent.inflater, parent, false)
                .also { it.root.tag = it }
            else -> ActivityReaderListItemTitleBinding.bind(convertView)
        }

        bind.title.updateTextSelectability()
        bind.title.text = item.textToDisplay
        bind.title.typeface = currentTypefaceBold()
        return bind.root
    }

    private fun TextView.updateTextSelectability() {
        setTextIsSelectable(true)
        setTextSelectionAwareClick { onClick() }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
        when (val item = getItem(position)) {
            is ReaderItem.Body -> viewBody(item, convertView, parent)
            is ReaderItem.Image -> viewImage(item, convertView, parent)
            is ReaderItem.BookEnd -> viewBookEnd(item, convertView, parent)
            is ReaderItem.BookStart -> viewBookStart(item, convertView, parent)
            is ReaderItem.Divider -> viewDivider(item, convertView, parent)
            is ReaderItem.Error -> viewError(item, convertView, parent)
            is ReaderItem.Padding -> viewPadding(item, convertView, parent)
            is ReaderItem.Progressbar -> viewProgressbar(item, convertView, parent)
            is ReaderItem.Title -> viewTitle(item, convertView, parent)
        }
}

private fun View.setTextSelectionAwareClick(action: () -> Unit) {
    setOnClickListener { action() }
    setOnTouchListener { _, event ->
        if (event.action == MotionEvent.ACTION_UP && !this.isFocused) {
            performClick()
        }
        false
    }
}
