package org.openjfx;

import java.util.Locale;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class UpdateCreatureDisplayRunnableGenerator {
    private Label displayLabel;
    private Pane creatureColorShowPane;

    public UpdateCreatureDisplayRunnableGenerator(Label displayLabel,Pane creatureColorShowPane) {
        this.displayLabel = displayLabel;
        this.creatureColorShowPane=creatureColorShowPane;
    }

    public Runnable generate(int creatureId,Color creatureColor,double speed,double hunger,double hp,double maxHp,double baseAttak){
        return new Runnable() {
            @Override
            public void run(){
                creatureColorShowPane.setStyle("-fx-background-color:#"+creatureColor.toString().substring(2, 8)+";");
                displayLabel.setText("id:"+creatureId+"\n"+"color:"+creatureColor+"\n"+"speed"+speed+"\n"+"hunger:"+hunger+"\n"+"hp:"+String.format(Locale.US,"%.3f", maxHp)+"/"+String.format(Locale.US,"%.3f", hp)+"\n"+"baseAttak:"+baseAttak);
            }
        };
    }

}
