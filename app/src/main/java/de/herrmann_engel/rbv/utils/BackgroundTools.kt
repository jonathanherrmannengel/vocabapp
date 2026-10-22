package de.herrmann_engel.rbv.utils

import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Runs a database operation that can take long with much data (e.g. deleting many cards) in the
 * background, so the app does not freeze, and then calls onDone. Meanwhile, a dialog blocks the
 * activity, so the user cannot navigate away or use items that are being changed (see
 * showWaitDialog). The operation is always completed. If the user has left the app meanwhile,
 * onDone waits until the activity is visible again; it is left out if the activity was closed.
 */
fun runInBackground(activity: ComponentActivity, operation: () -> Unit, onDone: () -> Unit) {
    activity.lifecycleScope.launch {
        val waitDialog = showWaitDialog(activity)
        withContext(Dispatchers.IO + NonCancellable) { operation() }
        waitDialog.dismiss()
        activity.lifecycle.withStarted(onDone)
    }
}
