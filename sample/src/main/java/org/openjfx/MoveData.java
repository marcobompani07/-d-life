package org.openjfx;

public class MoveData {
    private double x,y;
    private int creatureNumber;
    public MoveData(double x,double y,int creatureNumber){
        this.x=x;
        this.y=x;
        this.creatureNumber=creatureNumber;
    }

    public double getX() {
        return this.x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return this.y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public int getCreatureNumber() {
        return this.creatureNumber;
    }

    public void setCreatureNumber(int creatureNumber) {
        this.creatureNumber = creatureNumber;
    }
    
}
