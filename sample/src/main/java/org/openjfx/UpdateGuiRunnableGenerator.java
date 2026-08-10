package org.openjfx;

public class UpdateGuiRunnableGenerator  {
    private MapGrapychHandler mapGrapychHandler;
    private ThreadSafeUpdateMapQueueCounter counter;
    public UpdateGuiRunnableGenerator ( MapGrapychHandler mapGrapychHandler, ThreadSafeUpdateMapQueueCounter counter){
        this.mapGrapychHandler=mapGrapychHandler;
        this.counter=counter;
    }
    public Runnable generate(Creature[] creatures, Food[] foodArray){
        return new Runnable() {
            @Override
            public void run(){
                mapGrapychHandler.update(creatures,foodArray);
                counter.decreaseCounter();
            }
        };
    }
    
    
}
