package org.openjfx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class MapGrapychHandler {
    private GraphicsContext gc;
    private  double standardUnit;
    private  double canvasWidth;
    private  double canvasHeight;


    public MapGrapychHandler(GraphicsContext gc, double standardUnit , double canvasWidth, double canvasHeight) {
        this.gc = gc;
        this.standardUnit = standardUnit;
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
    }
   

    public void update(Creature[] creatures,Food[] foodArray)  {
        gc.clearRect(0, 0, canvasWidth, canvasHeight);
        for (int i=0;i<creatures.length;i++) {
            Creature creature=creatures[i];
            if(creature!=null){
                gc.setFill(creature.getColor());
                gc.fillRect(creature.getX()*standardUnit,creature.getY()*standardUnit,creature.getWidth()*standardUnit,creature.getHeight()*standardUnit);
            }
        }
        for(int i=0;i<foodArray.length;i++){
            if(foodArray[i]!=null){
                gc.setFill(Color.LIME);
                gc.fillOval(foodArray[i].getX()*standardUnit,foodArray[i].getY()*standardUnit, 10*standardUnit, 10*standardUnit);
                
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
}
