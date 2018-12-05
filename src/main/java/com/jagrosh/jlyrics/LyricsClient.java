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
    
    public LyricsClient()
    {
        this(null);
    }
    
    public LyricsClient(Executor executor)
    {
        this.defaultSource = config.getString("lyrics.default");
        this.userAgent = config.getString("lyrics.user-agent");
        this.timeout = config.getInt("lyrics.timeout");
        this.executor = executor == null ? Executors.newCachedThreadPool() : executor;
    }
    
    public CompletableFuture<Lyrics> getLyrics(String search)
    {
        return getLyrics(search, defaultSource);
    }
    
    public CompletableFuture<Lyrics> getLyrics(String search, String source)
    {
        String cacheKey = source + "||" + search;
        if(cache.containsKey(cacheKey))
            return CompletableFuture.completedFuture(cache.get(cacheKey));
        try
        {
            String searchUrl = String.format(config.getString("lyrics." + source + ".search"), search);
            String select = config.getString("lyrics." + source + ".select");
            String titleSelector = config.getString("lyrics." + source + ".title");
            String authorSelector = config.getString("lyrics." + source + ".author");
            String contentSelector = config.getString("lyrics." + source + ".content");
            return CompletableFuture.supplyAsync(() -> 
            {
                try
                {
                    Document doc = Jsoup.connect(searchUrl).userAgent(userAgent).timeout(timeout).get();
                    String url = doc.selectFirst(select).attr("abs:href");
                    if(url==null)
                        return null;
                    doc = Jsoup.connect(url).userAgent(userAgent).timeout(timeout).get();
                    Lyrics lyrics = new Lyrics(doc.selectFirst(titleSelector).text(), 
                            doc.selectFirst(authorSelector).text(), 
                            cleanWithNewlines(doc.selectFirst(contentSelector)), 
                            source);
                    cache.put(cacheKey, lyrics);
                    return lyrics;
                }
                catch(IOException | NullPointerException ex)
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
    
    public class Lyrics
    {
        private final String title, author, content, source;
        
        private Lyrics(String title, String author, String content, String source)
        {
            this.title = title;
            this.author = author;
            this.content = content;
            this.source = source;
        }
        
        public String getTitle()
        {
            return title;
        }
        
        public String getAuthor()
        {
            return author;
        }
        
        public String getContent()
        {
            return content;
        }
        
        public String getSource()
        {
            return source;
        }
    }
}
