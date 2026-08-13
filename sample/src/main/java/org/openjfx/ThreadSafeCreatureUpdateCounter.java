

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

		counter = current + 1;

		if(counter >= max){
			System.out.println("counter is maxed");
			long currentTime=System.currentTimeMillis();
			if ((currentTime-startTime)<=10){
				System.out.println("sleeping");
				Thread.sleep(10-(currentTime-startTime));
			}else{
				System.out.println("not sleeping");
				System.err.println("cicle time:"+(currentTime-startTime));
			}
			
			startTime=System.currentTimeMillis();
			counter=0;
		}

		return current;
	}
}