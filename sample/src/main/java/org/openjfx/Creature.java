package org.openjfx;

import javafx.scene.paint.Color;

public class Creature {
	private int id;
	private double x;
	private double y;
	private double width;
	private double height;
	private Color color;
	private double speed;
	private double hunger;
	private int view;
	private ThreadSafeFoodArray foodArray;
	private BackgroundGridElement[][] backgroundGrid;

	public Creature(int id, double x, double y, double width, double height,Color color, double speed, ThreadSafeFoodArray foodArray, BackgroundGridElement[][] backgroundGrid) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.speed = speed;
		this.color= color;
		this.hunger = 0;
		this.view = 5;
		this.foodArray = foodArray;
		this.backgroundGrid = backgroundGrid;
	}

	public Creature (Creature c) {
		this(c.getId(), c.getX(), c.getY(), c.getWidth(), c.getHeight(),c.getColor(), c.getSpeed(), c.getFoodArray(), c.getBackgroundGrid());
	}

	public Creature() {
		this(0, 0, 0, 10, 10,Color.RED, 2.0, null, null);
	}

	public void update() throws InterruptedException{

		if(this.getHunger() < 100){
			double weightedSpeed = this.getSpeed();

			if(this.getHunger() >= 60){
				double hungerEffect = (this.getHunger() - 60) / 40;
				weightedSpeed = this.getSpeed() * (1 - hungerEffect);
			}

			Object [] closestFoodData = findClosestFood();
			Food closestFood = (Food) closestFoodData[0];
			double closestDistance = (double) closestFoodData[1];

			double newX, newY;

			if(closestFood != null && closestDistance > 0){
				double directionX = (closestFood.getX() - this.getX()) / closestDistance;
				double directionY = (closestFood.getY() - this.getY()) / closestDistance;

				newX = this.getX() + directionX * weightedSpeed;
				newY = this.getY() + directionY * weightedSpeed;	
			}else{
				newX = this.getX() + Math.random() * (weightedSpeed * 2) - (weightedSpeed * 2) / 2;
				newY = this.getY() + Math.random() * (weightedSpeed * 2) - (weightedSpeed * 2) / 2;
			}

			newX = Math.max(0, Math.min(App.WORLD_WIDTH - this.getWidth(), newX));
			newY = Math.max(0, Math.min(App.WORLD_HEIGHT - this.getHeight(), newY));

			eatFood(newX, newY);
			
			move(newX, newY);

			this.setHunger(this.getHunger() + this.getSpeed() / 100);
		}
	}

	private Object[] findClosestFood() throws InterruptedException{
		Food closestFood = null;
		double closestDistance = Double.MAX_VALUE;

		int gridPositionX = (int) (this.getX() / 10);
		int gridPositionY = (int) (this.getY() / 10);

		int startX = gridPositionX - this.getView();
		int endX = gridPositionX + this.getView();
		int startY = gridPositionY - this.getView();
		int endY = gridPositionY + this.getView();

		for(int x = startX; x <= endX; x++){
			for(int y = startY; y <= endY; y++){
				if(x >= 0 && x < backgroundGrid.length && y >= 0 && y < backgroundGrid[0].length){
					int foodId = backgroundGrid[x][y].getFood();

					if(foodId != -1){
						Food food = foodArray.request(foodId);

						if(food != null){
							double distance = Math.sqrt(Math.pow(food.getX() - this.getX(), 2) + Math.pow(food.getY() - this.getY(), 2));
							if(distance < closestDistance){
								closestDistance = distance;
								closestFood = food;
							}
						}

						foodArray.release(foodId);
					}
				}
			}
		}

		return new Object[]{closestFood, closestDistance};
	}

	private void eatFood(double newX, double newY) throws InterruptedException{
		int foodIndex = 0;
		while (foodIndex < foodArray.getLength()) {
			Food food = foodArray.request(foodIndex);
			if (food != null) {
				double foodX = food.getX();
				double foodY = food.getY();

				double creatureClosestX = Math.max(newX, Math.min(foodX, newX + this.getWidth()));
				double creatureClosestY = Math.max(newY, Math.min(foodY, newY + this.getHeight()));

				double distanceToFood = Math.sqrt(Math.pow(foodX - creatureClosestX, 2) + Math.pow(foodY - creatureClosestY, 2));

				if (distanceToFood <= Food.FOOD_WIDTH / 2) {
					this.setHunger(this.getHunger() - 20);

					if (this.getHunger() < 0) {
						this.setHunger(0);
					}

					int foodGridX = (int) (foodX / 10);
					int foodGridY = (int) (foodY / 10);
					backgroundGrid[foodGridX][foodGridY].setFood(-1);
					foodArray.release(foodIndex);
					foodArray.requestRemove(foodIndex);
					break;
				}
			}

			foodArray.release(foodIndex);
			foodIndex++;
		}
	}

	public synchronized void move(double newX, double newY) {
		this.x = newX;
		this.y = newY;
	}

	public synchronized  int getId() {
		return id;
	}

	public synchronized double getX() {
		return x;
	}

	public synchronized  void setX(double x) {
		this.x = x;
	}

	public synchronized double getY() {
		return y;
	}

	public synchronized void setY(double y) {
		this.y = y;
	}

	public synchronized double getWidth() {
		return width;
	}

	public synchronized void setWidth(double width) {
		this.width = width;
	}

	public synchronized double getHeight() {
		return height;
	}

	public synchronized void setHeight(double height) {
		this.height = height;
	}

	public synchronized Color getColor() {
		return this.color;
	}

	public synchronized void setColor(Color color) {
		this.color = color;
	}

	public synchronized double getSpeed() {
		return speed;
	}

	public synchronized void setSpeed(double speed) {
		this.speed = speed;
	}

	public synchronized double getHunger() {
		return hunger;
	}

	public synchronized void setHunger(double hunger) {
		this.hunger = hunger;
	}

	public synchronized int getView() {
		return view;
	}

	public synchronized ThreadSafeFoodArray getFoodArray() {
		return foodArray;
	}

	public synchronized BackgroundGridElement[][] getBackgroundGrid() {
		return backgroundGrid;
	}

}
