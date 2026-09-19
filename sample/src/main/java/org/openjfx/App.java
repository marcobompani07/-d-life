



package org.openjfx;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class App extends Application {
    private static final int MAX_CREATURES = 10000;
	private static final int INITIAL_CREATURES = 200;
    private static final int FOODSPAWNOUNT = 100;
    private static final boolean startFormSave=false;
    

	private static int nextCreatureId = 0;
	private static int currentCreatureCount = INITIAL_CREATURES;
    public static double MutationRate=1;

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

    private UpdateCreatureDisplayRunnableGenerator updateCreatureDisplayRunnableGenerator;
    private CreatureInfoDisplayThread creatureInfoDisplayThread;
    private FoodGeneratorThread foodGeneratorThread;
	private ThreadSafeUpdateMapQueueCounter mapCounter;
    private UpdateGuiRunnableGenerator updateRunnableGenerator;
    private MovmentHandlingThread movmentHandlingThread;
    private ThreadSafeCreatureUpdateCounter updateCounter;

    private  Creature[] creatures;
    private Food[] foodArray;
    private BackgroundGridElement[][] backgroundGrid;
    private double standardUnit;
    private ThreadSafeBackgroundGrid threadSafeBackgroundGrid ;
    private ThreadSafeFoodArray threadSafeFoodArray;
    private ThreadSafeCreaturesArray creaturesArray;


    //gui
    private  BorderPane simulationRoot;
    private  Scene simulationScene;
    private  Pane simulationBackgroundPane;
    private  Canvas  canvas;
    private  GraphicsContext gc;
    private  double canvasWidth;
    private  double canvasHeight;
    private  Spinner<Integer> zoomSpinner;
    private  Button zoomButton;
    private  Label creatureCounterLabel;
    private  Button saveButton;
    private  HBox simulationTopBar;
    private  Label dispayCreatureLabel;
    private  Pane creatureColorShowPane;
    private  VBox rigthBar;

    //components
    private static MapGraphicsHandler mapGrapychHandler;
    private TextField saveNameTextFiled;
    
    //runnables
    private Runnable updateCreatureCountDisplay=new Runnable() {
        @Override
        public void run(){
            creatureCounterLabel.setText("creature count : "+currentCreatureCount);
        }
    };
    private Runnable ResetCreatureDisplayRunnable=new Runnable(){
        @Override
        public  void run(){
            creatureColorShowPane.setStyle("");
            dispayCreatureLabel.setText("");
        }
    };
    
    @Override
    public void start(Stage stage) throws InterruptedException {
        stage.setMaximized(true);
        BorderPane startRoot=new BorderPane();
        startRoot.setStyle("-fx-background-color: #222222;"); 
        stage.setScene(new Scene(startRoot));
        standardUnit = Screen.getPrimary().getVisualBounds().getWidth() * 0.75 / UNIT_DIVISION;
        Button startButton=new Button("start"),startFormSaveButton=new Button("start form save");
        HBox startButtonBox=new HBox(standardUnit*15,startButton,startFormSaveButton);
        startButtonBox.setStyle("-fx-padding: "+(standardUnit*15)+"px;");
        startButtonBox.setAlignment(Pos.CENTER);
        startRoot.setCenter(startButtonBox);
        startButton.setOnAction(e -> {
            guiInit(stage,"");
            newDataInit();
            componentsInit();
            eventsInit();
        }); 
        startFormSaveButton.setOnAction(e->{
            List<String> nameList = new ArrayList<>();
            try (DirectoryStream<Path> stream=Files.newDirectoryStream((Path.of("src", "main", "resources", "saves")))){
                    for (Path entry : stream) {
                    if (Files.isRegularFile(entry)) {
                        nameList.add(entry.getFileName().toString().split("\\.")[0]);
                    }
                 }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            String[] saveFileNames = nameList.toArray(new String[nameList.size()]);
            BorderPane loadSaveRoot=new BorderPane();
            Scene loadSaveScene= new Scene(loadSaveRoot);
            loadSaveRoot.setStyle("-fx-background-color: #222222;"); 
            Button loadSaveButton=new Button("load save");
            ComboBox loadSaveComboBox=new ComboBox();
            loadSaveComboBox.getItems().addAll(saveFileNames);
            loadSaveComboBox.setEditable(false);
            HBox loadSaveBox=new HBox(standardUnit*15,loadSaveButton,loadSaveComboBox);
            loadSaveBox.setStyle("-fx-padding: "+(standardUnit*15)+"px;");
            loadSaveBox.setAlignment(Pos.CENTER);
            loadSaveRoot.setTop(loadSaveBox);
            loadSaveComboBox.setStyle("-fx-font-size: " + (standardUnit * 50) + "px;");
            loadSaveComboBox.getSelectionModel().select(0);
            
            stage.setScene(loadSaveScene);
            loadSaveButton.setOnAction(ev->{
                guiInit(stage,loadSaveComboBox.getValue().toString());
                loadSave(loadSaveComboBox.getValue().toString());
                componentsInit();
                eventsInit();
            });
            
        });

        stage.show();
        stage.setOnCloseRequest(event -> {
            if(movmentHandlingThread!=null){
                movmentHandlingThread.Stop();
            }
            if(foodGeneratorThread!=null){
                foodGeneratorThread.Stop();
            }
            if(creatureInfoDisplayThread!=null){
                creatureInfoDisplayThread.Stop();
            }
            if (workers!=null){
                for (CreatureWorker worker : workers) {
                    if(worker!=null){
                        worker.stopWorker();
                    }
                }
            }

            Platform.exit();
        });
        
       
    }

    private  void guiInit(Stage stage,String fname){
        simulationRoot = new BorderPane();
        simulationRoot.setStyle("-fx-background-color: #222222;"); 
        simulationScene = new Scene(simulationRoot);
        simulationBackgroundPane= new Pane();
        simulationBackgroundPane.setStyle("-fx-background-color: #343434;" );
        simulationRoot.setCenter(simulationBackgroundPane);
        simulationRoot.setCenter(simulationBackgroundPane);
        simulationBackgroundPane.setMaxWidth(Screen.getPrimary().getVisualBounds().getWidth()*0.75);
        simulationBackgroundPane.setMaxHeight(Screen.getPrimary().getVisualBounds().getHeight()*0.75);
        canvas=new Canvas();
        canvas.widthProperty().bind(simulationBackgroundPane.maxWidthProperty());
        canvas.heightProperty().bind(simulationBackgroundPane.maxHeightProperty());
        gc = canvas.getGraphicsContext2D();
        simulationBackgroundPane.getChildren().add(canvas);
        canvasWidth=Screen.getPrimary().getVisualBounds().getWidth() * 0.75;
        canvasHeight=Screen.getPrimary().getVisualBounds().getHeight() * 0.75;
        //double standardUnit=simulationBackgroundPane.maxWidthProperty().get()/UNIT_DIVISION;
        WORLD_HEIGHT=simulationBackgroundPane.maxHeightProperty().get()/standardUnit*WORLD_MULTIPLIYER;
        zoomSpinner=new Spinner(10,500,100,10);
        zoomSpinner.setEditable(true);
        zoomButton = new Button("SetZoom");
        creatureCounterLabel=new Label();
        creatureCounterLabel.setTextFill(Color.web("#ffffff"));
        saveButton=new Button("Save");
        saveNameTextFiled=new TextField(fname);
        saveNameTextFiled.setPromptText("save name");
        simulationTopBar = new HBox(10*standardUnit, zoomSpinner,zoomButton,creatureCounterLabel,saveButton,saveNameTextFiled);
        simulationTopBar.setStyle("-fx-padding: "+(10*standardUnit)+"px;-fx-background-color: #494848;");
        simulationTopBar.setSpacing(15*standardUnit);
        simulationRoot.setTop(simulationTopBar);
        dispayCreatureLabel=new Label();
        dispayCreatureLabel.setTextFill(Color.web("#ffffff"));
        creatureColorShowPane=new Pane();
        creatureColorShowPane.setMaxSize(50*standardUnit, 50*standardUnit);
        creatureColorShowPane.setMinSize(50*standardUnit, 50*standardUnit);
        rigthBar=new VBox(10*standardUnit,creatureColorShowPane,dispayCreatureLabel);
        rigthBar.setAlignment(Pos.TOP_CENTER);
        rigthBar.setStyle("-fx-padding: "+(10*standardUnit)+"px;-fx-background-color: #494848;");
        rigthBar.setSpacing(15*standardUnit);
        rigthBar.setMaxWidth(standardUnit*150);
        rigthBar.setMinWidth(standardUnit*150);
        simulationRoot.setRight(rigthBar);
        stage.setScene(simulationScene);
        stage.setMaximized(true);
    }

    public  void newDataInit( ){
        creatures= new Creature[MAX_CREATURES];
        foodArray=new Food[MAX_CREATURES ];
        backgroundGrid= new BackgroundGridElement[(int)(WORLD_WIDTH+1) / 10][(int)(WORLD_HEIGHT+1) / 10];
        for(int i=0;i<backgroundGrid.length;i++){
            for(int i2=0;i2<backgroundGrid[i].length;i2++){
                backgroundGrid[i][i2]=new BackgroundGridElement();
            }
        }
        threadSafeBackgroundGrid = new ThreadSafeBackgroundGrid(backgroundGrid);
        threadSafeFoodArray=new ThreadSafeFoodArray(foodArray);
        creaturesArray=new ThreadSafeCreaturesArray(creatures);
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
    }

    public void loadSave(String fname){
        CreatureSaveData[] cCreatures=null;
        ObjectMapper mapper=new ObjectMapper();
        try {
            Path filePath = Path.of("src", "main", "resources", "saves", fname+".json");
            HashMap<String, Object> saveData=(HashMap)mapper.readValue(filePath.toFile(),  HashMap.class);
            foodArray=mapper.convertValue(saveData.get("foodItems"),Food[].class);
            backgroundGrid=mapper.convertValue(saveData.get("backgroundGrid"),BackgroundGridElement[][].class);
            currentCreatureCount=mapper.convertValue(saveData.get("creatureNumber"),Integer.class);
            cCreatures=mapper.convertValue(saveData.get("creatures"),CreatureSaveData[].class);
            creatures=new Creature[cCreatures.length];
        } catch (IOException ex) {
            ex.printStackTrace();
            
        }
        threadSafeBackgroundGrid = new ThreadSafeBackgroundGrid(backgroundGrid);
        threadSafeFoodArray=new ThreadSafeFoodArray(foodArray);
        creaturesArray=new ThreadSafeCreaturesArray(creatures);
        for(int i=0;i<creatures.length;i++){
            if(cCreatures[i]!=null){
                creatures[i]=new Creature(cCreatures[i].getId(),cCreatures[i].getX(),cCreatures[i].getY(),cCreatures[i].getWidth(),cCreatures[i].getHeight(),cCreatures[i].getHp(),cCreatures[i].getBaseAttack(),cCreatures[i].getColor(),cCreatures[i].getSpeed(),threadSafeFoodArray, creaturesArray, backgroundGrid, threadSafeBackgroundGrid);
                creatures[i].setLastReproductionTime(cCreatures[i].getLastReproductionTime());
            }else{
                creatures[i]=null;
            }
        }
    }

    public void componentsInit(){
        mapGrapychHandler=new MapGraphicsHandler(gc,standardUnit,canvasWidth,canvasHeight);
        updateCreatureDisplayRunnableGenerator=new UpdateCreatureDisplayRunnableGenerator(dispayCreatureLabel,creatureColorShowPane);
        creatureInfoDisplayThread=new CreatureInfoDisplayThread(updateCreatureDisplayRunnableGenerator, creaturesArray,updateCreatureCountDisplay,ResetCreatureDisplayRunnable);
        foodGeneratorThread=new FoodGeneratorThread(threadSafeFoodArray, backgroundGrid,App.FOODSPAWNOUNT);
        mapCounter=new ThreadSafeUpdateMapQueueCounter();
        updateRunnableGenerator=new UpdateGuiRunnableGenerator(mapGrapychHandler, mapCounter);
        movmentHandlingThread=new MovmentHandlingThread(updateRunnableGenerator,mapCounter,creaturesArray,threadSafeFoodArray);
        updateCounter = new ThreadSafeCreatureUpdateCounter(creaturesArray);
        workers = new CreatureWorker[WORKER_COUNT];
        creatureInfoDisplayThread.setCreatureId(-1);
        foodGeneratorThread.start();
        movmentHandlingThread.start();
        creatureInfoDisplayThread.start();
        for (int i = 0; i < WORKER_COUNT; i++) {
            try {
                CreatureActionHandler actionHandler = new CreatureActionHandler(creaturesArray, updateCounter);
                workers[i] = new CreatureWorker(actionHandler);
                workers[i].start();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            
        }
    }
    public void eventsInit(){
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
            
            MapGraphicsHandler.focusedCreatureId=index;
            creatureInfoDisplayThread.setCreatureId(index);
            creatureColorShowPane.setStyle("");
            dispayCreatureLabel.setText("");
        });


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


        zoomButton.setOnAction(ev -> {
            int zoomValue=zoomSpinner.getValue();
            double offsetX=(canvasWidth-(canvasWidth/((ZOOM*100)/((double)zoomValue))))/2*ZOOM;
            double offsetY=(canvasHeight-(canvasHeight/((ZOOM*100)/((double)zoomValue))))/2*ZOOM;
            ZOOM=((double)zoomValue)/100;
            mapGrapychHandler.addOffsetX(offsetX);
            mapGrapychHandler.addOffsetY(offsetY);
        });


        saveButton.setOnAction(ev -> {
            try {
                movmentHandlingThread.Stop();
                movmentHandlingThread.join();
                foodGeneratorThread.Stop();
                foodGeneratorThread.join();
                creatureInfoDisplayThread.Stop();
                creatureInfoDisplayThread.join();
                for (CreatureWorker worker : workers) {
                    worker.stopWorker();
                    worker.join();
                }
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            String fname="unnamed"+System.currentTimeMillis();
            if (saveNameTextFiled.getText()!=""&&saveNameTextFiled.getText()!=null){
                fname=saveNameTextFiled.getText();
            }
            Path filePath = Path.of("src", "main", "resources", "saves", saveNameTextFiled.getText()+".json");
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
                Map<String, Object> saveData = new HashMap<>();
                CreatureSaveData[] creatureSaveData=new CreatureSaveData[creatures.length];
                for(int i=0;i<creatureSaveData.length;i++){
                    if(creatures[i]!=null){
                    creatureSaveData[i]=creatures[i].toCreatureSaveData();
                    }else{
                        creatureSaveData[i]=null;
                    }
                }
                saveData.put("creatures", creatureSaveData);
                saveData.put("foodItems", foodArray);
                saveData.put("backgroundGrid", backgroundGrid);
                saveData.put("creaturecounter",updateCounter);
                saveData.put("creatureNumber", currentCreatureCount);
                try {
                    mapper.writeValue(filePath.toFile(),saveData);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                
            movmentHandlingThread=new MovmentHandlingThread(movmentHandlingThread);
            movmentHandlingThread.start();
            foodGeneratorThread=new FoodGeneratorThread(foodGeneratorThread);
            foodGeneratorThread.start();
            creatureInfoDisplayThread=new CreatureInfoDisplayThread(creatureInfoDisplayThread);
            creatureInfoDisplayThread.start();
            for (int i = 0; i < WORKER_COUNT; i++) {
            workers[i] = new CreatureWorker(workers[i]);
            workers[i].start();
            }
        });
    }

	public static synchronized void removeCreature(int id){
		currentCreatureCount--;
        if(MapGraphicsHandler.focusedCreatureId==id){
            MapGraphicsHandler.focusedCreatureId=-1;
            CreatureInfoDisplayThread.creatureId=-1;
            System.out.println("unfocussed:"+MapGraphicsHandler.focusedCreatureId);
            
        }
	}

	public static synchronized void cancelCreatureCreation(){
		currentCreatureCount--;
	}

    public static synchronized void addCreature(){
        currentCreatureCount++;
    }


    public static void main(String[] args) {
        launch();
    }


}