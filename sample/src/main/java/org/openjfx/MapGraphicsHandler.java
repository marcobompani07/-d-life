package org.openjfx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;

public class MapGraphicsHandler {
    private GraphicsContext gc;
    private  double standardUnit;
    private  double canvasWidth;
    private  double canvasHeight;
    private double  offsetX;
    private double  offsetY;
    private int focusedCreatureId;

	private Image backgroundImage;
	private static final double BACKGROUND_TILE_SIZE = 1024;


    public MapGraphicsHandler(GraphicsContext gc, double standardUnit , double canvasWidth, double canvasHeight) {
        this.gc = gc;
        this.standardUnit = standardUnit;
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        offsetX=0;
        offsetY=0;
        focusedCreatureId=-1;

		backgroundImage = new Image(getClass().getResource("/img/grass.png").toExternalForm());
    }
   

    public void update(Creature[] creatures,Food[] foodArray)  {
        gc.clearRect(0, 0, canvasWidth, canvasHeight);

		drawBackground();

        for (int i=0;i<creatures.length;i++) {
            Creature creature=creatures[i];
            if(creature!=null){
                if(focusedCreatureId==i){
                    setOffsetX(((-(creature.getX()+creature.getWidth()/2)*standardUnit*App.ZOOM)+canvasWidth/2));
                    setOffsetY(((-(creature.getY()+creature.getHeight()/2)*standardUnit*App.ZOOM)+canvasHeight/2));
                }
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

	public void drawBackground(){
		double tileWidth = BACKGROUND_TILE_SIZE * standardUnit * App.ZOOM;
		double tileHeight = BACKGROUND_TILE_SIZE * standardUnit * App.ZOOM;

		double worldWidth = App.WORLD_WIDTH * standardUnit * App.ZOOM;
		double worldHeight = App.WORLD_HEIGHT * standardUnit * App.ZOOM;

		int tilesX = (int) Math.ceil(worldWidth / tileWidth);
		int tilesY = (int) Math.ceil(worldHeight / tileHeight);

		for (int x = 0; x < tilesX; x++) {
			for (int y = 0; y < tilesY; y++) {
				gc.drawImage(
					backgroundImage,
					x * tileWidth + offsetX,
					y * tileHeight + offsetY,
					tileWidth,
					tileHeight
				);
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
        if( offsetX+offset>0){
            offsetX=0;
        }else if((-(offsetX+offset)>((App.WORLD_WIDTH*standardUnit*App.ZOOM-canvasWidth)))){ 
            offsetX=-((App.WORLD_WIDTH*standardUnit*App.ZOOM-canvasWidth));
        }else{
            offsetX+=offset;
        }
    }
    public void addOffsetY(double offset){
        if(offsetY+offset>0){
            offsetY=0;
        }else if((-(offsetY+offset))>((App.WORLD_HEIGHT*standardUnit*App.ZOOM-canvasHeight))){ 
            offsetY=-((App.WORLD_HEIGHT*standardUnit*App.ZOOM-canvasHeight));
        }else{
            offsetY+=offset;
        }
    }

    public void setOffsetX(double offset){
        if(offset>0){
            offsetX=0;
        }else if((-(offset))>((App.WORLD_WIDTH*standardUnit*App.ZOOM-canvasWidth))){ 
            offsetX=(-(App.WORLD_WIDTH*standardUnit*App.ZOOM-canvasWidth));
        }else{
            offsetX=offset;
        }
    }
    public void setOffsetY(double offset){
        if(offset>0){
            offsetY=0;
        }else if((-(offset))>((App.WORLD_HEIGHT*standardUnit*App.ZOOM-canvasHeight))){ 
            offsetY=(-(App.WORLD_HEIGHT*standardUnit*App.ZOOM-canvasHeight));
        }else{
            offsetY=offset;
        }
    }

    public double getOffsetX() {
        return this.offsetX;
    }


    public double getOffsetY() {
        return this.offsetY;
    }


    public void focusCreature(int id){
        this.focusedCreatureId=id;
    }
}
