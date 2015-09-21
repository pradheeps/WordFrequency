/**
 * Created by Pradheep on 9/8/15.
 */
package com.aol;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.*;

public class WordCount {

    //HashMap to store the words and its frequency
    private static HashMap<String, Integer> map = new HashMap<String, Integer>();

    //variable used to get top 10 high frequency words
    private static int TOP_WORDS = 10;

    /**
     * Pass the url one by one to a method to fetch the content
     *
     * @param urls
     */
    private static void getAllPages(String[] urls) {
        for (String url : urls) {
            try {
                Stopwatch timer = new Stopwatch();
                //call method to get page contents from URLS
                getPageText(url);
                System.out.println("Time to Process URL: " + url + " " + timer.elapsedTime() + " milliseconds");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Gets the page contents from the URL and call HashMap insert method
     *
     * @param url
     * @throws Exception
     */
    private static void getPageText(String url) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet(url);
        CloseableHttpResponse response = httpclient.execute(httpget);
        try {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                BufferedReader rd = new BufferedReader(
                        new InputStreamReader(entity.getContent()));

                StringBuilder result = new StringBuilder();
                String line = "";
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }

                //Using Jsoup to extract text from html
                Document doc = Jsoup.parse(result.toString());
                String str = doc.text().replaceAll("\\P{Alpha}-", " ").toLowerCase();
                String[] arr = str.split("[^a-z-]+");
                //System.out.println(Arrays.toString(arr));

                //Call method to insert into HashMap
                insertWords(arr);
            }
        } finally {
            response.close();
            httpclient.close();
        }
    }

    /**
     * Inserts the words and its frequency from the array to Concurrent HashMap
     *
     * @param arr
     */
    private static void insertWords(String[] arr) {
        for (String word : arr) {
            if (map.containsKey(word))
                map.put(word, map.get(word) + 1);
            else
                map.put(word, 1);
        }
    }

    /**
     * Sort the HashMap entries in ArrayList using Collections Sort and Comparator
     *
     * @param map
     */
    private static void sortByValue(HashMap<String, Integer> map) {
        ArrayList<Map.Entry<String, Integer>> entries =
                new ArrayList<Map.Entry<String, Integer>>(map.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<String, Integer>>()
                {
                    public int compare(Map.Entry<String, Integer> entry1, Map.Entry<String, Integer> entry2) {
                        return (entry1.getValue().equals(entry2.getValue()) ? entry1.getKey().compareTo(entry2.getKey()) : entry2.getValue() - entry1.getValue());
                    }
                }
        );

        //Print the top 10 words to console
        for (Map.Entry<String, Integer> entry : entries) {
            if (--TOP_WORDS < 0)
                break;
            System.out.printf("%s: %d\n", entry.getKey(), entry.getValue());
        }
    }


    /**
     * @param args
     */
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("Enter 5 URLs separated by comma");
        String input = in.nextLine();
        String[] URLs = input.split(",");
        Stopwatch endtoend = new Stopwatch();
        if (URLs.length == 5)
            getAllPages(URLs);
        else
            System.out.println("Invalid number of URLs");

        //Sort the HashMap based on Values to get the top 10 words
        sortByValue(map);
        System.out.println("End to end elapsed time:" + endtoend.elapsedTime() + " milliseconds");
    }
}
