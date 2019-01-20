/**
 * Created by fresheed on 17.04.18.
 */
package org.fresheed.runup;

import REVIEW_DATE_PROPERTY
import com.orgzly.org.OrgHead
import timestamps
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.pow

class IntervalScanner(val scale: Int) {
    val unitDuration = Duration.ofDays(1)

    fun shouldReview(note: OrgHead, currentTime: LocalDateTime): Boolean {
        if (note.timestamps.isNotEmpty()){
            val sortedTimestamps=note.timestamps.sorted()
            val latestRepetition=sortedTimestamps.last()
            val newInterval=if (note.timestamps.size > 1) {
//                val latestInterval=Duration.between(sortedTimestamps.get(sortedTimestamps.size-2), latestRepetition)
//                latestInterval.multipliedBy(scale.toLong())
                val multiplier=scale.toDouble().pow(note.timestamps.size-1).toLong()
                unitDuration.multipliedBy(multiplier)
            } else {
                unitDuration
            }
            val reviewAt=latestRepetition+newInterval
            return currentTime>=reviewAt
        } else {
            throw MalformedNoteException("WIP (${note.title})")
        }
    }
}


class MalformedNoteException(val msg: String): Exception(msg){

}