

package org.openjfx;

public class ThreadSafeCreatureUpdateCounter {
	private int counter;
	private final int max;
	private ThreadSafeCreaturesArray creatureArray;
	private long startTime;

	public ThreadSafeCreatureUpdateCounter(ThreadSafeCreaturesArray creatureArray) {
		this.creatureArray = creatureArray;
		this.max = creatureArray.getLength();
		this.counter = 0;
		startTime=System.currentTimeMillis();
	}

	public synchronized int getNext() throws InterruptedException {
		int current = counter;

		while (creatureArray.getCreatures()[current] == null) {
			current++;

			if (current >= max) {
				current = 0;
			}
		}

		counter = current + 1;

		if(counter >= max){
			if ((System.currentTimeMillis()-startTime)<=10){
				Thread.sleep(10-(System.currentTimeMillis()-startTime));
			}
			startTime=System.currentTimeMillis();
			counter=0;
		}

		return current;
	}
}