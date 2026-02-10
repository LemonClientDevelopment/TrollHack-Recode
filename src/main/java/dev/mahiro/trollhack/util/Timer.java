package dev.mahiro.trollhack.util;

/**
 * Simple timer utility
 */
public class Timer {
    private long time = -1;
    
    /**
     * Check if timer has passed
     */
    public boolean passed(long ms) {
        return System.currentTimeMillis() - time >= ms;
    }
    
    /**
     * Check if timer has passed (alias)
     */
    public boolean passedMs(long ms) {
        return passed(ms);
    }
    
    /**
     * Reset timer
     */
    public void reset() {
        time = System.currentTimeMillis();
    }
    
    /**
     * Get elapsed time
     */
    public long getElapsed() {
        return System.currentTimeMillis() - time;
    }
}
