package org.openjfx;

public class FoodGeneratorThread extends Thread{
    private  Food[] foodArray;
    private  BackgroundGridElement[][] backgroundGrid;
    private boolean stop;
    private int foodGenerationCount;
    public FoodGeneratorThread(Food[]foodArray,BackgroundGridElement[][]backgroundGrid, int foodGenerationCount){
        this.foodArray=foodArray;
        this.backgroundGrid=backgroundGrid;
        this.foodGenerationCount=foodGenerationCount;
        stop=false;
    }
    @Override
    public void run(){
        while(!stop){
            long stantingTime=System.currentTimeMillis();
            int generatedFood=0;
            for(int i=0;i<foodArray.length&&generatedFood<foodGenerationCount;i++){
                if(foodArray[i]==null){
                    int x=((int)(Math.random()*100));
                    int y=((int)(Math.random()*100));
                    foodArray[i]=new Food(i, 1,x*10,y*10);
                    backgroundGrid[x][y].setFood(i);
                    generatedFood++;
                }
            }
            long currentTime=System.currentTimeMillis();
            System.out.println(currentTime-stantingTime);
            if ((currentTime-stantingTime)<=1000){
                try {
                   Thread.sleep(1000-(currentTime-stantingTime)); 
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
