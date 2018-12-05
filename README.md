# JLyrics
🎼 Expandable lyrics-scraping API for Java

## Example
```java
LyricsClient client = new LyricsClient();
Lyrics lyrics = client.getLyrics("smooth criminal").get()
System.out.println(lyrics.getContent()); // As he came into the window ...
```

## Features
* Async or blocking (uses [CompletableFuture](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html))
* Built-in caching to prevent duplicate scrapes
* Highly customizable (see reference.conf and [lightbend/config](https://github.com/lightbend/config))
* Easily expandable to many lyrics sites

## Included Lyrics Sites
* A-Z Lyrics
* [your contribution here]
