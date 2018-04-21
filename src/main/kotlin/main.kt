/**
 * Created by fresheed on 12.04.18.
 */

package org.fresheed.runup;

import com.orgzly.org.OrgHead
import initializeNote
import isFreshNote
import markReviewed
import notes
import readConspect
import timestamps
import writeConspect
import java.io.File
import java.time.LocalDateTime


val scanner=IntervalScanner(scale=2)


fun main(args: Array<String>){
    if (args.size<2){
        throw IllegalArgumentException("Path to conspects list and action required as arguments")
    }

    val conspectsPathes=readConspectPathes(args[0])
    val conspectFiles=listConspectFiles(conspectsPathes)
    println("Conspects: \n  "+conspectFiles.joinToString(separator = "\n  ")+"\n")

    val action=args[1]
    println("Action: ${action}")
    when {
        "list_fresh_notes".equals(action) -> conspectFiles.forEach(::listFreshNotes)
        "init_fresh_notes".equals(action) -> {
            if (args.size<3){
                throw IllegalArgumentException("Conspect name should be specified")
            }
            val filename=args[2]
            val targetFile=conspectFiles.firstOrNull{it.name.equals(filename)}?:throw IllegalArgumentException("Conspect ${filename} was not found")
            updateConspect(targetFile)
            println("Initialized fresh notes in ${filename}")
        }
        "list_forgetting_notes".equals(action) -> conspectFiles.forEach(::listForgettingNotes)
        "mark_reviewed_note".equals(action) -> {
            if (args.size<4){
                throw IllegalArgumentException("Conspect name and note name should be specified")
            }
            val filename=args[2]
            val noteTitle=args[3]
            val targetFile=conspectFiles.firstOrNull{it.name.equals(filename)}?:throw IllegalArgumentException("Conspect ${filename} was not found")
            val conspect=readConspect(targetFile)
            val targetNote=conspect.notes.firstOrNull{it.title.equals(noteTitle)}?:throw IllegalArgumentException("Note ${noteTitle} was not found")
            markReviewed(targetNote)
            writeConspect(targetFile, conspect)
            println("Note '${noteTitle}' (${filename}) reviewed")
        }
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
    val describeNote={note: OrgHead -> "${note.title} (${conspectFile.name}); last reviewed ${lastTimestamp(note)}"}
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


private fun readConspectPathes(listPath: String): List<String> {
    return File(listPath).readLines().filter{it.isNotBlank()}
}


private fun listConspectFiles(directories: List<String>): List<File> {
    val listConspectsDir={path: String -> File(path).walkTopDown().filter{it.extension=="org"}}
    return directories.flatMap{ listConspectsDir(it).asIterable()}
}
