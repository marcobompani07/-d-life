package org.openjfx;

public class Creature {
	private int id;
	private double x;
	private double y;
	private double width;
	private double height;

	public Creature(int id, double x, double y, double width, double height) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public Creature (Creature c) {
		this(c.getId(), c.getX(), c.getY(), c.getWidth(), c.getHeight());
	}

	public Creature() {
		this(0, 0, 0, 10, 10);
	}

	public void update(){
		double newX = this.x + Math.random() * 10 - 5;
		double newY = this.y + Math.random() * 10 - 5;

		move(newX, newY);
	}

	public void move(double newX, double newY) {
		this.x = newX;
		this.y = newY;
	}

	public int getId() {
		return id;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}
}
