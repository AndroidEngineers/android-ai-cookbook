package com.androidengineers.pocketcommunity

import com.androidengineers.pocketcommunity.data.*
import org.junit.Assert.*
import org.junit.Test

class DiscoveryTest {
    @Test
    fun missingFactsStayUnknown() {
        val e =
            parseDiscovery(
                    """{"cities":[],"events":[{"id":"e1","title":"Community","venue":null,"registration_url":null,"is_free":null}],"sponsors":[]}"""
                )
                .single()
        assertEquals("Venue not listed", e.venue)
        assertEquals("Price not listed", e.entry)
        assertEquals("", e.website)
        assertEquals("Date not listed", e.date)
    }

    @Test
    fun sponsoredListingsRemainLabelled() {
        val e =
            parseDiscovery(
                    """{"cities":[{"id":"c","name":"Bengaluru"}],"events":[{"id":"e1","city_id":"c","title":"Community","is_free":false}],"sponsors":[{"sponsor_campaigns":{"event_id":"e1"}}]}"""
                )
                .single()
        assertTrue(e.sponsored)
        assertEquals("Bengaluru", e.city)
        assertEquals("Paid · check official pricing", e.entry)
    }
}
