package com.zombiemike;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.io.IOException;
import java.nio.file.Paths;

//aquith          TF-IDF Value: 0,000257
/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {

        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testCacheInput() throws IOException {

        SearchEngine se = new SearchEngine(Paths.get("c:\\users\\micke\\ideaprojects\\Simesearch\\files"));
        se.search("fox and frog");

        assertTrue( se.getCache().containsKey("and fox frog"));
    }

    public void testCacheMiss() throws IOException {

        SearchEngine se = new SearchEngine(Paths.get("c:\\users\\micke\\ideaprojects\\Simesearch\\files"));

        assertTrue( se.search("fox and frog").equals("MISS"));
    }

    public void testCacheHit() throws IOException {

        SearchEngine se = new SearchEngine(Paths.get("c:\\users\\micke\\ideaprojects\\Simesearch\\files"));
        se.search("fox and frog");
        assertTrue( se.search("fox and frog").equals("HIT"));
    }

    public void testSingleWordCacheInput() throws IOException {

        SearchEngine se = new SearchEngine(Paths.get("c:\\users\\micke\\ideaprojects\\Simesearch\\files"));
        se.search("frog");

        assertTrue( se.search("frog").equals("HIT"));
    }

    public void testSingleWordCacheHit() throws IOException {

        SearchEngine se = new SearchEngine(Paths.get("c:\\users\\micke\\ideaprojects\\Simesearch\\files"));
        se.search("frog");

        assertTrue(  se.search("frog").equals("HIT"));

    }






}
