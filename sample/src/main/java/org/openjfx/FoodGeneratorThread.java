package org.openjfx;

public class FoodGeneratorThread extends Thread{
    private  Food[] foodArray;
    private  BackgroundGridElement[][] backgroundGrid;
    private boolean stop;
    public FoodGeneratorThread(Food[]foodArray,BackgroundGridElement[][]backgroundGrid){
        this.foodArray=foodArray;
        this.backgroundGrid=backgroundGrid;
        stop=false;
    }
    @Override
    public void run(){
        while(!stop){
            long stantingTime=System.currentTimeMillis();
            boolean found=false;
            for(int i=0;i<foodArray.length&&!found;i++){
                if(foodArray[i]==null){
                    int x=((int)(Math.random()*991));
                    int y=((int)(Math.random()*981));
                    foodArray[i]=new Food(i, 1,x,y);
                    //backgroundGrid[x][y].setFood(i);
                    found=true;
                }
            }
            long currentTime=System.currentTimeMillis();
            System.out.println(currentTime-stantingTime);
            if ((currentTime-stantingTime)<=100){
                try {
                   Thread.sleep(100-(currentTime-stantingTime)); 
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
            }
        }
        
    }
    public void Stop(){
        stop=true;
    }
}
