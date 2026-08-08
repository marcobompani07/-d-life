package org.openjfx;

import javafx.scene.canvas.GraphicsContext;

public class MapGrapychHandler {
    private GraphicsContext gc;
    private  double standardUnitX;
    private  double standardUnitY;
    private  double canvasWidth;
    private  double canvasHeight;
    private ThreadSafeCreaturesArray creatures;


    public MapGrapychHandler(GraphicsContext gc, double standardUnitX, double standardUnitY, double canvasWidth, double canvasHeight,ThreadSafeCreaturesArray creatures) {
        this.gc = gc;
        this.standardUnitX = standardUnitX;
        this.standardUnitY = standardUnitY;
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.creatures = creatures;
    }
   

    public void update()  {
        gc.clearRect(0, 0, canvasWidth, canvasHeight);
        for (int i=0;i<creatures.getLength();i++) {
            try {
                Creature creature=creatures.request(i);
                if(creature!=null){
                    gc.setFill(creature.getColor());
                    gc.fillRect(creature.getX()*standardUnitX,creature.getY()*standardUnitY,creature.getWidth()*standardUnitX,creature.getHeight()*standardUnitX);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
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
}
