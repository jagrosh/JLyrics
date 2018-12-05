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
import com.typesafe.config.ConfigFactory;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class LyricsTest
{
    @Test
    public void configurationTest()
    {
        Config config = ConfigFactory.load();
        assertNotNull(config);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void invalidSource() throws InterruptedException, ExecutionException
    {
        LyricsClient client = new LyricsClient();
        
        Lyrics ignored = client.getLyrics("smooth criminal", "Source not in config anywhere").get();
    }
    
    @Test
    public void azlyricsTest() throws InterruptedException, ExecutionException
    {
        LyricsClient client = new LyricsClient("A-Z Lyrics");
        
        Lyrics lyrics = client.getLyrics("ellie goulding lights").get();
        assertNotNull(lyrics);
        assertNotNull(lyrics.getTitle());
        assertNotNull(lyrics.getAuthor());
        assertNotNull(lyrics.getContent());
        assertNotNull(lyrics.getSource());
        
        lyrics = client.getLyrics("jklsjdgv89y32hr9").get();
        assertNull(lyrics);
        
        lyrics = client.getLyrics("smooth criminal").get();
        assertNotNull(lyrics);
        assertNotNull(lyrics.getTitle());
        assertNotNull(lyrics.getAuthor());
        assertNotNull(lyrics.getContent());
        assertNotNull(lyrics.getSource());
    }
    
    @Test
    public void geniusTest() throws InterruptedException, ExecutionException
    {
        LyricsClient client = new LyricsClient("Genius");
        
        Lyrics lyrics = client.getLyrics("ellie goulding lights").get();
        assertNotNull(lyrics);
        assertNotNull(lyrics.getTitle());
        assertNotNull(lyrics.getAuthor());
        assertNotNull(lyrics.getContent());
        assertNotNull(lyrics.getSource());
        
        lyrics = client.getLyrics("jklsjdgv89y32hr9").get();
        assertNull(lyrics);
        
        lyrics = client.getLyrics("smooth criminal").get();
        assertNotNull(lyrics);
        assertNotNull(lyrics.getTitle());
        assertNotNull(lyrics.getAuthor());
        assertNotNull(lyrics.getContent());
        assertNotNull(lyrics.getSource());
    }
    
    @Test
    public void musicmatchTest() throws InterruptedException, ExecutionException
    {
        LyricsClient client = new LyricsClient("MusicMatch");
        
        Lyrics lyrics = client.getLyrics("ellie goulding lights").get();
        assertNotNull(lyrics);
        assertNotNull(lyrics.getTitle());
        assertNotNull(lyrics.getAuthor());
        assertNotNull(lyrics.getContent());
        assertNotNull(lyrics.getSource());
        
        lyrics = client.getLyrics("jklsjdgv89y32hr9").get();
        assertNull(lyrics);
        
        lyrics = client.getLyrics("smooth criminal").get();
        assertNotNull(lyrics);
        assertNotNull(lyrics.getTitle());
        assertNotNull(lyrics.getAuthor());
        assertNotNull(lyrics.getContent());
        assertNotNull(lyrics.getSource());
    }
}
