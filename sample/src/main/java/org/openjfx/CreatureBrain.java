package org.openjfx;

public class CreatureBrain {

    private double  eatProbability;
    private Creature CreatureTarget;
    private Food foodTarget;
    private double movmentTargetX;
    private double movmentTargetY;
    private boolean randomMoovmentTargetSet;
    private ThreadSafeFoodArray generaFloodArray;
    private ThreadSafeCreaturesArray generalCreaturesArray;
    private double riproductionPercentage;

    public CreatureBrain(double  eatProbability,double creatureX, double creatureY,ThreadSafeFoodArray generaFloodArray,ThreadSafeCreaturesArray generalCreaturesArray,double riproductionPercentage) {
        this.eatProbability = eatProbability;
        foodTarget=null;
        CreatureTarget=null;
        movmentTargetX=creatureX;
        movmentTargetY=creatureY;
        this.generaFloodArray=generaFloodArray;
        this.generalCreaturesArray=generalCreaturesArray;
        this.riproductionPercentage=riproductionPercentage;
    }
    
    
    public MovmentTargetOutput tink(Food foodArray[],Creature[] creatureArray,double creatureX, double creatureY){
        if(Math.random()<eatProbability){
            foodTarget=analizeFood(foodArray, creatureX, creatureY);
            if(foodTarget!=null){
                randomMoovmentTargetSet=false;
                movmentTargetX=foodTarget.getX();
                foodTarget.getY();
            }else{
                if(!randomMoovmentTargetSet){
                    randomMoovmentTargetSet=true;
                    movmentTargetX=Math.random()*App.WORLD_WIDTH/8+1+creatureX-Math.random()*App.WORLD_WIDTH/4;
                    movmentTargetY=Math.random()*App.WORLD_HEIGHT/8+1+creatureY-Math.random()*App.WORLD_HEIGHT/4;
                }
            }
        }else{

        }
        return new MovmentTargetOutput(movmentTargetX,movmentTargetX);
    }

    public boolean tryRiproduction(){
        return Math.random()<riproductionPercentage;
    }
    private Food analizeFood(Food foodArray[],double creatureX,double creatureY){
        double minDistance;
        Food cFood=null;
        try {
            cFood=generaFloodArray.request(foodTarget.getId());
        } catch (InterruptedException e) {
        }
        if(foodTarget!=null&&cFood==null){
            foodTarget=null;
        }
        if(foodTarget==null){
            minDistance=Double.MAX_VALUE;
        }else{
            minDistance=getDistance(creatureX, creatureY, (double)foodTarget.getX(), (double)foodTarget.getY());
        }
        Food minFood=foodTarget;
        for(int i=0;i<foodArray.length;i++){
                double foodDistance=getDistance(creatureX, creatureY, (double)foodArray[i].getX(), (double)foodArray[i].getY());
                if(foodDistance<minDistance){
                    minDistance=foodDistance;
                    minFood=foodArray[i];
                }
        }
        return minFood;
    }



    private double getDistance(double creatureX,double creatureY,double targetX,double targetY){
        double calcx=targetX - creatureX;
        double calcy=targetY - creatureY;
        return Math.sqrt(calcx*calcx +calcy*calcy);
    }
}
