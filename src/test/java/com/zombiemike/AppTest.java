package com.zombiemike;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.nio.file.Path;
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
    public void testCache() throws IOException {

        assertTrue( true );

    }

}
