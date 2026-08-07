package org.openjfx;

public class ThreadSafeUpdateQueueCounter {
    private int counter;

    public ThreadSafeUpdateQueueCounter() {
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
