package org.openjfx;

public class ThreadSafeCreaturesArray {
    private Creature[] creatures;
    private boolean[] requestedCreatures;
    public ThreadSafeCreaturesArray(Creature[] creatures) {
        this.creatures = creatures;
        requestedCreatures=new boolean[creatures.length];
        for(int i=0;i<requestedCreatures.length;i++){
            requestedCreatures[i]=false;
        }
    }

    public synchronized Creature request(int i)throws InterruptedException{
        while(requestedCreatures[i]){
            System.out.println("loked:"+i);
            wait();
        }
        requestedCreatures[i]=true;
        System.out.println("aquired:"+i);
        return creatures[i];
    }


    public synchronized void release(int i){
        requestedCreatures[i]=false;
        System.out.println("released:"+i);
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
