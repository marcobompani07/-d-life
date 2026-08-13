package org.openjfx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class MapGraphicsHandler {
    private GraphicsContext gc;
    private  double standardUnit;
    private  double canvasWidth;
    private  double canvasHeight;
    private double  offsetX;
    private double  offsetY;


    public MapGraphicsHandler(GraphicsContext gc, double standardUnit , double canvasWidth, double canvasHeight) {
        this.gc = gc;
        this.standardUnit = standardUnit;
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        offsetX=0;
        offsetY=0;
    }
   

    public void update(Creature[] creatures,Food[] foodArray)  {
        gc.clearRect(0, 0, canvasWidth, canvasHeight);
        for (int i=0;i<creatures.length;i++) {
            Creature creature=creatures[i];
            if(creature!=null){
                gc.setFill(creature.getColor());
                gc.fillRect(creature.getX()*standardUnit *App.ZOOM+offsetX,creature.getY()*standardUnit*App.ZOOM+offsetY,creature.getWidth()*standardUnit*App.ZOOM,creature.getHeight()*standardUnit*App.ZOOM);
            }
        }
        for(int i=0;i<foodArray.length;i++){
            if(foodArray[i]!=null){
                gc.setFill(Color.LIME);
                gc.fillOval(foodArray[i].getX()*standardUnit*App.ZOOM+offsetX,foodArray[i].getY()*standardUnit*App.ZOOM+offsetY, 10*standardUnit*App.ZOOM, 10*standardUnit*App.ZOOM);
                
            }
        }
    }

    public GraphicsContext getGc() {
        return this.gc;
    }

    public void setGc(GraphicsContext gc) {
        this.gc = gc;
    }

    public double getstandardUnit() {
        return this.standardUnit;
    }

    public void setstandardUnit(double standardUnit) {
        this.standardUnit = standardUnit;
    }

    public void addOffsetX(double offset){
        offsetX+=offset;
    }
    public void addOffsetY(double offset){
        offsetY+=offset;
    }
}
