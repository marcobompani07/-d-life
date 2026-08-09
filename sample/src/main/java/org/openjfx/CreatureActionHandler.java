package org.openjfx;

public class CreatureActionHandler {
	private int creatureIndex;
	private ThreadSafeCreatureUpdateCounter updateCounter;
	private ThreadSafeCreaturesArray creatureArray;

	public CreatureActionHandler(ThreadSafeCreaturesArray creatureArray, ThreadSafeCreatureUpdateCounter updateCounter) throws InterruptedException {
		this.creatureArray = creatureArray;
		this.updateCounter = updateCounter;
		this.creatureIndex = updateCounter.getNext();
	}

	public void updateCreature() throws InterruptedException {
		Creature creature = creatureArray.request(creatureIndex);
		
		if (creature != null) {
			creature.update();
		}

		creatureArray.release(creatureIndex);
		creatureIndex = updateCounter.getNext();
	}
}
