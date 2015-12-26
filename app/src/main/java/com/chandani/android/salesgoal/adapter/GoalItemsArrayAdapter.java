package com.chandani.android.salesgoal.adapter;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.chandani.android.salesgoal.BuildConfig;
import com.chandani.android.salesgoal.R;
import com.chandani.android.salesgoal.db.Item;
import com.chandani.android.salesgoal.db.SalesDbColumn.DbEntry;
import com.chandani.android.salesgoal.db.SalesDbHelper;

import java.util.ArrayList;

/**
 * Created by chandani on 11/22/15.
 */
public class GoalItemsArrayAdapter extends ArrayAdapter<Item> {
        private final Context context;
        private ArrayList<Item> items;
        private long goalId;

        public GoalItemsArrayAdapter(Context context, ArrayList<Item> values, long goalId) {
            super(context, R.layout.list_items, values);
            this.context = context;
            this.items = values;
            this.goalId = goalId;
            setNotifyOnChange(true);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.list_items, parent, false);
            final EditText itemlist = (EditText)rowView.findViewById(R.id.item);
            itemlist.setText(this.items.get(position).getName());
            final EditText price = (EditText)rowView.findViewById(R.id.price);
            price.setText(String.valueOf(this.items.get(position).getPrice()));
            final EditText count = (EditText)rowView.findViewById(R.id.plan_count);
            count.setText(String.valueOf(this.items.get(position).getPlanCount()));
            final Button delBtn = (Button)rowView.findViewById(R.id.item_del_btn);
            long itemId = this.items.get(position).getId();
            delBtn.setTag(itemId);
            SalesDbHelper todayDbhelper = new SalesDbHelper(GoalItemsArrayAdapter.this.context);
            final SQLiteDatabase db = todayDbhelper.getWritableDatabase();
            itemlist.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    String itemFilter = DbEntry._ID+"=" + GoalItemsArrayAdapter.this.items.get(position).getId();
                    ContentValues goalArgs = new ContentValues();
                    String itemName = String.valueOf(itemlist.getText());
                    goalArgs.put(DbEntry.COLUMN_NAME_ITEM, itemName);
                    db.update(DbEntry.TABLE_ITEM, goalArgs, itemFilter, null);
                }
            });

            price.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    String itemFilter = DbEntry._ID+"=" + GoalItemsArrayAdapter.this.items.get(position).getId();
                    ContentValues goalArgs = new ContentValues();
                    String itemPrice = String.valueOf(price.getText());
                    goalArgs.put(DbEntry.COLUMN_NAME_PRICE, itemPrice);
                    db.update(DbEntry.TABLE_ITEM, goalArgs, itemFilter, null);
                }
            });

            count.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    String itemFilter = DbEntry._ID+"=" + GoalItemsArrayAdapter.this.items.get(position).getId();
                    ContentValues goalArgs = new ContentValues();
                    String itemCount = String.valueOf(count.getText());
                    goalArgs.put(DbEntry.COLUMN_NAME_PLAN_COUNT, itemCount);
                    db.update(DbEntry.TABLE_ITEM, goalArgs, itemFilter, null);
                }
            });

            delBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    /*********************************/
                    AlertDialog.Builder deleteDialog = new AlertDialog.Builder(GoalItemsArrayAdapter.this.context);
                    deleteDialog.setMessage(R.string.deleteDialog).setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //delete script
                            /*******delete from database*********/
                            String[] whereArgs = new String[] { delBtn.getTag().toString() };
                            db.delete(DbEntry.TABLE_ITEM, DbEntry._ID + "=?", whereArgs);
                            db.delete(DbEntry.TABLE_INVEST, DbEntry.COLUMN_NAME_ITEM_ID + "=?", whereArgs);
                            //remove from arraylist
                            GoalItemsArrayAdapter.this.items.remove(position);
                            GoalItemsArrayAdapter.this.notifyDataSetChanged();
                            /******delete from database end******/
                        }
                    })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //User cancelled the dialog
                                    dialog.cancel();
                                }
                            });
                    deleteDialog.show();
                    /*********************************/
                }
            });
            //context menu start

            //context menu end
            //5. return rowView
            return rowView;
        }
    }