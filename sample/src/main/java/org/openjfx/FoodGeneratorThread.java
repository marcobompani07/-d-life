package org.openjfx;

public class FoodGeneratorThread extends Thread{
    private  Food[] foodArray;
    private  BackgroundGridElement[][] backgroundGrid;
    public FoodGeneratorThread(Food[]foodArray,BackgroundGridElement[][]backgroundGrid){
        this.foodArray=foodArray;
        this.backgroundGrid=backgroundGrid;
    }
    @Override
    public void run(){
        boolean found=false;
        for(int i=0;i<foodArray.length||found;i++){
            if(foodArray[i]!=null){
                int x=((int)(Math.random()*991));
                int y=((int)(Math.random()*981));
                foodArray[i]=new Food(i, 1,x,y);
                backgroundGrid[x][y].setFood(i);
            }
        }
    }
}
