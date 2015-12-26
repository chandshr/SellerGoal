package com.chandani.android.salesgoal.adapter;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.chandani.android.salesgoal.R;
import com.chandani.android.salesgoal.activity.GoalActivity;
import com.chandani.android.salesgoal.db.Goal;
import com.chandani.android.salesgoal.db.SalesDbColumn.DbEntry;
import com.chandani.android.salesgoal.db.SalesDbHelper;

import java.util.ArrayList;

/**
 * Created by chandani on 11/21/15.
 */
public class GoalArrayAdapter extends ArrayAdapter<Goal> {

    private final Context context;
    private ArrayList<Goal> goals;
    private String prevGoalTitle;

    public GoalArrayAdapter(Context context, ArrayList<Goal> values){
        super(context, R.layout.list_goal, values);
        this.context = context;
        this.goals = values;
        setNotifyOnChange(true);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_goal, parent, false);
        final EditText goallist = (EditText)rowView.findViewById(R.id.goal_add_list);
        final EditText endlist = (EditText)rowView.findViewById(R.id.goal_end_list);

        goallist.setText(this.goals.get(position).getGoal());
        endlist.setText(this.goals.get(position).getEnd());
        Button itemView = (Button)rowView.findViewById(R.id.itemView);
        Button delGoal = (Button)rowView.findViewById(R.id.goalDel);
        itemView.setTag(this.goals.get(position).getId());
        delGoal.setTag(this.goals.get(position).getId());
        GoalArrayAdapter.this.prevGoalTitle =  GoalArrayAdapter.this.goals.get(position).getGoal();

        SalesDbHelper salesDbHelper = new SalesDbHelper(GoalArrayAdapter.this.context);
        final SQLiteDatabase db = salesDbHelper.getWritableDatabase();

        goallist.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
//                String goalFilter = DbEntry.COLUMN_NAME_GOAL+"='" + GoalArrayAdapter.this.prevGoalTitle+"'";
                String goalFilter = DbEntry._ID+"=" + GoalArrayAdapter.this.goals.get(position).getId();
                ContentValues goalArgs = new ContentValues();
                String goalTitle = goallist.getText().toString();
                goalArgs.put(DbEntry.COLUMN_NAME_GOAL, goalTitle);
                db.update(DbEntry.TABLE_GOAL, goalArgs, goalFilter, null);
            }
        });

        endlist.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
//                String goalFilter = DbEntry.COLUMN_NAME_GOAL+"='" + GoalArrayAdapter.this.prevGoalTitle+"'";
                String goalFilter = DbEntry._ID+"=" + GoalArrayAdapter.this.goals.get(position).getId();
                ContentValues goalArgs = new ContentValues();
                String deadline = endlist.getText().toString();
//                GoalArrayAdapter.this.prevGoalTitle = goalTitle;
                goalArgs.put(DbEntry.COLUMN_NAME_DEADLINE, deadline);
                db.update(DbEntry.TABLE_GOAL, goalArgs, goalFilter, null);
            }

        });

        delGoal.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                /*********************************/
                AlertDialog.Builder deleteDialog = new AlertDialog.Builder(GoalArrayAdapter.this.context);
                deleteDialog.setMessage(R.string.deleteDialog).setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //delete script
                        /*******delete from database*********/
                        db.delete(DbEntry.TABLE_GOAL, DbEntry._ID + "=" + GoalArrayAdapter.this.goals.get(position).getId(), null);
                        long id = GoalArrayAdapter.this.goals.get(position).getId();
                        String[] whereArgs = new String[]{String.valueOf(id)};
                        db.delete(DbEntry.TABLE_ITEM, DbEntry.COLUMN_NAME_GOAL + "=?", whereArgs);
                        db.delete(DbEntry.TABLE_INVEST, DbEntry.COLUMN_NAME_GOAL + "=?", whereArgs);
                        //remove from arraylist
                        GoalArrayAdapter.this.goals.remove(position);
                        GoalArrayAdapter.this.notifyDataSetChanged();
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

        return rowView;
    }
}
