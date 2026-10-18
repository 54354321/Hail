package com.aistra.hail.ui.main

import org.junit.Assert.*
import org.junit.Test

class MainAccessStateTest {
    @Test fun coldStartAndCanceledLoginStayLocked() {
        val access = MainAccessState()
        assertFalse(access.unlocked)
        assertTrue(access.begin())
        access.failed()
        assertFalse(access.unlocked)
        assertFalse(access.authenticating)
    }

    @Test fun enablingRequiresSuccessfulAuthentication() {
        val access = MainAccessState()
        access.begin(true)
        assertFalse(access.unlocked)
        assertEquals(true, access.authenticated())
        assertTrue(access.unlocked)
        assertNull(access.pendingSetting)
    }

    @Test fun cancelingASettingChangeDiscardsIt() {
        val access = MainAccessState()
        access.begin(false)
        access.failed()
        assertNull(access.pendingSetting)
        assertFalse(access.unlocked)
    }

    @Test fun backgroundReturnRequiresAuthenticationAgain() {
        val access = MainAccessState()
        access.begin()
        access.authenticated()
        access.background()
        assertFalse(access.unlocked)
        assertTrue(access.begin())
    }

    @Test fun credentialActivityDoesNotCreateASecondPrompt() {
        val access = MainAccessState()
        access.begin()
        access.background()
        assertFalse(access.begin())
        access.authenticated()
        assertTrue(access.unlocked)
    }

    @Test fun doubleTappingCannotReplaceThePendingRequest() {
        val access = MainAccessState()
        access.begin(true)
        assertFalse(access.begin(false))
        assertEquals(true, access.authenticated())
    }
}
