package com.zombiemike;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;



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

    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    public static void main(String[] args){


        if(args.length!=0) {
            //Get the path to where the test files are stored.
            Path dir = Paths.get(args[0]);
            LOG.debug("Filepath: "+dir.toAbsolutePath().toString());

            try {
                SearchEngine mSearch = new SearchEngine(dir);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println(("!!! Invalid path or folder does not include any .txt documents !!!").toUpperCase());
            }

        }else{
            System.out.println("HOW TO USE THIS SEARCH ENGINE: \nThis search engine takes a String in the form of a path as input argument (eg. C:\\User\\...\\FilesToSearch)\n" +
                    "In the specified folder, the search engine indexes all *.txt documents and make them searchable\n\n" +
                    "Please provide a path to your file as an input argument and run the application again");
        }



    }
}
