package com.chandani.android.salesgoal.db;

/**
 * Created by chandani on 11/23/15.
 */
public class Sales {

    private int day;
    private int salesCount;

    public Sales(int day, int salesCount){

    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getSalesCount() {
        return salesCount;
    }

    public void setSalesCount(int salesCount) {
        this.salesCount = salesCount;
    }

}
