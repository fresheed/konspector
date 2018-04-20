/**
 * Created by fresheed on 12.04.18.
 */
package org.fresheed.runup;

import com.orgzly.org.OrgHead
import org.junit.jupiter.api.Test
import ConspectTests
import isFreshNote
import org.junit.jupiter.api.Assertions.*

/**
 * Created by fresheed on 14.01.18.
 */
open class ProcessingTests: ConspectTests() {

    @Test
    fun processedNoteSkipped() {
        assertFalse(isFreshNote(parseNote("* hello\n   :PROPERTIES:\n  :REVIEW_DATE:   10\n  :END:")))
    }

    @Test
    fun rootNoteSkipped() {
        assertFalse(isFreshNote(parseNote("* hello\n   :PROPERTIES:\n  :END:")))
    }

    @Test
    fun freshNoteFound() {
        assertTrue(isFreshNote(parseNote("** hello\n   :PROPERTIES:\n  :END:")))
    }


}