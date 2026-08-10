package org.openjfx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class App extends Application {
    private static final int MAX_CREATURES = 100;
	private static final int INITIAL_CREATURES = 100;
	private static final int WORKER_COUNT = Runtime.getRuntime().availableProcessors()-2;
	private CreatureWorker[] workers;

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
        gc.setStroke(Color.RED);
        gc.setLineWidth(4);
        gc.strokeRect(0, 0, canvas.getWidth(), canvas.getHeight());
        double canvasWidth=canvas.getWidth();
        double canvasHeight=canvas.getHeight();
        double standardUnitX=backgroundPane.maxWidthProperty().get()/1000;
        double standardUnitY=backgroundPane.maxHeightProperty().get()/1000;

		Creature[] creatures = new Creature[MAX_CREATURES];
		
		for(int i = 0; i < INITIAL_CREATURES; i++) {
			creatures[i] = new Creature(i, Math.random() *1000, Math.random() * 1000, 10, 10, Color.color(Math.random(), Math.random(), Math.random()), Math.random() * 15 + 1);
		}
		
		ThreadSafeUpdateMapQueueCounter mapCounter=new ThreadSafeUpdateMapQueueCounter();
        ThreadSafeCreaturesArray creaturesArray=new ThreadSafeCreaturesArray(creatures);
        MapGrapychHandler mapGrapychHandler=new MapGrapychHandler(gc,standardUnitX,standardUnitY,canvasWidth,canvasHeight);
        UpdateGuiRunnableGenerator updateRunnableGenerator=new UpdateGuiRunnableGenerator(mapGrapychHandler, mapCounter);
        MovmentHandlingThread movmentHandlingThread=new MovmentHandlingThread(updateRunnableGenerator,mapCounter,creaturesArray);
        ThreadSafeCreatureUpdateCounter updateCounter = new ThreadSafeCreatureUpdateCounter(creaturesArray);
		workers = new CreatureWorker[WORKER_COUNT];

		for (int i = 0; i < WORKER_COUNT; i++) {
			CreatureActionHandler actionHandler = new CreatureActionHandler(creaturesArray, updateCounter);
			workers[i] = new CreatureWorker(actionHandler);
			workers[i].start();
		}

		movmentHandlingThread.start();
        stage.setOnCloseRequest(event -> {

            movmentHandlingThread.Stop();

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