package org.openjfx;

import javafx.application.Platform;
import javafx.scene.paint.Color;

public class CreatureInfoDisplayThread extends Thread {
    private  boolean stop;
    private UpdateCreatureDisplayRunnableGenerator RunnableGenerator;
    private Runnable updateCreatureCountDisplay,ResetCreatureDisplayRunnable;
    static  int creatureId;
    private ThreadSafeCreaturesArray creaturesArray;
    private boolean resetted;

    public CreatureInfoDisplayThread(UpdateCreatureDisplayRunnableGenerator RunnableGenerator,ThreadSafeCreaturesArray creaturesArray,Runnable updateCreatureCountDisplay,Runnable ResetCreatureDisplayRunnable) {
        stop=false;
        this.RunnableGenerator=RunnableGenerator;
        creatureId=-1;
        this.creaturesArray=creaturesArray;
        this.updateCreatureCountDisplay=updateCreatureCountDisplay;
        this.ResetCreatureDisplayRunnable=ResetCreatureDisplayRunnable;
        resetted=false;
    }
    @Override
    public void run(){
        while (!stop) {
            try {
                if(creatureId>-1){
                    resetted=false;
                    Creature creature=creaturesArray.request(creatureId);
                    if (creature!=null){
                        Color color=creature.getColor();
                        double speed=creature.getSpeed();
                        double hunger=creature.getHunger();
                        double hp=creature.getHp();
                        double maxHp=creature.getMaxHp();
                        double baseAttak=creature.getBaseAttack();
                        creaturesArray.release(creatureId);
                        Platform.runLater(RunnableGenerator.generate(creatureId, color, speed, hunger,hp,maxHp,baseAttak));
                        
                    }else{
                        creaturesArray.release(creatureId);
                    }
                }else if(!resetted){
                    Platform.runLater(ResetCreatureDisplayRunnable);
                    resetted=true;
                }
                Platform.runLater(updateCreatureCountDisplay);
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public void setCreatureId(int creatureId){
        this.creatureId=creatureId;
    }
    public void Stop(){
        stop=true;
    }
    
    
}
