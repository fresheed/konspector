import com.orgzly.org.OrgHead
import com.orgzly.org.parser.OrgNodeInList
import com.orgzly.org.parser.OrgParsedFile
import com.orgzly.org.parser.OrgParser
import java.io.File
import java.io.FileReader

/**
 * Created by fresheed on 20.04.18.
 */

fun getParserBuilder() = OrgParser.Builder().setTodoKeywords(arrayOf("TODO"))


val OrgParsedFile.notes: List<OrgHead>
    get() = headsInList.filter{it.level > 1}.map(OrgNodeInList::getHead)


fun readConspect(file: File): OrgParsedFile{
    return getParserBuilder().setInput(FileReader(file)).build().parse()
}

fun writeConspect(file: File, conspect: OrgParsedFile) {
    val settings=conspect.file.settings
    settings.isIndented=true
    file.writeText(conspect.toString())
}


