package com.gk.db;

import java.util.ArrayList;
import java.util.List;

import com.gk.entity.Battery;
import com.gk.entity.Plan;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DBManager {
	private DBhelper helper;
	private SQLiteDatabase db;

	// 构造函数
	public DBManager(Context context) {
		helper = new DBhelper(context);

		db = helper.getWritableDatabase();
	}

	// 如果在一定的时间内需要重复的操作数据库，那么不要调用close()方法，
	// 关闭游标就可以了。在Activity注销或者真正不再需要的时候调用数据库的colse()方法.

	public Cursor queryTaskCursor() {
		Cursor c = db.rawQuery("SELECT * FROM Battery", null);
		return c;
	}

	public Cursor queryPlanCursor() {
		Cursor c = db.rawQuery("SELECT * FROM Plan", null);
		return c;
	}

	public void closedb() {
		db.close();
	}

	/**
	 * 新建计划
	 * 
	 * @param task
	 */
	public int insertPlan(Plan plan) {
		// db.beginTransaction();
		int pid = 0;
		// try {
		if (!existPlan(plan.getPlanName())) {
			ContentValues cv = new ContentValues();
			cv.putNull("id");
			cv.put("planName", plan.getPlanName());
			cv.put("runState", plan.getRunState());
			cv.put("createTime", plan.getCreateTime());
			cv.put("updateTime", plan.getUpdateTime());

			db.insert("Plan", null, cv);

			Cursor c = db
					.rawQuery("SELECT last_insert_rowid() from Plan", null);
			if (c.moveToFirst()) {
				pid = c.getInt(0);
			}
			c.close();

		} else {
			pid = -1;
		}
		return pid;
	}

	/**
	 * 新建任务集
	 * 
	 * @param task
	 * @param planname
	 */
	public void insertBattery(Battery battery, int pid) {

		ContentValues cv = new ContentValues();
		cv.putNull("id");
		cv.put("plan_id", pid);
		cv.put("rate", battery.getRate());
		cv.put("value", battery.getValue());
		cv.put("createTime", battery.getCreateTime());

		db.insert("Battery", null, cv);

		db.close();

	}
	
	/**
	 * 查找计划是否存在
	 * 
	 * @param pname
	 * @return
	 */
	public boolean existPlan(String pname) {
		boolean exist = false;
		Cursor c = db.rawQuery("SELECT * FROM Plan where planName=?",
				new String[] { pname });
		if (c.moveToFirst()) {
			exist = true;
		}
		c.close();
		return exist;
	}
	
	
	/**
	 * 查找所有plan
	 * 
	 * @return
	 */
	public List<Plan> queryAllPlan() {
		List<Plan> planlist = new ArrayList<Plan>();
		Cursor c = db.rawQuery("SELECT * from Plan", null);
		while (c.moveToNext()) {
		
			Plan plan = new Plan();
			plan.setId(c.getInt(c.getColumnIndex("id")));
			plan.setPlanName(c.getString(c.getColumnIndex("planName")));
			plan.setRunState(c.getInt(c.getColumnIndex("runState")));
			plan.setCreateTime(c.getString(c.getColumnIndex("createTime")));
			plan.setUpdateTime(c.getString(c.getColumnIndex("updateTime")));
			planlist.add(plan);
		}
		c.close();
		return planlist;

	}
	
	/**
	 * 查找所有Task
	 * 
	 * @return
	 */
	public List<Battery> queryResultById(int pid) {
		List<Battery> batterylist = new ArrayList<Battery>();
		Cursor c = db.rawQuery("SELECT * from Battery where plan_id=?", new String[] { String.valueOf(pid) });
		while (c.moveToNext()) {
			Battery battery = new Battery();
			battery.setId(c.getInt(c.getColumnIndex("id")));
			battery.setPlanid(c.getInt(c.getColumnIndex("plan_id")));
			battery.setRate(c.getInt(c.getColumnIndex("rate")));
			battery.setValue(c.getInt(c.getColumnIndex("value")));
			battery.setCreateTime(c.getString(c.getColumnIndex("createTime")));
			batterylist.add(battery);
		}
		c.close();
		return batterylist;
	}
	
	public boolean delPlanAndTask(int planid){
		String whereTask =  "plan_id=?";
		String wherePlan =  "id=?";
		String[] whereArgs = new String[] {String.valueOf(planid)};  
		boolean deltask=true;
		try{
			db.delete("Battery", whereTask, whereArgs);
		}catch (SQLException e) {
			deltask=false;
		}
		
		if(deltask){
			try{
				db.delete("Plan", wherePlan, whereArgs);
			}catch (SQLException e) {
				return false;
			}
		}

		return true;
	}
	


}
