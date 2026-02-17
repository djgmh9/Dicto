package com.example.dicto

import android.content.ClipDescription
import android.content.Context
import android.content.ClipboardManager

fun getClipboardText(context: Context): String? {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    // Check if the clipboard actually contains text
    if (clipboard.hasPrimaryClip() && clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true) {
        val item = clipboard.primaryClip?.getItemAt(0)
        return item?.text?.toString()
    }
    return null
}