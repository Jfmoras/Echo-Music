package echo.music.iad1tya.podcast

import android.util.Xml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.net.HttpURLConnection
import java.net.URL

/** Public RSS 2.0 podcast feeds: no account, tracking SDK or proprietary API required. */
data class PodcastEpisode(
  val id: String,
  val title: String,
  val audioUrl: String,
  val description: String,
  val published: String,
  val duration: String,
)

data class PodcastFeed(
  val feedUrl: String,
  val title: String,
  val description: String,
  val imageUrl: String?,
  val episodes: List<PodcastEpisode>,
)

class PodcastRssRepository {
  suspend fun load(feedUrl: String): PodcastFeed = withContext(Dispatchers.IO) {
    require(URL(feedUrl).protocol == "https") { "Podcast feeds must use HTTPS" }
    val connection = URL(feedUrl).openConnection() as HttpURLConnection
    connection.connectTimeout = 12_000
    connection.readTimeout = 20_000
    connection.setRequestProperty("Accept", "application/rss+xml, application/xml, text/xml")
    connection.setRequestProperty("User-Agent", "Streamer/1.0 (podcast RSS reader)")
    try {
      require(connection.responseCode in 200..299) { "RSS request failed: ${connection.responseCode}" }
      require(connection.contentLengthLong <= 5_000_000L) { "RSS feed too large" }
      connection.inputStream.buffered().use { input ->
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(input, "UTF-8")
        var event = parser.eventType
        var feedTitle = ""
        var feedDescription = ""
        var feedImage: String? = null
        var inItem = false
        var inImage = false
        var id = ""
        var title = ""
        var audio = ""
        var description = ""
        var published = ""
        var duration = ""
        val episodes = mutableListOf<PodcastEpisode>()
        while (event != XmlPullParser.END_DOCUMENT) {
          when (event) {
            XmlPullParser.START_TAG -> when (parser.name.lowercase()) {
              "item" -> {
                inItem = true
                id = ""; title = ""; audio = ""; description = ""; published = ""; duration = ""
              }
              "image" -> if (!inItem) inImage = true
              "enclosure" -> if (inItem) {
                val url = parser.getAttributeValue(null, "url").orEmpty()
                val type = parser.getAttributeValue(null, "type").orEmpty()
                if (url.startsWith("https://") && (type.startsWith("audio/") || type.isBlank())) audio = url
              }
              "itunes:image" -> if (!inItem) {
                feedImage = parser.getAttributeValue(null, "href") ?: feedImage
              }
              "title" -> {
                val value = parser.nextText().trim()
                if (inItem) title = value else if (!inImage) feedTitle = value
              }
              "description", "content:encoded" -> {
                val value = parser.nextText().trim()
                if (inItem && description.isBlank()) description = value
                else if (!inItem && !inImage && feedDescription.isBlank()) feedDescription = value
              }
              "guid" -> if (inItem) id = parser.nextText().trim()
              "pubdate" -> if (inItem) published = parser.nextText().trim()
              "itunes:duration" -> if (inItem) duration = parser.nextText().trim()
              "url" -> if (inImage && !inItem) feedImage = parser.nextText().trim()
            }
            XmlPullParser.END_TAG -> when (parser.name.lowercase()) {
              "image" -> inImage = false
              "item" -> {
                if (title.isNotBlank() && audio.isNotBlank()) {
                  episodes.add(PodcastEpisode(id.ifBlank { audio }, title, audio, description, published, duration))
                }
                inItem = false
              }
            }
          }
          event = parser.next()
        }
        require(feedTitle.isNotBlank()) { "Not a supported RSS podcast feed" }
        PodcastFeed(feedUrl, feedTitle, feedDescription, feedImage, episodes)
      }
    } finally {
      connection.disconnect()
    }
  }
}
