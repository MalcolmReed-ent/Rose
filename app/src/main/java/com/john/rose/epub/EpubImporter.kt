import android.util.Log
import com.john.rose.data.local.chapter.Chapter
import com.john.rose.data.local.chapter.ChapterBody
import com.john.rose.data.local.library.LibraryItem
import com.john.rose.epub.EpubBook
import com.john.rose.repository.AppFileResolver
import com.john.rose.repository.AppRepository
import com.john.rose.utils.fileImporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

suspend fun epubImporter(
    storageFolderName: String,
    appRepository: AppRepository,
    appFileResolver: AppFileResolver,
    epub: EpubBook,
    addToLibrary: Boolean
): Unit = withContext(Dispatchers.IO) {
    val localBookUrl = appFileResolver.getLocalBookPath(storageFolderName)

    Log.d("InsideImporter", "Selected FileName: $storageFolderName")

    // First clean any previous entries from the book
    appRepository.bookChapters.chapters(localBookUrl)
        .map { it.url }
        .let { appRepository.chapterBody.removeRows(it) }
    appRepository.bookChapters.removeAllFromBook(localBookUrl)
    appRepository.libraryBooks.remove(localBookUrl)

    if (epub.coverImage != null) {
        fileImporter(
            targetFile = appFileResolver.getStorageBookCoverImageFile(storageFolderName),
            imageData = epub.coverImage.image
        )
    }

    // Insert new book data
    LibraryItem(
        title = epub.title,
        description = epub.description,
        url = localBookUrl,
        coverImageUrl = appFileResolver.getLocalBookCoverPath(),
        inLibrary = addToLibrary,
        author = epub.author,
    ).let { appRepository.libraryBooks.insert(it) }

    epub.chapters.mapIndexed { i, chapter ->
        Chapter(
            title = chapter.title,
            url = appFileResolver.getLocalBookChapterPath(storageFolderName, chapter.absPath),
            bookUrl = localBookUrl,
            position = i
        )
    }.let { appRepository.bookChapters.insert(it) }

    epub.chapters.map { chapter ->
        ChapterBody(
            url = appFileResolver.getLocalBookChapterPath(storageFolderName, chapter.absPath),
            body = chapter.body
        )
    }.let { appRepository.chapterBody.insertReplace(it) }

    epub.images.map {
        async {
            fileImporter(
                targetFile = appFileResolver.getStorageBookImageFile(storageFolderName, it.absPath),
                imageData = it.image
            )
        }
    }.awaitAll()
}
