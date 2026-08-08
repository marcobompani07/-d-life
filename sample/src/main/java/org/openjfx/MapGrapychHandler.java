package org.openjfx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class MapGrapychHandler {
    private GraphicsContext gc;
    private  double standardUnitX;
    private  double standardUnitY;
    private  double canvasWidth;
    private  double canvasHeight;
    private Creature[] creatures;


    public MapGrapychHandler(GraphicsContext gc, double standardUnitX, double standardUnitY, double canvasWidth, double canvasHeight, Creature[] creatures) {
        this.gc = gc;
        this.standardUnitX = standardUnitX;
        this.standardUnitY = standardUnitY;
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.creatures = creatures;
    }
   

    public void update(){
        gc.clearRect(0, 0, canvasWidth, canvasHeight);
        for (int i=0;i<creatures.length;i++) {
            if(creatures[i]!=null){
                gc.setFill(creatures[i].getColor());
                gc.fillRect(creatures[i].getX()*standardUnitX,creatures[i].getY(),creatures[i].getHeight());
            }
        }
    }

    public GraphicsContext getGc() {
        return this.gc;
    }

    public void setGc(GraphicsContext gc) {
        this.gc = gc;
    }

    public double getStandardUnitX() {
        return this.standardUnitX;
    }

    public void setStandardUnitX(double standardUnitX) {
        this.standardUnitX = standardUnitX;
    }

    public double getStandardUnitY() {
        return this.standardUnitY;
    }

    public void setStandardUnitY(double standardUnitY) {
        this.standardUnitY = standardUnitY;
    }

    public Creature[] getCreatures() {
        return this.creatures;
    }

    public void setCreatures(Creature[] creatures) {
        this.creatures = creatures;
    }

    public double getCanvasWidth() {
        return this.canvasWidth;
    }

    public void setCanvasWidth(double canvasWidth) {
        this.canvasWidth = canvasWidth;
    }

    public double getCanvasHeight() {
        return this.canvasHeight;
    }

    public void setCanvasHeight(double canvasHeight) {
        this.canvasHeight = canvasHeight;
    }


}
