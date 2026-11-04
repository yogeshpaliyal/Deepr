package com.yogeshpaliyal.deepr.backup

import com.yogeshpaliyal.deepr.GetLinksForBackup
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Writes links in the Netscape Bookmark File format, which can be imported by
 * Chrome, Firefox, Safari, Edge and by Deepr's own bookmark importers.
 *
 * Links are grouped into one folder per profile. Tags are written to the TAGS
 * attribute and notes to a <DD> description.
 */
class HtmlBookmarkWriter {
    fun writeToHtml(
        outputStream: OutputStream,
        data: List<GetLinksForBackup>,
    ) {
        outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write(build(data))
        }
    }

    fun build(data: List<GetLinksForBackup>): String =
        buildString {
            appendLine("<!DOCTYPE NETSCAPE-Bookmark-file-1>")
            appendLine("<!-- This is an automatically generated file.")
            appendLine("     It will be read and overwritten.")
            appendLine("     DO NOT EDIT! -->")
            appendLine("<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">")
            appendLine("<TITLE>Bookmarks</TITLE>")
            appendLine("<H1>Bookmarks</H1>")
            appendLine("<DL><p>")
            data.groupBy { it.profileName }.forEach { (profileName, links) ->
                appendLine("    <DT><H3>${escape(profileName)}</H3>")
                appendLine("    <DL><p>")
                links.forEach { appendLink(it) }
                appendLine("    </DL><p>")
            }
            appendLine("</DL><p>")
        }

    private fun StringBuilder.appendLink(item: GetLinksForBackup) {
        append("        <DT><A HREF=\"").append(escape(item.link)).append('"')
        toEpochSeconds(item.createdAt)?.let { append(" ADD_DATE=\"").append(it).append('"') }
        toEpochSeconds(item.lastOpenedAt)?.let { append(" LAST_VISIT=\"").append(it).append('"') }
        val tags = item.tags?.takeIf { it.isNotBlank() }
        if (tags != null) {
            val normalized =
                tags
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .joinToString(",")
            append(" TAGS=\"").append(escape(normalized)).append('"')
        }
        append('>')
        append(escape(item.name.ifBlank { item.link }))
        appendLine("</A>")
        if (item.notes.isNotBlank()) {
            append("        <DD>").appendLine(escape(item.notes))
        }
    }

    /** Parses SQLite's CURRENT_TIMESTAMP format (UTC) into epoch seconds. */
    private fun toEpochSeconds(timestamp: String?): Long? {
        if (timestamp.isNullOrBlank()) return null
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")
            format.parse(timestamp)?.time?.div(1000)
        } catch (_: Exception) {
            null
        }
    }

    private fun escape(value: String): String =
        buildString(value.length) {
            value.forEach { ch ->
                when (ch) {
                    '&' -> append("&amp;")
                    '<' -> append("&lt;")
                    '>' -> append("&gt;")
                    '"' -> append("&quot;")
                    '\'' -> append("&#39;")
                    else -> append(ch)
                }
            }
        }
}
