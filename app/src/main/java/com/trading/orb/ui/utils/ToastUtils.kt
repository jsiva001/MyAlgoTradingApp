package com.trading.orb.ui.utils

import android.content.Context
import android.widget.Toast

object ToastUtils {
    fun showShort(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showLong(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    fun showSuccess(context: Context, message: String) {
        showShort(context, message)
    }

    fun showError(context: Context, message: String) {
        showLong(context, message)
    }
}
