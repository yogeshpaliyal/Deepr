package com.yogeshpaliyal.deepr.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.UserAgent
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for NetworkRepository to ensure URL normalization works correctly
 */
class NetworkRepositoryTest {
    private val mockHtmlContent =
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta property="og:title" content="Test Title" />
            <meta property="og:image" content="https://example.com/image.jpg" />
        </head>
        <body>
            <h1>Test Page</h1>
        </body>
        </html>
        """.trimIndent()

    @Test
    fun getLinkInfo_normalizesUrlWithoutScheme() =
        runTest {
            // Create a mock HTTP client that tracks the URL it received
            var receivedUrl: String? = null
            val mockEngine =
                MockEngine { request ->
                    receivedUrl = request.url.toString()
                    respond(
                        content = mockHtmlContent,
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type", "text/html"),
                    )
                }

            val httpClient = HttpClient(mockEngine)
            val htmlParser = HtmlParser()
            val repository = NetworkRepository(httpClient, htmlParser)

            // Test with URL without https://
            val result = repository.getLinkInfo("example.com")

            // Verify the URL was normalized to include https://
            assertTrue(result.isSuccess)
            assertEquals("https://example.com/", receivedUrl)
        }

    @Test
    fun getLinkInfo_preservesUrlWithScheme() =
        runTest {
            var receivedUrl: String? = null
            val mockEngine =
                MockEngine { request ->
                    receivedUrl = request.url.toString()
                    respond(
                        content = mockHtmlContent,
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type", "text/html"),
                    )
                }

            val httpClient = HttpClient(mockEngine)
            val htmlParser = HtmlParser()
            val repository = NetworkRepository(httpClient, htmlParser)

            // Test with URL that already has https://
            val result = repository.getLinkInfo("https://example.com")

            // Verify the URL was not modified
            assertTrue(result.isSuccess)
            assertEquals("https://example.com/", receivedUrl)
        }

    @Test
    fun getLinkInfo_normalizesUrlWithPath() =
        runTest {
            var receivedUrl: String? = null
            val mockEngine =
                MockEngine { request ->
                    receivedUrl = request.url.toString()
                    respond(
                        content = mockHtmlContent,
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type", "text/html"),
                    )
                }

            val httpClient = HttpClient(mockEngine)
            val htmlParser = HtmlParser()
            val repository = NetworkRepository(httpClient, htmlParser)

            // Test with URL without https:// but with path
            val result = repository.getLinkInfo("example.com/path")

            // Verify the URL was normalized and path preserved
            assertTrue(result.isSuccess)
            assertEquals("https://example.com/path", receivedUrl)
        }

    @Test
    fun getLinkInfo_handlesHttpStatusError() =
        runTest {
            val mockEngine =
                MockEngine {
                    respond(
                        content = "",
                        status = HttpStatusCode.NotFound,
                        headers = headersOf("Content-Type", "text/html"),
                    )
                }

            val httpClient = HttpClient(mockEngine)
            val htmlParser = HtmlParser()
            val repository = NetworkRepository(httpClient, htmlParser)

            val result = repository.getLinkInfo("example.com")

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull()?.message?.contains("404") == true)
        }

    @Test
    fun getLinkInfo_extractsTitleFromHtml() =
        runTest {
            val mockEngine =
                MockEngine {
                    respond(
                        content = mockHtmlContent,
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type", "text/html"),
                    )
                }

            val httpClient = HttpClient(mockEngine)
            val htmlParser = HtmlParser()
            val repository = NetworkRepository(httpClient, htmlParser)

            val result = repository.getLinkInfo("example.com")

            assertTrue(result.isSuccess)
            val linkInfo = result.getOrNull()
            assertEquals("Test Title", linkInfo?.title)
            assertEquals("https://example.com/image.jpg", linkInfo?.image)
        }

    @Test
    fun getLinkInfo_sendsUserAgentHeader() =
        runTest {
            // Create a mock HTTP client that captures the User-Agent header
            var receivedUserAgent: String? = null
            val mockEngine =
                MockEngine { request ->
                    receivedUserAgent = request.headers["User-Agent"]
                    respond(
                        content = mockHtmlContent,
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type", "text/html"),
                    )
                }

            val httpClient =
                HttpClient(mockEngine) {
                    install(UserAgent) {
                        agent = "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                    }
                }
            val htmlParser = HtmlParser()
            val repository = NetworkRepository(httpClient, htmlParser)

            val result = repository.getLinkInfo("example.com")

            // Verify the User-Agent header was sent
            assertTrue(result.isSuccess)
            assertEquals(
                "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36",
                receivedUserAgent,
            )
        }
}
