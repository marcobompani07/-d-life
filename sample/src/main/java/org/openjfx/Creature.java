package org.openjfx;

import javafx.scene.paint.Color;

public class Creature {
	private int id;
	private CreatureBrain brain;
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
	private double hpRegenPercentage = 0.15;
	private double maxHp;
	private double baseAttack;
	private int view;
	private int attackRange;
	private long lastAttackTime = 0;
	private static final long ATTACK_COOLDOWN = 500;
	private double reproductionRate;
	private long lastReproductionTime = 0;
	public static final long REPRODUCTION_COOLDOWN = 100000;
	private ThreadSafeFoodArray foodArray;
	private ThreadSafeCreaturesArray creatureArray;
	private BackgroundGridElement[][] backgroundGrid;
	private ThreadSafeBackgroundGrid threadSafeBackgroundGrid;

	public Creature(int id, double x, double y, double width, double height, double hp, double baseAttack, Color color, double speed, ThreadSafeFoodArray foodArray, ThreadSafeCreaturesArray creaturesArray, BackgroundGridElement[][] backgroundGrid, ThreadSafeBackgroundGrid threadSafeBackgroundGrid) {
		this.id = id;
		this.brain = new CreatureBrain();
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.speed = speed;
		this.color= color;
		this.hunger = 0;
		this.hp = hp;
		this.maxHp = hp;
		this.baseAttack = baseAttack;
		this.view = 5;
		this.attackRange = 1;
		this.reproductionRate = 0.1;
		this.foodArray = foodArray;
		this.creatureArray = creaturesArray;
		this.backgroundGrid = backgroundGrid;
		this.threadSafeBackgroundGrid = threadSafeBackgroundGrid;
		this.lastReproductionTime=System.currentTimeMillis();

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
			double closestFoodDistance = (double) closestFoodData[1];

			Object[] closestCreatureData = findClosestCreature();

			int action = this.brain.think(closestFoodData, closestCreatureData, this.getX(), this.getY(), this.getView(), this.getAttackRange(), this.getHunger(), this.getHp(), this.getMaxHp(), this);
			double newX, newY, newDirectionX, newDirectionY;
			Creature target = this.brain.getCreatureTarget();

			// perform an action based on brain output
			switch (action){
				case CreatureBrain.EAT:
					if(closestFood != null){
						eatFood(closestFood, closestFood.getId(), this.getX(), this.getY());
					}

					break;
				
				case CreatureBrain.MOVE_TO_FOOD:
					if (closestFood == null || closestFoodDistance <= 0) break;

					newDirectionX = (closestFood.getX() - this.getX()) / closestFoodDistance;
					newDirectionY = (closestFood.getY() - this.getY()) / closestFoodDistance;

					newX = this.getX() + newDirectionX * weightedSpeed;
					newY = this.getY() + newDirectionY * weightedSpeed;	
					newX = Math.max(0, Math.min(App.WORLD_WIDTH - this.getWidth(), newX));
					newY = Math.max(0, Math.min(App.WORLD_HEIGHT - this.getHeight(), newY));

					if(closestFood != null){
						eatFood(closestFood, closestFood.getId(), newX, newY);
					}

					if(move(newX, newY)){
						this.setHunger(this.getHunger() + this.getSpeed() / 100);

						if(this.getHunger() >= 100){
							threadSafeBackgroundGrid.removeCreature(this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.getId());
							creatureArray.removeCreature(this.getId());
							App.removeCreature(this.getId());
							return;
						}
					}else{
						this.setDirectionX(this.getDirectionX() * -1);
						this.setDirectionY(this.getDirectionY() * -1);
					}

					break;

				case CreatureBrain.MOVE_TO_CREATURE:
					if (target == null) break;

					double targetDistanceX = target.getX() - this.getX();
					double targetDistanceY = target.getY() - this.getY();
					double targetDistance = Math.sqrt(targetDistanceX * targetDistanceX + targetDistanceY * targetDistanceY);

					if (targetDistance <= 0) break;

					newDirectionX = targetDistanceX / targetDistance;
					newDirectionY = targetDistanceY / targetDistance;

					newX = this.getX() + newDirectionX * weightedSpeed;
					newY = this.getY() + newDirectionY * weightedSpeed;	
					newX = Math.max(0, Math.min(App.WORLD_WIDTH - this.getWidth(), newX));
					newY = Math.max(0, Math.min(App.WORLD_HEIGHT - this.getHeight(), newY));

					if(move(newX, newY)){
						this.setHunger(this.getHunger() + this.getSpeed() / 100);

						if(this.getHunger() >= 100){
							threadSafeBackgroundGrid.removeCreature(this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.getId());
							creatureArray.removeCreature(this.getId());
							App.removeCreature(this.getId());
							return;
						}
					}else{
						this.setDirectionX(this.getDirectionX() * -1);
						this.setDirectionY(this.getDirectionY() * -1);
					}

					break;

				case CreatureBrain.ATTACK:
					if(target == null || target.getHp() <= 0  || target.getHunger() >= 100){
						this.brain.setCreatureTarget(null);
						break;
					}

					long currentTime = System.currentTimeMillis();

					if(currentTime - lastAttackTime >= ATTACK_COOLDOWN){
						target.takeDamage(weightedAttack);
						lastAttackTime = currentTime;

						this.setHunger(this.getHunger() + this.getBaseAttack() /20);

						if(target == null || target.getHp() <= 0 || target.getHunger() >= 100){
							this.brain.setCreatureTarget(null);
						}

						if(this.getHunger() >= 100){
							threadSafeBackgroundGrid.removeCreature(this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.getId());
							creatureArray.removeCreature(this.getId());
							App.removeCreature(this.getId());
							return;
						}
					}

					break;

				case CreatureBrain.FLEE:
					if(target != null && target.getHp() > 0){
						double fleeX = this.getX() - target.getX();
						double fleeY = this.getY() - target.getY();
						double distance = Math.sqrt(fleeX * fleeX + fleeY * fleeY);

						if(distance > 0){
							newDirectionX = fleeX / distance;
							newDirectionY = fleeY / distance;

							this.setDirectionX(newDirectionX);
							this.setDirectionY(newDirectionY);

							newX = this.getX() + newDirectionX * weightedSpeed;
							newY = this.getY() + newDirectionY * weightedSpeed;
							newX = Math.max(0, Math.min(App.WORLD_WIDTH - this.getWidth(), newX));
							newY = Math.max(0, Math.min(App.WORLD_HEIGHT - this.getHeight(), newY));

							if (move(newX, newY)) {
								this.setHunger(this.getHunger() + this.getSpeed() / 100);
							} else {
								this.setDirectionX(-newDirectionY);
								this.setDirectionY(newDirectionX);
							}
						}
					}else {
						newX = this.getX() + this.getDirectionX() * weightedSpeed;
						newY = this.getY() + this.getDirectionY() * weightedSpeed;
						newX = Math.max(0, Math.min(App.WORLD_WIDTH - this.getWidth(), newX));
						newY = Math.max(0, Math.min(App.WORLD_HEIGHT - this.getHeight(), newY));
						move(newX, newY);
					}

					break;

				case CreatureBrain.RANDOM_MOVEMENT:
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

					if(move(newX, newY)){
						this.setHunger(this.getHunger() + this.getSpeed() / 100);

						if(this.getHunger() >= 100){
							threadSafeBackgroundGrid.removeCreature(this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.getId());
							creatureArray.removeCreature(this.getId());
							App.removeCreature(this.getId());
							return;
						}
					}else{
						this.setDirectionX(this.getDirectionX() * -1);
						this.setDirectionY(this.getDirectionY() * -1);
					}
					break;

				case CreatureBrain.REPRODUCE:
					this.setHunger(this.getHunger() + 50);
					this.setHp(this.getHp() / 2);

					reproduct();
					this.brain.setCreatureTarget(null);
					break;
			}
		}else{
			threadSafeBackgroundGrid.removeCreature(this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.getId());
			creatureArray.removeCreature(this.getId());
			App.removeCreature(this.getId());
			return;
		}
	}

	private void reproduct(){
		int newId = creatureArray.getAvailableId();

		if(newId == -1){
			return;
		}

		double childWidth = 10;
		double childHeight = 10;

		// set child spawnpoint in a radius around parent
		double minRadius = 15;
		double maxRadius = 40;
		double radius = minRadius + Math.random() * (maxRadius - minRadius);
		
		double angle = Math.random() * 2 * Math.PI;
		double spawnX = this.getX() + radius * Math.cos(angle);
		double spawnY = this.getY() + radius * Math.sin(angle);
		spawnX = Math.max(0, Math.min(App.WORLD_WIDTH - childWidth, spawnX));
    	spawnY = Math.max(0, Math.min(App.WORLD_HEIGHT - childHeight, spawnY));

		/*double childHp = mutate(this.getMaxHp(), 0.15, 50, 500);
		double childAttack = mutate(this.getBaseAttack(), 0.15, 1, 25);
		double childSpeed = mutate(this.getSpeed(), 0.10, 0.2, 3);

		Color childColor = mutateColor(this.getColor(), 0.02);*/
		double childHp;
		double childAttack;
		double childSpeed;
		Color childColor;

		// mutate child proprieties based also on the parent ones
		childHp= mutate(this.getMaxHp(), 0.15*App.MutationRate, 50, 500);
		childAttack= mutate(this.getBaseAttack(), 0.15*App.MutationRate, 1, 25);
		childSpeed = mutate(this.getSpeed(), 0.10*App.MutationRate, 0.2, 3);
		childColor = mutateColor(this.getColor(), 0.02*App.MutationRate);

		Creature newChild = new Creature(newId, spawnX, spawnY, childWidth, childHeight, childHp, childAttack, childColor, childSpeed, this.foodArray, this.creatureArray, this.backgroundGrid, this.threadSafeBackgroundGrid);

		if(threadSafeBackgroundGrid.addCreature(spawnX, spawnY, childWidth, childHeight, newId)){
			creatureArray.addCreature(newChild);
			App.addCreature();
		}/*else{
			App.cancelCreatureCreation();
		}*/
	}

	private double mutate(double baseValue, double mutationFactor, double min, double max){
		double change = ((Math.random() * 2 - 1) * mutationFactor) + 1.0;
		double mutatedValue = baseValue * change;
		return Math.max(min, Math.min(max, mutatedValue));
	}

	private Color mutateColor(Color parentColor, double variation){
		double r = Math.max(0, Math.min(1, parentColor.getRed() + (Math.random() * 2 - 1) * variation));
		double g = Math.max(0, Math.min(1, parentColor.getGreen() + (Math.random() * 2 - 1) * variation));
		double b = Math.max(0, Math.min(1, parentColor.getBlue() + (Math.random() * 2 - 1) * variation));

		return Color.color(r, g, b);
	}

	public synchronized boolean takeDamage(double damage){
		this.setHp(this.getHp() - damage);

		if(this.getHp() <= 0){
			threadSafeBackgroundGrid.removeCreature(this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.getId());
			creatureArray.removeCreature(this.getId());
			App.removeCreature(this.getId());

			return true;
		}

		return false;
	}

	// this finds the closest food searching only within it's view without slowing down the simulation
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
							double creatureClosestX = Math.max(this.getX(), Math.min(food.getX(), this.getX() + this.getWidth()));
							double creatureClosestY = Math.max(this.getY(), Math.min(food.getY(), this.getY() + this.getHeight()));

							double distance = Math.sqrt(Math.pow(food.getX() - creatureClosestX, 2) + Math.pow(food.getY() - creatureClosestY, 2));

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
	
	// this finds the closest creature searching only within it's view without slowing down the simulation
	private Object[] findClosestCreature() throws InterruptedException{
		Creature closestCreature = null;
		double closestDistance = Double.MAX_VALUE;

		int startX = (int) Math.max(0, Math.min(this.getX() / 10, backgroundGrid.length - 1));
		int endX = (int) Math.max(0, Math.min(((this.getX() + this.getWidth()) / 10) - 1, backgroundGrid.length - 1));

		int startY = (int) Math.max(0, Math.min(this.getY() / 10, backgroundGrid[0].length - 1));
		int endY = (int) Math.max(0, Math.min(((this.getY() + this.getHeight()) / 10) - 1, backgroundGrid[0].length - 1));

		int range = this.getView();

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
							
							if(distance < closestDistance && distance <= this.getView() * 10){
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

			double healAmount = this.getMaxHp() * this.getHpRegenPercentage();
			this.setHp(Math.min(this.getMaxHp(), this.getHp() + healAmount));

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

	public double getDistanceToCreature(Creature creature) {

		double distanceX = Math.max(0, Math.max(this.getX() - (creature.getX() + creature.getWidth()), creature.getX() - (this.getX() + this.getWidth())));

		double distanceY = Math.max(0, Math.max(this.getY() - (creature.getY() + creature.getHeight()), creature.getY() - (this.getY() + this.getHeight())));

		return Math.sqrt(distanceX * distanceX + distanceY * distanceY);
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

	public synchronized double getMaxHp(){
		return maxHp;
	}

	public synchronized double getHpRegenPercentage(){
		return hpRegenPercentage;
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

	public synchronized double getReproductionRate() {
		return reproductionRate;
	}

	public synchronized double getLastReproductionTime() {
		return lastReproductionTime;
	}
	public void setLastReproductionTime(long lastReproductionTime) {
		this.lastReproductionTime = lastReproductionTime;
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
	public synchronized  CreatureSaveData toCreatureSaveData(){
		return new CreatureSaveData(this.getId(),this.getX(),this.getY(),this.getWidth(),this.getHeight(),this.getHp(),this.getBaseAttack(),this.getColor(),this.getSpeed(),System.currentTimeMillis()-lastReproductionTime);
	}
}
