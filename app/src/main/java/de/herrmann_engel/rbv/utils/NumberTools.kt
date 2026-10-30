package de.herrmann_engel.rbv.utils

import android.content.Context

// A whole number written as in the app's language, like %d in a string resource; for numbers shown
// on their own and in texts assembled with TextUtils.expandTemplate, which takes no format arguments
fun formatNumber(context: Context, number: Int): String =
    String.format(context.resources.configuration.locales[0], "%d", number)
