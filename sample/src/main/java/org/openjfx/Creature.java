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
	private ThreadSafeFoodArray foodArray;

	public Creature(int id, double x, double y, double width, double height,Color color, double speed, ThreadSafeFoodArray foodArray) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.speed = speed;
		this.color= color;
		this.hunger = 0;
		this.foodArray = foodArray;
	}

	public Creature (Creature c) {
		this(c.getId(), c.getX(), c.getY(), c.getWidth(), c.getHeight(),c.getColor(), c.getSpeed(), c.getFoodArray());
	}

	public Creature() {
		this(0, 0, 0, 10, 10,Color.RED, 2.0, null);
	}

	public void update() throws InterruptedException{

		if(this.getHunger() < 100){
			double weightedSpeed = this.getSpeed();

			if(this.getHunger() >= 60){
				double hungerEffect = (this.getHunger() - 60) / 40;
				weightedSpeed = this.getSpeed() * (1 - hungerEffect);
			}

			double newX = this.getX() + Math.random() * weightedSpeed - weightedSpeed / 2;
			double newY = this.getY() + Math.random() * weightedSpeed - weightedSpeed / 2;

			newX = Math.max(0, Math.min(App.WORLD_WIDTH - this.getWidth(), newX));
			newY = Math.max(0, Math.min(App.WORLD_HEIGHT - this.getHeight(), newY));

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

						foodArray.release(foodIndex);
						break;
					}
				}

				foodArray.release(foodIndex);
				foodIndex++;
			}
			
			move(newX, newY);

			this.setHunger(this.getHunger() + this.getSpeed() / 100);
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

	public synchronized ThreadSafeFoodArray getFoodArray() {
		return foodArray;
	}

}
