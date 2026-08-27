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
    private boolean targeted,eatFood;

    public CreatureBrain(double  eatProbability,double creatureX, double creatureY,ThreadSafeFoodArray generaFloodArray,ThreadSafeCreaturesArray generalCreaturesArray,double riproductionPercentage) {
        this.eatProbability = eatProbability;
        foodTarget=null;
        CreatureTarget=null;
        movmentTargetX=creatureX;
        movmentTargetY=creatureY;
        this.generaFloodArray=generaFloodArray;
        this.generalCreaturesArray=generalCreaturesArray;
        this.riproductionPercentage=riproductionPercentage;
        this.targeted=false;
    }
    
    
    public MovmentTargetOutput tink(Food foodArray[],Creature[] creatureArray,double creatureX, double creatureY){
        if (!targeted){
            eatFood=Math.random()<eatProbability;
        }
        if(eatFood){
            foodTarget=analizeFood(foodArray, creatureX, creatureY);
            if(foodTarget!=null){
                randomMoovmentTargetSet=false;
                movmentTargetX=foodTarget.getX();
                movmentTargetY=foodTarget.getY();
            }else{
                if(!randomMoovmentTargetSet){
                    randomMoovmentTargetSet=true;
                    targeted=false;
                    movmentTargetX=Math.random()*App.WORLD_WIDTH/8+1+creatureX-Math.random()*App.WORLD_WIDTH/4;
                    movmentTargetY=Math.random()*App.WORLD_HEIGHT/8+1+creatureY-Math.random()*App.WORLD_HEIGHT/4;
                }
            }
        }else{
            CreatureTarget=analizeCreatures(creatureArray, creatureX, creatureY);
            if(CreatureTarget!=null){
                randomMoovmentTargetSet=false;
                movmentTargetX=CreatureTarget.getX();
                movmentTargetY=CreatureTarget.getY();
            }else{
                if(!randomMoovmentTargetSet){
                    randomMoovmentTargetSet=true;
                    targeted=false;
                    movmentTargetX=Math.random()*App.WORLD_WIDTH/8+1+creatureX-Math.random()*App.WORLD_WIDTH/4;
                    movmentTargetY=Math.random()*App.WORLD_HEIGHT/8+1+creatureY-Math.random()*App.WORLD_HEIGHT/4;
                }
            }
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
    private Creature analizeCreatures(Creature creatureArray[],double creatureX,double creatureY){
        Creature minCreature=null;
        if(CreatureTarget!=null){
            if(!contains(creatureArray, CreatureTarget)){
                CreatureTarget=null;
                return null;
            }else{
                minCreature=CreatureTarget;
            }
        }else{
            double minDistance=Double.MAX_VALUE;
             for(int i=0;i<creatureArray.length;i++){
                double creatureDistance=getDistance(creatureX, creatureY, (double)creatureArray[i].getX(), (double)creatureArray[i].getY());
                if(creatureDistance<minDistance){
                    minDistance=creatureDistance;
                    minCreature=creatureArray[i];
                }
        }
        }
        return minCreature;
    }

    private boolean  contains(Creature creatureArray[],Creature creatureToFind){
        for(int i=0;i<creatureArray.length;i++){
            if(creatureArray[i].getId()==creatureToFind.getId()){
                return true;
            }
        }
        return false;
    }


    private double getDistance(double creatureX,double creatureY,double targetX,double targetY){
        double calcx=targetX - creatureX;
        double calcy=targetY - creatureY;
        return Math.sqrt(calcx*calcx +calcy*calcy);
    }
}
