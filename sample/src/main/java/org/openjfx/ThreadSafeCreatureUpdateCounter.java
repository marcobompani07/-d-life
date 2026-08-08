package org.openjfx;

public class ThreadSafeCreatureUpdateCounter {
	private int counter;
	private final int max;
	private ThreadSafeCreatureArray creatureArray;

	public ThreadSafeCreatureUpdateCounter(ThreadSafeCreatureArray creatureArray) {
		this.creatureArray = creatureArray;
		this.max = creatureArray.getLength();
		this.counter = 0;
	}

	public synchronized int getNext() {
		int current = counter;

		while (creatureArray.getCreatureAtIndex(current) == null) {
			current++;
			if (current >= max) {
				current = 0;
			}
		}

		counter = current + 1;

		if(counter >= max){
			counter = 0;
		}

		return current;
	}
}
