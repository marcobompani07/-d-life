package org.openjfx;

import javafx.scene.paint.Color;

public class Creature {
	private int id;
	private double x;
	private double y;
	private double width;
	private double height;
	private Color color;

	public Creature(int id, double x, double y, double width, double height,Color color) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.color= color;
	}

	public Creature (Creature c) {
		this(c.getId(), c.getX(), c.getY(), c.getWidth(), c.getHeight(),c.getColor());
	}

	public Creature() {
		this(0, 0, 0, 10, 10,Color.RED);
	}

	public void update(){
		double newX = this.x + Math.random() * 10 - 5;
		double newY = this.y + Math.random() * 10 - 5;

		move(newX, newY);
		System.out.println("Creature " + id + " moved to (" + newX + ", " + newY + "), from thread: " + Thread.currentThread().getName());
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

}
