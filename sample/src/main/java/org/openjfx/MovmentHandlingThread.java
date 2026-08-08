package org.openjfx;

import javafx.application.Platform;

public class MovmentHandlingThread extends Thread{  
    private Runnable update;
    private ThreadSafeUpdateQueueCounter counter;
    private boolean stop;
    public MovmentHandlingThread(Runnable update,ThreadSafeUpdateQueueCounter counter){
        this.update=update;
        this.counter=counter;
        stop=false;
    }
    @Override
    public void run(){
        while (!stop){
            long startTime=System.currentTimeMillis();
            if (counter.getCounter()<2){
                Platform.runLater(update);
                counter.increaseCounter();
            }
            long sleeptime= 10-(System.currentTimeMillis()-startTime);
            try{
                if (sleeptime>0){
                    Thread.sleep(sleeptime);
                }
            }catch(InterruptedException e){
                e.printStackTrace();
            }  
        }
    }

    public void Stop(){
        stop=true;
    }
}
