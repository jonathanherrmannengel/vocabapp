package de.herrmann_engel.rbv.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * Returns the activity behind this context by unwrapping context wrappers, or null if there is
 * none.
 */
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
