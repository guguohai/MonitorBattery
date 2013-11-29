package com.gk.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.gk.adapter.PlanAdapter;
import com.gk.db.DBManager;
import com.gk.entity.Battery;
import com.gk.entity.Plan;
import com.gk.activity.R;

import android.os.AsyncTask;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private BroadcastReceiver BatteryReceiver = null;
	private Button BtnStart, BtnStop, btnClean;
	private ListView lvPlans;
	private TextView startTips;
	private EditText etRate;
	private int planid;
	private EditText etPlanName;
	private int changeNum = 0;
	private SharedPreferences sp;
	private Editor editor;
	private boolean isRunning=false;

	private PlanAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		initView();
	}

	private void initView() {
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		sp = getSharedPreferences("battery", Context.MODE_PRIVATE);
		editor = sp.edit();
		
		btnClean = (Button) findViewById(R.id.btn_clean);

		lvPlans = (ListView) findViewById(R.id.lv_plans);

		adapter = new PlanAdapter(this, getPlanList());
		lvPlans.setAdapter(adapter);

		etRate = (EditText) findViewById(R.id.et_batteryVal);

		BtnStart = (Button) findViewById(R.id.btn_start);
		BtnStop = (Button) findViewById(R.id.btn_stop);
		startTips = (TextView) findViewById(R.id.battery_tips);

		BtnStart.setOnClickListener(new startBattery());
		BtnStop.setOnClickListener(new stopTask());
		BtnStop.setEnabled(false);

		lvPlans.setOnItemClickListener(new listPlan());
		btnClean.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(isRunning){
					Toast.makeText(MainActivity.this, "正在测试中,不能删除！",5000).show();
					return;
				}
				DBManager dbm = new DBManager(MainActivity.this);
				HashMap<Integer, Boolean> cbState = adapter.state;
				for (int j = 0; j < adapter.getCount(); j++) {
					if (cbState.get(j) != null) {
						Plan plan = (Plan) adapter.getItem(j);
						dbm.delPlanAndTask(plan.getId());
					}
				}
				dbm.closedb();

				adapter.setItemList(getPlanList());
				adapter.notifyDataSetChanged();
			}
		});

	}

	private List<Plan> getPlanList() {
		DBManager dbm = new DBManager(this);
		List<Plan> plist = dbm.queryAllPlan();
		dbm.closedb();
		return plist;
	}

	private class listPlan implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			Plan p = (Plan) arg0.getItemAtPosition(arg2);
			Intent intent = new Intent(MainActivity.this, BatteryResult.class);

			intent.putExtra("pid", p.getId());
			startActivity(intent);
		}
	}

	private class stopTask implements OnClickListener {
		@Override
		public void onClick(View v) {
			isRunning=false;
			if (BatteryReceiver != null) {
				unregisterReceiver(BatteryReceiver);
			}

			editor.remove("value");
			editor.commit();

			BtnStart.setEnabled(true);
			BtnStop.setEnabled(false);
			// batterylist = null;

			startTips.setText("测试结束！");
		}
	}

	@Override
	protected void onDestroy() {
		editor.remove("value");
		editor.commit();
		isRunning=false;
		super.onDestroy();
	}

	class startBattery implements OnClickListener {
		@Override
		public void onClick(View v) {
			LayoutInflater factory = LayoutInflater.from(MainActivity.this);
			View textEntryView = factory.inflate(R.layout.plan_dialog, null);
			etPlanName = (EditText) textEntryView
					.findViewById(R.id.et_planname);
			UUID uuid = UUID.randomUUID();
			CharSequence cs = "Battery_" + uuid.toString().split("-")[0];
			etPlanName.setHint(cs);

			AlertDialog.Builder ad1 = new AlertDialog.Builder(MainActivity.this);
			ad1.setTitle("输入本次测试名称");
			ad1.setIcon(android.R.drawable.ic_dialog_info);
			ad1.setView(textEntryView);

			ad1.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int i) {
					String pname = "";
					if (etPlanName.getText().toString().equals("")) {
						pname = etPlanName.getHint().toString();
					} else {
						pname = etPlanName.getText().toString();
					}
					// receiverAction(pname);
					registerBattery(pname);

				}
			});
			ad1.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int i) {
				}
			});
			ad1.show();
		}
	}

	private void registerBattery(String sname) {

		editor.putInt("value", 0);
		editor.commit();

		BtnStart.setEnabled(false);
		BtnStop.setEnabled(true);

		Plan plan = new Plan();
		plan.setPlanName(sname);
		plan.setRunState(1);
		plan.setCreateTime(getCurrentTime());

		DBManager dbm = new DBManager(MainActivity.this);
		planid = dbm.insertPlan(plan);
		adapter.setItemList(dbm.queryAllPlan());
		adapter.notifyDataSetChanged();
		dbm.closedb();

		if (etRate.getText().toString().equals("")) {
			String rateStr = etRate.getHint().toString();
			changeNum = Integer.parseInt(rateStr);
		} else {
			String rateStr = etRate.getText().toString();
			changeNum = Integer.parseInt(rateStr);
		}

		startTips.setText("开始测试！");
		BatteryReceiver = new BatteryReceiver();
		registerReceiver(BatteryReceiver, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));
	}

	private boolean diffBattery(int b) {
		int val = sp.getInt("value", 0);

		// 先提取上一次的电量，再写入本次的电量
		editor.putInt("value", b);
		editor.commit();

		if ((val - b) >= changeNum) {
			return true;
		}
		return false;
	}

	private class BatteryReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			isRunning=true;
			
			int currentBattery = intent.getExtras().getInt("level");// 获得当前电量
			int total = intent.getExtras().getInt("scale");// 获得总电量
			int percent = currentBattery * 100 / total;
			startTips.setText("开始测试！(目前电量：" + percent + ")");
			DBManager dbm = new DBManager(MainActivity.this);

			Battery battery = new Battery();
			battery.setPlanid(planid);
			battery.setRate(changeNum);
			battery.setCreateTime(getCurrentTime());
			battery.setValue(percent);

			if (sp.getInt("value", 0) == 0) {
				dbm.insertBattery(battery, planid);
			}

			if (diffBattery(percent)) {
				dbm.insertBattery(battery, planid);
			}

		}
	}

	@SuppressLint("SimpleDateFormat")
	public static String getCurrentTime() {
		String startdate = "";
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		startdate = formatter.format(curDate);
		return startdate;
	}

	@Override
	public void onBackPressed() {
		exitDialog();
	}

	private void exitDialog() {
		AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
		dlg.setIcon(android.R.drawable.ic_dialog_info);
		dlg.setTitle("退出提示");
		String nameStr = getResources().getString(R.string.app_name);
		dlg.setMessage("确定要退出" + nameStr + "吗？");

		dlg.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				MainActivity.this.finish();
			}
		});
		dlg.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub

			}
		});
		dlg.create().show();
	}

}
