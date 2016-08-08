package com.e;

import java.util.*;
import java.util.stream.Stream;


/**
 * Created by Mikael André on 2016-08-02.
 *
 * An object of this class is created for each word in the index of the search engine.
 *
 * The object contains a HashMap holding all documents in witch the current word occurs as the key, and the number of
 * occurrences in that document as the value. It also holds the total number of occurrences oc the word in all indexed
 * files. This may be used when normalizing the search result.
 *
 * The objec does not contain the actual word it represents, since each object is already mapped as the value to that
 * specific ford in the index of the search engine.
 */

public class IndexedWord {


    private HashMap<String,Integer> occurrences;
    private int totalOccurrences;

    /**
     * Since an object is only created the first time a word occurs in any document, the constructor adds this words
     * first occurence to the list of documents, with the value 1.
     * @param document
     */
    public IndexedWord(String document) {
        this.occurrences = new HashMap<>();
        this.totalOccurrences = 1;
        occurrences.put(document,1);
    }

    //******************GETTERS*****************/
    public HashMap<String, Integer> getOccurrences() {
        return occurrences;
    }

    public int getTotalOccurrences() {
        return totalOccurrences;
    }
    //*******************************************/


    /**
     * This method is called when an indexed word appears anew in a document where it have already been found.
     * @param document
     */
    public void incrementOccurrence(String document) {

        occurrences.put(document, occurrences.get(document)+1);
        totalOccurrences++;
    }

    /**
     * This method is called the first time an already indexed word is found in a new document.
     * @param document
     */
    public void addOccurrence(String document) {

        occurrences.put(document,1);
        totalOccurrences++;
    }


    /**
     *
     * @return A LinkedHashMap() of the documents containing the search term, sorted according to the term frequency
     * of the search term in each document.
     *
     * To sort the list, the Map of documents is converted to a Stream, and sorted into a LinkedHashMap, in descending
     * order.
     *
     */
    public Map<String, Integer> getDocumentList(){

        Map<String, Integer> result = new LinkedHashMap<>();
        Stream<Map.Entry<String, Integer>> stream = occurrences.entrySet().stream();

        stream.sorted( (c1, c2) -> c2.getValue().compareTo(c1.getValue()) )
                .forEachOrdered( e -> result.put(e.getKey(), e.getValue()) );

        return result;

    }

}
