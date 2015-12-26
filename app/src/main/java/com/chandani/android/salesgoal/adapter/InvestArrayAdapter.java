package com.chandani.android.salesgoal.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.chandani.android.salesgoal.R;
import com.chandani.android.salesgoal.db.Item;
import com.chandani.android.salesgoal.db.SalesDbColumn.DbEntry;
import com.chandani.android.salesgoal.db.SalesDbHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by chandani on 11/23/15.
 */
public class InvestArrayAdapter extends ArrayAdapter<Item> {

    private final Context context;
    private final ArrayList<Item> invest_arr;
    private String date;
    private long goalId;

    public InvestArrayAdapter(Context context, ArrayList<Item> values, String date, long goal) {
        super(context, R.layout.list_invest, values);
        this.context = context;
        this.invest_arr = values;
        this.date = date;
        this.goalId = goal;
        setNotifyOnChange(true);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        //1. create inflater
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //2. get rowView from inflater
        View rowView = inflater.inflate(R.layout.list_invest, parent, false);
        //3. get all the editText view from the rowView
        final TextView investlist = (TextView)rowView.findViewById(R.id.invest_item_list);
        final EditText investCount = (EditText)rowView.findViewById(R.id.invest_salesCount_list);
        //4. Set the text for editText view
        investlist.setText(this.invest_arr.get(position).getName());
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String investCurrHrQuery = "SELECT * FROM " + DbEntry.TABLE_INVEST + " WHERE " + DbEntry.COLUMN_NAME_ITEM_ID + " = " +this.invest_arr.get(position).getId()+
                " AND " + DbEntry.COLUMN_NAME_GOAL + " = " + this.goalId + " AND "+DbEntry.COLUMN_NAME_INVEST_DATE+" = '"+this.date+"'";
        final SalesDbHelper salesDbHelper = new SalesDbHelper(InvestArrayAdapter.this.context);
        final SQLiteDatabase db = salesDbHelper.getWritableDatabase();
        Cursor investCurr = db.rawQuery(investCurrHrQuery, null);
        if (investCurr.getCount() > 0) {
            investCurr.moveToFirst();
            do {
                String salesCount = investCurr.getString(investCurr.getColumnIndex(DbEntry.COLUMN_NAME_SALES_COUNT));
                investCount.setText(salesCount);
            } while (investCurr.moveToNext());
            investCurr.close();
        }
        // Gets the data repository in write mode
        investCount.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            //when the empty hr text box is edited for updated
            @Override
            public void afterTextChanged(Editable s) {
                //plan start
                String planQuery = "SELECT * FROM " + DbEntry.TABLE_ITEM + " WHERE " + DbEntry.COLUMN_NAME_ITEM + " = '" + InvestArrayAdapter.this.invest_arr.get(position).getName()
                        + "' AND " + DbEntry.COLUMN_NAME_GOAL + " = " + InvestArrayAdapter.this.goalId;
                Cursor p = db.rawQuery(planQuery, null);
                long planCount = 0;
                if (p.getCount() > 0) {
                    p.moveToFirst();
                    do {
                        planCount = p.getInt(p.getColumnIndex(DbEntry.COLUMN_NAME_PLAN_COUNT));
                    } while (p.moveToNext());
                    p.close();
                }
                if(planCount>0){
                    String investStr = investCount.getText().toString();
                    if(!investStr.isEmpty()){
                        long investCountVal = Long.parseLong(investStr);
                        float ratio = investCountVal/planCount;
                    }
                    String itemFilter = DbEntry.COLUMN_NAME_ITEM_ID+"='"+ InvestArrayAdapter.this.invest_arr.get(position).getId()+"' AND "+DbEntry.COLUMN_NAME_INVEST_DATE+"='"+InvestArrayAdapter.this.date+"'";
                    ContentValues args = new ContentValues();
                    args.put(DbEntry.COLUMN_NAME_ITEM_ID, InvestArrayAdapter.this.invest_arr.get(position).getId());
                    args.put(DbEntry.COLUMN_NAME_GOAL, InvestArrayAdapter.this.invest_arr.get(position).getGoalId());
                    args.put(DbEntry.COLUMN_NAME_INVEST_DATE, InvestArrayAdapter.this.date);
                    args.put(DbEntry.COLUMN_NAME_SALES_COUNT, String.valueOf(investCount.getText()));
                    int update = db.update(DbEntry.TABLE_INVEST, args, itemFilter, null);
                    if(update<=0){
                        db.insert(DbEntry.TABLE_INVEST, DbEntry._ID, args);
                    }
                }else{
                    investCount.setError("Set plan in plan interface");
                }
            }
        });
        //5. return rowView
        return rowView;
    }
}
