package org.openjfx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;



/**SSSS
 * JavaFX App
 */
public class App extends Application {

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
        CreatureGraphicHandler creature=new CreatureGraphicHandler((int)(backgroundPane.maxWidthProperty().get()*0.2),(int)(backgroundPane.maxHeightProperty().get()*0.2),(int)(backgroundPane.maxHeightProperty().get()*0.05),(int)(backgroundPane.maxHeightProperty().get()*0.05),Color.RED,gc);
        creature.place();
        creature.move((int)(backgroundPane.maxWidthProperty().get()*0.5),(int)(backgroundPane.maxHeightProperty().get()*0.5));
    }

    public static void main(String[] args) {
        launch();
    }

}