package com.chandani.android.salesgoal.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chandani.android.salesgoal.R;
import com.chandani.android.salesgoal.adapter.CalendarGridAdapter;
import com.chandani.android.salesgoal.db.Goal;
import com.chandani.android.salesgoal.db.Item;
import com.chandani.android.salesgoal.db.SalesDbColumn.DbEntry;
import com.chandani.android.salesgoal.db.SalesDbHelper;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TreeMap;

@TargetApi(3)
public class CalendarActivity extends AppCompatActivity implements View.OnClickListener {

    private long goalId;
    private static final String tag = "CalendarActivity";

    private TextView currentMonth;
    private Button selectedDayMonthYearButton;
    private ImageView prevMonth;
    private ImageView nextMonth;
    private ImageView prevWeek;
    private ImageView nextWeek;
    private GridView calendarView;
    private CalendarGridAdapter adapter;
    private Calendar _calendar;
    @SuppressLint("NewApi")
    private int month, year;
    @SuppressWarnings("unused")
    @SuppressLint({ "NewApi", "NewApi", "NewApi", "NewApi" })
    private final DateFormat dateFormatter = new DateFormat();
    private static final String dateTemplate = "MMMM yyyy";

    private int actualYear, actualMonth, actualDay, numDays;
    private String goalDeadline, goalStartDate;

    private float goalAmount;
    private String goalName;

