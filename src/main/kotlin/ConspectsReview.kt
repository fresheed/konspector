import com.orgzly.org.OrgHead
import com.orgzly.org.OrgProperty
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Created by fresheed on 20.04.18.
 */


val REVIEW_DATE_PROPERTY: String = "REVIEW_DATE"
val NOTE_ID_PROPERTY: String = "NOTE_ID"
val dateFormat= DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")


fun isFreshNote(note: OrgHead): Boolean {
    val properties=note.properties.map{it.name}
    val hasTimestamps=(REVIEW_DATE_PROPERTY in properties)
    val hasId=(NOTE_ID_PROPERTY in properties)
    if (hasTimestamps!=hasId){
        throw RuntimeException("Malformed note: ${note.title}")
    }
    return !hasId // equals to !hasTimestamps
}


fun initializeNote(note: OrgHead) {
    val currentTime = LocalDateTime.now()
    note.addProperty(OrgProperty(REVIEW_DATE_PROPERTY, currentTime.format(dateFormat)))
    val id= UUID.randomUUID().toString().substring(0, 8)
    note.addProperty(OrgProperty(NOTE_ID_PROPERTY, id))
}


val OrgHead.timestamps: List<LocalDateTime>
    get() = properties.filter{it.name.equals(REVIEW_DATE_PROPERTY)}.map{LocalDateTime.parse(it.value, dateFormat)}

