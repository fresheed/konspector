/**
 * Created by fresheed on 12.04.18.
 */
package org.fresheed.runup;

import org.junit.jupiter.api.Test
import ConspectTests
import com.orgzly.org.OrgHead
import dateFormat
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime

/**
 * Created by fresheed on 14.01.18.
 */
class IntervalScannerTests: ConspectTests() {
    private val currentTime =LocalDateTime.parse("2018-04-02 12:00", dateFormat)
    private val scanner=IntervalScanner(scale=2)

    private fun checkScanner(expected: Boolean, note: OrgHead) = assert(scanner.shouldReview(note, currentTime) == expected)

    @Test
    fun timestampsRequired() {
        val note=makeRepeatedNote(emptyList<String>())
        assertThrows<MalformedNoteException>(MalformedNoteException::class.java, { scanner.shouldReview(note, currentTime) })
    }

    @Test
    fun freshForgettingNoteDetected() {
        checkScanner(true, makeRepeatedNote(listOf("2018-04-01 12:00")))
    }

    @Test
    fun freshRecentNoteSkipped() {
        checkScanner(false, makeRepeatedNote(listOf("2018-04-01 13:00")))
    }

    @Test
    fun oldForgettingNoteDetected() {
        checkScanner(true, makeRepeatedNote(listOf("2018-03-30 12:00", "2018-03-31 12:00")))
    }

    @Test
    fun oldRecentNoteSkipped() {
        checkScanner(false, makeRepeatedNote(listOf("2018-03-30 12:01", "2018-03-31 12:01")))
    }

    @Test
    fun exactlyExtendedPassedIntervalDetected() {
        checkScanner(true, makeRepeatedNote(listOf("2018-03-01 01:00", "2018-03-31 11:59")))
    }

    @Test
    fun exactlyExtendedCurrentIntervalSkipped() {
        checkScanner(false, makeRepeatedNote(listOf("2018-03-01 01:00", "2018-03-31 12:01")))
    }

    private fun makeRepeatedNote(repetitions: List<String>): OrgHead {
        val repetitionsProperties=repetitions.map{":REVIEW_DATE:   ${it}"}.joinToString(separator="\n")
        val noteContent="* hello\n   :PROPERTIES:\n${repetitionsProperties}\n  :END:"
        return parseContent(noteContent).headsInList[0].head
    }

}