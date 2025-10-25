# Bookmark Import Interface

This directory contains the extensible bookmark import interface and its implementations.

## Overview

The `BookmarkImporter` interface provides a standard way to import bookmarks from various sources. This design allows easy extension to support new import formats without modifying existing code.

## Architecture

### Base Interface

**BookmarkImporter** - The main interface that all importers must implement:
- `import(uri: Uri)`: Imports bookmarks from a given URI
- `getDisplayName()`: Returns a human-readable name for the importer
- `getSupportedMimeTypes()`: Returns an array of MIME types the importer can handle

### Implementations

1. **CsvBookmarkImporter** - Imports from CSV files exported by Deepr
   - Supports: `text/csv`, `text/comma-separated-values`, `application/csv`
   - Imports: link, name, notes, tags, thumbnail, opened count

2. **HtmlBookmarkImporter** - Abstract base class for HTML-based bookmark imports
   - Supports: `text/html`, `application/xhtml+xml`
   - Provides common HTML parsing logic that can be extended

3. **ChromeBookmarkImporter** - Imports Chrome/Chromium browser bookmarks
   - Extends: `HtmlBookmarkImporter`
   - Handles Chrome-specific HTML bookmark format
   - Extracts folder structure and tags

4. **MozillaBookmarkImporter** - Imports Mozilla/Firefox browser bookmarks
   - Extends: `HtmlBookmarkImporter`
   - Handles Firefox-specific HTML bookmark format
   - Filters out special Firefox folders

## Usage

### In ImportRepository

```kotlin
class ImportRepositoryImpl(
    private val context: Context,
    private val deeprQueries: DeeprQueries,
) : ImportRepository {
    private val csvImporter = CsvBookmarkImporter(context, deeprQueries)
    private val chromeImporter = ChromeBookmarkImporter(context, deeprQueries)
    private val mozillaImporter = MozillaBookmarkImporter(context, deeprQueries)

    override fun getAvailableImporters(): List<BookmarkImporter> =
        listOf(csvImporter, chromeImporter, mozillaImporter)

    override suspend fun importBookmarks(
        uri: Uri,
        importer: BookmarkImporter,
    ): RequestResult<ImportResult> {
        return importer.import(uri)
    }
}
```

### Adding a New Importer

To add support for a new bookmark format:

1. Create a new class that implements `BookmarkImporter`:

```kotlin
class MyCustomImporter(
    private val context: Context,
    private val deeprQueries: DeeprQueries,
) : BookmarkImporter {
    override suspend fun import(uri: Uri): RequestResult<ImportResult> {
        // Your import logic here
    }

    override fun getDisplayName(): String = "My Custom Format"

    override fun getSupportedMimeTypes(): Array<String> =
        arrayOf("application/x-custom")
}
```

2. Add it to `ImportRepositoryImpl`:

```kotlin
private val customImporter = MyCustomImporter(context, deeprQueries)

override fun getAvailableImporters(): List<BookmarkImporter> =
    listOf(csvImporter, chromeImporter, mozillaImporter, customImporter)
```

## Testing

Tests for the import interface are located in:
`app/src/test/java/com/yogeshpaliyal/deepr/backup/importer/BookmarkImporterTest.kt`

The tests verify:
- Interface implementation compliance
- Correct display names
- Proper MIME type support
- Basic functionality

## Future Enhancements

Potential additions to the import system:
- Safari bookmarks support
- Edge bookmarks support
- JSON-based bookmark formats
- Pocket/Instapaper integration
- Browser sync services (Chrome Sync, Firefox Sync)
