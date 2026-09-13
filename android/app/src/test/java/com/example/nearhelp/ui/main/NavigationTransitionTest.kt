package com.example.nearhelp.ui.main

import com.example.nearhelp.AiCrisisAssistantNavKey
import com.example.nearhelp.CommunityMapNavKey
import com.example.nearhelp.HomeNavKey
import com.example.nearhelp.ProfileNavKey
import com.example.nearhelp.ui.victim.VictimNavTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationTransitionTest {

    @Test
    fun victimNavTab_ordinalOrder_isConsistentForDirectionalTransitions() {
        // Spatial progression: HOME -> CHAT -> MAP -> PROFILE
        assertEquals(0, VictimNavTab.HOME.ordinal)
        assertEquals(1, VictimNavTab.CHAT.ordinal)
        assertEquals(2, VictimNavTab.MAP.ordinal)
        assertEquals(3, VictimNavTab.PROFILE.ordinal)

        // Verifying forward directional check
        assertTrue(VictimNavTab.MAP.ordinal > VictimNavTab.HOME.ordinal)
        assertTrue(VictimNavTab.PROFILE.ordinal > VictimNavTab.CHAT.ordinal)
        assertTrue(VictimNavTab.HOME.ordinal < VictimNavTab.PROFILE.ordinal)
    }

    @Test
    fun navKeys_supportAllBottomDestinations() {
        val keys = listOf(
            HomeNavKey,
            AiCrisisAssistantNavKey(),
            CommunityMapNavKey,
            ProfileNavKey,
        )
        assertEquals(4, keys.size)
    }
}
