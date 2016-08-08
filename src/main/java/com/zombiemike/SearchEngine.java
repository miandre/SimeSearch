package com.zombiemike;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by Micke on 2016-08-02.
 *
 * The SearchEngine object is the main part of the search engine.
 * The constructor first creates the index that is used in the actual search.
 * The index is in the form of a Hash map with the searchable words as keys,
 * and a {@Link IndexedWord} object as the value.
 *
 * Then the {@Link indexFiles()} method is called to index the files in the specified folder.
 * Finally, the search engine is implemented as an infinite loop where the user can search for single words.
 *
 */
public class SearchEngine {

    private HashMap<String, IndexedWord> index;
    private Path dir;

    public SearchEngine(Path dir) throws IOException {
        this.index =new HashMap<>();
        this.dir = dir;
        indexFiles();
        search();
    }

    /**
     * This method is used to index the files in the defined folders, in the form of an inverted index, where
     * the searchable words are mapped to the documents where they can be found.
     *
     * The method extracts each line from a document as a String and then splits the string into words, ignoring
     * all "non words" (i.e. whitespace characters, punctuations etc.)
     *
     * Each word is then indexed using the {@Link handleWord() method}.
     *
     * @throws IOException
     */
    private void indexFiles() throws IOException {
        int docCount = 0;
        int wordCount = 0;
        double start = System.currentTimeMillis();


        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.{txt}")) {

            //go through all documents
            for (Path entry: stream) {
                List<String> allLines = Files.readAllLines(entry, Charset.defaultCharset());
                String documentName = entry.toString().split("\\W+")[1];
                docCount++;
                //Go through all lines in each document
                for (String s:allLines) {
                    String[] words = s.trim().split("\\W+");

                    //Go through each word of a non empty line and index the word and document occurrence
                    if(words.length>0) {
                        for (int i = 0; i<words.length; i++) {
                            handleWord(words[i].toLowerCase(), documentName);
                            wordCount++;
                        }
                    }
                }
            }

        } catch (DirectoryIteratorException ex) {
            // I/O error encountered during the iteration, the cause is an IOException
            throw ex.getCause();
        }

        System.out.println(wordCount+" words in "+docCount+" documents indexed in "+ (System.currentTimeMillis()-start)+"ms.");
    }

    /**
     * This method is used to handle each word that are to be indexed.
     *
     * If the word has not occurred before in any document, the word is added as a key to the index,
     * and an IndexedWord object is created as the corresponding value.
     *
     * If the word has already occurred in the same document, the frequency of the word in the current document
     * is incremented.
     *
     * If the word has occured before, but not in the current document, the document is mapped to the word.
     *
     * @param word
     * @param documentName
     */
    private void handleWord(String word, String documentName) {

        //Add word to index
        if (!index.containsKey(word)) {
            index.put(word, new IndexedWord(documentName));


            //increment the number off occurrences for the active document
        }else if (index.get(word).getOccurrences().get(documentName)!=null) {
            index.get(word).incrementOccurrence(documentName);


            //Add a new document to the  word
        }else{
            index.get(word).addOccurrence(documentName);
        }
    }


    /**
     * This method is the accual search methot that allows the user to search for a single word, and prints
     * aa list of the documents where the word occurs, starting with the document with the most occurrences of the
     * word.
     *
     * If the word is not present in any document, that fact is presented to the user. =)
     */
    private void search() {
        while(true){
            System.out.println("\nEnter a word: ");

            Scanner input = new Scanner(System.in);

            //Wait for user input
            String phrase = input.next().trim();

            double stop=0;
            double start = System.currentTimeMillis();
            if(index.get(phrase)!= null ) {
                Set<String> result = index.get(phrase).getDocumentList().keySet();
                stop = System.currentTimeMillis();
                System.out.println("The word " + phrase + " can be found in the following documents: ");
                for (String s: result) {
                    System.out.println(s);
                }

            }else {
                System.out.println("The word " + phrase + " can not be found in any document");
                stop = System.currentTimeMillis();
            }
            System.out.println("Search time: "+(stop-start)+"ms");
        }

    }

}
