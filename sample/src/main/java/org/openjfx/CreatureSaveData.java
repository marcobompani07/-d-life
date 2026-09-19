package org.openjfx;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javafx.scene.paint.Color;
public class CreatureSaveData {
	private int id;
	private double x;
	private double y;
	private double directionX;
	private double directionY;
	private double width;
	private double height;
	private double red;
	private double green;
	private double blue;
	private double speed;
	private double hunger;
	private double hp;
	private double maxHp;
	private double baseAttack;
	private int view;
	private int attackRange;
	private long lastAttackTime = 0;
	private static final long ATTACK_COOLDOWN = 500;
	private double reproductionRate;
	private long lastReproductionTimeOffset =0;
	private static final long REPRODUCTION_COOLDOWN = 10000;

	public CreatureSaveData(int id, double x, double y, double width, double height, double hp, double baseAttack, Color color, double speed, long lastReproductionTimeOffset) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.speed = speed;
		this.red=color.getRed();
		this.blue=color.getBlue();
		this.green=color.getGreen();
		this.hunger = 0;
		this.hp = hp;
		this.maxHp = hp;
		this.baseAttack = baseAttack;
		this.view = 5;
		this.attackRange = 1;
		this.reproductionRate = 0.1;
		this.lastReproductionTimeOffset=lastReproductionTimeOffset;
		double angle = Math.random() * 2 * Math.PI;
		this.setDirectionX(Math.cos(angle));
		this.setDirectionY(Math.sin(angle));
	}

	public CreatureSaveData (CreatureSaveData c) {
		this(c.getId(), c.getX(), c.getY(), c.getWidth(), c.getHeight(), c.getHp(), c.getBaseAttack(), c.getColor(), c.getSpeed(),c.getLastReproductionTime());
    }

	public CreatureSaveData() {
		this(0, 0, 0, 10, 10, 100, 5,Color.RED, 2.0,0 );
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
	@JsonIgnore
	public synchronized Color getColor() {
		return Color.color(red, green, blue );
	}
	@JsonIgnore
	public synchronized void setColor(Color color) {
		this.red=color.getRed();
		this.blue=color.getBlue();
		this.green=color.getGreen();;
	}

	public synchronized double getSpeed() {
		return speed;
	}

	public synchronized void setSpeed(double speed) {
		this.speed = speed;
	}

	public synchronized double getHunger() {
		return hunger;
	}

	public synchronized void setHunger(double hunger) {
		this.hunger = hunger;
	}

	public synchronized int getView() {
		return view;
	}

	public synchronized int getAttackRange(){
		return attackRange;
	}

	public synchronized double getHp(){
		return hp;
	}

	public synchronized double getMaxHp(){
		return maxHp;
	}

	public synchronized void setHp(double hp){
		this.hp = hp;
	}

	public synchronized double getBaseAttack(){
		return baseAttack;
	}

	public synchronized void setBaseAttack(double baseAttack){
		this.baseAttack = baseAttack;
	}

	public synchronized double getDirectionX() {
		return directionX;
	}

	public synchronized  void setDirectionX(double directionX) {
		this.directionX = directionX;
	}

	public synchronized double getDirectionY() {
		return directionY;
	}

	public synchronized void setDirectionY(double directionY) {
		this.directionY = directionY;
	}

	public synchronized double getReproductionRate() {
		return reproductionRate;
	}

	public long getLastReproductionTimeOffset() {
		return this.lastReproductionTimeOffset;
	}

	public void setLastReproductionTimeOffset(long lastReproductionTimeOffset) {
		this.lastReproductionTimeOffset = lastReproductionTimeOffset;
	}

	@JsonIgnore
	public synchronized long getLastReproductionTime() {
		return System.currentTimeMillis()-lastReproductionTimeOffset;
	}


	public double getRed() {
		return this.red;
	}

	public void setRed(double red) {
		this.red = red;
	}

	public double getGreen() {
		return this.green;
	}

	public void setGreen(double green) {
		this.green = green;
	}

	public double getBlue() {
		return this.blue;
	}

	public void setBlue(double blue) {
		this.blue = blue;
	}

}
