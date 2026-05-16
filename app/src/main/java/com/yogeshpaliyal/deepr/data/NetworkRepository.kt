package com.yogeshpaliyal.deepr.data

import com.yogeshpaliyal.deepr.util.normalizeLink
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders

class NetworkRepository(
    val httpParser: HtmlParser,
    private val httpClient: HttpClient = HttpClient(CIO),
) {
    suspend fun getLinkInfo(url: String): Result<LinkInfo> {
        val normalizedUrl = normalizeLink(url)
        try {
            val response =
                httpClient.get(normalizedUrl) {
                    header(
                        HttpHeaders.UserAgent,
                        "facebookexternalhit/1.1",
                    )
                }
            if (response.status.value != 200) {
                return Result.failure(Exception("Failed to fetch data from $normalizedUrl, status code: ${response.status.value}"))
            }
            // Return the response body as text
            return Result.success(
                httpParser.getTitleAndImageFromHtml(
                    response.bodyAsText(),
                    normalizedUrl,
                ),
            )
        } catch (e: Exception) {
            return Result.failure(
                Exception(
                    "Error fetching data from $normalizedUrl: ${e.message}",
                    e,
                ),
            )
        }
    }
}
