package de.herrmann_engel.rbv.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

class ContextTools {

    fun getActivity(context: Context): Activity? {
        return when (context) {
            is Activity -> context
            is ContextWrapper -> getActivity(context.baseContext)
            else -> null
        }
    }

}
