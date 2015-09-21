/**
 * Created by Pradheep on 9/9/15.
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
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class WordCountAsynchronous {

    private static HashMap<String, Integer> map = new HashMap<String, Integer>();
    private static int TOP_WORDS = 10;
    //create a new single thread executor
    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * Pass the url one by one to an asynchronous call to fetch the content
     *
     * @param urls
     */
    private static void getAllPages(String[] urls) {
        for (String url : urls) {
            try {
                Stopwatch timer = new Stopwatch();
                Runnable asynctask = new AsynchronousTask(url);
                Future future = executor.submit(asynctask);
                while (!future.isDone()) {
                    //System.out.println("Task is in progress...");
                    Thread.sleep(10);
                }
                System.out.println("Time to Process URL: " + url + " " + timer.elapsedTime() + " milliseconds");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();
        System.out.println("All Asynchronous tasks are finished!");
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
        HttpEntity entity = response.getEntity();
        InputStreamReader istream = new InputStreamReader(entity.getContent(),"UTF-8");
        try {
            if (entity != null) {
                BufferedReader rd = new BufferedReader(istream);
                StringBuffer result = new StringBuffer();
                String line = "";
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }

                //Using Jsoup to extract text from html
                Document doc = Jsoup.parse(result.toString());
                String str = doc.text().replaceAll("\\P{Alpha}-", " ").toLowerCase();
                String[] arr = str.split("[^a-z-]+");

                //Call method to insert into HashMap
                insertWords(arr);
            }
        } finally {
            response.close();
            httpclient.close();
            istream.close();
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
                        return (entry1.getValue().equals(entry2.getValue())) ? entry1.getKey().compareTo(entry2.getKey()) : entry2.getValue() - entry1.getValue();
                    }
                }
        );
        for (Map.Entry<String, Integer> ent : entries) {
            if (--TOP_WORDS < 0) break;
            System.out.printf("%s: %d\n", ent.getKey(), ent.getValue());
        }
    }


    /**
     * Runnable Class to be executed by the Executor Service
     */
    public static class AsynchronousTask implements Runnable {

        private String url;

        //Constructor
        AsynchronousTask(String myurl) {
            this.url = myurl;
        }

        //Asynchronous Call to get the contents from the URL and insert into HashMap
        public void run() {
            try {
                getPageText(url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Main
     *
     * @param args
     */
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("Enter 5 URLs spearated by comma");
        String input = in.nextLine();
        String[] URLs = input.split(",");
        Stopwatch endtoend = new Stopwatch();
        if (URLs.length == 5) {
            getAllPages(URLs);
        } else
            System.out.println("Invalid number of URLs");

        //Sort the HashMap based on Values to get the top 10 words
        sortByValue(map);
        System.out.println(System.getProperty("file.encoding"));
        System.out.println("End to end Elapsed time:" + endtoend.elapsedTime() + " milliseconds");
    }
}

