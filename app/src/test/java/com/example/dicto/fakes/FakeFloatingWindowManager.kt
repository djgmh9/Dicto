package com.example.dicto.fakes

import com.example.dicto.domain.manager.IFloatingWindowManager

/**
 * Fake implementation of IFloatingWindowManager for testing
 * Tracks calls for verification without actual window management
 */
class FakeFloatingWindowManager : IFloatingWindowManager {
    var isPermissionGrantedValue = true
        private set
    var startCallCount = 0
        private set
    var stopCallCount = 0
        private set
    var showCallCount = 0
        private set

    override fun isPermissionGranted(): Boolean = isPermissionGrantedValue

    fun setPermissionGranted(granted: Boolean) {
        isPermissionGrantedValue = granted
    }

    override fun startFloatingWindow() {
        startCallCount++
    }

    override fun stopFloatingWindow() {
        stopCallCount++
    }

    override fun showFloatingButton() {
        showCallCount++
    }

    fun reset() {
        isPermissionGrantedValue = true
        startCallCount = 0
        stopCallCount = 0
        showCallCount = 0
    }
}


