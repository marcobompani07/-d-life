package org.openjfx;

public class FoodGeneratorThread extends Thread{
    private  ThreadSafeFoodArray foodArray;
    private  BackgroundGridElement[][] backgroundGrid;
    private boolean stop;
    private int foodGenerationCount;
    public FoodGeneratorThread(ThreadSafeFoodArray foodArray,BackgroundGridElement[][]backgroundGrid, int foodGenerationCount){
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
            for(int i=0;i<foodArray.getLength()&&generatedFood<foodGenerationCount;i++){
                try {
                    Food food=foodArray.request(i);
                    if(food==null){
                        foodArray.release(i);
                        int x=((int)(Math.random()* backgroundGrid.length));
                        int y=((int)(Math.random()* backgroundGrid[0].length));
						
                        while (backgroundGrid[x][y].getFood() != -1){
                            x= (int) (Math.random() * backgroundGrid.length);
                            y= (int) (Math.random() * backgroundGrid[0].length);
                        };

                        foodArray.requestAdd(i, new Food(i, 1,x*10,y*10));
                        backgroundGrid[x][y].setFood(i);
                        generatedFood++;
                    }else{
                        foodArray.release(i);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            long currentTime=System.currentTimeMillis();
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
