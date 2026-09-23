package com.androidengineers.pocketchat

import com.androidengineers.pocketchat.data.AndroidEngineersGuide
import org.junit.Assert.*
import org.junit.Test

class AndroidEngineersGuideTest {
    @Test fun mentorshipLinkDoesNotAlsoSelectCoursesPrefix() {
        val links=AndroidEngineersGuide.mentionedResources("Explore [mentorship](https://www.androidengineers.in/masterclass/one-to-one-mentoring).")
        assertEquals(listOf("1:1 mentorship"),links.map{it.title})
    }
    @Test fun arbitraryAndLookalikeDestinationsDoNotBecomeResourceButtons() {
        assertTrue(AndroidEngineersGuide.mentionedResources("https://www.androidengineers.in/masterclass-evil https://example.com https://www.androidengineers.in/codelabs/fake").isEmpty())
    }
    @Test fun knownCodelabAndRoadmapLinksAreRecognized() {
        assertEquals(2,AndroidEngineersGuide.mentionedResources("https://www.androidengineers.in/codelabs\nhttps://www.androidengineers.in/roadmap").size)
    }
}