    private SalesDbHelper salesDbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String test = getIntent().getStringExtra("GOAL");
            this.goalId = Long.parseLong(getIntent().getStringExtra("GOAL"));
        }

        _calendar = Calendar.getInstance(Locale.getDefault());
        month = _calendar.get(Calendar.MONTH) + 1;
        year = _calendar.get(Calendar.YEAR);
        actualYear = year;
        actualMonth = month;
        actualDay = _calendar.get(Calendar.DAY_OF_MONTH);
        numDays = _calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        prevMonth = (ImageView) this.findViewById(R.id.prevMonth);
        prevMonth.setOnClickListener(this);

        currentMonth = (TextView) this.findViewById(R.id.currentMonth);
        currentMonth.setText(DateFormat.format(dateTemplate,
                _calendar.getTime()));

        nextMonth = (ImageView) this.findViewById(R.id.nextMonth);
        nextMonth.setOnClickListener(this);

        calendarView = (GridView) this.findViewById(R.id.calendar);

        this.salesDbHelper = new SalesDbHelper(getApplicationContext());

        //get start and end date
        this.setGoalData();
        setTitle("Progress in "+this.goalName);

        //week grid start
        ArrayList<String> weekDays = new ArrayList<>();
        weekDays.add("Sun");
        weekDays.add("Mon");
        weekDays.add("Tues");
        weekDays.add("Wed");
        weekDays.add("Thur");
        weekDays.add("Fri");
        weekDays.add("Sat");
        GridView gridWeek = (GridView) findViewById(R.id.week);
        ArrayAdapter<String> adp=new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line,weekDays);
        gridWeek.setAdapter(adp);
        //week grid end

        // Initialised
        adapter = new CalendarGridAdapter(getApplicationContext(),
                R.id.calendar_day_gridcell, month, year);
        adapter.setData(actualDay, actualMonth, actualYear, selectedDayMonthYearButton, this.goalId);
        adapter.notifyDataSetChanged();
        calendarView.setAdapter(adapter);

        /*****GRAPH START*******/
        setGraphData(this.goalId, month);
        /*****GRAPH END*******/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_calendar, menu);
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
        }else if(id == R.id.action_detail){
            Intent graph_intent = new Intent(this, GoalActivity.class);
            graph_intent.putExtra("GOAL_ID", String.valueOf(this.goalId));
            startActivityForResult(graph_intent, 0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     *
     * @param month
     * @param year
     */
    private void setGridCellAdapterToDate(int month, int year) {
        adapter = new CalendarGridAdapter(getApplicationContext(),
                R.id.calendar_day_gridcell, month, year);
        _calendar.set(year, month - 1, _calendar.get(Calendar.DAY_OF_MONTH));
        currentMonth.setText(DateFormat.format(dateTemplate,
                _calendar.getTime()));
        adapter.setData(actualDay, actualMonth, actualYear, selectedDayMonthYearButton, this.goalId);
        adapter.notifyDataSetChanged();
        calendarView.setAdapter(adapter);
        /*****GRAPH START*******/
        this.salesDbHelper = new SalesDbHelper(getApplicationContext());
        setGraphData(this.goalId, month);
        /*****GRAPH END*******/
    }

    @Override
    public void onClick(View v) {
        GraphView graph1 = (GraphView) findViewById(R.id.graph1);
        graph1.removeAllSeries();
        if (v == prevMonth) {
            if (month <= 1) {
                month = 12;
                year--;
            } else {
                month--;
            }
            Log.d(tag, "Setting Prev Month in CalendarGridAdapter: " + "Month: "
                    + month + " Year: " + year);
            setGridCellAdapterToDate(month, year);
        }
        if (v == nextMonth) {
            if (month > 11) {
                month = 1;
                year++;
            } else {
                month++;
            }
            Log.d(tag, "Setting Next Month in CalendarGridAdapter: " + "Month: "
                    + month + " Year: " + year);
            setGridCellAdapterToDate(month, year);
        }
    }

    public ArrayList<Item> item_arr(){
        SQLiteDatabase db = this.salesDbHelper.getReadableDatabase();
        ArrayList<Item> goalItem = new ArrayList<Item>();
        String sortOrder = DbEntry._ID + " DESC";
        Cursor cursor = db.rawQuery("SELECT * FROM " + DbEntry.TABLE_ITEM + " where " + DbEntry.COLUMN_NAME_GOAL + "= ? order by "+DbEntry.COLUMN_NAME_ITEM +" ASC", new String[]{String.valueOf(this.goalId)});
        if(!cursor.isAfterLast())
        {
            cursor.moveToFirst();
            do{
                long id = cursor.getInt(cursor.getColumnIndex(DbEntry._ID));
                String name = cursor.getString(cursor.getColumnIndex(DbEntry.COLUMN_NAME_ITEM));
                long goal = cursor.getInt(cursor.getColumnIndex(DbEntry.COLUMN_NAME_GOAL));
                long price = cursor.getInt(cursor.getColumnIndex(DbEntry.COLUMN_NAME_PRICE));
                long planCount = cursor.getInt(cursor.getColumnIndex(DbEntry.COLUMN_NAME_PLAN_COUNT));
                Item item = new Item(id, name, price, goal, planCount);
                goalItem.add(item);
                cursor.moveToNext();
            } while (!cursor.isAfterLast());
        }
        return goalItem;
    }

    public void setGraphData(long goalId, int month) {
        ArrayList<Item> itemArr = this.item_arr();

        LineGraphSeries<DataPoint>[] ratio_series = new LineGraphSeries[itemArr.size()];
        GraphView graph1 = (GraphView) findViewById(R.id.graph1);
        graph1.removeAllSeries();

        String monthString = new DateFormatSymbols().getMonths()[month - 1];
        SQLiteDatabase db = this.salesDbHelper.getReadableDatabase();
        Cursor last_invest_row = db.rawQuery("SELECT * FROM " + DbEntry.TABLE_INVEST + " where " + DbEntry.COLUMN_NAME_INVEST_DATE + " LIKE '%" + monthString + "%' ORDER BY " + DbEntry._ID + " DESC LIMIT 1", null);

        float totalPlan = 0;
        float allItemsTotalSales = 0;

//        ArrayList<Float> totaldataArr = new ArrayList<Float>();
        TreeMap<Integer, Float> combineSalesHash = new TreeMap<Integer, Float>();

        for (int i = 0; i < itemArr.size(); i++) {
            float totalSales = 0;
            Cursor plan_cursor = db.rawQuery("SELECT * FROM " + DbEntry.TABLE_ITEM + " where " + DbEntry.COLUMN_NAME_GOAL + " = " + this.goalId + " AND " + DbEntry.COLUMN_NAME_ITEM + " = '" + itemArr.get(i).getName() + "' LIMIT 1", null);
            float price = 0;
            if (plan_cursor.moveToFirst()) {
                do {
                    price = Float.parseFloat(plan_cursor.getString(plan_cursor.getColumnIndex(DbEntry.COLUMN_NAME_PRICE)));
                    int count = Integer.parseInt(plan_cursor.getString(plan_cursor.getColumnIndex(DbEntry.COLUMN_NAME_PLAN_COUNT)));
                    totalPlan += (price * count);
                } while (plan_cursor.moveToNext());
            }
            plan_cursor.close();
            Cursor invest_cursor = db.rawQuery("SELECT * FROM " + DbEntry.TABLE_INVEST + " where " + DbEntry.COLUMN_NAME_GOAL + " = " + this.goalId + " AND " + DbEntry.COLUMN_NAME_ITEM_ID + " = '" + itemArr.get(i).getId() + "' AND " + DbEntry.COLUMN_NAME_INVEST_DATE + " LIKE '%" + monthString + "%' ORDER BY CAST(" + DbEntry.COLUMN_NAME_INVEST_DATE + " AS INTEGER) ASC", null);
            int j = 0;
            int day = 0;
            ArrayList<DataPoint> datapointArr = new ArrayList<DataPoint>();
            if (invest_cursor.moveToFirst()) {
                String salesCountStr = invest_cursor.getString(invest_cursor.getColumnIndex(DbEntry.COLUMN_NAME_SALES_COUNT));
                if (salesCountStr != null) {
                    String prev_date = invest_cursor.getString(invest_cursor.getColumnIndex(DbEntry.COLUMN_NAME_INVEST_DATE));
                    int prev_day = Integer.parseInt(prev_date.split("\\-")[0]);
                    int min_day = prev_day;
                    do {
                        String invest_date = invest_cursor.getString(invest_cursor.getColumnIndex(DbEntry.COLUMN_NAME_INVEST_DATE));
                        int salesCount = invest_cursor.getInt(invest_cursor.getColumnIndex(DbEntry.COLUMN_NAME_SALES_COUNT));
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        day = Integer.parseInt(invest_date.split("\\-")[0]);
                        while (day - prev_day > 1) {
                            DataPoint dataPoint = new DataPoint(prev_day + 1, totalSales);
                            DataPoint totaldataPoint = new DataPoint(prev_day + 1, allItemsTotalSales);
                            datapointArr.add(dataPoint);
                            int index = prev_day + 1;
                            Float value = combineSalesHash.get(index);
                            if (value != null) {
                                combineSalesHash.put(index, value + totalSales);
                            } else {
                                combineSalesHash.put(index, totalSales);
                            }
                            prev_day = prev_day + 1;
                        }
                        if (day < min_day) {
                            int temp = day + 1; //temp stores previous min value
                            while (temp < min_day) {
                                DataPoint dataPoint = new DataPoint(temp, totalSales);
                                DataPoint totaldataPoint = new DataPoint(temp, allItemsTotalSales);
                                datapointArr.add(dataPoint);
                                Float value = combineSalesHash.get(temp);
                                if (value != null) {
                                    combineSalesHash.put(temp, value + totalSales);
                                } else {
                                    combineSalesHash.put(temp, totalSales);
                                }

                                temp++;
                            }
                            min_day = day; //min_day is updated to new min
                        }
                        totalSales += (price * salesCount);
                        allItemsTotalSales += totalSales;
                        DataPoint aDataPoint = new DataPoint(day, totalSales);
//                        DataPoint totaldataPoint = new DataPoint(day, allItemsTotalSales);
                        datapointArr.add(aDataPoint);
                        Float value = combineSalesHash.get(day);
                        if (combineSalesHash.size() > 0 && value != null) {
                            combineSalesHash.put(day, value + totalSales);
                        } else {
                            combineSalesHash.put(day, totalSales);
                        }
                        prev_day = day;
                        j++;
                    } while (invest_cursor.moveToNext());
                    DataPoint[] dataPoints = new DataPoint[datapointArr.size()];
//                    Collections.sort(datapointArr, new DataPointCompare());
//                    Collections.sort(totaldatapointArr, new DataPointCompare());
                    for (int k = 0; k < datapointArr.size(); k++) {
                        dataPoints[k] = datapointArr.get(k);
//                        totaldataPoints[k] = totaldatapointArr.get(k);
                    }

                    ratio_series[i] = new LineGraphSeries<>(dataPoints);
                    ratio_series[i].setTitle(itemArr.get(i).getName());
                    ratio_series[i].setDrawDataPoints(true);
                    ratio_series[i].setDataPointsRadius(4);

                    Random rnd = new Random();
                    ratio_series[i].setColor(Color.rgb(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)));

                    ratio_series[i].setOnDataPointTapListener(new OnDataPointTapListener() {
                        @Override
                        public void onTap(Series series, DataPointInterface dataPoint) {
                            Toast.makeText(CalendarActivity.this.getApplicationContext(), "Money made from " + series.getTitle() + " till " + ((int) dataPoint.getX()) + "th is $" + dataPoint.getY(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    graph1.addSeries(ratio_series[i]);
                }
            }
            invest_cursor.close();
        }

        this.setTotalItemSalesLineSeries(combineSalesHash, graph1);

        this.setGraphLabels(graph1); //labels for y-axis and x-axis

        graph1.getLegendRenderer().setVisible(true);
        graph1.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.BOTTOM);
        graph1.getLegendRenderer().setWidth(250);
        graph1.getLegendRenderer().setBackgroundColor(Color.argb(50, 50, 0, 200));
    }

    public void setTotalItemSalesLineSeries(TreeMap<Integer, Float> combineSalesHash, GraphView graph1 ){
        if(combineSalesHash.size()>0){
            DataPoint[] totaldataPoints = new DataPoint[combineSalesHash.size()];
            int i = 0;
            for (Integer date : combineSalesHash.keySet()) {
                combineSalesHash.get(date);
                float value = combineSalesHash.get(date);
                totaldataPoints[i] = new DataPoint(date, value);
                i++;
            }

            LineGraphSeries totalSalesSeries = new LineGraphSeries<>(totaldataPoints);

            totalSalesSeries.setTitle("Total Sell");
            totalSalesSeries.setDrawDataPoints(true);
            totalSalesSeries.setDataPointsRadius(4);
            int green = getResources().getColor(R.color.green);
            totalSalesSeries.setColor(green);

            totalSalesSeries.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series, DataPointInterface dataPoint) {
                    Toast.makeText(CalendarActivity.this.getApplicationContext(), "Money made from " + series.getTitle() + " till " + ((int) dataPoint.getX()) + "th is $" + dataPoint.getY(), Toast.LENGTH_SHORT).show();
                }
            });

            graph1.addSeries(totalSalesSeries);
        }
    }

    public void setGoalData(){
        String deadline = "";
        String startDay = "";
        SQLiteDatabase db = this.salesDbHelper.getReadableDatabase();
        Cursor goalCur = db.rawQuery("SELECT * FROM " + DbEntry.TABLE_GOAL + " where " + DbEntry._ID + " = " + this.goalId , null);
        if (goalCur.moveToFirst()) {
            this.goalDeadline = goalCur.getString(goalCur.getColumnIndex(DbEntry.COLUMN_NAME_DEADLINE));
            this.goalStartDate = goalCur.getString(goalCur.getColumnIndex(DbEntry.COLUMN_NAME_START));
            this.goalAmount = goalCur.getFloat(goalCur.getColumnIndex(DbEntry.COLUMN_NAME_TOTAL_AMOUNT));
            this.goalName = goalCur.getString(goalCur.getColumnIndex(DbEntry.COLUMN_NAME_GOAL));
        }
        goalCur.close();
        db.close();
    }

    public void setGraphLabels(GraphView graph1){
        NumberFormat x = NumberFormat.getInstance();
        NumberFormat y = NumberFormat.getInstance();

        x.setMaximumFractionDigits(0);
        y.setMaximumFractionDigits(0);

        int startDay = Integer.parseInt(this.goalStartDate.split("\\/")[0]);
        int startMonth = Integer.parseInt(this.goalStartDate.split("\\/")[1]);

        //Xaxis startDate start
        if(startMonth==this.month){
            graph1.getViewport().setMinX(startDay);
        }else{
            graph1.getViewport().setMinX(1);
        }

        int deadlineDay = Integer.parseInt(this.goalDeadline.split("\\/")[0]);
        int deadlineMonth = Integer.parseInt(this.goalDeadline.split("\\/")[1]);

        //Xaxis deadline start
        if(deadlineMonth==this.month) {
            graph1.getViewport().setMaxX(deadlineDay);
        }else{
            graph1.getViewport().setMaxX(this.numDays); //total no. of days in a month
        }

        graph1.getViewport().setMaxY(this.goalAmount);
        graph1.getViewport().setMinY(1);

        graph1.getViewport().setXAxisBoundsManual(true);
        graph1.getViewport().setYAxisBoundsManual(true);

        int purple = getResources().getColor(R.color.lightpurple);
        graph1.setBackgroundColor(purple);
        graph1.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(x, y) {
            //            graph1.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // show normal x values
                    return super.formatLabel(value, isValueX);
                } else {
                    // show currency for y values
                    return "$" + super.formatLabel(value, isValueX);
                }
            }
        });

        graph1.getGridLabelRenderer().setNumVerticalLabels(8);

//        graph1.getViewport().setScrollable(true);
        graph1.getViewport().setScalable(true);
    }

    /*******Compare Class START *******/
    private class DataPointCompare implements Comparator<DataPoint> {
        public int compare(DataPoint a, DataPoint b) {
            double aX = a.getX();
            double bX = b.getX();
            if (aX < bX) {
                return -1;
            }
            else if (aX > bX) {
                return 1;
            }
            else {
                return 0;
            }
        }
    }
    /*******Compare Class END *******/

    //this onResume function refreshes the graph with the newly entered investment graph
    @Override
    public void onResume() {
        super.onResume();
        setGraphData(goalId, month);
        this.adapter.notifyDataSetChanged();
    }


}
