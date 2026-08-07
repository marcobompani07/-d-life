package org.openjfx;

import java.util.concurrent.ConcurrentLinkedQueue;

public class TestExternalThread extends Thread {
    private  ConcurrentLinkedQueue<MoveData> outMoveQueue;
    private  MoveData[] testMoveData={new MoveData(100,100,0),new MoveData(200,100,0),new MoveData(300,200,0),new MoveData(700,100,0)};
    public TestExternalThread(ConcurrentLinkedQueue<MoveData> outMoveQueue){
        this.outMoveQueue=outMoveQueue;
    }
    @Override
    public void run(){
        for(int i=0;i<testMoveData.length;i++){
            outMoveQueue.add(testMoveData[i]);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
