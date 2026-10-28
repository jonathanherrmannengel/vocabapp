package de.herrmann_engel.rbv.media

import de.herrmann_engel.rbv.db.DbMedia

// Media files that can be displayed as images
private val IMAGE_MIME_TYPES = setOf("image/png", "image/jpeg", "image/webp", "image/gif")

val DbMedia.isImage: Boolean
    get() = mime in IMAGE_MIME_TYPES
