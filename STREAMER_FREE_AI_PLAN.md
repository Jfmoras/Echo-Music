# Streamer: no-key intelligent recommendations and optional local AI

## Available today in upstream source
- `generate/LocalTasteEngine.kt`: recommendations based on playback stats, liked tracks, skips, time of day, and YouTube's related tracks. No user-supplied AI key. **Not a local LLM**.
- `generate/RecommendationEngine.kt`: extends taste scoring with optional external Last.fm metadata.
- `ai/AiPlaylistGenerator.kt`: uses OpenRouter with a user-provided key. Puter branch is a placeholder and must not be advertised as functional.

## Safe development sequence
1. Surface the existing LocalTasteEngine as the default no-key personalized recommendations option; keep it labeled 'Smart recommendations', not 'offline AI'.
2. Preserve standard music functionality and test before merging.
3. For true offline generative AI: ship an optional model downloader and separately packaged GGUF model using vetted llama.cpp Android bindings, with model checksum/license checks, device capability check and an unload option. Never bundle an unverified third-party model or require a remote API key.
4. Transcript summarization only after the transcript is available; otherwise explicitly state it cannot summarize unheard audio.
5. Do not connect podcast screen to a second ExoPlayer. Integrate with the shared playback service separately.

## Acceptance requirements
- No API key required for smart recommendations.
- No false claim that remote YouTube suggestions or online podcast audio work fully offline.
- No AI model download without user consent.
- Any offline model must be usable in airplane mode after download.
