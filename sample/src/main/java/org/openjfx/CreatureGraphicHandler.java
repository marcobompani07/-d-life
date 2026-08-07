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
    public void move(double newX,double newY){
        parenGraphicsContext.clearRect(x, y, width, heigth);
        this.x=newX;
        this.y=newY;
        place();
    }
    public void place(){
        parenGraphicsContext.setFill(color);
        parenGraphicsContext.fillRect(x, y, width, heigth);
    }
}
