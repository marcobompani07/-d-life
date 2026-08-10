package org.openjfx;

import javafx.scene.paint.Color;

public class Creature {
	private int id;
	private double x;
	private double y;
	private double width;
	private double height;
	private Color color;
	private double speed;
	private double hunger;

	public Creature(int id, double x, double y, double width, double height,Color color, double speed) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.speed = speed;
		this.color= color;
		this.hunger = 0;
	}

	public Creature (Creature c) {
		this(c.getId(), c.getX(), c.getY(), c.getWidth(), c.getHeight(),c.getColor(), c.getSpeed());
	}

	public Creature() {
		this(0, 0, 0, 10, 10,Color.RED, 2.0);
	}

	public void update(){

		if(hunger < 100){
			double newX = this.x + Math.random() * this.speed - this.speed / 2;
			double newY = this.y + Math.random() * this.speed - this.speed / 2;

			newX = Math.max(0, Math.min(App.WORLD_WIDTH - this.width, newX));
			newY = Math.max(0, Math.min(App.WORLD_HEIGHT - this.height, newY));
			
			move(newX, newY);

			hunger += (this.speed / 100);
		}
	}

	public synchronized void move(double newX, double newY) {
		this.x = newX;
		this.y = newY;
	}

	public synchronized  int getId() {
		return id;
	}

	public synchronized double getX() {
		return x;
	}

	public synchronized  void setX(double x) {
		this.x = x;
	}

	public synchronized double getY() {
		return y;
	}

	public synchronized void setY(double y) {
		this.y = y;
	}

	public synchronized double getWidth() {
		return width;
	}

	public synchronized void setWidth(double width) {
		this.width = width;
	}

	public synchronized double getHeight() {
		return height;
	}

	public synchronized void setHeight(double height) {
		this.height = height;
	}

	public synchronized Color getColor() {
		return this.color;
	}

	public synchronized void setColor(Color color) {
		this.color = color;
	}

	public synchronized double getSpeed() {
		return speed;
	}

	public synchronized void setSpeed(double speed) {
		this.speed = speed;
	}

}
