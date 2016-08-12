package com.zombiemike;
import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collector;
import java.util.stream.Collectors;
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
    private HashMap<String, LinkedHashMap<String,Double>> cache;

    private Path dir;
    private int docCount;
    private int wordCount;

    public SearchEngine(Path dir) throws IOException{
        this.index =new HashMap<>();
        this.documentsLength =new HashMap<>();
        this.dir = dir;
        this.cache = new LinkedHashMap<>();
        double start = System.currentTimeMillis();
        indexFiles();
        calculateTfIdf();
        LOG.info(wordCount+" words in "+docCount+" documents indexed in "+ (System.currentTimeMillis()-start)+"ms.");

    }

    public HashMap<String, LinkedHashMap<String, Double>> getCache() {

        return cache;
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
     * This method is used to search for documents containing the words in the search phrase
     *
     * The result of a single word is fetched directly.
     *
     * A phrase of multiple words are first sorted, and then looked for in a cache memory
     * The reason to sort the result is: since the phrase "bar foo" will generate the same result
     * as "foo bar", only one of them need to be stored in the cache.
     *
     * If a phrase is not found in the cache, a new result is produced, stored, and presented to the user.
     *
     * @param query a string containing n words to search for in the corpus
     * @return HIT if the phrase was in the cache, or just a single word, MISS otherwise
     */
    public String search(String query) {

        System.out.println(query);
        double stop=0;
        double start = System.nanoTime();
        String returnString;
        ArrayList<String> queryList = new ArrayList<>();
        StringBuilder cacheInputBuilder = new StringBuilder("");

        queryList.addAll(Arrays.asList(query.toLowerCase().split("\\W+")));

        if(queryList.size()==1){
            stop = System.nanoTime();
            printSearchResult(query, index.get(query).getTfIdfList());

        }else{

            Stream<String> stream = queryList.stream();
            stream.sorted((s1,s2)->s1.compareTo(s2)).forEachOrdered(s->cacheInputBuilder.append(s+" "));
            String cacheInput = cacheInputBuilder.toString().trim();

            if (cache.containsKey(cacheInput)){
                returnString = "HIT";
                stop = System.nanoTime();
                printSearchResult(query, cache.get(cacheInput));

            }else {
                returnString="MISS";


                LinkedHashMap mergedResult = generateResult(queryList);
                stop = System.nanoTime();
                printSearchResult(query,mergedResult);

                cache.put(cacheInput,mergedResult );

            }

            System.out.println("Search time: "+(stop-start)/1000000+"ms");

            return returnString;
        }
        System.out.println("Search time: "+(stop-start)/1000000+"ms");
        return "HIT";
    }


    /**
     * This method is a stub for the recursive method mergeLists(). It collects the result list for each term in the
     * search phrase, and put them in a list that is used as an argument for mergeLists();
     *
     * @param queryList
     * @return null If any of the terms are not in the corpus at all, otherwise a list of all
     * documents containing all terms in the search phrase, sorted according to TF-IDF
     */
    private LinkedHashMap<String,Double> generateResult(ArrayList<String> queryList) {

        ArrayList<LinkedHashMap<String,Double>> resultList = new ArrayList<>();
        queryList.forEach(s->{if(index.containsKey(s)) resultList.add(index.get(s).getTfIdfList()); else resultList.add(null);});

        if(resultList.contains(null)) return null;
        else return mergeLists(resultList);

    }

    /**
     * Recursive method to merge the result of each term in a search phrase to a single list, containing only
     * the relevant documents, sorted by the combined TF-IDF value from each term/document relation.
     * @param resultList a list containing each result from every term in the search phrase
     * @return a list containing the mintersection of all th input lists, with added values
     */
    private LinkedHashMap<String,Double> mergeLists(ArrayList<LinkedHashMap<String,Double>> resultList) {

        if(resultList.size()==1) {
            LinkedHashMap<String,Double> result = new LinkedHashMap<>();
            resultList.get(0).entrySet().stream()
            .sorted((c1,c2)-> c2.getValue().compareTo(c1.getValue()))
                    .forEachOrdered(entry->result.put(entry.getKey(),entry.getValue()));

            return result;
        }else{

            LinkedHashMap<String,Double> temp = new LinkedHashMap<>();
            Stream<Map.Entry<String,Double>> stream = resultList.get(0).entrySet().stream();
            stream.filter(entry -> resultList.get(1).containsKey(entry.getKey()))
                    .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue() + resultList.get(1).get(entry.getKey()))).forEach((k,v)->temp.put(k,v));

            resultList.remove(0);
            resultList.remove(0);
            resultList.add(temp);
        }

        return  mergeLists(resultList);
    }

    /**
     * A method to print the result from the latest search query
     * @param query the current search quesry
     * @param result a map containing the search result
     */
    private void printSearchResult(String query, Map<String,Double> result){


        if(result != null ) {
            System.out.println("Search for: " + query + " resulted in the following list of documents: ");

            result.entrySet().forEach(s->System.out.format("%-15s"+" TF-IDF Value: "+"%5.6f%n", s.getKey(),s.getValue()));


        }else {
            System.out.println("The query " + query + " does not match any documents");
        }

    }


}
