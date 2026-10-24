package echo.music.iad1tya.podcast

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.launch

/** Standalone first-pass screen; wire into Screens/navigation after UI integration review. */
@Composable
fun PodcastScreen(modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val repository = remember { PodcastRssRepository() }
  val scope = rememberCoroutineScope()
  val player = remember { ExoPlayer.Builder(context).build() }
  DisposableEffect(player) { onDispose { player.release() } }
  var feedUrl by remember { mutableStateOf("") }
  var podcast by remember { mutableStateOf<PodcastFeed?>(null) }
  var loading by remember { mutableStateOf(false) }
  var error by remember { mutableStateOf<String?>(null) }
  var currentEpisode by remember { mutableStateOf<String?>(null) }

  Column(
    modifier = modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Text("Podcasts", style = MaterialTheme.typography.headlineMedium)
    Text("Enter the RSS URL of a podcast to load its episodes. Spotify show page links are not RSS feeds.")
    OutlinedTextField(
      value = feedUrl,
      onValueChange = { feedUrl = it },
      label = { Text("Podcast RSS feed URL") },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true
    )
    Button(
      enabled = !loading && feedUrl.trim().let { it.startsWith("https://") || it.startsWith("http://") },
      onClick = {
        scope.launch {
          loading = true
          error = null
          try { podcast = repository.load(feedUrl.trim()) }
          catch (e: Exception) { error = e.message ?: "Unable to load podcast" }
          finally { loading = false }
        }
      }
    ) { Text("Open feed") }
    if (loading) CircularProgressIndicator()
    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    podcast?.let { feed ->
      Text(feed.title, style = MaterialTheme.typography.titleLarge)
      Text(feed.description, maxLines = 3)
      LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(feed.episodes, key = { it.id }) { episode ->
          Column(Modifier.fillMaxWidth().clickable {
            player.setMediaItem(MediaItem.fromUri(episode.audioUrl))
            player.prepare()
            player.play()
            currentEpisode = episode.id
          }) {
            Text(episode.title, style = MaterialTheme.typography.titleMedium)
            Text(episode.published)
            Text(if (currentEpisode == episode.id) "Playing / tap to restart" else "Tap to play")
          }
        }
      }
    }
  }
}
