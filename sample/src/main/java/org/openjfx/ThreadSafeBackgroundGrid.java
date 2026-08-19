package org.openjfx;

public class ThreadSafeBackgroundGrid {
	private BackgroundGridElement[][] grid;

	public ThreadSafeBackgroundGrid(BackgroundGridElement[][] grid){
		this.grid = grid;
	}

	public synchronized int getCreature(int x, int y){
		return grid[x][y].getCreature();
	}

	private boolean isInsideGrid(int x, int y){
		return x >= 0 && x < grid.length && y >= 0 && y < grid[0].length;
	}

	public synchronized boolean isFree(int x, int y, int creatureId){
		if(!isInsideGrid(x, y)){
			return false;
		}

		return grid[x][y].getCreature() == -1 || grid[x][y].getCreature() == creatureId;
	}

	public synchronized boolean moveCreature(double oldX, double oldY, double newX, double newY, double width, double height, int creatureId){
		int oldStartX = (int) Math.max(0, Math.min((oldX / 10), grid.length - 1));
		int oldEndX = Math.max(0, Math.min(getEndGrid(oldX, width), grid.length - 1));

		int oldStartY = (int) Math.max(0, Math.min((oldY / 10), grid[0].length - 1));
		int oldEndY = Math.max(0, Math.min(getEndGrid(oldY, height), grid[0].length - 1));

		int newStartX = (int) Math.max(0, Math.min((newX / 10), grid.length - 1));
		int newEndX = Math.max(0, Math.min(getEndGrid(newX, width), grid.length - 1));

		int newStartY = (int) Math.max(0, Math.min((newY / 10), grid[0].length - 1));
		int newEndY = Math.max(0, Math.min(getEndGrid(newY, height), grid[0].length - 1));

		for (int gridX = newStartX; gridX <= newEndX; gridX++) {
            for (int gridY = newStartY; gridY <= newEndY; gridY++) {

                if (!isFree(gridX, gridY, creatureId)) {
                    return false;
                }
            }
        }

		for (int gridX = oldStartX; gridX <= oldEndX; gridX++) {
            for (int gridY = oldStartY; gridY <= oldEndY; gridY++) {

                grid[gridX][gridY].setCreature(-1);
            }
        }

		for (int gridX = newStartX; gridX <= newEndX; gridX++) {
            for (int gridY = newStartY; gridY <= newEndY; gridY++) {

                grid[gridX][gridY].setCreature(creatureId);
            }
        }

		return true;
	}

	public synchronized boolean addCreature(double x, double y, double width, double height, int creatureId){
		int startX = (int) Math.max(0, Math.min((x / 10), grid.length - 1));
		int endX = Math.max(0, Math.min(getEndGrid(x, width), grid.length - 1));

		int startY = (int) Math.max(0, Math.min((y / 10), grid[0].length - 1));
		int endY = Math.max(0, Math.min(getEndGrid(y, height), grid[0].length - 1));

		for (int gridX = startX; gridX <= endX; gridX++) {
            for (int gridY = startY; gridY <= endY; gridY++) {

                if (!isFree(gridX, gridY, creatureId)) {
                    return false;
                }
            }
        }

		for (int gridX = startX; gridX <= endX; gridX++) {
            for (int gridY = startY; gridY <= endY; gridY++) {

                grid[gridX][gridY].setCreature(creatureId);
            }
        }

		return true;
	}

	public synchronized void removeCreature(double x, double y, double width, double height, int creatureId){
		int startX = (int) Math.max(0, Math.min((x / 10), grid.length - 1));
		int endX = Math.max(0, Math.min(getEndGrid(x, width), grid.length - 1));

		int startY = (int) Math.max(0, Math.min((y / 10), grid[0].length - 1));
		int endY = Math.max(0, Math.min(getEndGrid(y, height), grid[0].length - 1));

		for (int gridX = startX; gridX <= endX; gridX++) {
            for (int gridY = startY; gridY <= endY; gridY++) {

                if (grid[gridX][gridY].getCreature() == creatureId) {
                    grid[gridX][gridY].setCreature(-1);
                }
            }
        }
	}

	private int getEndGrid(double position, double size) {
		return (int) Math.ceil((position + size) / 10) - 1;
	}
}
