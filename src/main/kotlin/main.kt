/**
 * Created by fresheed on 12.04.18.
 */

package org.fresheed.runup;

import com.orgzly.org.OrgHead
import initializeNote
import isFreshNote
import notes
import readConspect
import timestamps
import writeConspect
import java.io.File
import java.time.LocalDateTime


val scanner=IntervalScanner(scale=2)


fun main(args: Array<String>){
    args.
    val directoryPath="./algorithms"
    val conspectFiles=listConspectFiles(listOf(directoryPath))
    println("Conspects: \n  "+conspectFiles.joinToString(separator = "\n  "))

    val action=args.firstOrNull()?:"list_forgetting_notes"
    println(action)

    when {
        "list_fresh_notes".equals(action) -> conspectFiles.forEach(::listFreshNotes)
        "init_fresh_notes".equals(action) -> conspectFiles.forEach(::updateConspect)
        "list_forgetting_notes".equals(action) -> conspectFiles.forEach(::listForgettingNotes)
        else -> println("Unexpected command: ${action}")
    }
}

private fun listFreshNotes(file: File) {
    val freshNotes=readConspect(file).notes.filter(::isFreshNote)
    freshNotes.forEach { println("New note: ${it.title} (${file.name})") }
}


private fun updateConspect(file: File) {
    val conspect=readConspect(file)
    val freshNotes = conspect.notes.filter(::isFreshNote)
    freshNotes.forEach(::initializeNote)
    writeConspect(file, conspect)
}


private fun listForgettingNotes(conspectFile: File){
    val conspect=readConspect(conspectFile)
    val freshNotes = conspect.notes.filter(::isFreshNote)
    val lastTimestamp={note: OrgHead -> note.timestamps.sorted().last()}
    val describeNote={note: OrgHead -> "${note.title} (${conspectFile.name}); reviewed ${lastTimestamp(note)}"}
    if (freshNotes.isNotEmpty()){
        println("${conspectFile.name}: fresh notes exist. Please initialize them before reviewing")
    } else {
        val currentTime = LocalDateTime.now()
        val forgettingNotes=conspect.notes.filter{scanner.shouldReview(it, currentTime)}
        if (forgettingNotes.isNotEmpty()) {
            val message=forgettingNotes.joinToString(separator="\n", transform=describeNote)
            println(message)
        }
    }
}


private fun listConspectFiles(directories: List<String>): List<File> {
    val listConspectsDir={path: String -> File(path).walkTopDown().filter{it.extension=="org"}}
    return directories.flatMap{ listConspectsDir(it).asIterable()}
}
