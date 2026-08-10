package org.openjfx;

import javafx.application.Platform;

public class MovmentHandlingThread extends Thread{  
    private UpdateGuiRunnableGenerator updateRunnableGenerator;
    private ThreadSafeUpdateMapQueueCounter counter;
    private boolean stop;
    private ThreadSafeCreaturesArray creaturesArray;
    private Food[] foodArray;
    public MovmentHandlingThread(UpdateGuiRunnableGenerator updateRunnableGenerator,ThreadSafeUpdateMapQueueCounter counter, ThreadSafeCreaturesArray creaturesArray,Food[] foodArray){
        this.updateRunnableGenerator=updateRunnableGenerator;
        this.counter=counter;
        this.creaturesArray=creaturesArray;
        this.foodArray=foodArray;
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
                        creaturesArray.release(i);
                        if (creature!= null){
                            outCreature[i]=new Creature(creature);
                        }else{
                            outCreature[i]=null;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Food[] outFoods=foodArray.clone();
                Platform.runLater(updateRunnableGenerator.generate(outCreature,outFoods));
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
