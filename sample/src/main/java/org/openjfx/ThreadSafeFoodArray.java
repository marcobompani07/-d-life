package org.openjfx;

public class ThreadSafeFoodArray {
    private Food[] foodArray;
    private boolean[] requestedFoods;
    public ThreadSafeFoodArray(Food[] foodArray) {
        this.foodArray = foodArray;
        requestedFoods=new boolean[foodArray.length];
        for(int i=0;i<requestedFoods.length;i++){
            requestedFoods[i]=false;
        }
    }

    public synchronized Food request(int i)throws InterruptedException{
        while(requestedFoods[i]){
            wait();
        }
        requestedFoods[i]=true;
        return foodArray[i];
    }


    public synchronized void release(int i){
        requestedFoods[i]=false;
        notifyAll();
    }

    public Food[] getFoods() {
        return this.foodArray;
    }

    public void setFoods(Food[] foodArray) {
        this.foodArray = foodArray;
    }
    public int getLength(){
        return foodArray.length;
    }
}
