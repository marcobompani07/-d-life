package org.openjfx;

import javafx.scene.paint.Color;

public class Creature {
	private int id;
	private double x;
	private double y;
	private double directionX;
	private double directionY;
	private double width;
	private double height;
	private Color color;
	private double speed;
	private double hunger;
	private double hp;
	private double baseAttack;
	private int view;
	private int attackRange;
	private long lastAttackTime = 0;
	private static final long ATTACK_COOLDOWN = 500;
	private ThreadSafeFoodArray foodArray;
	private ThreadSafeCreaturesArray creatureArray;
	private BackgroundGridElement[][] backgroundGrid;
	private ThreadSafeBackgroundGrid threadSafeBackgroundGrid;

	public Creature(int id, double x, double y, double width, double height, double hp, double baseAttack, Color color, double speed, ThreadSafeFoodArray foodArray, ThreadSafeCreaturesArray creaturesArray, BackgroundGridElement[][] backgroundGrid, ThreadSafeBackgroundGrid threadSafeBackgroundGrid) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.speed = speed;
		this.color= color;
		this.hunger = 0;
		this.hp = hp;
		this.baseAttack = baseAttack;
		this.view = 5;
		this.attackRange = 1;
		this.foodArray = foodArray;
		this.creatureArray = creaturesArray;
		this.backgroundGrid = backgroundGrid;
		this.threadSafeBackgroundGrid = threadSafeBackgroundGrid;

