/**
 * Created by fresheed on 12.04.18.
 */
package org.fresheed.runup;

import org.junit.jupiter.api.Test
import ConspectTests
import com.orgzly.org.OrgHead
import dateFormat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.function.Executable
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.TemporalAmount

/**
 * Created by fresheed on 14.01.18.
 */
class IntervalScannerTests: ConspectTests() {
    private val time=LocalDateTime.parse("2018-04-02 12:00", dateFormat)
    private val scanner=IntervalScanner(scale=2)

    @Test
    fun timestampsRequired() {
        val note=makeRepeatedNote(emptyList<String>())
        assertThrows<MalformedNoteException>(MalformedNoteException::class.java, { scanner.shouldReview(note, time) })
    }

    @Test
    fun freshNoteDetected() {
        val note=makeRepeatedNote(listOf("2018-04-01 12:00"))
        assertTrue(scanner.shouldReview(note, time))
    }

    @Test
    fun recentNoteSkipped() {
        val note=makeRepeatedNote(listOf("2018-04-01 13:00"))
        assertFalse(scanner.shouldReview(note, time))
    }

    @Test
    fun stableNoteSkipped() {
        val note=makeRepeatedNote(listOf("2018-03-31 12:00", "2018-04-01 12:00"))
        assertFalse(scanner.shouldReview(note, time))
    }

    @Test
    fun forgettingNoteDetected() {
        val note=makeRepeatedNote(listOf("2018-04-01 01:00", "2018-04-01 12:00"))
        assertTrue(scanner.shouldReview(note, time))
    }

    @Test
    fun onlyLatestIntervalProcessed() {
        val note=makeRepeatedNote(listOf("2018-04-01 01:00", "2018-03-31 12:00", "2018-04-01 12:00"))
        assertTrue(scanner.shouldReview(note, time))
    }

    @Test
    fun checkExport() {
        val note=parseContent("*** hello\n   :PROPERTIES:\n   :FOO:   bar\n  :END:")
        println(note.toString())
    }

    private fun makeRepeatedNote(repetitions: List<String>): OrgHead {
        val repetitionsProperties=repetitions.map{":REVIEW_DATE:   ${it}"}.joinToString(separator="\n")
        val noteContent="* hello\n   :PROPERTIES:\n${repetitionsProperties}\n  :END:"
        return parseContent(noteContent).headsInList[0].head
    }

}