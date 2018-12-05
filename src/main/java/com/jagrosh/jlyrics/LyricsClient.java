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
        this(null, null);
    }
    
    public LyricsClient(String defaultSource)
    {
        this(defaultSource, null);
    }
    
    public LyricsClient(Executor executor)
    {
        this(null, executor);
    }
    
    public LyricsClient(String defaultSource, Executor executor)
    {
        this.defaultSource = defaultSource == null ? config.getString("lyrics.default") : defaultSource;
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
                    if(jsonSearch)
                    {
                        String body = Jsoup.connect(searchUrl).timeout(timeout).ignoreContentType(true).execute().body();
                        JSONObject json = new JSONObject(body);
                        doc = Jsoup.parse(XML.toString(json));
                    }
                    else
                        doc = Jsoup.connect(searchUrl).userAgent(userAgent).timeout(timeout).get();
                    Element urlElement = doc.selectFirst(select);
                    String url;
                    if(jsonSearch)
                        url = urlElement.text();
                    else
                        url = urlElement.attr("abs:href");
                    if(url==null || url.isEmpty())
                        return null;
                    doc = Jsoup.connect(url).userAgent(userAgent).timeout(timeout).get();
                    Lyrics lyrics = new Lyrics(doc.selectFirst(titleSelector).text(), 
                            doc.selectFirst(authorSelector).text(), 
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
    
    public class Lyrics
    {
        private final String title, author, content, url, source;
        
        private Lyrics(String title, String author, String content, String url, String source)
        {
            this.title = title;
            this.author = author;
            this.content = content;
            this.url = url;
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
        
        public String getURL()
        {
            return url;
        }
        
        public String getSource()
        {
            return source;
        }
    }
}
