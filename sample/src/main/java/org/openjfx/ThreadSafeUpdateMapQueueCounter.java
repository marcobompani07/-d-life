package org.openjfx;

public class ThreadSafeUpdateMapQueueCounter {
    private int counter;

    public ThreadSafeUpdateMapQueueCounter() {
        counter=0;
    }

    public synchronized  int getCounter() {
        return counter;
    }
    public synchronized void decreaseCounter(){
        if(counter>0){
            counter--;
        }
    }
    public synchronized void increaseCounter(){
        counter++;
    }
}
