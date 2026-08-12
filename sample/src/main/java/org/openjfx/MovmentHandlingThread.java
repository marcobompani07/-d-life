package org.openjfx;

import javafx.application.Platform;

public class MovmentHandlingThread extends Thread{  
    private UpdateGuiRunnableGenerator updateRunnableGenerator;
    private ThreadSafeUpdateMapQueueCounter counter;
    private boolean stop;
    private ThreadSafeCreaturesArray creaturesArray;
    private ThreadSafeFoodArray foodArray;
    public MovmentHandlingThread(UpdateGuiRunnableGenerator updateRunnableGenerator,ThreadSafeUpdateMapQueueCounter counter, ThreadSafeCreaturesArray creaturesArray,ThreadSafeFoodArray foodArray){
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
                        if (creature!= null){
                            outCreature[i]=new Creature(creature);
                        }else{
                            outCreature[i]=null;
                        }
                        creaturesArray.release(i);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Food[] outFoodArray=new Food[foodArray.getLength()];
                for (int i=0;i<outFoodArray.length;i++){
                    try {
                        Food food=foodArray.request(i);
                        if (food!= null){
                            outFoodArray[i]=new Food(food);
                            System.out.println("added food:"+food);
                        }else{
                            outFoodArray[i]=null;
                            System.out.println("not added food:"+food);
                        }
                        foodArray.release(i);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Platform.runLater(updateRunnableGenerator.generate(outCreature,outFoodArray));
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
