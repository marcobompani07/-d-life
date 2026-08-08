package org.openjfx;

public class CreatureWorker extends Thread  {
	private boolean running;
	private CreatureActionHandler actionHandler;

	public CreatureWorker(CreatureActionHandler actionHandler) {
		this.actionHandler = actionHandler;
		this.running = true;
	}

	@Override
	public void run() {
		while (running) {
			actionHandler.updateCreature();
		}
	}

	public void stopWorker() {
		running = false;
	}
}
