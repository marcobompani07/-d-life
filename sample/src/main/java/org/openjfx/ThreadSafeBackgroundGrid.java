package org.openjfx;

public class ThreadSafeBackgroundGrid {
	private BackgroundGridElement[][] grid;

	public ThreadSafeBackgroundGrid(BackgroundGridElement[][] grid){
		this.grid = grid;
	}

	public synchronized int getCreature(int x, int y){
		return grid[x][y].getCreature();
	}

	public synchronized boolean isFree(int x, int y, int creatureId){
		return grid[x][y].getCreature() == -1 || grid[x][y].getCreature() == creatureId;
	}

	public synchronized boolean moveCreature(double oldX, double oldY, double newX, double newY, double width, double height, int creatureId){
		int oldStartX = (int) (oldX / 10);
        int oldEndX = getEndGrid(oldX, width);

        int oldStartY = (int) (oldY / 10);
        int oldEndY = getEndGrid(oldY, height);

        int newStartX = (int) (newX / 10);
        int newEndX = getEndGrid(newX, width);

        int newStartY = (int) (newY / 10);
        int newEndY = getEndGrid(newY, height);

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
		int startX = (int) (x / 10);
        int endX = getEndGrid(x, width);

        int startY = (int) (y / 10);
        int endY = getEndGrid(y, height);

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
		int startX = (int) (x / 10);
        int endX = getEndGrid(x, width);

        int startY = (int) (y / 10);
        int endY = (int) ((y + height) / 10);

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
