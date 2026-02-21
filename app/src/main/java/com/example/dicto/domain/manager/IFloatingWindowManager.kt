package com.example.dicto.domain.manager

/**
 * IFloatingWindowManager - Interface for floating window coordination
 *
 * This interface defines the contract for floating window management,
 * allowing for different implementations (real, fake for testing, etc.)
 */
interface IFloatingWindowManager {
    /**
     * Check if system alert window permission is granted
     */
    fun isPermissionGranted(): Boolean

    /**
     * Start the floating window service
     */
    fun startFloatingWindow()

    /**
     * Stop the floating window service
     */
    fun stopFloatingWindow()

    /**
     * Show the floating button (used when returning from overlay)
     */
    fun showFloatingButton()
}

