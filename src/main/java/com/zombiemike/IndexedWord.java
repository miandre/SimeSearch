package com.zombiemike;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;


/**
 * Created by Mikael André on 2016-08-02.
 *
 * An object of this class is created for each word in the index of the search engine.
 *
 * The object contains a HashMap holding all documents in witch the current word occurs as the key, and the number of
 * occurrences in that document as the value. It also holds the total number of occurrences oc the word in all indexed
 * files. This may be used when normalizing the search result.
 *
 * The object does not contain the actual word it represents, since each object is already mapped as the value to that
 * specific word in the index of the search engine.
 */

public class IndexedWord {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private HashMap<String, Integer> occurrences;
    private LinkedHashMap<String, Double> tfIdfList;

    private int totalOccurrences;


    /**
     * Since an object is only created the first time a word occurs in any document, the constructor adds this words
     * first occurence to the list of documents, with the value 1.
     *
     * @param document
     */
    public IndexedWord(String document) {
        this.occurrences = new HashMap<>();
        this.tfIdfList = new LinkedHashMap<>();
        this.totalOccurrences = 1;
        occurrences.put(document, 1);
    }

    //******************GETTERS N' SETTERS*****************/
    public HashMap<String, Integer> getOccurrences() {
        return occurrences;
    }


    public LinkedHashMap<String,Double> getTfIdfList(){

        return this.tfIdfList;
    }

    public int getTotalOccurrences() {
        return totalOccurrences;
    }

    //*******************************************/


    /**
     * This method is used do build the sorted list of documents containing the current word
     * and their associated tf-idf value
     * @param doc current associated document
     * @param tfIdf tf-idf value of the current word in relation tu current document
     */
    public void addTfIdf(String doc, double tfIdf){

        this.tfIdfList.put(doc,tfIdf);
    }


    /**
     * This method is called when an indexed word appears anew in a document where it have already been found.
     *
     * @param document
     */
    public void incrementOccurrence(String document) {

        occurrences.put(document, occurrences.get(document) + 1);
        totalOccurrences++;
    }


    /**
     * This method is called the first time an already indexed word is found in a new document.
     *
     * @param document
     */
    public void addOccurrence(String document) {

        occurrences.put(document, 1);
        totalOccurrences++;
    }
}

