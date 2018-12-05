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

/**
 * Lyrics for some song
 * 
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class Lyrics
{
    private final String title, author, content, url, source;

    protected Lyrics(String title, String author, String content, String url, String source)
    {
        this.title = title;
        this.author = author;
        this.content = content;
        this.url = url;
        this.source = source;
    }

    /**
     * The title of the song
     * 
     * @return the title of the song
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * The author/artist of the song
     * 
     * @return the author/artist of the song
     */
    public String getAuthor()
    {
        return author;
    }

    /**
     * The content of the lyrics
     * 
     * @return the lyrics
     */
    public String getContent()
    {
        return content;
    }

    /**
     * The URL the lyrics can be found at
     * 
     * @return the URL the lyrics can be found at
     */
    public String getURL()
    {
        return url;
    }

    /**
     * The source that was used to find these lyrics
     * 
     * @return the source that was used to find these lyrics
     */
    public String getSource()
    {
        return source;
    }
}
