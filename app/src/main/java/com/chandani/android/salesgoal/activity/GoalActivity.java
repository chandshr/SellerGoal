package com.chandani.android.salesgoal.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.chandani.android.salesgoal.R;
import com.chandani.android.salesgoal.adapter.GoalItemsArrayAdapter;
import com.chandani.android.salesgoal.db.Item;
import com.chandani.android.salesgoal.db.SalesDbColumn.DbEntry;
import com.chandani.android.salesgoal.db.SalesDbHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GoalActivity extends AppCompatActivity {

    private long goalId;
    private GoalItemsArrayAdapter goalItemsArrayAdapter;
    private SalesDbHelper salesDbHelper;

    private String goalName;
    private String goalStartDate;
    private String goalEndDate;
    private float goalAmount;

    private float goalProgress;
    private float timeProgress;

    private ArrayList<Item> item_arrlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal);
        this.salesDbHelper = new SalesDbHelper(getApplicationContext());
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.goalId = Long.parseLong(getIntent().getStringExtra("GOAL_ID"));
        }
        setGoalData();
        setTitle("Items in " + this.goalName);
        listItems();
        this.setMessage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_goal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_home) {
            Intent goallist_intent = new Intent(this, GoalListActivity.class);
            startActivityForResult(goallist_intent, 0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void listItems(){
        setItemList();
        ListView itemListView = (ListView) findViewById(R.id.list_items);
        this.goalItemsArrayAdapter = new GoalItemsArrayAdapter(this, item_arrlist, this.goalId);
        itemListView.setAdapter(goalItemsArrayAdapter);
    }

    public void setItemList(){
        this.item_arrlist = get_items_db();
    }

    public void setMessage(){
        SQLiteDatabase db = this.salesDbHelper.getReadableDatabase();

        float totalPlan = 0;
        int investRowCount = 0;

        HashMap<Integer, Float> priceHash = new HashMap<Integer, Float>();
        Cursor plan_cursor = db.rawQuery("SELECT * FROM " + DbEntry.TABLE_ITEM + " where " + DbEntry.COLUMN_NAME_GOAL + " = " + this.goalId, null);
        if (plan_cursor.moveToFirst()) {
            do {
                float price = Float.parseFloat(plan_cursor.getString(plan_cursor.getColumnIndex(DbEntry.COLUMN_NAME_PRICE)));
                int count = Integer.parseInt(plan_cursor.getString(plan_cursor.getColumnIndex(DbEntry.COLUMN_NAME_PLAN_COUNT)));
                totalPlan += (price * count);
                priceHash.put(plan_cursor.getInt(plan_cursor.getColumnIndex(DbEntry._ID)), price);
            } while (plan_cursor.moveToNext());
        }
        plan_cursor.close();

        float totalSales = 0;
        for(int itemId : priceHash.keySet()){
            Cursor invest_cursor = db.rawQuery("SELECT * FROM " + DbEntry.TABLE_INVEST + " where " + DbEntry.COLUMN_NAME_GOAL + " = " + this.goalId + " AND " + DbEntry.COLUMN_NAME_ITEM_ID + " = " + itemId, null);
            investRowCount = invest_cursor.getCount();
            if(invest_cursor.moveToFirst()){
                do{
                    int count = invest_cursor.getInt(invest_cursor.getColumnIndex(DbEntry.COLUMN_NAME_SALES_COUNT));
                    float price = priceHash.get(itemId);
                    totalSales += (count*price);
                }while(invest_cursor.moveToNext());
            }
        }
        db.close();

        this.goalProgress = (totalSales/this.goalAmount)*100;

//        View view1 = this.findViewById(android.R.id.content);
//        TextView msg = (TextView) view1.findViewById(R.id.message);
//        msg.setText("Chandani Shrestha I love you ");
        View view1 = this.findViewById(android.R.id.content);
        TextView msg = (TextView) view1.findViewById(R.id.message);
        float diffplan = this.goalAmount-totalPlan;
        float diffinvest = this.goalAmount-totalSales;
        if(totalPlan>0 && totalPlan<this.goalAmount &&totalSales<=0){
            msg.setText("Add more item or increase items. As, you are $"+diffplan+" away from ultimate goal of $"+this.goalAmount);
        }else if(totalPlan>=this.goalAmount && totalSales<=0){
            msg.setText("You have planned for $"+totalPlan+" for a goal of $"+this.goalAmount);
        }else if(totalPlan>=this.goalAmount&&totalSales>0 &&investRowCount>0){
            int avgDay = (int)(this.goalAmount*investRowCount)/(int)totalSales;
            long noRemainDays = 0;
            long isDeadlinePassed = 0;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            try {
                Date startDate = simpleDateFormat.parse(this.goalStartDate);
                Date deadline = simpleDateFormat.parse(this.goalEndDate);
                String todaystr = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
                Date today = simpleDateFormat.parse(todaystr);
                isDeadlinePassed = remainDays(deadline, today);

                noRemainDays = remainDays(today, deadline);

            } catch (ParseException e) {
                e.printStackTrace();
            }
            if(isDeadlinePassed>0){
                msg.setText("You have made total sale of $"+totalSales+" for a goal of $"+this.goalAmount+" and your deadline was on "+this.goalEndDate);
            }
            else if (diffinvest > 0 && isDeadlinePassed < 0) {
                msg.setText("You have made total sale of $"+totalSales+" for a goal of $"+this.goalAmount+" you need to earn $"+diffinvest+" more. If you go at this rate you will acheive your goal after "+avgDay+" days. But you have "+noRemainDays+"days remaining. Your deadline is on "+this.goalEndDate);
            }else{
                msg.setText("You have made total sale of $"+totalSales+" for a goal of $"+this.goalAmount);
            }
        }
    }

    public long remainDays(Date lower, Date higher){
        long different = higher.getTime() - lower.getTime();
        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long noRemainDays = (different/daysInMilli);
        return noRemainDays;
    }

    public ArrayList<Item> get_items_db(){
        SQLiteDatabase db = this.salesDbHelper.getReadableDatabase();
        ArrayList<Item> goalRequire = new ArrayList<Item>();
        String sortOrder = DbEntry._ID + " DESC";
        String goalIdStr = String.valueOf(this.goalId);
        if(goalIdStr !=null){
            Cursor cursor = db.rawQuery("SELECT * FROM " + DbEntry.TABLE_ITEM + " where " + DbEntry.COLUMN_NAME_GOAL + " = "+this.goalId, null);
            //start
            db = this.salesDbHelper.getReadableDatabase();

            if(!cursor.isAfterLast())
            {
                cursor.moveToFirst();
                do{
                    String name = cursor.getString(cursor.getColumnIndex(DbEntry.COLUMN_NAME_ITEM));
                    long id = cursor.getInt(cursor.getColumnIndex("_id"));
                    float price = cursor.getFloat(cursor.getColumnIndex(DbEntry.COLUMN_NAME_PRICE));
                    long goal_id = cursor.getInt(cursor.getColumnIndex(DbEntry.COLUMN_NAME_GOAL));
                    long plan_count = cursor.getInt(cursor.getColumnIndex(DbEntry.COLUMN_NAME_PLAN_COUNT));
                    Item item = new Item(id, name, price, goal_id, plan_count);
                    goalRequire.add(item);
                    cursor.moveToNext();
                } while (!cursor.isAfterLast());
            }
            cursor.close();
        }
        db.close();
        return goalRequire;
    }

    public void addItem(View view){
        EditText itemName = (EditText) findViewById(R.id.item_add);
        String itemNameStr = itemName.getText().toString();
        EditText price = (EditText) findViewById(R.id.price_add);
        String priceStr = price.getText().toString();
        EditText count = (EditText) findViewById(R.id.plan_count_add);
        String countStr = count.getText().toString();
        if(itemNameStr.isEmpty()){
            itemName.setError("Enter item name");
        }else if(priceStr.isEmpty()){
            price.setError("Enter price for 1 item");
        }else if(countStr.isEmpty()){
            count.setError("Enter count");
        }else if(itemNameStr != null && priceStr!=null && countStr!=null){
            ContentValues plan_arr = new ContentValues();
            plan_arr.put(DbEntry.COLUMN_NAME_GOAL, this.goalId);
            plan_arr.put(DbEntry.COLUMN_NAME_ITEM, itemNameStr);
            plan_arr.put(DbEntry.COLUMN_NAME_PRICE, priceStr);
            plan_arr.put(DbEntry.COLUMN_NAME_PLAN_COUNT, countStr);

            SQLiteDatabase goalDB = salesDbHelper.getWritableDatabase();
            long planRowId;
            planRowId = goalDB.insert(
                    DbEntry.TABLE_ITEM,
                    DbEntry._ID,
                    plan_arr);
            goalDB.close();
        }
        itemName.setText("");
        price.setText("");
        count.setText("");
        listItems();
        setMessage();
    }

    public void setGoalData(){
        float amount = 0;

        SQLiteDatabase db = this.salesDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DbEntry.TABLE_GOAL + " where " + DbEntry._ID + " = " + this.goalId, null);
        if(cursor.moveToFirst()){
            this.goalName = cursor.getString(cursor.getColumnIndex(DbEntry.COLUMN_NAME_GOAL));
            this.goalAmount = cursor.getFloat(cursor.getColumnIndex(DbEntry.COLUMN_NAME_TOTAL_AMOUNT));
            this.goalStartDate = cursor.getString(cursor.getColumnIndex(DbEntry.COLUMN_NAME_START));
            this.goalEndDate = cursor.getString(cursor.getColumnIndex(DbEntry.COLUMN_NAME_DEADLINE));
        }

        View view1 = this.findViewById(android.R.id.content);
        final EditText start = (EditText) view1.findViewById(R.id.startDate);
        start.setText(this.goalStartDate);

        final EditText end = (EditText) view1.findViewById(R.id.endDate);
        end.setText(this.goalEndDate);

        final EditText goalamount = (EditText) view1.findViewById(R.id.goalAmount);
        goalamount.setText(String.valueOf(this.goalAmount));

        TextView progress = (TextView) view1.findViewById(R.id.message);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date startDate = simpleDateFormat.parse(this.goalStartDate);
            Date endDate = simpleDateFormat.parse(this.goalEndDate);
            String todaystr = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
            Date today = simpleDateFormat.parse(todaystr);
            long pastDays = remainDays(startDate, today);
            long givenDays = remainDays(startDate, endDate);
            long isDeadlinePassed = remainDays(endDate, today);
            this.timeProgress = (pastDays*100)/givenDays;
            if(isDeadlinePassed>0){
                progress.setText("The deadline was on "+endDate);
            }else if(pastDays<0){
                long remainday = (1+pastDays)*-1;
                progress.setText(remainday+"days remaining to start the goal. And you have completed "+this.goalProgress+"% of the goal");
            } else if(givenDays>0){
                progress.setText(this.goalProgress+"% of goal is acheived in "+this.timeProgress+"% of given time. And you have "+givenDays+" remaining to meet the deadline");
            }else if(this.goalProgress>0 && startDate==endDate) {
                progress.setText(this.goalProgress+"% of goal is acheived in 100% of given time");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        start.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                SQLiteDatabase db = GoalActivity.this.salesDbHelper.getWritableDatabase();
                String goalFilter = DbEntry._ID + "=" + GoalActivity.this.goalId;
                ContentValues goalArgs = new ContentValues();
                String startStr = start.getText().toString();
                goalArgs.put(DbEntry.COLUMN_NAME_START, startStr);
                db.update(DbEntry.TABLE_GOAL, goalArgs, goalFilter, null);
                db.close();
            }
        });

        end.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                SQLiteDatabase db = GoalActivity.this.salesDbHelper.getWritableDatabase();
                String goalFilter = DbEntry._ID + "=" + GoalActivity.this.goalId;
                ContentValues goalArgs = new ContentValues();
                String endStr = end.getText().toString();
                goalArgs.put(DbEntry.COLUMN_NAME_DEADLINE, endStr);
                db.update(DbEntry.TABLE_GOAL, goalArgs, goalFilter, null);
                db.close();
            }
        });

        goalamount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                SQLiteDatabase db = GoalActivity.this.salesDbHelper.getWritableDatabase();
                String goalFilter = DbEntry._ID + "=" + GoalActivity.this.goalId;
                ContentValues goalArgs = new ContentValues();
                String amountStr = goalamount.getText().toString();
                goalArgs.put(DbEntry.COLUMN_NAME_TOTAL_AMOUNT, amountStr);
                db.update(DbEntry.TABLE_GOAL, goalArgs, goalFilter, null);
                GoalActivity.this.goalAmount = Float.parseFloat(amountStr);
                GoalActivity.this.setMessage();
                TextView msg = (TextView) findViewById(R.id.message);
                msg.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                db.close();
            }
        });
    }

    public void getCalendar(View view){
        Intent graph_intent = new Intent(this, CalendarActivity.class);
        graph_intent.putExtra("GOAL", String.valueOf(this.goalId));
        startActivityForResult(graph_intent, 0);
    }

}
