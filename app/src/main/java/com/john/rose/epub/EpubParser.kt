package com.john.rose.epub

import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import java.io.File
import java.io.InputStream
import java.util.zip.ZipInputStream
import kotlin.io.path.invariantSeparatorsPathString

// Models
data class EpubChapter(
    val absPath: String,
    val title: String?,
    val body: String,
    val index: Int = 0
)

data class EpubBook(
    val fileName: String,
    val title: String,
    val author: String?,
    val description: String?,
    val coverImage: EpubImage?,
    val chapters: List<EpubChapter>,
    val images: List<EpubImage>,
    val toc: List<ToCEntry> = emptyList()
)

data class EpubImage(
    val absPath: String, 
    val image: ByteArray,
    val mimeType: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as EpubImage
        return absPath == other.absPath && image.contentEquals(other.image)
    }

    override fun hashCode(): Int {
        var result = absPath.hashCode()
        result = 31 * result + image.contentHashCode()
        return result
    }
}

data class ManifestItem(
    val id: String,
    val absPath: String,
    val mediaType: String,
    val properties: String,
    val spineIndex: Int = -1
)

data class ToCEntry(
    val chapterTitle: String,
    val chapterLink: String,
    val level: Int = 0
)

data class EpubFile(
    val absPath: String,
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as EpubFile
        return absPath == other.absPath && data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        var result = absPath.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}

private suspend fun getZipFiles(
    inputStream: InputStream
): Map<String, EpubFile> = withContext(Dispatchers.IO) {
    ZipInputStream(inputStream).use { zipInputStream ->
        zipInputStream
            .entries()
            .filterNot { it.isDirectory }
            .map {
                EpubFile(absPath = it.name, data = zipInputStream.readBytes())
            }
            .associateBy { it.absPath }
    }
}

@Throws(Exception::class)
suspend fun epubParser(
    inputStream: InputStream
): EpubBook = withContext(Dispatchers.Default) {
    val files = getZipFiles(inputStream)

    val container = files["META-INF/container.xml"]
        ?: throw Exception("META-INF/container.xml file missing")

    val containerDoc = Jsoup.parse(String(container.data))
    val opfFilePath = containerDoc.select("rootfile").firstOrNull()
        ?.attr("full-path")
        ?.decodedURL ?: throw Exception("Invalid container.xml file")
        
    val rootPath = opfFilePath.substringBefore('/', "")
    val opfFile = files[opfFilePath] ?: throw Exception(".opf file missing")

    val document = Jsoup.parse(String(opfFile.data))
    val metadata = document.select("metadata").firstOrNull()
        ?: throw Exception(".opf file metadata section missing")
    val manifest = document.select("manifest").firstOrNull()
        ?: throw Exception(".opf file manifest section missing")
    val spine = document.select("spine").firstOrNull()
        ?: throw Exception(".opf file spine section missing")
    val guide = document.select("guide").firstOrNull()

    val metadataTitle = metadata.select("dc\\:title").firstOrNull()?.text()
        ?: "Unknown Title"
    val metadataCreator = metadata.select("dc\\:creator").firstOrNull()?.text()

    // Updated description parsing | These Are The New Stuff 
    val metadataDesc = metadata.select("dc\\:description").firstOrNull()?.text()
        ?: metadata.select("description").firstOrNull()?.text()
        ?: metadata.select("meta[name=description]").firstOrNull()?.attr("content")
        ?: metadata.select("meta[property=dcterms:description]").firstOrNull()?.text()
        ?: metadata.select("meta[property=dc:description]").firstOrNull()?.text()

    val manifestItems = buildManifestItems(manifest, spine, rootPath)
    val tocEntries = parseTableOfContents(files, manifestItems, rootPath)
    val coverImage = extractCoverImage(files, metadata, manifest, guide, rootPath)
    val chapters = parseChapters(files, spine, manifestItems, tocEntries, rootPath)
    val images = extractImages(files, manifestItems)

    return@withContext EpubBook(
        fileName = metadataTitle.asFileName(),
        title = metadataTitle,
        author = metadataCreator,
        description = metadataDesc,
        coverImage = coverImage,
        chapters = chapters,
        images = images.toList(),
        toc = tocEntries
    )
}

private fun buildManifestItems(
    manifest: Element,
    spine: Element,
    rootPath: String
): Map<String, ManifestItem> {
    val spineItems = spine.select("itemref")
        .mapIndexed { index, node -> node.attr("idref") to index }
        .toMap()
        
    return manifest.select("item").map { item ->
        val id = item.attr("id")
        ManifestItem(
            id = id,
            absPath = item.attr("href").decodedURL.toAbsolutePath(rootPath),
            mediaType = item.attr("media-type"),
            properties = item.attr("properties"),
            spineIndex = spineItems[id] ?: -1
        )
    }.associateBy { it.id }
}

private fun String.toAbsolutePath(rootPath: String): String {
    val path = if (startsWith(rootPath)) this else "$rootPath/$this"
    return File(path).canonicalFile
        .toPath()
        .invariantSeparatorsPathString
        .removePrefix("/")
}

