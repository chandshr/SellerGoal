package com.chandani.android.salesgoal.activity;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.chandani.android.salesgoal.R;
import com.chandani.android.salesgoal.adapter.InvestArrayAdapter;
import com.chandani.android.salesgoal.db.Goal;
import com.chandani.android.salesgoal.db.Item;
import com.chandani.android.salesgoal.db.SalesDbColumn.DbEntry;
import com.chandani.android.salesgoal.db.SalesDbHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class InvestActivity extends Activity {

    private long goalId;
    private String date;

    private InvestArrayAdapter investArrayAdapter;
    private ArrayList<Item> invest_values;
    private ListView listView ;
    private SalesDbHelper salesDbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invest);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.date = getIntent().getStringExtra("DATE");
            this.goalId = Long.parseLong(getIntent().getStringExtra("GOAL_ID"));
        }
        this.salesDbHelper = new SalesDbHelper(getApplicationContext());
        setTitle(this.getGoalName()+", "+this.date);
        listInvest();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_invest, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void listInvest(){
        this.invest_values = get_invest_arr();
        this.investArrayAdapter = new InvestArrayAdapter(this, this.invest_values, this.date, this.goalId);
        listView = (ListView) findViewById(R.id.list_invest);
        listView.setAdapter(this.investArrayAdapter);
    }

    public ArrayList<Item> get_invest_arr() {
        SQLiteDatabase db = salesDbHelper.getReadableDatabase();
        SalesDbHelper mDbHelper = new SalesDbHelper(this);
        ArrayList<Item> itemDetailList = new ArrayList<Item>();
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String curDate = df.format(c.getTime());
        Cursor cursor = db.rawQuery("SELECT * FROM " + DbEntry.TABLE_ITEM + " where " + DbEntry.COLUMN_NAME_GOAL + " = " + this.goalId + " order by " + DbEntry.COLUMN_NAME_ITEM + " ASC", null);
        if (!cursor.isAfterLast()) {
            cursor.moveToFirst();
            do {
                String itemTitle = cursor.getString(cursor.getColumnIndex(DbEntry.COLUMN_NAME_ITEM));
                long id = cursor.getInt(cursor.getColumnIndex(DbEntry._ID));
                long goal = cursor.getInt(cursor.getColumnIndex(DbEntry.COLUMN_NAME_GOAL));
                long price = cursor.getInt(cursor.getColumnIndex(DbEntry.COLUMN_NAME_PRICE));
                long planCount = cursor.getInt(cursor.getColumnIndex(DbEntry.COLUMN_NAME_PLAN_COUNT));
                Item item = new Item(id, itemTitle, price, goal, planCount);
                itemDetailList.add(item);
                cursor.moveToNext();
            } while (!cursor.isAfterLast());
        }
        return itemDetailList;
    }

    public String getGoalName(){
        SQLiteDatabase db = this.salesDbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + DbEntry.TABLE_GOAL + " where " + DbEntry._ID + " = " + this.goalId, null);

        ArrayList<Goal> goalDetailList = new ArrayList<Goal>();
        String goalName = "";
        if(!cursor.isAfterLast())
        {
            cursor.moveToFirst();
            do{
                long id = cursor.getInt(cursor.getColumnIndex(DbEntry._ID));
                goalName = cursor.getString(cursor.getColumnIndex(DbEntry.COLUMN_NAME_GOAL));
                String start = cursor.getString(cursor.getColumnIndex(DbEntry.COLUMN_NAME_START));
                String end = cursor.getString(cursor.getColumnIndex(DbEntry.COLUMN_NAME_DEADLINE));
                String amount = cursor.getString(cursor.getColumnIndex(DbEntry.COLUMN_NAME_TOTAL_AMOUNT));
                goalDetailList.add(new Goal(id, goalName,start, end, amount));
                cursor.moveToNext();
            } while (!cursor.isAfterLast());
        }
        cursor.close();
        db.close();
        return goalName;
    }
}
