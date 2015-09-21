/**
 * Created by Pradheep on 9/9/15.
 */
package com.aol;

public class Stopwatch {
    private final long start;

    public Stopwatch() {
        start = System.currentTimeMillis();
    }

    // return time (in milliseconds) since this object was created
    public double elapsedTime() {
        long now = System.currentTimeMillis();
        return now - start;
    }
}
