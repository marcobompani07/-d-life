package org.openjfx;

public class BackgroundGridElement {
    private int foodId;
	private int creatureId;

    public BackgroundGridElement(){
        foodId = -1;
		creatureId = -1;
    }

    public int getFood(){
        return foodId;
    }
	
    public void setFood(int foodId){
        this.foodId=foodId;
    }

	public int getCreature(){
		return creatureId;
	}

	public void setCreature(int creatureId){
		this.creatureId = creatureId;
	}
}
