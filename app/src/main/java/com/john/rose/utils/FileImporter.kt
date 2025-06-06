package com.john.rose.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File


suspend fun fileImporter(
    targetFile: File,
    imageData: ByteArray,
) = withContext(Dispatchers.IO) {
    targetFile.parentFile?.also { parent ->
        parent.mkdirs()
        if (parent.exists()) {
            targetFile.writeBytes(imageData)
        }
    }
}