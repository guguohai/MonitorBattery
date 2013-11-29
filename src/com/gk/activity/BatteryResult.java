package com.gk.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import com.gk.activity.R;
import com.gk.adapter.BatteryAdapter;
import com.gk.db.DBManager;
import com.gk.entity.Battery;
import com.gk.entity.Plan;
import com.gk.entity.PlanType;

public class BatteryResult extends Activity {
	private List<Battery> batterylist;
	private ListView listv;
	private Spinner spinnerType;
	private Button btnDel;
	private ArrayAdapter<PlanType> planTypeAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.result);

		initView();
	}

	private void initView() {
		Intent intent=getIntent();
		int pid=intent.getIntExtra("pid", 0);
		DBManager dbm=new DBManager(this);
		
		//spinnerType = (Spinner) findViewById(R.id.spinner_type);
		listv = (ListView) findViewById(R.id.lv_battery);

		BatteryAdapter adapter = new BatteryAdapter(this, dbm.queryResultById(pid));
		listv.setAdapter(adapter);
		//btnDel = (Button) findViewById(R.id.btn_del);

//		planTypeAdapter = new ArrayAdapter<PlanType>(this,
//				android.R.layout.simple_spinner_item, getPlanTypes());
//
//		// 设置下拉列表的风格
//		planTypeAdapter
//				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//		spinnerType.setAdapter(planTypeAdapter);
//
//		spinnerType.setOnItemSelectedListener(new SpinnerSelectedListener());
//		btnDel.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				PlanType p = (PlanType) spinnerType.getSelectedItem();
//				DBManager dbm=new DBManager(BatteryResult.this);
//				dbm.delPlanAndTask(p.getId());
//			}
//		});

//		setListViewAdapter();
	}

	private void setListViewAdapter() {
		PlanType p = (PlanType) spinnerType.getSelectedItem();
		batterylist = getBatterys(p.getId());

		BatteryAdapter adapter = new BatteryAdapter(this, batterylist);
		listv.setAdapter(adapter);
	}

	private List<PlanType> getPlanTypes() {
		DBManager dbm = new DBManager(this);
		List<Plan> planlist = dbm.queryAllPlan();

		List<PlanType> ptypelist = new ArrayList<PlanType>();

		for (int n = 0; n < planlist.size(); n++) {
			PlanType pt = new PlanType(planlist.get(n).getId(), planlist.get(n)
					.getPlanName());
			ptypelist.add(pt);
		}

		return ptypelist;
	}

	private List<Battery> getBatterys(int pid) {
		DBManager dbm = new DBManager(this);
		return dbm.queryResultById(pid);
	}

	private class SpinnerSelectedListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			PlanType p = (PlanType) spinnerType.getSelectedItem();

			setListViewAdapter();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub

		}
	}
}
