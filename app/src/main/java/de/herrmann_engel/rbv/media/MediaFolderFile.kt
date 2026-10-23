package de.herrmann_engel.rbv.media

import android.net.Uri
import android.provider.DocumentsContract

/**
 * A file in the media folder, as listed by [MediaFileManager]. mimeType is null if the document
 * provider does not know it.
 */
class MediaFolderFile(val name: String, val uri: Uri, val mimeType: String?) {
    // False for folders and for documents without a type
    val isFile: Boolean
        get() = !mimeType.isNullOrEmpty() && mimeType != DocumentsContract.Document.MIME_TYPE_DIR
}
