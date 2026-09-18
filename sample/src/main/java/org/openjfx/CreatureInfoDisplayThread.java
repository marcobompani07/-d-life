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

    public CreatureInfoDisplayThread(CreatureInfoDisplayThread c){
        stop=false;
        this.RunnableGenerator=c.getRunnableGenerator();
        this.creatureId=c.getCreatureId();
        this.creaturesArray=c.getCreaturesArray();
        this.updateCreatureCountDisplay=c.getUpdateCreatureCountDisplay();
         this.ResetCreatureDisplayRunnable=c.getResetCreatureDisplayRunnable();
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
    public int getCreatureId(){
        return this.creatureId;
    }
    public void Stop(){
        stop=true;
    }

    public boolean isStop() {
        return this.stop;
    }

    public boolean getStop() {
        return this.stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public UpdateCreatureDisplayRunnableGenerator getRunnableGenerator() {
        return this.RunnableGenerator;
    }

    public void setRunnableGenerator(UpdateCreatureDisplayRunnableGenerator RunnableGenerator) {
        this.RunnableGenerator = RunnableGenerator;
    }

    public Runnable getUpdateCreatureCountDisplay() {
        return this.updateCreatureCountDisplay;
    }

    public void setUpdateCreatureCountDisplay(Runnable updateCreatureCountDisplay) {
        this.updateCreatureCountDisplay = updateCreatureCountDisplay;
    }

    public Runnable getResetCreatureDisplayRunnable() {
        return this.ResetCreatureDisplayRunnable;
    }

    public void setResetCreatureDisplayRunnable(Runnable ResetCreatureDisplayRunnable) {
        this.ResetCreatureDisplayRunnable = ResetCreatureDisplayRunnable;
    }

    public ThreadSafeCreaturesArray getCreaturesArray() {
        return this.creaturesArray;
    }

    public void setCreaturesArray(ThreadSafeCreaturesArray creaturesArray) {
        this.creaturesArray = creaturesArray;
    }

    public boolean isResetted() {
        return this.resetted;
    }

    public boolean getResetted() {
        return this.resetted;
    }

    public void setResetted(boolean resetted) {
        this.resetted = resetted;
    }
    
    
}