private fun String.ensureRootPath(rootPath: String): String {
    return if (startsWith(rootPath)) this else "$rootPath/$this"
}

private suspend fun parseTableOfContents(
    files: Map<String, EpubFile>,
    manifestItems: Map<String, ManifestItem>,
    rootPath: String
): List<ToCEntry> = withContext(Dispatchers.Default) {
    val ncxFilePath = manifestItems["ncx"]?.absPath
    val ncxFile = files[ncxFilePath] ?: return@withContext emptyList()

    val doc = Jsoup.parse(ncxFile.data.inputStream(), "UTF-8", "")
    val navMap = doc.selectFirst("navMap") ?: return@withContext emptyList()

    navMap.select("navPoint").map { navPoint ->
        val title = navPoint.selectFirst("navLabel")?.selectFirst("text")?.text() ?: ""
        var link = navPoint.selectFirst("content")?.attr("src") ?: ""
        if (!link.startsWith(rootPath)) {
            link = "$rootPath/$link"
        }
        val level = navPoint.parents().count { it.tagName() == "navPoint" }
        ToCEntry(title, link, level)
    }
}

private fun extractCoverImage(
    files: Map<String, EpubFile>,
    metadata: Element,
    manifest: Element,
    guide: Element?,
    rootPath: String
): EpubImage? {
    // Try metadata cover ID first
    val metadataCoverId = metadata.select("meta")
        .find { it.attr("name") == "cover" }
        ?.attr("content")

    if (metadataCoverId != null) {
        val coverItem = manifest.select("item")
            .find { it.attr("id") == metadataCoverId }
        
        if (coverItem != null) {
            val coverPath = coverItem.attr("href").decodedURL.toAbsolutePath(rootPath)
            files[coverPath]?.let { 
                return EpubImage(
                    absPath = it.absPath,
                    image = it.data,
                    mimeType = coverItem.attr("media-type")
                )
            }
        }
    }

    // Try guide reference
    guide?.select("reference")
        ?.find { it.attr("type") == "cover" }
        ?.let { ref ->
            val coverPath = ref.attr("href").decodedURL.toAbsolutePath(rootPath)
            val coverFile = files[coverPath]
            if (coverFile != null) {
                return parseCoverImageFromXhtml(coverFile, files, rootPath)
            }
        }

    // Try manifest cover item
    manifest.select("item")
        .find { it.attr("id") == "cover" }
        ?.let { coverItem ->
            val coverPath = coverItem.attr("href").decodedURL.toAbsolutePath(rootPath)
            files[coverPath]?.let {
                return EpubImage(
                    absPath = it.absPath,
                    image = it.data,
                    mimeType = coverItem.attr("media-type")
                )
            }
        }

    return null
}

private fun parseCoverImageFromXhtml(
    coverFile: EpubFile,
    files: Map<String, EpubFile>,
    rootPath: String
): EpubImage? {
    val doc = Jsoup.parse(coverFile.data.inputStream(), "UTF-8", "")
    val imgTag = doc.selectFirst("img, image")
    
    if (imgTag != null) {
        var imgSrc = imgTag.attr("src").takeIf { it.isNotBlank() }
            ?: imgTag.attr("xlink:href").takeIf { it.isNotBlank() }
            ?: return null

        imgSrc = imgSrc.toAbsolutePath(rootPath)
        val imgFile = files[imgSrc]
        
        if (imgFile != null) {
            return EpubImage(
                absPath = imgFile.absPath,
                image = imgFile.data
            )
        }
    }
    return null
}

private suspend fun parseChapters(
    files: Map<String, EpubFile>,
    spine: Element,
    manifestItems: Map<String, ManifestItem>,
    tocEntries: List<ToCEntry>,
    rootPath: String
): List<EpubChapter> = withContext(Dispatchers.Default) {
    val chapters = mutableListOf<EpubChapter>()
    var chapterIndex = 0

    spine.select("itemref").forEach { itemRef ->
        val itemId = itemRef.attr("idref")
        val spineItem = manifestItems[itemId]

        if (spineItem != null && isChapter(spineItem)) {
            val chapterUrl = spineItem.absPath.ensureRootPath(rootPath)
            val tocEntry = findTocEntryForChapter(tocEntries, chapterUrl)
            val parser = EpubXMLFileParser(chapterUrl, files[chapterUrl]?.data ?: ByteArray(0), files)

            when {
                spineItem.mediaType.startsWith("image/") -> {
                    chapters.add(EpubChapter(
                        absPath = "image_${spineItem.absPath}",
                        title = tocEntry?.chapterTitle,
                        body = parser.parseAsImage(spineItem.absPath),
                        index = chapterIndex++
                    ))
                }
                else -> {
                    val content = parser.parseAsDocument()
                    if (content.body.isNotBlank()) {
                        chapters.add(EpubChapter(
                            absPath = chapterUrl,
                            title = tocEntry?.chapterTitle ?: content.title ?: "Chapter ${chapters.size + 1}",
                            body = content.body,
                            index = chapterIndex++
                        ))
                    }
                }
            }
        }
    }
    chapters
}

