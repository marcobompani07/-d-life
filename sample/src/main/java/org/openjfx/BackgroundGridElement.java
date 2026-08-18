package org.openjfx;

public class BackgroundGridElement {
    private int foodId;
	private int creatureId;

    public BackgroundGridElement(){
        foodId = -1;
		creatureId = -1;
    }

    public synchronized int getFood(){
        return foodId;
    }
	
    public synchronized void setFood(int foodId){
        this.foodId=foodId;
    }

	public synchronized int getCreature(){
		return creatureId;
	}

	public synchronized void setCreature(int creatureId){
		this.creatureId = creatureId;
	}
}
