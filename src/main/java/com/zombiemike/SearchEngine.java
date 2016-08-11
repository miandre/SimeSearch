package com.zombiemike;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Stream;
import java.util.Scanner;

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

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private HashMap<String, Integer> documentsLength;
        private HashMap<String, IndexedWord> index;
    private Path dir;
    private int docCount;
    private int wordCount;

    public SearchEngine(Path dir) throws IOException{
        this.index =new HashMap<>();
        this.documentsLength =new HashMap<>();
        this.dir = dir;

        double start = System.currentTimeMillis();
        indexFiles();
        calculateTfIdf();
        LOG.info(wordCount+" words in "+docCount+" documents indexed in "+ (System.currentTimeMillis()-start)+"ms.");
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
        docCount = 0;
        wordCount = 0;


        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.{txt}")) {
            LOG.info("Indexing files...\n");
            //go through all documents
            for (Path entry: stream) {
                List<String> allLines = Files.readAllLines(entry, Charset.defaultCharset());
                List<String>  wordsInDoc = new ArrayList<>();
                String documentName = entry.getFileName().toString().split("\\W+")[0];
                docCount++;
                int wordPerDocumentCount = 0;
                //Go through all lines in each document
                for (String s:allLines) {
                    String[] words = s.trim().split("\\W+");

                    //Go through each word of a non empty line and index the word and document occurrence
                    if(words.length>1) {
                        for (int i = 0; i<words.length; i++) {
                            wordsInDoc.add(words[i].toLowerCase());
                            wordCount++;

                        }
                    }
                }

                wordsInDoc.forEach(s->handleWord(s, documentName));
                documentsLength.put(documentName,wordsInDoc.size());

            }
            
            LOG.info("Done indexing...Calculating tf-idf values....\n");


        } catch (DirectoryIteratorException ex) {
            // I/O error encountered during the iteration, the cause is an IOException
            System.out.println("Invalid path, or, folder does not include  any .txt documents");
            throw ex.getCause();
        }

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
     * If the word has occurred before, but not in the current document, the document is mapped to the word.
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
     * This method is used to calculate the TF-IDF value for each word and its assoiated documents
     * The following calculations are used :
     *  TF is calculated as the relative frequency of a word in each documents,
     *  or: "(rf/d)"
     *  where rf = the raw frequency of the term and d is the number of terms in the document
     *
     *  IDF is calculated as the 10-base logarithm of the quote between the total number of documents in the corpus
     *  and the number of documents in which the term appears or:
     *  "Log((N/n(f))"
     *  where N is the number of documents in the corpus and n(f) is the number of documents containing the search term
     *
     *  Hence, the TF-IDF value is calculated as TF*IDF, or: "(rf/d)*Log10(N/n(f))"
     *
     *  The calculated values are then stored back in the IndexedWord object as a sorted list, allowing for fast access
     *  in the search.
     *
     *
     */
    private void calculateTfIdf() {

        HashMap<String,Double> tempTfIdf = new HashMap<>();
        LinkedHashMap<String,Double> tempTfIdfSorted = new LinkedHashMap<>();

        for(Map.Entry<String,IndexedWord> item : index.entrySet() ){
            tempTfIdf.clear();
            tempTfIdfSorted.clear();

            for(Map.Entry<String,Integer> docOccItem : item.getValue().getOccurrences().entrySet()){
                int documentsContainingWord = item.getValue().getOccurrences().entrySet().size();

                Double tfIdf = ((docOccItem.getValue()/(double)documentsLength.get(docOccItem.getKey()))*(Math.log10(docCount/documentsContainingWord)));

                tempTfIdf.put(docOccItem.getKey(),tfIdf);
            }

            Stream<Map.Entry<String,Double>> stream = tempTfIdf.entrySet().stream();
            stream.sorted((c1,c2)-> c2.getValue().compareTo(c1.getValue()))
                    .forEachOrdered(entry->item.getValue().addTfIdf(entry.getKey(),entry.getValue()));


        }

    }


    /**
     * This method is the actual search method that allows the user to search for a single word, and prints
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
            double start = System.nanoTime();


            if(index.get(phrase)!= null ) {

                Map<String,Double> result = index.get(phrase).getTfIdfList();

                stop = System.nanoTime();
                System.out.println("The word " + phrase + " can be found in the following documents: ");

                result.entrySet().forEach(s->System.out.format("%-15s"+" TF-IDF Value: "+"%5.6f%n", s.getKey(),s.getValue()));

            }else {
                System.out.println("The word " + phrase + " can not be found in any document");
                stop = System.nanoTime();
            }
            System.out.println("Search time: "+(stop-start)/1000000+"ms");
        }

    }


}
