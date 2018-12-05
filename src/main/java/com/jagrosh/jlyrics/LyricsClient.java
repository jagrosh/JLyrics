/*
 * Copyright 2018 John Grosh (john.a.grosh@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jlyrics;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class LyricsClient
{
    private final Config config = ConfigFactory.load();
    private final HashMap<String, Lyrics> cache = new HashMap<>();
    private final OutputSettings noPrettyPrint = new OutputSettings().prettyPrint(false);
    private final Whitelist newlineWhitelist = Whitelist.none().addTags("br", "p");
    private final Executor executor;
    private final String defaultSource, userAgent;
    private final int timeout;
    
    /**
     * Constructs a new {@link LyricsClient} using all defaults
     */
    public LyricsClient()
    {
        this(null, null);
    }
    
    /**
     * Constructs a new {@link LyricsClient}, specifying the default source
     * for lyrics
     * 
     * @param defaultSource the default source for lyrics
     */
    public LyricsClient(String defaultSource)
    {
        this(defaultSource, null);
    }
    
    /**
     * Constructs a new {@link LyricsClient}, specifying an {@link Executor}
     * to be used for making {@link CompletableFuture}s
     * 
     * @param executor the executor to use internally
     */
    public LyricsClient(Executor executor)
    {
        this(null, executor);
    }
    
    /**
     * Constructs a new {@link LyricsClient}, specifying the default source
     * for lyrics as well as an {@link Executor} to be used for making 
     * {@link CompletableFuture}s
     * 
     * @param defaultSource the default source for lyrics
     * @param executor the executor to use internally
     */
    public LyricsClient(String defaultSource, Executor executor)
    {
        this.defaultSource = defaultSource == null ? config.getString("lyrics.default") : defaultSource;
        this.userAgent = config.getString("lyrics.user-agent");
        this.timeout = config.getInt("lyrics.timeout");
        this.executor = executor == null ? Executors.newCachedThreadPool() : executor;
    }
    
    /**
     * Gets the lyrics for the provided search from the default source. To get lyrics
     * asynchronously, call {@link CompletableFuture#thenAccept(java.util.function.Consumer)}.
     * To block and return lyrics, use {@link CompletableFuture#get()}.
     * 
     * @param search the song info to search for
     * @return a {@link CompletableFuture} to access the lyrics. The Lyrics object may be null if no lyrics were found.
     */
    public CompletableFuture<Lyrics> getLyrics(String search)
    {
        return getLyrics(search, defaultSource);
    }
    
    /**
     * Gets the lyrics for the provided search from the provided source. To get lyrics
     * asynchronously, call {@link CompletableFuture#thenAccept(java.util.function.Consumer)}.
     * To block and return lyrics, use {@link CompletableFuture#get()}.
     * 
     * @param search the song info to search for
     * @param source the source to use (must be defined in config)
     * @return a {@link CompletableFuture} to access the lyrics. The Lyrics object may be null if no lyrics were found.
     */
    public CompletableFuture<Lyrics> getLyrics(String search, String source)
    {
        String cacheKey = source + "||" + search;
        if(cache.containsKey(cacheKey))
            return CompletableFuture.completedFuture(cache.get(cacheKey));
        try
        {
            String searchUrl = String.format(config.getString("lyrics." + source + ".search.url"), search);
            boolean jsonSearch = config.getBoolean("lyrics." + source + ".search.json");
            String select = config.getString("lyrics." + source + ".search.select");
            String titleSelector = config.getString("lyrics." + source + ".parse.title");
            String authorSelector = config.getString("lyrics." + source + ".parse.author");
            String contentSelector = config.getString("lyrics." + source + ".parse.content");
            return CompletableFuture.supplyAsync(() -> 
            {
                try
                {
                    Document doc;
                    Connection connection = Jsoup.connect(searchUrl).userAgent(userAgent).timeout(timeout);
                    if(jsonSearch)
                    {
                        String body = connection.ignoreContentType(true).execute().body();
                        JSONObject json = new JSONObject(body);
                        doc = Jsoup.parse(XML.toString(json));
                    }
                    else
                        doc = connection.get();
                    
                    Element urlElement = doc.selectFirst(select);
                    String url;
                    if(jsonSearch)
                        url = urlElement.text();
                    else
                        url = urlElement.attr("abs:href");
                    if(url==null || url.isEmpty())
                        return null;
                    doc = Jsoup.connect(url).userAgent(userAgent).timeout(timeout).get();
                    Lyrics lyrics = new Lyrics(doc.selectFirst(titleSelector).ownText(), 
                            doc.selectFirst(authorSelector).ownText(), 
                            cleanWithNewlines(doc.selectFirst(contentSelector)),
                            url,
                            source);
                    cache.put(cacheKey, lyrics);
                    return lyrics;
                }
                catch(IOException | NullPointerException | JSONException ex)
                {
                    return null;
                }
            }, executor);
        }
        catch(ConfigException ex)
        {
            throw new IllegalArgumentException(String.format("Source '%s' does not exist or is not configured correctly", source));
        }
        catch(Exception ignored)
        {
            return null;
        }
    }
    
    private String cleanWithNewlines(Element element)
    {
        return Jsoup.clean(Jsoup.clean(element.html(), newlineWhitelist), "", Whitelist.none(), noPrettyPrint);
    }
}
