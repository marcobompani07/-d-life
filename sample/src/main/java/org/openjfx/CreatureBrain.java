package org.openjfx;

public class CreatureBrain {
	public static final int RANDOM_MOVEMENT = 0;
    public static final int MOVE_TO_FOOD = 1;
    public static final int MOVE_TO_CREATURE = 2;
    public static final int ATTACK = 3;
    public static final int EAT = 4;
	public static final int REPRODUCE = 6;

	private Creature creatureTarget;

	public CreatureBrain() {
		this.creatureTarget = null;
	}

	public int think(Object[] closestFoodData, Object[] closestCreatureData, double creatureX, double creatureY, int view, int attackRange, double hunger, double hp, double maxHp, Creature callingCreature) {
		Food closestFood = (Food) closestFoodData[0];
		double closestFoodDistance = (double) closestFoodData[1];

		Creature closestCreature = (Creature) closestCreatureData[0];
		double closestCreatureDistance = (double) closestCreatureData[1];

		if (creatureTarget != null) {
			double distanceX = creatureTarget.getX() - creatureX;
			double distanceY = creatureTarget.getY() - creatureY;

			double targetDistance = creatureTarget.getDistanceToCreature(callingCreature);

			if (targetDistance > view * 10 || creatureTarget.getHp() <= 0) {
				creatureTarget = null;
			}
		}

		if (creatureTarget == null && closestCreature != null) {
			creatureTarget = closestCreature;
		}

		if(closestFood != null && closestFoodDistance <= Food.FOOD_WIDTH / 2){
			return EAT;
		}

		if (closestFood != null && (hunger >= 40 || creatureTarget == null || hp <= (maxHp - ((maxHp * 60) / 100)))){
			return MOVE_TO_FOOD;
		}

		if(creatureTarget != null && hp <= (hp - ((hp * 60) / 100))){
			return RANDOM_MOVEMENT;
		}

		if(creatureTarget != null){
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
