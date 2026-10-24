# Streamer player interaction specification

Streamer keeps EchoMusic's existing single shared music playback session and `BottomSheetPlayer`. Do not replace it with a separate activity or a second ExoPlayer.

## Required behavior
- Collapsed: docked mini-player immediately above the bottom navigation, displaying square artwork, song/podcast title, creator, previous/play-pause/next, and a playback-progress indicator.
- Swipe up or tap mini-player: expand the same player sheet into full-screen now-playing mode.
- Swipe down or use the top down-chevron: collapse to the mini-player without interrupting audio.
- Expanded: prominent square cover artwork, title and artist/creator, seeking, shuffle, previous/play-pause/next and repeat; bottom tabs for Up Next, Lyrics, Related (podcasts may use Episodes / Details instead).
- Preserve playback while browsing and switching Home / Search / Podcasts / Library.
- Use an artwork-derived muted background gradient and ensure contrast of controls.

## Existing foundations in upstream
- `ui/component/BottomSheet.kt` already implements a gesture-driven sheet with collapsed/expanded anchors.
- `ui/player/Player.kt` already contains `BottomSheetPlayer` and `MiniPlayer` collapsedContent.
- `MainActivity.kt` hosts `rememberBottomSheetState`.
- Podcast screen currently owns a separate player; it **must be integrated with the shared Media3 playback/session** before this is production-ready.

## Acceptance tests
1. Music keeps playing during expand/collapse transitions and screen navigation.
2. Expanded player has identical queue/media item to collapsed mini-player.
3. Android back gesture collapses expanded player before navigating away.
4. Screen rotation and background foreground transitions do not reset playback.
5. Podcast episodes and music share audio focus and media notification (no simultaneous players).
6. No clipped mini-player controls or navigation overlap in gesture navigation mode.
