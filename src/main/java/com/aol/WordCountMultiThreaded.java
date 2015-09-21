/**
 * Created by Pradheep on 9/9/15.
 */

package com.aol;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WordCountMultiThreaded {

    private static ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<String, Integer>();
    private static int TOP_WORDS = 10;

    /**
     * Main
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        Scanner in = new Scanner(System.in);
        System.out.println("Enter 5 URLs spearated by comma");

        String input = in.nextLine();
        String[] URLs = input.split(",");
        Stopwatch endtoend = new Stopwatch();
        if (URLs.length != 5) {
            System.out.println("Invalid number of URLs");
            System.exit(0);
        }

        // Create an HttpClient with the ThreadSafeClientConnManager.
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(5);

        CloseableHttpClient httpclient = HttpClients.custom()
                .setConnectionManager(cm)
                .build();

        try {
            // create a thread for each URL
            GetThread[] threads = new GetThread[URLs.length];
            for (int i = 0; i < threads.length; i++) {
                HttpGet httpget = new HttpGet(URLs[i]);
                threads[i] = new GetThread(httpclient, httpget, i + 1);
            }

            // start the threads
            for (int j = 0; j < threads.length; j++) {
                threads[j].start();
            }

            // join the threads
            for (int j = 0; j < threads.length; j++) {
                threads[j].join();
            }

        } finally {
            httpclient.close();
        }
        sortByValue(map);
        System.out.println("End to end Elapsed time:" + endtoend.elapsedTime() + " milliseconds");
    }

    /**
     * Sort the HashMap entries in ArrayList using Collections Sort and Comparator
     * @param map
     */
    private static void sortByValue(ConcurrentHashMap<String, Integer> map) {
        ArrayList<Map.Entry<String, Integer>> entries =
                new ArrayList<Map.Entry<String, Integer>>(map.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<String, Integer>>()

                {
                    //sort by value in descending order, if values are equal sort by key
                    public int compare(Map.Entry<String, Integer> entry1, Map.Entry<String, Integer> entry2) {
                        return (entry1.getValue().equals(entry2.getValue())) ? entry1.getKey().compareTo(entry2.getKey()) : entry2.getValue() - entry1.getValue();
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

    //thread action
    static class GetThread extends Thread {

        private final CloseableHttpClient httpClient;
        private final HttpContext context;
        private final HttpGet httpget;
        private final int id;

        public GetThread(CloseableHttpClient httpClient, HttpGet httpget, int id) {
            this.httpClient = httpClient;
            this.context = new BasicHttpContext();
            this.httpget = httpget;
            this.id = id;
        }

        /**
         * Executes the GetMethod
         */
        @Override
        public void run() {
            try {
                System.out.println(id + " - about to get something from " + httpget.getURI());
                CloseableHttpResponse response = httpClient.execute(httpget, context);
                try {
                    System.out.println(id + " - get executed");
                    // get the response body as an array of bytes
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        BufferedReader rd = new BufferedReader(
                                new InputStreamReader(entity.getContent()));
                        StringBuffer result = new StringBuffer();
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
                }
            } catch (Exception e) {
                System.out.println(id + " - error: " + e);
            }
        }

    }

    /**
     * Inserts the words and its frequency from the array to Concurrent HashMap
     *
     * @param arr
     */
    private static synchronized void insertWords(String[] arr) {
        for (String word : arr) {
            if (map.containsKey(word))
                map.put(word, map.get(word) + 1);
            else
                map.put(word, 1);


            /*Integer value = map.putIfAbsent(word,1);
            if(value != null){
                map.put(word,value+1);
            }*/
        }
    }
}