private fun isChapter(item: ManifestItem): Boolean {
    val extension = item.absPath.substringAfterLast('.').lowercase()
    return listOf("xhtml", "html", "xml", "htm").contains(extension)
}

private fun findTocEntryForChapter(tocEntries: List<ToCEntry>, chapterUrl: String): ToCEntry? {
    val chapterUrlWithoutFragment = chapterUrl.substringBefore('#')
    return tocEntries.firstOrNull { tocEntry ->
        val tocLinkWithoutFragment = tocEntry.chapterLink.substringBefore('#')
        tocLinkWithoutFragment.equals(chapterUrlWithoutFragment, ignoreCase = true)
    }
}

private fun extractImages(
    files: Map<String, EpubFile>,
    manifestItems: Map<String, ManifestItem>
): List<EpubImage> {
    val imageExtensions = listOf("png", "gif", "raw", "jpg", "jpeg", "webp", "svg")
        .map { ".$it" }

    val unlistedImages = files
        .asSequence()
        .filter { (_, file) ->
            imageExtensions.any { file.absPath.endsWith(it, ignoreCase = true) }
        }
        .map { (_, file) ->
            EpubImage(absPath = file.absPath, image = file.data)
        }

    val listedImages = manifestItems.asSequence()
        .map { it.value }
        .filter { it.mediaType.startsWith("image") }
        .mapNotNull { item -> 
            files[item.absPath]?.let { file ->
                EpubImage(
                    absPath = file.absPath,
                    image = file.data,
                    mimeType = item.mediaType
                )
            }
        }

    return (listedImages + unlistedImages).distinctBy { it.absPath }.toList()
}

class EpubXMLFileParser(
    private val fileAbsolutePath: String,
    private val data: ByteArray,
    private val zipFile: Map<String, EpubFile>
) {
    data class Output(val body: String, val title: String?)

    private val fileParentFolder: File = File(fileAbsolutePath).parentFile ?: File("")

    fun parseAsDocument(): Output {
        val doc = Jsoup.parse(data.inputStream(), "UTF-8", "")
        val body = doc.body()
        val chapterTitle = body.selectFirst("h1, h2, h3, h4, h5, h6")?.text()
        body.selectFirst("h1, h2, h3, h4, h5, h6")?.remove()
        return Output(
            title = chapterTitle,
            body = getNodeStructuredText(body)
        )
    }

    fun parseAsImage(absolutePathImage: String): String {
        val bitmap = zipFile[absolutePathImage]?.data?.runCatching {
            BitmapFactory.decodeByteArray(this, 0, this.size)
        }?.getOrNull()

        val text = BookTextMapper.ImgEntry(
            path = absolutePathImage,
            yrel = bitmap?.let { it.height.toFloat() / it.width.toFloat() } ?: 1.45f
        ).toXMLString()

        return "\n\n$text\n\n"
    }

    private fun declareImgEntry(node: org.jsoup.nodes.Node): String {
        val attrs = node.attributes().associate { it.key to it.value }
        val relPathEncoded = attrs["src"] ?: attrs["xlink:href"] ?: ""

        val absolutePathImage = File(fileParentFolder, relPathEncoded.decodedURL)
            .canonicalFile
            .toPath()
            .invariantSeparatorsPathString
            .removePrefix("/")

        return parseAsImage(absolutePathImage)
    }

    private fun getPTraverse(node: org.jsoup.nodes.Node): String {
        fun innerTraverse(node: org.jsoup.nodes.Node): String =
            node.childNodes().joinToString("") { child ->
                when {
                    child.nodeName() == "br" -> "\n"
                    child.nodeName() == "img" -> declareImgEntry(child)
                    child.nodeName() == "image" -> declareImgEntry(child)
                    child is TextNode -> child.text()
                    else -> innerTraverse(child)
                }
            }

        val paragraph = innerTraverse(node).trim()
        return if (paragraph.isEmpty()) "" else "$paragraph\n\n"
    }

    private fun getNodeTextTraverse(node: org.jsoup.nodes.Node): String {
        val children = node.childNodes()
        if (children.isEmpty()) return ""

        return children.joinToString("") { child ->
            when {
                child.nodeName() == "p" -> getPTraverse(child)
                child.nodeName() == "br" -> "\n"
                child.nodeName() == "hr" -> "\n\n"
                child.nodeName() == "img" -> declareImgEntry(child)
                child.nodeName() == "image" -> declareImgEntry(child)
                child is TextNode -> {
                    val text = child.text().trim()
                    if (text.isEmpty()) "" else "$text\n\n"
                }
                else -> getNodeTextTraverse(child)
            }
        }
    }

    private fun getNodeStructuredText(node: org.jsoup.nodes.Node): String {
        val children = node.childNodes()
        if (children.isEmpty()) return ""

        return children.joinToString("") { child ->
            when {
                child.nodeName() == "p" -> getPTraverse(child)
                child.nodeName() == "br" -> "\n"
                child.nodeName() == "hr" -> "\n\n"
                child.nodeName() == "img" -> declareImgEntry(child)
                child.nodeName() == "image" -> declareImgEntry(child)
                child is TextNode -> child.text().trim()
                else -> getNodeTextTraverse(child)
            }
        }
    }
}
