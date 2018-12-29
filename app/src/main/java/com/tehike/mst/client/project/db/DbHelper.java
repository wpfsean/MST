package com.tehike.mst.client.project.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * 描述：数据库存储聊天记录
 * ===============================
 * @author wpfse wpfsean@126.com
 * @Create at:2018/12/29 18:01
 * @version V1.0
 */
public class DbHelper extends SQLiteOpenHelper {

    // 数据库表名
    public static final String TABLE_NAME = "chatHistory";
    // 数据库版本号
    public static final int DB_VERSION = 1;

    public static final String NAME = "name";
    public static final String AGE = "age";


    public DbHelper(Context context) {
        super(context, "tehike.db", null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String sql2 = "CREATE TABLE " + TABLE_NAME + " (" + "_id"
                + " INTEGER PRIMARY KEY AUTOINCREMENT," + "time" + " TEXT," +
                "fromUser" + " TEXT,"+ "mess" + " TEXT," + "toUser" + " TEXT)";

        sqLiteDatabase.execSQL(sql2);
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