		double angle = Math.random() * 2 * Math.PI;
		this.setDirectionX(Math.cos(angle));
		this.setDirectionY(Math.sin(angle));
	}

	public Creature (Creature c) {
		this(c.getId(), c.getX(), c.getY(), c.getWidth(), c.getHeight(), c.getHp(), c.getBaseAttack(), c.getColor(), c.getSpeed(), c.getFoodArray(), c.getCratureArray(), c.getBackgroundGrid(), c.getThreadSafeBackgroundGrid());
	}

	public Creature() {
		this(0, 0, 0, 10, 10, 100, 5,Color.RED, 2.0, null, null, null, null);
	}

	public void update() throws InterruptedException{

		if(this.getHunger() < 100 && this.getHp() > 0){
			double weightedSpeed = this.getSpeed();
			double weightedAttack = this.getBaseAttack();

			if(this.getHunger() >= 60){
				double hungerEffect = (this.getHunger() - 60) / 40;
				weightedSpeed = this.getSpeed() * (1 - hungerEffect);
				weightedAttack = this.getBaseAttack() * (1 - hungerEffect);
			}

			Object [] closestFoodData = findClosestFood();
			Food closestFood = (Food) closestFoodData[0];
			double closestDistance = (double) closestFoodData[1];

			double newX, newY;

			if(closestFood != null && closestDistance > 0){
				double newDirectionX = (closestFood.getX() - this.getX()) / closestDistance;
				double newDirectionY = (closestFood.getY() - this.getY()) / closestDistance;

				newX = this.getX() + newDirectionX * weightedSpeed;
				newY = this.getY() + newDirectionY * weightedSpeed;	
			}else{
				if(Math.random() < 0.01){
					double angle = Math.random() * 2 * Math.PI;
					this.setDirectionX(Math.cos(angle));
					this.setDirectionY(Math.sin(angle));
				}

				newX = this.getX() + this.getDirectionX() * weightedSpeed;
				newY = this.getY() + this.getDirectionY() * weightedSpeed;

				if(newX <= 0 || newX >= App.WORLD_WIDTH - this.getWidth()){
					this.setDirectionX(this.getDirectionX() * -1);
				}

				if(newY <= 0 || newY >= App.WORLD_HEIGHT - this.getHeight()){
					this.setDirectionY(this.getDirectionY() * -1);
				}
			}

			newX = Math.max(0, Math.min(App.WORLD_WIDTH - this.getWidth(), newX));
			newY = Math.max(0, Math.min(App.WORLD_HEIGHT - this.getHeight(), newY));

			if(closestFood != null){
				eatFood(closestFood, closestFood.getId(), newX, newY);
			}

			Object[] closestCreatureData = findClosestCreature();
			Creature closestCreature = (Creature) closestCreatureData[0];
			double closestCreatureDistance = (double) closestCreatureData[1];

			if(closestCreature != null && closestCreatureDistance <= this.getAttackRange() * 10){
				long currentTime = System.currentTimeMillis();

				if(currentTime - lastAttackTime >= ATTACK_COOLDOWN){
					closestCreature.takeDamage(weightedAttack);
					lastAttackTime = currentTime;
				}
			}

			if(move(newX, newY)){
				this.setHunger(this.getHunger() + this.getSpeed() / 100);

				if(this.getHunger() >= 100){
					threadSafeBackgroundGrid.removeCreature(this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.getId());
					creatureArray.removeCreature(this.getId());
				}
			}else{
				this.setDirectionX(this.getDirectionX() * -1);
				this.setDirectionY(this.getDirectionY() * -1);
			}
		}
	}

	public synchronized boolean takeDamage(double damage){
		this.setHp(this.getHp() - damage);

		if(this.getHp() <= 0){
			threadSafeBackgroundGrid.removeCreature(this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.getId());
			creatureArray.removeCreature(this.getId());

			return true;
		}

		return false;
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
	
	private Object[] findClosestCreature() throws InterruptedException{
		Creature closestCreature = null;
		double closestDistance = Double.MAX_VALUE;

		int startX = (int) Math.max(0, Math.min(this.getX() / 10, backgroundGrid.length - 1));
		int endX = (int) Math.max(0, Math.min(((this.getX() + this.getWidth()) / 10) - 1, backgroundGrid.length - 1));

		int startY = (int) Math.max(0, Math.min(this.getY() / 10, backgroundGrid[0].length - 1));
		int endY = (int) Math.max(0, Math.min(((this.getY() + this.getHeight()) / 10) - 1, backgroundGrid[0].length - 1));

		int range = this.getAttackRange();

		for (int x = startX - range; x <= endX + range; x++) {

        	for (int y = startY - range; y <= endY + range; y++) {

				if(x >= 0 && x < backgroundGrid.length && y >= 0 && y < backgroundGrid[0].length){

					int creatureId = threadSafeBackgroundGrid.getCreature(x, y);

					if(creatureId != -1 && creatureId != this.getId()){
						Creature creature = creatureArray.getCreature(creatureId);

						if(creature != null){

							double distanceX = Math.max(0, Math.max(this.getX() - (creature.getX() + creature.getWidth()), creature.getX() - (this.getX() + this.getWidth())));
							double distanceY = Math.max(0, Math.max(this.getY() - (creature.getY() + creature.getHeight()), creature.getY() - (this.getY() + this.getHeight())));

							double distance = Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
							
							if(distance < closestDistance && distance <= this.getAttackRange() * 10){
								closestDistance = distance;
								closestCreature = creature;
							}
						}
					}
				}
			}
		}

		return new Object[]{closestCreature, closestDistance};
	}

	private void eatFood(Food food, int foodId, double newX, double newY) throws InterruptedException{
		if(food == null){
			return;
		}

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

			foodArray.requestRemove(foodId);
		}
	}

	public boolean move(double newX, double newY) {
		boolean moved = threadSafeBackgroundGrid.moveCreature(this.getX(), this.getY(), newX, newY, this.getWidth(), this.getHeight(), this.getId());

		if(moved){
			this.x = newX;
			this.y = newY;
		}

		return moved;
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

	public synchronized int getAttackRange(){
		return attackRange;
	}

	public synchronized double getHp(){
		return hp;
	}

	public synchronized void setHp(double hp){
		this.hp = hp;
	}

	public synchronized double getBaseAttack(){
		return baseAttack;
	}

	public synchronized void setBaseAttack(double baseAttack){
		this.baseAttack = baseAttack;
	}

	public synchronized double getDirectionX() {
		return directionX;
	}

	public synchronized  void setDirectionX(double directionX) {
		this.directionX = directionX;
	}

	public synchronized double getDirectionY() {
		return directionY;
	}

	public synchronized void setDirectionY(double directionY) {
		this.directionY = directionY;
	}

	public synchronized ThreadSafeFoodArray getFoodArray() {
		return foodArray;
	}

	public synchronized ThreadSafeCreaturesArray getCratureArray() {
		return creatureArray;
	}

	public synchronized BackgroundGridElement[][] getBackgroundGrid() {
		return backgroundGrid;
	}

	public synchronized ThreadSafeBackgroundGrid getThreadSafeBackgroundGrid() {
		return threadSafeBackgroundGrid;
	}
}
