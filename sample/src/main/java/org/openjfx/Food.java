package org.openjfx;

public class Food {
    private int id;
    private int categoryId;
    private int x;
    private int y;
	public static final int FOOD_WIDTH = 10;
	public static final int FOOD_HEIGHT = 10;

    public Food(int id,int categoryId,int x,int y){
        this.id=id;
        this.categoryId=categoryId;
        this.x=x;
        this.y=y;
    }
    public Food(Food inFood){
        this.id=inFood.getId();
        this.categoryId=inFood.getCategoryId();
        this.x=inFood.getX();
        this.y=inFood.getY();
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCategoryId() {
        return this.categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

}
