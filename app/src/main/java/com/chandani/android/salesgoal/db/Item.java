package com.chandani.android.salesgoal.db;

/**
 * Created by chandani on 11/22/15.
 */
public class Item {

    private long id;
    private String name;
    private float price;
    private long goalId;
    private long planCount;
    private long salesCount;

    public Item(long id, String name, float price, long goalId, long planCount){
        this.id = id;
        this.name = name;
        this.price = price;
        this.goalId = goalId;
        this.planCount = planCount;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPlanCount() {
        return planCount;
    }

    public void setPlanCount(long planCount) {
        this.planCount = planCount;
    }

    public long getGoalId() {
        return goalId;
    }

    public void setGoalId(long goalId) {
        this.goalId = goalId;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
