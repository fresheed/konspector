/**
 * Created by fresheed on 12.04.18.
 */

package org.fresheed.runup;

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


val scanner = IntervalScanner(scale = 2)


fun main(args: Array<String>) {
    val conspectsPath = getRequiredArg(args, 0, "path to conspects")
    val allPathes = readConspectPathes(conspectsPath)
    val allFiles = listConspectFiles(allPathes)
    val (conspectFiles, ignoredFiles) = allFiles.partition { !it.name.startsWith("_") }
    println("Ignored pathes: " + ignoredFiles.joinToString(separator = ", "))
    println("Conspects: \n  " + conspectFiles.joinToString(separator = "\n  ") + "\n")

    val action = getRequiredArg(args, 1, "action")
    println("Action: ${action}")
    when (action) {
        "init_fresh_notes" -> initFreshNotes(args, conspectFiles)
        "list_forgetting_notes" -> listForgettingNotes(conspectFiles)
        "mark_reviewed_note" -> markReviewedNotes(args, conspectFiles)
        else -> println("Unexpected command: ${action}")
    }
}

private fun markReviewedNotes(args: Array<String>, conspectFiles: List<File>) {
    val filename = getRequiredArg(args, 2, "conspect name")
    val targetFile = tryFindFile(filename, conspectFiles)
    val conspect = readConspect(targetFile)
    val conspectNotes = conspect.notes.filterNot { isFreshNote(it) || "TODO".equals(it.state) }
    val notesIds = getRequiredArg(args, 3, "notes IDs")
    val targetNotes = if (notesIds.equals("ALL")) {
        conspectNotes.filter(::shouldReviewNow)
    } else {
        val noteIds = notesIds.split(",")
        val tNs = conspectNotes.filter { note -> noteIds.any { it.equals(note.id) } }
        if (tNs.size != noteIds.size) {
            tNs
        } else {
            throw IllegalArgumentException("Some IDs are unknown")
        }
    }
    targetNotes.forEach(::markReviewed)
    writeConspect(targetFile, conspect)
    println("Reviewed notes:\n  ${targetNotes.map { it.title }.joinToString(separator = "\n  ")}")
}

private fun listForgettingNotes(conspectFiles: List<File>) {
    conspectFiles.forEach { conspectFile ->
        val conspect = readConspect(conspectFile)
        val lastTimestamp = { note: OrgHead -> note.timestamps.sorted().last() }
        val describeNote = { note: OrgHead -> "${note.title} (${conspectFile.name}); id ${note.id}, last reviewed ${lastTimestamp(note)}" }
        val incompleteNotes = conspect.notes.filter { "TODO".equals(it.state) }
        if (incompleteNotes.isNotEmpty()) {
            println("${conspectFile.name}: some notes are incomplete: ${incompleteNotes.joinToString(", ")}")
        }
        val (freshNotes, reviewedNotes) = conspect.notes.partition(::isFreshNote)
        if (reviewedNotes.isNotEmpty()) {
            val forgettingNotes = reviewedNotes.filter(::shouldReviewNow)
            if (forgettingNotes.isNotEmpty()) {
                println(forgettingNotes.joinToString(separator = "\n", transform = describeNote))
            }
            if (freshNotes.isNotEmpty()) {
                println("${conspectFile.name}: fresh notes exist. Please initialize them before reviewing")
            }
        } else {
            println("${conspectFile.name}: conspect is not initialized")
        }
    }
}

private fun initFreshNotes(args: Array<String>, conspectFiles: List<File>) {
    val filename = getRequiredArg(args, 2, "conspect name")
    val targetFile = tryFindFile(filename, conspectFiles)
    val conspect = readConspect(targetFile)
    val freshNotes = conspect.notes.filter { isFreshNote(it) && !"TODO".equals(it.state) }
    freshNotes.forEach(::initializeNote)
    writeConspect(targetFile, conspect)
    println("Initialized fresh notes in ${targetFile.name}:\n  ${freshNotes.map { it.title }.joinToString("\n  ")}")

}

private fun tryFindFile(filename: String, conspectFiles: List<File>) =
        conspectFiles.firstOrNull { it.name.equals(filename) } ?: throw IllegalArgumentException("Conspect ${filename} was not found")

private fun getRequiredArg(args: Array<String>, index: Int, desc: String): String {
    if (args.size < index + 1) {
        throw IllegalArgumentException("Missing required argument: " + desc)
    }
    return args[index]
}

private fun shouldReviewNow(note: OrgHead): Boolean {
    val currentTime = LocalDateTime.now()
    return scanner.shouldReview(note, currentTime)
}


private fun readConspectPathes(listPath: String): List<String> {
    return File(listPath).readLines().filter { it.isNotBlank() }
}


private fun listConspectFiles(directories: List<String>): List<File> {
    val listConspectsDir = { path: String -> File(path).walkTopDown().filter { it.extension == "org" } }
    return directories.flatMap { listConspectsDir(it).asIterable() }
}
