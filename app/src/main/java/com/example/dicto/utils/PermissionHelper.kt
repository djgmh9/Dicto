package com.example.dicto.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log

/**
 * PermissionHelper - Utility for checking and requesting overlay permission
 *
 * Single Responsibility: Handle overlay permission checking and requesting
 */
object PermissionHelper {

    /**
     * Check if overlay permission is granted
     */
    fun canDrawOverlays(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true // No permission needed before Android M
        }
    }

    /**
     * Open settings to request overlay permission
     */
    fun requestOverlayPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                Log.d("PermissionHelper", "Opening overlay permission settings")
            } catch (e: Exception) {
                Log.e("PermissionHelper", "Error opening permission settings: ${e.message}")
            }
        }
    }
}

