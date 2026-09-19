package org.openjfx;

public class CreatureBrain {
	public static final int RANDOM_MOVEMENT = 0;
    public static final int MOVE_TO_FOOD = 1;
    public static final int MOVE_TO_CREATURE = 2;
    public static final int ATTACK = 3;
    public static final int EAT = 4;
	public static final int FLEE = 5;
	public static final int REPRODUCE = 6;

	private Creature creatureTarget;

	public CreatureBrain() {
		this.creatureTarget = null;
	}

	public int think(Object[] closestFoodData, Object[] closestCreatureData, double creatureX, double creatureY, int view, int attackRange, double hunger, double hp, double maxHp, Creature callingCreature) {
		Food closestFood = (Food) closestFoodData[0];
		double closestFoodDistance = (double) closestFoodData[1];
		Creature closestCreature = (Creature) closestCreatureData[0];

		if (creatureTarget != null && creatureTarget.getHp() <= 0) {
            creatureTarget = null;
        }

		if (creatureTarget == null && closestCreature != null && closestCreature.getHp() > 0) {
			creatureTarget = closestCreature;
		}
		
		if(creatureTarget != null){
			double targetDistance = callingCreature.getDistanceToCreature(creatureTarget);
			if(targetDistance > view * 10 || creatureTarget.getHp() <= 0){
				creatureTarget = null;
			}
		}

		if(closestFood != null && closestFoodDistance <= Food.FOOD_WIDTH / 2){
			return EAT;
		}

		boolean isLowHp = hp <= (maxHp * 0.60);
		boolean isTooHungry = hunger >= 75.0;

		if(isLowHp || isTooHungry){
			if(closestFood != null){
				return MOVE_TO_FOOD;
			} else if (creatureTarget != null && creatureTarget.getHp() > 0){
				return FLEE;
			}
		}

		if (closestFood != null && (hunger >= 40 || creatureTarget == null)){
			return MOVE_TO_FOOD;
		}

		long currentTime = System.currentTimeMillis();
		boolean isCooldownReady = (currentTime - callingCreature.getLastReproductionTime()) >= Creature.REPRODUCTION_COOLDOWN;
		boolean isWellFedAndHealthy = (hunger < 30.0) && (hp >= maxHp * 0.80);

		if(isCooldownReady && isWellFedAndHealthy) {
			if(Math.random() <= callingCreature.getReproductionRate()){
				if(creatureTarget == null){
					return REPRODUCE;
				}

				double targetDistance = callingCreature.getDistanceToCreature(creatureTarget);
				if(targetDistance <= (attackRange * 10)){
					return REPRODUCE;
				} else {
					return MOVE_TO_CREATURE;
				}
			}
		}

		if(creatureTarget != null && creatureTarget.getHp() > 0){
			double targetDistance = callingCreature.getDistanceToCreature(creatureTarget);

			if(targetDistance <= attackRange * 10){
				return ATTACK;
			}

			return MOVE_TO_CREATURE;
		}
		
		return RANDOM_MOVEMENT;
	}

	public Creature getCreatureTarget() {
		return this.creatureTarget;
	}

	public void setCreatureTarget(Creature creatureTarget){
		this.creatureTarget = creatureTarget;
	}
}
