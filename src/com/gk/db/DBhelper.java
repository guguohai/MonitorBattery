package com.gk.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBhelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "gktest.db";
	private static final int DATABASE_VERSION = 1;

	public DBhelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
	
		db.execSQL("CREATE TABLE IF NOT EXISTS Plan(" +
				"id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"planName VARCHAR NOT NULL," +
				"runState INTEGER NOT NULL," +
				"createTime DATETIME," +
				"updateTime DATETIME)");

		// 任务运行结果		
		db.execSQL("CREATE TABLE IF NOT EXISTS Battery(" +
				"id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"plan_id INTEGER NOT NULL," +
				"rate INTEGER," +
				"value INTEGER," +
				"createTime DATETIME)");
				//"FOREIGN KEY(task_id) REFERENCES Task(id)," +
				//"FOREIGN KEY(task_name) REFERENCES Task(taskName))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("ALTER TABLE Task ADD COLUMN other STRING");
		
	}

}
