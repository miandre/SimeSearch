package com.zombimike;
import java.io.*;
import java.nio.file.*;

/**
 * Created by Mikael André on 2016-08-02.
 *
 * mSearch is a simple case-insensitive search engine that indexes *,txt files and allow the user to search
 * for words within these files. The search result is a list of the documents including the search term.
 * The list is scorted in descending order with the first result being the document where the
 * search term occurs most frequently
 *
 * @author Mikael André
 */

public class App {

    public static void main(String[] args){

        //Get the path to where the test files are stored.
        Path dir = Paths.get("files");

        /* -----------debug--------------------------
        System.out.println("Working Directory = " +
                dir.toAbsolutePath().toString());
        */


        try {
            SearchEngine mSearch = new SearchEngine(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
