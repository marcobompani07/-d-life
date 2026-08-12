package org.openjfx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class App extends Application {
    private static final int MAX_CREATURES = 100;
	private static final int INITIAL_CREATURES = 100;
	private static final int WORKER_COUNT = Runtime.getRuntime().availableProcessors()-2;
	private CreatureWorker[] workers;
	static final double WORLD_WIDTH = 1000;
	static double WORLD_HEIGHT = 0;
    static double ZOOM=1;
    private boolean startDrag=false;
    private boolean hasDragged=false;
    private double previusDragX=0,previusDragY=0;
    @Override
    public void start(Stage stage) throws InterruptedException {
        stage.setMaximized(true);
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #222222;"); 
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        Pane backgroundPane= new Pane();
        backgroundPane.setStyle("-fx-background-color: #343434;");
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
        double standardUnit=backgroundPane.maxWidthProperty().get()/WORLD_WIDTH;
        WORLD_HEIGHT=backgroundPane.maxHeightProperty().get()/standardUnit;
        MapGrapychHandler mapGrapychHandler=new MapGrapychHandler(gc,standardUnit,canvasWidth,canvasHeight);
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
        //zoomSpinner.setTranslateX(15*standardUnit);
        Button zoomButton = new Button("SetZoom");
        zoomButton.setOnAction(e -> {
            int zoomValue=zoomSpinner.getValue();
            ZOOM=((double)zoomValue)/100;
        });
        HBox topBar = new HBox(10*standardUnit, zoomSpinner,zoomButton);
        topBar.setStyle("-fx-padding: "+(10*standardUnit)+"px;-fx-background-color: #494848;");
        topBar.setSpacing(15*standardUnit);
        root.setTop(topBar);

        



		Creature[] creatures = new Creature[MAX_CREATURES];
        Food[] foodArray=new Food[MAX_CREATURES*10];
        BackgroundGridElement[][] backgroundGrid= new BackgroundGridElement[(int)WORLD_WIDTH][(int)WORLD_HEIGHT];
        for(int i=0;i<backgroundGrid.length;i++){
            for(int i2=0;i2<backgroundGrid[i].length;i2++){
                backgroundGrid[i][i2]=new BackgroundGridElement();
            }
        }
        ThreadSafeFoodArray threadSafeFoodArray=new ThreadSafeFoodArray(foodArray);
		
		for(int i = 0; i < INITIAL_CREATURES; i++) {
			creatures[i] = new Creature(i, Math.random() *WORLD_WIDTH, Math.random() * WORLD_HEIGHT, 10, 10, Color.color(Math.random(), Math.random(), Math.random()), Math.random() + 1, threadSafeFoodArray);
		}
		
        FoodGeneratorThread foodGeneratorThread=new FoodGeneratorThread(threadSafeFoodArray, backgroundGrid,10);
		ThreadSafeUpdateMapQueueCounter mapCounter=new ThreadSafeUpdateMapQueueCounter();
        ThreadSafeCreaturesArray creaturesArray=new ThreadSafeCreaturesArray(creatures);
        UpdateGuiRunnableGenerator updateRunnableGenerator=new UpdateGuiRunnableGenerator(mapGrapychHandler, mapCounter);
        MovmentHandlingThread movmentHandlingThread=new MovmentHandlingThread(updateRunnableGenerator,mapCounter,creaturesArray,threadSafeFoodArray);
        ThreadSafeCreatureUpdateCounter updateCounter = new ThreadSafeCreatureUpdateCounter(creaturesArray);
		workers = new CreatureWorker[WORKER_COUNT];

		for (int i = 0; i < WORKER_COUNT; i++) {
			CreatureActionHandler actionHandler = new CreatureActionHandler(creaturesArray, updateCounter);
			workers[i] = new CreatureWorker(actionHandler);
			workers[i].start();
		}
        foodGeneratorThread.start();
		movmentHandlingThread.start();
        
        stage.setOnCloseRequest(event -> {

            movmentHandlingThread.Stop();
            foodGeneratorThread.Stop();
			for (CreatureWorker worker : workers) {
                worker.stopWorker();
            }

            Platform.exit();
        });
       
    }

    public static void main(String[] args) {
        launch();
    }


}