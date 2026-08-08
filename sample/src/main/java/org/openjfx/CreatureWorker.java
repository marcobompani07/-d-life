package org.openjfx;

public class CreatureWorker extends Thread  {
	private volatile boolean running;
	private CreatureActionHandler actionHandler;

	public CreatureWorker(CreatureActionHandler actionHandler) {
		this.actionHandler = actionHandler;
		this.running = true;
	}

	@Override
	public void run() {
		while (running) {
			try {
				actionHandler.updateCreature();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}
	}

	public void stopWorker() {
		running = false;
	}
}
