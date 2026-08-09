package org.openjfx;

import javafx.application.Platform;

public class MovmentHandlingThread extends Thread{  
    private UpdateGuiRunnableGenerator updateRunnableGenerator;
    private ThreadSafeUpdateMapQueueCounter counter;
    private boolean stop;
    private ThreadSafeCreaturesArray creaturesArray;
    public MovmentHandlingThread(UpdateGuiRunnableGenerator updateRunnableGenerator,ThreadSafeUpdateMapQueueCounter counter, ThreadSafeCreaturesArray creaturesArray){
        this.updateRunnableGenerator=updateRunnableGenerator;
        this.counter=counter;
        this.creaturesArray=creaturesArray;
        stop=false;
    }
    @Override
    public void run(){
        while (!stop){
            long startTime=System.currentTimeMillis();
            if (counter.getCounter()<1){
                counter.increaseCounter();
                Creature[] outCreature=new Creature[creaturesArray.getLength()];
                for (int i=0;i<outCreature.length;i++){
                    try {
                        Creature creature=creaturesArray.request(i);
                        if (creature!= null){
                            outCreature[i]=new Creature(creaturesArray.request(i));
                        }else{
                            outCreature[i]=null;
                        }
                       
                        creaturesArray.release(i);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Platform.runLater(updateRunnableGenerator.generate(outCreature));
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
