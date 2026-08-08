package org.openjfx;

import java.util.LinkedList;

public class TreadSafeCreaturesArray {
    private Creature[] creatures;
    private boolean[] requestedCreatures;
    private LinkedList<Thread>[] waitingThreads;
    public TreadSafeCreaturesArray(Creature[] creatures) {
        this.creatures = creatures;
        requestedCreatures=new boolean[creatures.length];
        waitingThreads=new LinkedList[creatures.length];
        for(int i=0;i<requestedCreatures.length;i++){
            requestedCreatures[i]=false;
            waitingThreads[i]=new LinkedList<Thread>();
        }
    }

    public synchronized Creature request(int i,Thread t){
        if(!requestedCreatures[i]){
            requestedCreatures[i]=true;
            return creatures[i];
        }else{
            waitingThreads[i].offer(t);
            try{
                
            }catch(interrupted Exception)
            t.wait();
            requestedCreatures[i]=true;
            return creatures[i];
        }
    }

    public Creature[] getCreatures() {
        return this.creatures;
    }

    public void setCreatures(Creature[] creatures) {
        this.creatures = creatures;
    }


}
