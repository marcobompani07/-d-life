package org.openjfx;

public class TestMoovmentThread extends Thread{
    private boolean stop;
    private CreatureGraphicHandler[] creatures;
    public TestMoovmentThread(CreatureGraphicHandler[] creatures){
        stop=false;
        this.creatures=creatures;
    }
    @Override
    public void run(){
        for(int i=0;i<1000;i++){
            creatures[0].setX(i);
            creatures[0].setY(i);
            System.out.println(i);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
    }
}
