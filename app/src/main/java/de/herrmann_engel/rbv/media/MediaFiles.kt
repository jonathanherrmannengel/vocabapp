package de.herrmann_engel.rbv.media

import android.app.Dialog
import android.net.Uri

/**
 * Access to the files in the media folder, as needed by the media adapters.
 * Implemented by [MediaFileManager].
 */
interface MediaFiles {
    /**
     * Shows an image in a fullscreen dialog. If the file is missing, the user is told so instead.
     * The optional dialog is closed if the user decides to handle the missing file.
     */
    fun showImageDialog(fileId: Int, cardId: Int, dialog: Dialog? = null)

    // Returns the URI of a media file by its database id, or null if it cannot be found
    fun getImageUri(id: Int): Uri?

    fun openFile(name: String)
    fun openFile(id: Int)
    fun shareFile(name: String)
    fun shareFile(id: Int)
    fun existsMediaFile(name: String?): Boolean

    /**
     * Asks for confirmation and deletes the file from the media folder. Without text, a default
     * explanation is shown. onDeleted is called after the user confirmed the deletion.
     */
    fun showDeleteDialog(fileName: String, text: String? = null, onDeleted: () -> Unit = {})
}
