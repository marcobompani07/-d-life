package org.openjfx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class App extends Application {
    private CreatureGraphicHandler[] creatures=new CreatureGraphicHandler[100];
    @Override
    public void start(Stage stage) {
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
        creatures[0]=new CreatureGraphicHandler(standardUnitX*200,standardUnitY*200,standardUnitX*25,standardUnitX*25,Color.RED,gc);
        creatures[0].setX(standardUnitX*500);
        creatures[0].setY(standardUnitY*500);
        ThreadSafeUpdateQueueCounter counter=new ThreadSafeUpdateQueueCounter();
        Runnable update=new Runnable() {
            @Override
            public void run(){
                gc.clearRect(0, 0, canvasWidth, canvasHeight);
                for (int i=0;i<creatures.length;i++) {
                    creatures[i].place();
                }
                counter.decreaseCounter();
            }
        };
        
        MovmentHandlingThread movmentHandlingThread=new MovmentHandlingThread(update,counter);
        movmentHandlingThread.start();
        stage.setOnCloseRequest(event -> {
            movmentHandlingThread.Stop();
        });
       
    }

    public static void main(String[] args) {
        launch();
    }

}