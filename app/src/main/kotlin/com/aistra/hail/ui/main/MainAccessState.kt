package com.aistra.hail.ui.main

import androidx.lifecycle.ViewModel

/** Retained over configuration changes, but never persisted as an unlocked session. */
internal class MainAccessState : ViewModel() {
    var unlocked = false
        private set
    var authenticating = false
        private set
    var pendingSetting: Boolean? = null
        private set

    fun begin(setting: Boolean? = null): Boolean {
        if (authenticating) return false
        pendingSetting = setting
        authenticating = true
        return true
    }

    fun authenticated(): Boolean? {
        val setting = pendingSetting
        pendingSetting = null
        authenticating = false
        unlocked = true
        return setting
    }

    fun failed() {
        pendingSetting = null
        authenticating = false
    }

    fun background() {
        unlocked = false
    }
}
