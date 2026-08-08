package org.openjfx;

public class TreadSafeCreaturesArray {
    private Creature[] creatures;
    private boolean[] requestedCreatures;
    public TreadSafeCreaturesArray(Creature[] creatures) {
        this.creatures = creatures;
        requestedCreatures=new boolean[creatures.length];
        for(int i=0;i<requestedCreatures.length;i++){
            requestedCreatures[i]=false;
        }
    }

    public synchronized Creature request(int i)throws InterruptedException{
        while(requestedCreatures[i]){
            wait();
        }
        requestedCreatures[i]=true;
        return creatures[i];
    }
    public synchronized void release(int i){
        requestedCreatures[i]=false;
        notifyAll();
    }

    public Creature[] getCreatures() {
        return this.creatures;
    }

    public void setCreatures(Creature[] creatures) {
        this.creatures = creatures;
    }
    public int getLength(){
        return creatures.length;
    }


}
