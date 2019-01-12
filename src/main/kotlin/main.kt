/**
 * Created by fresheed on 12.04.18.
 */

package org.fresheed.runup;

import NOTE_ID_PROPERTY
import com.orgzly.org.OrgHead
import id
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

    val allPathes=readConspectPathes(args[0])
    val allFiles=listConspectFiles(allPathes)
    val (conspectFiles, ignoredFiles)=allFiles.partition{!it.name.startsWith("_")}
    println("Ignored pathes: "+ignoredFiles.joinToString(separator = ", "))
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
        }
        "list_forgetting_notes".equals(action) -> conspectFiles.forEach(::listForgettingNotes)
        "mark_reviewed_note".equals(action) -> {
            if (args.size<4){
                throw IllegalArgumentException("Conspect name and note name should be specified")
            }
            val filename=args[2]
            val targetFile=conspectFiles.firstOrNull{it.name.equals(filename)}?:throw IllegalArgumentException("Conspect ${filename} was not found")
            val conspect=readConspect(targetFile)
            val conspectNotes=conspect.notes.filterNot{isFreshNote(it) || "TODO".equals(it.state)}
            val targetNotes=if (args[3].equals("ALL")){
                conspectNotes.filter(::shouldReviewNow)
            } else {
                val noteIds=args[3].split(",")
                val tNs=conspectNotes.filter{note -> noteIds.any{it.equals(note.id)}}
                if (tNs.size!=noteIds.size) {
                    tNs
                } else {
                    throw IllegalArgumentException("Some IDs are unknown")
                }
            }
            targetNotes.forEach(::markReviewed)
            writeConspect(targetFile, conspect)
            println("Reviewed notes:\n  ${targetNotes.map{it.title}.joinToString(separator="\n  ")}")
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
    val freshNotes = conspect.notes.filter{isFreshNote(it) && !"TODO".equals(it.state)}
    freshNotes.forEach(::initializeNote)
    writeConspect(file, conspect)
    println("Initialized fresh notes in ${file.name}:\n  ${freshNotes.map{it.title}.joinToString("\n  ")}")
}


private fun listForgettingNotes(conspectFile: File) {
    val conspect = readConspect(conspectFile)
    val lastTimestamp = { note: OrgHead -> note.timestamps.sorted().last() }
    val describeNote = { note: OrgHead -> "${note.title} (${conspectFile.name}); id ${note.id}, last reviewed ${lastTimestamp(note)}" }
    val incompleteNotes = conspect.notes.filter{"TODO".equals(it.state)}
    if (incompleteNotes.isNotEmpty()){
        println("${conspectFile.name}: some notes are incomplete: ${incompleteNotes.joinToString(", ")}")
    }
    val (freshNotes, reviewedNotes) = conspect.notes.partition(::isFreshNote)
    if (reviewedNotes.isNotEmpty()) {
        val forgettingNotes = reviewedNotes.filter(::shouldReviewNow)
        if (forgettingNotes.isNotEmpty()) {
            println(forgettingNotes.joinToString(separator = "\n", transform = describeNote))
        }
        if (freshNotes.isNotEmpty()){
            println("${conspectFile.name}: fresh notes exist. Please initialize them before reviewing")
        }
    } else {
        println("${conspectFile.name}: conspect is not initialized")
    }
}


private fun shouldReviewNow(note: OrgHead): Boolean {
    val currentTime = LocalDateTime.now()
    return scanner.shouldReview(note, currentTime)
}


private fun readConspectPathes(listPath: String): List<String> {
    return File(listPath).readLines().filter{it.isNotBlank()}
}


private fun listConspectFiles(directories: List<String>): List<File> {
    val listConspectsDir={path: String -> File(path).walkTopDown().filter{it.extension=="org"}}
    return directories.flatMap{ listConspectsDir(it).asIterable()}
}
