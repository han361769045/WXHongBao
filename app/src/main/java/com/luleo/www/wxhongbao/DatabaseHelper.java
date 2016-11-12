package com.luleo.www.wxhongbao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;

/**
 * Created by Leo on 2015/3/9.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    // 数据库名称
    private static final String DATABASE_NAME = "Member.db";

    // 数据库version
    private static final int DATABASE_VERSION = 1;


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // 可以用配置文件来生成 数据表，有点繁琐，不喜欢用
        // super(context, DATABASE_NAME, null, DATABASE_VERSION,
        // R.raw.ormlite_config);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {

    }
}
