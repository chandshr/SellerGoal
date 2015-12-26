package com.chandani.android.salesgoal.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.chandani.android.salesgoal.R;
import com.chandani.android.salesgoal.activity.InvestActivity;
import com.chandani.android.salesgoal.db.Goal;
import com.chandani.android.salesgoal.db.Item;
import com.chandani.android.salesgoal.db.SalesDbColumn.DbEntry;
import com.chandani.android.salesgoal.db.SalesDbHelper;
import com.jjoe64.graphview.series.DataPoint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by chandani on 11/23/15.
 */
public class CalendarGridAdapter extends BaseAdapter implements View.OnClickListener {

    private static final String tag = "CalendarGridAdapter";
    private final Context _context;
    private long goalId;
    private String goalName;

    private final List<String> list;
    private static final int DAY_OFFSET = 1;
    private final String[] weekdays = new String[] { "Sun", "Mon", "Tue",
            "Wed", "Thu", "Fri", "Sat" };
    private final String[] months = { "January", "February", "March",
            "April", "May", "June", "July", "August", "September",
            "October", "November", "December" };
    private final int[] daysOfMonth = { 31, 28, 31, 30, 31, 30, 31, 31, 30,
            31, 30, 31 };
    private int daysInMonth;
    private int currentDayOfMonth;
    private int currentWeekDay;
    private Button gridcell;
    private TextView num_events_per_day;
    private final HashMap<String, Integer> eventsPerMonthMap;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat(
            "dd-MMM-yyyy");
    private int actualDay;
    private int actualMonth;
    private int actualYear;
    private Button selectedDayMonthYearButton;
    private SalesDbHelper salesDbHelper;
    private ArrayList<DataPoint> ratios;

    // Days in Current Month
    public CalendarGridAdapter(Context context, int textViewResourceId,
                               int month, int year) {
        super();
        this._context = context;
        this.salesDbHelper = new SalesDbHelper(CalendarGridAdapter.this._context);
        this.list = new ArrayList<String>();
        Log.d(tag, "==> Passed in Date FOR Month: " + month + " "
                + "Year: " + year);
        Calendar calendar = Calendar.getInstance();
        setCurrentDayOfMonth(calendar.get(Calendar.DAY_OF_MONTH));
        setCurrentWeekDay(calendar.get(Calendar.DAY_OF_WEEK));
        Log.d(tag, "New Calendar:= " + calendar.getTime().toString());
        Log.d(tag, "CurrentDayOfWeek :" + getCurrentWeekDay());
        Log.d(tag, "CurrentDayOfMonth :" + getCurrentDayOfMonth());
        // Print Month
        printMonth(month, year);

        // Find Number of Events
        eventsPerMonthMap = findNumberOfEventsPerMonth(year, month);
    }

    public void setData(int actualDay, int actualMonth, int actualYear, Button selectedDayMonthYearButton, long id) {
        this.actualDay = actualDay;
        this.actualMonth = actualMonth;
        this.actualYear = actualYear;
        this.selectedDayMonthYearButton = selectedDayMonthYearButton;
        this.goalId = id;
    }

    private String getMonthAsString(int i) {
        return months[i];
    }

    private String getWeekDayAsString(int i) {
        return weekdays[i];
    }

    private int getNumberOfDaysOfMonth(int i) {
        return daysOfMonth[i];
    }

