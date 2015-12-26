package com.chandani.android.salesgoal.db;

/**
 * Created by chandani on 11/21/15.
 */
public class Goal {

    private long id;
    private String goal;
    private String start;
    private String end;
    private String amount;

    public Goal(long id, String goal, String start, String end, String amount){
        this.id = id;
        this.goal = goal;
        this.start = start;
        this.end = end;
        this.amount = amount;
    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
