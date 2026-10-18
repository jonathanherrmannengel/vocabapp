package de.herrmann_engel.rbv.utils

import android.content.Context
import android.text.format.DateUtils

/**
 * Formats a point in time (Unix time in seconds) as short numeric date and time in the user's
 * language, following the device's 12/24-hour setting.
 */
fun formatDateTime(context: Context, unixSeconds: Long): String =
    DateUtils.formatDateTime(
        context,
        unixSeconds * 1000L,
        DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR or
            DateUtils.FORMAT_NUMERIC_DATE or DateUtils.FORMAT_SHOW_TIME
    )
