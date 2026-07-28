package com.yogeshpaliyal.deepr.backup

import com.yogeshpaliyal.deepr.ListDeeprWithTagsAsc
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Writer for generating Netscape/Mozilla bookmark HTML format.
 * This format is compatible with Firefox, Chrome, and other browsers.
 */
class HtmlWriter {
    fun writeToHtml(
        outputStream: OutputStream,
        data: List<ListDeeprWithTagsAsc>,
    ) {
        outputStream.bufferedWriter().use { writer ->
            // Write HTML header
            writer.write("<!DOCTYPE NETSCAPE-Bookmark-file-1>\n")
            writer.write("<!-- This is an automatically generated file.\n")
            writer.write("     It will be read and overwritten.\n")
            writer.write("     DO NOT EDIT! -->\n")
            writer.write("<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">\n")
            writer.write("<TITLE>Bookmarks</TITLE>\n")
            writer.write("<H1>Bookmarks</H1>\n")
            // Note: <DL><p> is the standard Netscape bookmark format (not a typo)
            // The <p> is part of the original format spec from Netscape Navigator
            writer.write("<DL><p>\n")

            // Group links by tags to create folders
            val tagGroups = mutableMapOf<String, MutableList<ListDeeprWithTagsAsc>>()
            val untaggedLinks = mutableListOf<ListDeeprWithTagsAsc>()

            data.forEach { item ->
                val tags = item.tagsNames?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
                if (tags.isNullOrEmpty()) {
                    untaggedLinks.add(item)
                } else {
                    // Add link to each tag group
                    tags.forEach { tag ->
                        tagGroups.getOrPut(tag) { mutableListOf() }.add(item)
                    }
                }
            }

            // Write untagged links first
            if (untaggedLinks.isNotEmpty()) {
                untaggedLinks.forEach { item ->
                    writeBookmark(writer, item)
                }
            }

            // Write tagged links in folders
            tagGroups.entries.sortedBy { it.key }.forEach { (tag, links) ->
                writer.write("    <DT><H3>${escapeHtml(tag)}</H3>\n")
                writer.write("    <DL><p>\n")
                links.forEach { item ->
                    writeBookmark(writer, item, indent = "        ")
                }
                // Note: </DL><p> is the standard Netscape bookmark format
                writer.write("    </DL><p>\n")
            }

            // Note: </DL><p> is the standard Netscape bookmark format
            writer.write("</DL><p>\n")
        }
    }

    private fun writeBookmark(
        writer: java.io.BufferedWriter,
        item: ListDeeprWithTagsAsc,
        indent: String = "    ",
    ) {
        // Convert timestamp to seconds (Unix timestamp)
        val addDate = try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val date = dateFormat.parse(item.createdAt)
            date?.time?.div(1000) ?: 0
        } catch (e: Exception) {
            0
        }

        val tags = item.tagsNames?.replace(",", ", ")?.trim() ?: ""
        val title = item.name?.ifBlank { item.link } ?: item.link

        writer.write("$indent<DT><A HREF=\"${escapeHtml(item.link)}\"")
        writer.write(" ADD_DATE=\"$addDate\"")
        if (tags.isNotEmpty()) {
            writer.write(" TAGS=\"${escapeHtml(tags)}\"")
        }
        if (item.isFavourite) {
            writer.write(" ICON=\"★\"")
        }
        writer.write(">${escapeHtml(title)}</A>\n")

        // Add notes as description if present
        if (!item.notes.isNullOrBlank()) {
            writer.write("$indent<DD>${escapeHtml(item.notes)}\n")
        }
    }

    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}
