package org.openjfx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class CreatureGraphicHandler {
    private double x,y,heigth,width;
    private Color color;
    private GraphicsContext parenGraphicsContext;
    public CreatureGraphicHandler( double x, double y,double heigth, double width,Color color,GraphicsContext parenGraphicsContext){
        this.x=x;
        this.y=y;
        this.width=width;
        this.heigth=heigth;
        this.color=color;
        this.parenGraphicsContext=parenGraphicsContext;
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

    public double getHeigth() {
        return this.heigth;
    }

    public void setHeigth(double heigth) {
        this.heigth = heigth;
    }

    public double getWidth() {
        return this.width;
    }

    public void setWidth(double width) {
        this.width = width;
    }
    
    public void place(){
        parenGraphicsContext.setFill(color);
        parenGraphicsContext.fillRect(x, y, width, heigth);
    }
}
