package com.chandani.android.salesgoal.db;

import android.provider.BaseColumns;

/**
 * Created by chandani on 11/20/15.
 */
public class SalesDbColumn {
    public SalesDbColumn(){}

    /*database table names and feilds*/
    public static abstract class DbEntry implements BaseColumns {
        public static final String TABLE_GOAL = "goal_tbl";
        public static final String COLUMN_NAME_GOAL = "goal";
        public static final String COLUMN_NAME_GOAL_ID = "goal_id";
        public static final String COLUMN_NAME_START = "start";
        public static final String COLUMN_NAME_DEADLINE = "deadline";
        public static final String COLUMN_NAME_TOTAL_AMOUNT = "amount";
        public static final String TABLE_ITEM = "plan_tbl";
        public static final String COLUMN_NAME_ITEM = "item_name";
        public static final String COLUMN_NAME_ITEM_ID = "item_id";
        public static final String COLUMN_NAME_PRICE = "item_price";
        public static final String COLUMN_NAME_PLAN_COUNT = "plan_count";
        public static final String TABLE_INVEST = "invest_tbl";
        public static final String COLUMN_NAME_SALES_COUNT = "sales_count";
        public static final String COLUMN_NAME_INVEST_DATE = "invest_date";
    }
}
