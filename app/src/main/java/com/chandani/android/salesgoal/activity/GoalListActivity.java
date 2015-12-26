package com.chandani.android.salesgoal.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.chandani.android.salesgoal.R;
import com.chandani.android.salesgoal.adapter.GoalArrayAdapter;
import com.chandani.android.salesgoal.db.Goal;
import com.chandani.android.salesgoal.db.SalesDbColumn;
import com.chandani.android.salesgoal.db.SalesDbColumn.DbEntry;
import com.chandani.android.salesgoal.db.SalesDbHelper;

import java.util.ArrayList;
import java.util.Calendar;

public class GoalListActivity extends AppCompatActivity {

    private SalesDbHelper salesDbHelper;
    private SQLiteDatabase goalDB;
    private GoalArrayAdapter goalArrayAdapter;
    private ListView goalListView ;
    private String prevGoalTitle;

    /** Deadline Start **/
    private DatePicker datePicker;
    private Calendar start_calendar;
    private Calendar end_calendar;
    private int end_year, end_month, end_day;
    private int start_year, start_month, start_day;
    private String startDate;
    private String endDate;
    /** Deadline End **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("Sells Goal");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_list);
        /**Deadline Start**/
        start_calendar = Calendar.getInstance();
        end_calendar = Calendar.getInstance();
        start_year = start_calendar.get(Calendar.YEAR);
        start_month = start_calendar.get(Calendar.MONTH);
        start_day = start_calendar.get(Calendar.DAY_OF_MONTH);
        end_year = start_calendar.get(Calendar.YEAR);
        end_month = start_calendar.get(Calendar.MONTH);
        end_day = start_calendar.get(Calendar.DAY_OF_MONTH);
        /**Deadline End**/
        this.salesDbHelper = new SalesDbHelper(getApplicationContext());
        listGoal();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_goal_list, menu);
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

    public void addGoal(View view){
        EditText goal = (EditText) findViewById(R.id.goal_add);
        EditText amount = (EditText) findViewById(R.id.amount_add);
        Button start = (Button) findViewById(R.id.goal_start);
        Button end = (Button) findViewById(R.id.goal_end);
        String goalStr = goal.getText().toString();
        String amountStr = amount.getText().toString();
        if(goalStr.isEmpty()){
            amount.setError(null);
            start.setError(null);
            end.setError(null);
            goal.setError("Enter Goal Name to proceed");
        }else if(amountStr.isEmpty()){
            goal.setError(null);
            start.setError(null);
            end.setError(null);
            amount.setError("Enter amount in numbers");
        }else if(this.startDate==null){
            goal.setError(null);
            amount.setError(null);
            end.setError(null);
            start.setError("Choose Start Date by clicking START button");
        }else if(this.endDate==null){
            goal.setError(null);
            amount.setError(null);
            start.setError(null);
            end.setError("Choose Deadline by clicking DEADLINE button");
        }else if(!goalStr.isEmpty()&&!amountStr.isEmpty()&&this.startDate!=null&&this.endDate!=null){
            goal.setError(null);
            amount.setError(null);
            start.setError(null);
            end.setError(null);
            ContentValues goal_arr = new ContentValues();
            goal_arr.put(DbEntry.COLUMN_NAME_GOAL, goalStr);
            goal_arr.put(DbEntry.COLUMN_NAME_TOTAL_AMOUNT, amountStr);
            goal_arr.put(DbEntry.COLUMN_NAME_START, this.startDate);
            goal_arr.put(DbEntry.COLUMN_NAME_DEADLINE, this.endDate);
            this.goalDB = this.salesDbHelper.getWritableDatabase();
            long insertId = this.goalDB.insert(DbEntry.TABLE_GOAL, DbEntry._ID, goal_arr);
            Goal goalObj = new Goal(insertId, goalStr, this.startDate, this.endDate, amountStr);
            this.goalArrayAdapter.insert(goalObj, 0);
            this.goalArrayAdapter.notifyDataSetChanged();
            this.goalDB.close();
            goal.setText("");
            amount.setText("");
            start.setText("START");
            end.setText("END");
        }
    }

    public void listGoal(){
        ArrayList<Goal> goal_arr = read_goal();
        /*START ADAPTER*/
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, goal_arr);
        this.goalArrayAdapter = new GoalArrayAdapter(this, goal_arr);
        this.goalListView = (ListView) findViewById(R.id.list_goal);
        this.goalListView.setAdapter(this.goalArrayAdapter);
        registerForContextMenu(this.goalListView);
    }

    public ArrayList<Goal> read_goal(){
        SQLiteDatabase db = this.salesDbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + DbEntry.TABLE_GOAL + " ORDER BY CAST(" + DbEntry.COLUMN_NAME_DEADLINE + " AS INTEGER) ASC", null);

        ArrayList<Goal> goalDetailList = new ArrayList<Goal>();
        if(!cursor.isAfterLast())
        {
            cursor.moveToFirst();
            do{
                long id = cursor.getInt(cursor.getColumnIndex(DbEntry._ID));
                String goal = cursor.getString(cursor.getColumnIndex(DbEntry.COLUMN_NAME_GOAL));
                String start = cursor.getString(cursor.getColumnIndex(DbEntry.COLUMN_NAME_START));
                String end = cursor.getString(cursor.getColumnIndex(DbEntry.COLUMN_NAME_DEADLINE));
                String amount = cursor.getString(cursor.getColumnIndex(DbEntry.COLUMN_NAME_TOTAL_AMOUNT));
                goalDetailList.add(new Goal(id, goal,start, end, amount));
                cursor.moveToNext();
            } while (!cursor.isAfterLast());
        }
        cursor.close();
        db.close();
        return goalDetailList;
    }

    /*************view item detail START ***********/
    public void goalActivity(View view){
        String goalId = view.getTag().toString();
        Intent graph_intent = new Intent(this, GoalActivity.class);
        graph_intent.putExtra("GOAL_ID", goalId);
        startActivityForResult(graph_intent, 0);
    }
    /*************view item detail END ***********/

    //start
    public void updateGoalTitle(View view){
        SalesDbHelper salesDbHelper = new SalesDbHelper(getApplicationContext());
        final SQLiteDatabase db = salesDbHelper.getWritableDatabase();
        final EditText goallist = (EditText)view.findViewById(R.id.goal_add_list);
        goallist.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                GoalListActivity.this.prevGoalTitle =  goallist.getText().toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(goallist.getText().length()>=0){
                    String goalFilter = DbEntry.COLUMN_NAME_GOAL+"='" + GoalListActivity.this.prevGoalTitle+"'";
                    ContentValues goalArgs = new ContentValues();
                    String goalTitle = String.valueOf(goallist.getText());
                    goalArgs.put(DbEntry.COLUMN_NAME_GOAL, goalTitle);
                    db.update(DbEntry.TABLE_GOAL, goalArgs, goalFilter, null);
                    ContentValues itemArgs = new ContentValues();
                    itemArgs.put(DbEntry.COLUMN_NAME_GOAL, goalTitle);
                    db.update(DbEntry.TABLE_ITEM, itemArgs, goalFilter, null);
                }
            }


        });
    }
    //end
    @SuppressWarnings("deprecation")
    public void setStart(View view) {
        showDialog(900);
        Toast.makeText(getApplicationContext(), "Choose Start Date", Toast.LENGTH_SHORT)
                .show();
    }

    @SuppressWarnings("deprecation")
    public void setEnd(View view) {
        showDialog(999);
        Toast.makeText(getApplicationContext(), "Choose Deadline", Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        // TODO Auto-generated method stub
        if (id == 900) {
            return new DatePickerDialog(this, startDateListener, this.end_year, this.end_month, this.end_day);
        }else if(id == 999){
            return new DatePickerDialog(this, endDateListener, this.start_year, this.start_month, this.start_day);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener startDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
//            showDate(arg1, arg2+1, arg3);
            //store it in database
            GoalListActivity.this.startDate = arg3+"/"+(arg2+1)+"/"+arg1; //  DD/MM/YYYY
            View view1 = GoalListActivity.this.findViewById(android.R.id.content);
            Button start = (Button) findViewById(R.id.goal_start);
            start.setText("Start: "+GoalListActivity.this.startDate);
        }
    };

    private DatePickerDialog.OnDateSetListener endDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
//            showDate(arg1, arg2+1, arg3);
            //store it in database
            GoalListActivity.this.endDate = arg3+"/"+(arg2+1)+"/"+arg1; // DD/MM/YY used this format because list goal is sorted by casting this string and the casting sorts by first element whih is a day
            View view1 = GoalListActivity.this.findViewById(android.R.id.content);
            Button end = (Button) findViewById(R.id.goal_end);
            end.setText("End: "+GoalListActivity.this.endDate);
        }
    };
}