    public String getItem(int position) {
        return list.get(position);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    /**
     * Prints Month
     *
     * @param mm
     * @param yy
     */
    private void printMonth(int mm, int yy) {
        Log.d(tag, "==> printMonth_old: mm: " + mm + " " + "yy: " + yy);
        int trailingSpaces = 0;
        int daysInPrevMonth = 0;
        int prevMonth = 0;
        int prevYear = 0;
        int nextMonth = 0;
        int nextYear = 0;

        int currentMonth = mm - 1;
        String currentMonthName = getMonthAsString(currentMonth);
        daysInMonth = getNumberOfDaysOfMonth(currentMonth);

        Log.d(tag, "Current Month: " + " " + currentMonthName + " having "
                + daysInMonth + " days.");

        GregorianCalendar cal = new GregorianCalendar(yy, currentMonth, 1);
        Log.d(tag, "Gregorian Calendar:= " + cal.getTime().toString());

        if (currentMonth == 11) {
            prevMonth = currentMonth - 1;
            daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
            nextMonth = 0;
            prevYear = yy;
            nextYear = yy + 1;
            Log.d(tag, "*->PrevYear: " + prevYear + " PrevMonth:"
                    + prevMonth + " NextMonth: " + nextMonth
                    + " NextYear: " + nextYear);
        } else if (currentMonth == 0) {
            prevMonth = 11;
            prevYear = yy - 1;
            nextYear = yy;
            daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
            nextMonth = 1;
            Log.d(tag, "**--> PrevYear: " + prevYear + " PrevMonth:"
                    + prevMonth + " NextMonth: " + nextMonth
                    + " NextYear: " + nextYear);
        } else {
            prevMonth = currentMonth - 1;
            nextMonth = currentMonth + 1;
            nextYear = yy;
            prevYear = yy;
            daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
            Log.d(tag, "***---> PrevYear: " + prevYear + " PrevMonth:"
                    + prevMonth + " NextMonth: " + nextMonth
                    + " NextYear: " + nextYear);
        }

        int currentWeekDay = cal.get(Calendar.DAY_OF_WEEK) - 1;
        trailingSpaces = currentWeekDay;

        Log.d(tag, "Week Day:" + currentWeekDay + " is "
                + getWeekDayAsString(currentWeekDay));
        Log.d(tag, "No. Trailing space to Add: " + trailingSpaces);
        Log.d(tag, "No. of Days in Previous Month: " + daysInPrevMonth);

        if (cal.isLeapYear(cal.get(Calendar.YEAR)))
            if (mm == 2)
                ++daysInMonth;
            else if (mm == 3)
                ++daysInPrevMonth;

        // Trailing Month days
        for (int i = 0; i < trailingSpaces; i++) {
            //insert start
            //insert end
            Log.d(tag,
                    "PREV MONTH:= "
                            + prevMonth
                            + " => "
                            + getMonthAsString(prevMonth)
                            + " "
                            + String.valueOf((daysInPrevMonth
                            - trailingSpaces + DAY_OFFSET)
                            + i));
            list.add(String
                    .valueOf((daysInPrevMonth - trailingSpaces + DAY_OFFSET)
                            + i)
                    + "-GREY"
                    + "-"
                    + getMonthAsString(prevMonth)
                    + "-"
                    + prevYear);
        }

        // Current Month Days
        for (int i = 1; i <= daysInMonth; i++) {
            Log.d(currentMonthName, String.valueOf(i) + " "
                    + getMonthAsString(currentMonth) + " " + yy);
            if (i == getCurrentDayOfMonth()) {
                list.add(String.valueOf(i) + "-BLUE" + "-"
                        + getMonthAsString(currentMonth) + "-" + yy);
            } else {
                list.add(String.valueOf(i) + "-WHITE" + "-"
                        + getMonthAsString(currentMonth) + "-" + yy);
            }
        }

        // Leading Month days
        for (int i = 0; i < list.size() % 7; i++) {
            Log.d(tag, "NEXT MONTH:= " + getMonthAsString(nextMonth));
            list.add(String.valueOf(i + 1) + "-GREY" + "-"
                    + getMonthAsString(nextMonth) + "-" + nextYear);
        }
    }

    /**
     * NOTE: YOU NEED TO IMPLEMENT THIS PART Given the YEAR, MONTH, retrieve
     * ALL entries from a SQLite database for that month. Iterate over the
     * List of All entries, and get the dateCreated, which is converted into
     * day.
     *
     * @param year
     * @param month
     * @return
     */
    private HashMap<String, Integer> findNumberOfEventsPerMonth(int year,
                                                                int month) {
        HashMap<String, Integer> map = new HashMap<String, Integer>();

        return map;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) _context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.grid_calendar, parent, false);
        }

        // Get a reference to the Day gridcell
        gridcell = (Button) row.findViewById(R.id.calendar_day_gridcell);
        gridcell.setOnClickListener(this);

        // ACCOUNT FOR SPACING

        Log.d(tag, "Current Day: " + getCurrentDayOfMonth());
        String[] day_color = list.get(position).split("-");
        String theday = day_color[0];
        String themonth = day_color[2];
        String theyear = day_color[3];
        if ((!eventsPerMonthMap.isEmpty()) && (eventsPerMonthMap != null)) {
            if (eventsPerMonthMap.containsKey(theday)) {
                num_events_per_day = (TextView) row
                        .findViewById(R.id.num_events_per_day);
                Integer numEvents = (Integer) eventsPerMonthMap.get(theday);
                num_events_per_day.setText(numEvents.toString());
            }
        }

        // Set the Day GridCell
        gridcell.setText(theday);
        gridcell.setTag(theday + "-" + themonth + "-" + theyear);
        /****Begin****/
        float ratioTotal = -1;

        //PLANCOUNT START
        float salesTotal = -1;
        final SalesDbHelper salesDbHelper = new SalesDbHelper(CalendarGridAdapter.this._context);
        final SQLiteDatabase db = salesDbHelper.getReadableDatabase();
        long planCount = 0;
        long planTotal = 0;

        String[] projection = {
                DbEntry._ID,
                DbEntry.COLUMN_NAME_ITEM,
                DbEntry.COLUMN_NAME_GOAL,
                DbEntry.COLUMN_NAME_PRICE,
                DbEntry.COLUMN_NAME_PLAN_COUNT
        };
        String sortOrder = DbEntry._ID + " DESC";
        Cursor cursor = db.query(
                DbEntry.TABLE_ITEM,  // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The goal_arr for the WHERE clause
                null,                                     // don't group the rows
                null,                                   // don't filter by row groups
                sortOrder                                 // The sort order
        );
        //end
        if(!cursor.isAfterLast())
        {
            cursor.moveToFirst();
            do{
                String name = cursor.getString(cursor.getColumnIndex(DbEntry.COLUMN_NAME_ITEM));
                long id = cursor.getInt(cursor.getColumnIndex("_id"));
                long price = cursor.getInt(cursor.getColumnIndex(DbEntry.COLUMN_NAME_PRICE));
                long goal_id = cursor.getInt(cursor.getColumnIndex(DbEntry.COLUMN_NAME_GOAL));
                long plan_count = cursor.getInt(cursor.getColumnIndex(DbEntry.COLUMN_NAME_PLAN_COUNT));
                planTotal += plan_count;
                cursor.moveToNext();
            } while (!cursor.isAfterLast());
        }
        //PLANCOUNT END
        String investCurrHrQuery = "SELECT * FROM " + DbEntry.TABLE_INVEST + " WHERE "+DbEntry.COLUMN_NAME_GOAL+" = "+this.goalId+" AND " +DbEntry.COLUMN_NAME_INVEST_DATE+" = '"+gridcell.getTag()+"'";
        Cursor investCurr = db.rawQuery(investCurrHrQuery, null);
        ArrayList<DataPoint> dataPoints = new ArrayList<>();
        if (investCurr.getCount() > 0) {
            ratioTotal = 0;
            salesTotal = 0;
            float ratio = 0;
            investCurr.moveToFirst();
            do {
                long salesCount = investCurr.getInt(investCurr.getColumnIndex(DbEntry.COLUMN_NAME_SALES_COUNT));
                //datapoints start
//                DataPoint dataPoint = new DataPoint(Integer.parseInt(gridcell.getTag().toString()), Float.parseFloat(todayInvesthr));
//                dataPoints.add(dataPoint);
                //datapoints stop
                ratioTotal += ratio;
                salesTotal += salesCount;
            } while (investCurr.moveToNext());
            investCurr.close();
        }
        ratioTotal = salesTotal/planTotal;
        /*************************DATAPOINTS START*****************************************/
        this.calendarRatio(dataPoints);
        this.ratios = dataPoints;
        /*************************DATAPOINTS END*****************************************/

        if(ratioTotal>0.8){
//            gridcell.setBackgroundColor(0xFF00FF00);
            gridcell.setBackgroundColor(Color.parseColor("#00e700")); //#00b400 lighter green
        }else if(ratioTotal>=0 && ratioTotal<=0.8) {
//            gridcell.setBackgroundColor(0xffffff00);
            gridcell.setBackgroundColor(Color.parseColor("#FFDF00")); //yellow
        }
        else {
            gridcell.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }
        /****End*****/

        if (day_color[1].equals("WHITE")) {
            gridcell.setTextColor(Color.parseColor("#696969"));
        }
        if (day_color[1].equals("GREY")) {
            gridcell.setTextColor(Color.parseColor("#a8a8a8"));
        }
        if (day_color[1].equals("BLUE")) {
            gridcell.setText(theday);/************ CURRENT DATE ***********************/
            gridcell.setTextColor(Color.parseColor("#ff1d8e"));
        }
        return row;
    }

    static final int REQUEST_FOR_ACTIVITY_CODE = 1;

    @Override
    public void onClick(View view) {
        String date_month_year = (String) view.getTag();
        //
        Intent intent = new Intent(_context, InvestActivity.class);
        intent.putExtra("GOAL_ID", String.valueOf(this.goalId));
        intent.putExtra("DATE", date_month_year);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        _context.startActivity(intent);
//        ((Activity) _context).startActivityForResult(intent, REQUEST_FOR_ACTIVITY_CODE);
        //
        try {
            Date parsedDate = dateFormatter.parse(date_month_year);
            Log.d(tag, "Parsed Date: " + parsedDate.toString());

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_FOR_ACTIVITY_CODE) {
            // Make sure the request was successful
            if (resultCode == 1) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.

                // Do something with the contact here (bigger example below)
            }
        }
    }

    public ArrayList<DataPoint> calendarRatio(ArrayList<DataPoint> ratiosArr){
        return ratiosArr;
    }


    public int getCurrentDayOfMonth() {
        return currentDayOfMonth;
    }

    private void setCurrentDayOfMonth(int currentDayOfMonth) {
        this.currentDayOfMonth = currentDayOfMonth;
    }

    public void setCurrentWeekDay(int currentWeekDay) {
        this.currentWeekDay = currentWeekDay;
    }

    public int getCurrentWeekDay() {
        return currentWeekDay;
    }
}
