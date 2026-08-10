package org.openjfx;

public class BackgroundGridElement {
    private int foodId;
    public BackgroundGridElement(){
        foodId=-1;
    }
    public int getFood(){
        return foodId;
    }
    public void setFood(int foodId){
        this.foodId=foodId;
    }
}
