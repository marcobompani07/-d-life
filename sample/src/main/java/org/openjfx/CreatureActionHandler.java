package org.openjfx;

public class CreatureActionHandler {
	private int creatureIndex;
	private ThreadSafeCreatureUpdateCounter updateCounter;
	private ThreadSafeCreatureArray creatureArray;

	public CreatureActionHandler(ThreadSafeCreatureArray creatureArray, ThreadSafeCreatureUpdateCounter updateCounter) {
		this.creatureArray = creatureArray;
		this.updateCounter = updateCounter;
		this.creatureIndex = updateCounter.getNext();
	}

	public void updateCreature() {
		Creature creature = creatureArray.getCreatureAtIndex(creatureIndex);
		if (creature != null) {
			creature.update();
		}

		creatureIndex = updateCounter.getNext();
		updateCreature();
	}
}
