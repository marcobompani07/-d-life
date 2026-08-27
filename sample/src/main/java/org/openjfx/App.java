package org.openjfx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class App extends Application {
    private static final int MAX_CREATURES = 100000;
	private static final int INITIAL_CREATURES = 200;
    private static final int FOODSPAWNOUNT = 100;

	private static int nextCreatureId = 0;
	private static int currentCreatureCount = 0;

	private static final int WORKER_COUNT = Runtime.getRuntime().availableProcessors()-2;
	private CreatureWorker[] workers;

    static final double WORLD_MULTIPLIYER = 10;
	static final double WORLD_WIDTH = 1000*WORLD_MULTIPLIYER;
    static final int UNIT_DIVISION = 1000;
	static double WORLD_HEIGHT = 0;

    static double ZOOM = 1;
    private boolean startDrag = false;
    private boolean hasDragged = false;
    private double previusDragX = 0, previusDragY = 0;

    @Override
    public void start(Stage stage) throws InterruptedException {
        stage.setMaximized(true);
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #222222;"); 
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        Pane backgroundPane= new Pane();
        backgroundPane.setStyle("-fx-background-color: #343434;" );
        root.setCenter(backgroundPane);
        //backgroundPane.setStyle("-fx-background-image: url("+getClass().getResource("/img/checkerboard-20x20.png").toExternalForm()+"); " +"-fx-background-repeat: repeat; " );
        root.setCenter(backgroundPane);
        backgroundPane.setMaxWidth(stage.getWidth()*0.75);
        backgroundPane.setMaxHeight(stage.getHeight()*0.75);
        Canvas canvas=new Canvas();
        canvas.widthProperty().bind(backgroundPane.maxWidthProperty());
        canvas.heightProperty().bind(backgroundPane.maxHeightProperty());
        GraphicsContext gc = canvas.getGraphicsContext2D();
        backgroundPane.getChildren().add(canvas);
        double canvasWidth=canvas.getWidth();
        double canvasHeight=canvas.getHeight();
        double standardUnit=backgroundPane.maxWidthProperty().get()/UNIT_DIVISION;
        WORLD_HEIGHT=backgroundPane.maxHeightProperty().get()/standardUnit*WORLD_MULTIPLIYER;
        MapGraphicsHandler mapGrapychHandler=new MapGraphicsHandler(gc,standardUnit,canvasWidth,canvasHeight);
        canvas.setOnMouseDragged(in->{
            if(!startDrag){
                startDrag=true;
                hasDragged=true;
                previusDragX=in.getX();
                previusDragY=in.getY();
            }else{
                double newDragX=in.getX();
                double newDragY=in.getY();
                mapGrapychHandler.addOffsetX(newDragX-previusDragX);
                mapGrapychHandler.addOffsetY(newDragY-previusDragY);
                previusDragX=newDragX;
                previusDragY=newDragY;
            }
        });
        canvas.setOnMouseReleased(in->{
            if(hasDragged){
                startDrag=false;
                hasDragged=false;
            }
        });

        
        Spinner<Integer> zoomSpinner=new Spinner(10,500,100,10);
        zoomSpinner.setEditable(true);
        Button zoomButton = new Button("SetZoom");
        zoomButton.setOnAction(e -> {
            int zoomValue=zoomSpinner.getValue();
            double offsetX=(canvasWidth-(canvasWidth/((ZOOM*100)/((double)zoomValue))))/2*ZOOM;
            double offsetY=(canvasHeight-(canvasHeight/((ZOOM*100)/((double)zoomValue))))/2*ZOOM;
            ZOOM=((double)zoomValue)/100;
            mapGrapychHandler.addOffsetX(offsetX);
            mapGrapychHandler.addOffsetY(offsetY);
        });
        HBox topBar = new HBox(10*standardUnit, zoomSpinner,zoomButton);
        topBar.setStyle("-fx-padding: "+(10*standardUnit)+"px;-fx-background-color: #494848;");
        topBar.setSpacing(15*standardUnit);
        root.setTop(topBar);
        Label dispayCreatureLabel=new Label();
        dispayCreatureLabel.setTextFill(Color.web("#ffffff"));
        Pane creatureColorShowPane=new Pane();
        creatureColorShowPane.setMaxSize(50*standardUnit, 50*standardUnit);
        creatureColorShowPane.setMinSize(50*standardUnit, 50*standardUnit);
        VBox rigthBar=new VBox(10*standardUnit,creatureColorShowPane,dispayCreatureLabel);
        rigthBar.setAlignment(Pos.TOP_CENTER);
        rigthBar.setStyle("-fx-padding: "+(10*standardUnit)+"px;-fx-background-color: #494848;");
        rigthBar.setSpacing(15*standardUnit);
        rigthBar.setMaxWidth(standardUnit*150);
        rigthBar.setMinWidth(standardUnit*150);
        root.setRight(rigthBar);

		Creature[] creatures = new Creature[MAX_CREATURES];
        Food[] foodArray=new Food[MAX_CREATURES ];
        BackgroundGridElement[][] backgroundGrid= new BackgroundGridElement[(int)(WORLD_WIDTH+1) / 10][(int)(WORLD_HEIGHT+1) / 10];

        for(int i=0;i<backgroundGrid.length;i++){
            for(int i2=0;i2<backgroundGrid[i].length;i2++){
                backgroundGrid[i][i2]=new BackgroundGridElement();
            }
        }

		ThreadSafeBackgroundGrid threadSafeBackgroundGrid = new ThreadSafeBackgroundGrid(backgroundGrid);

        ThreadSafeFoodArray threadSafeFoodArray=new ThreadSafeFoodArray(foodArray);
		ThreadSafeCreaturesArray creaturesArray=new ThreadSafeCreaturesArray(creatures);
		
		nextCreatureId = 0;
		while(nextCreatureId < INITIAL_CREATURES){
			int width = 10;
			int height = 10;
			double x = Math.random() * (WORLD_WIDTH - width);
			double y = Math.random() * (WORLD_HEIGHT - height);
			double hp = Math.random() * 100 + 100;
			double baseAttack = Math.random() * 10 + 1;

			Creature creature = new Creature(nextCreatureId, x, y, width, height, hp, baseAttack, Color.color(Math.random(), Math.random(), Math.random()), Math.random() +0.2, threadSafeFoodArray, creaturesArray, backgroundGrid, threadSafeBackgroundGrid);
			
			if(threadSafeBackgroundGrid.addCreature(x, y, width, height, nextCreatureId)){
				creatures[nextCreatureId] = creature;
				nextCreatureId++;
			}
		}

        UpdateCreatureDisplayRunnableGenerator updateCreatureDisplayRunnableGenerator=new UpdateCreatureDisplayRunnableGenerator(dispayCreatureLabel,creatureColorShowPane);
        CreatureInfoDisplayThread creatureInfoDisplayThread=new CreatureInfoDisplayThread(updateCreatureDisplayRunnableGenerator, creaturesArray);
        FoodGeneratorThread foodGeneratorThread=new FoodGeneratorThread(threadSafeFoodArray, backgroundGrid,App.FOODSPAWNOUNT);
		ThreadSafeUpdateMapQueueCounter mapCounter=new ThreadSafeUpdateMapQueueCounter();
        UpdateGuiRunnableGenerator updateRunnableGenerator=new UpdateGuiRunnableGenerator(mapGrapychHandler, mapCounter);
        MovmentHandlingThread movmentHandlingThread=new MovmentHandlingThread(updateRunnableGenerator,mapCounter,creaturesArray,threadSafeFoodArray);
        ThreadSafeCreatureUpdateCounter updateCounter = new ThreadSafeCreatureUpdateCounter(creaturesArray);
		mapGrapychHandler.focusCreature(-1);
		workers = new CreatureWorker[WORKER_COUNT];
        creatureInfoDisplayThread.setCreatureId(-1);
		for (int i = 0; i < WORKER_COUNT; i++) {
			CreatureActionHandler actionHandler = new CreatureActionHandler(creaturesArray, updateCounter);
			workers[i] = new CreatureWorker(actionHandler);
			workers[i].start();
		}

        canvas.setOnMouseClicked(in->{
            double x=-((-in.getX()+mapGrapychHandler.getOffsetX())/standardUnit)/ZOOM;
            double y=-((-in.getY()+mapGrapychHandler.getOffsetY())/standardUnit)/ZOOM;
            int index=-1;
            for(int i2=0;i2<3;i2++){
                for (int i=0;i<3;i++) {
                    int indexX=((int)x)/10+(i2-1);
                    int indexY=((int)y)/10+(i-1);
                    if(indexX!=0&&indexY!=0&&index==-1){
                        index=backgroundGrid[indexX][indexY].getCreature();
                    }
                }
            }
            
            mapGrapychHandler.focusCreature(index);
            creatureInfoDisplayThread.setCreatureId(index);
            creatureColorShowPane.setStyle("");
            dispayCreatureLabel.setText("");
        });

        foodGeneratorThread.start();
		movmentHandlingThread.start();
        creatureInfoDisplayThread.start();
        stage.setOnCloseRequest(event -> {

            movmentHandlingThread.Stop();
            foodGeneratorThread.Stop();
            creatureInfoDisplayThread.Stop();
			for (CreatureWorker worker : workers) {
                worker.stopWorker();
            }

            Platform.exit();
        });
       
    }

	public static synchronized void removeCreature(){
		currentCreatureCount--;
	}

	public static synchronized void cancelCreatureCreation(){
		currentCreatureCount--;
	}

    public static void main(String[] args) {
        launch();
    }


}

