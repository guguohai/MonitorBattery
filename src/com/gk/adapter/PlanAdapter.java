package com.gk.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.gk.db.DBManager;
import com.gk.entity.Battery;
import com.gk.entity.Plan;
import com.gk.activity.R;

import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * 实例化Adapter
 * 
 * @author guohai@live.com
 * 
 */
public class PlanAdapter extends BaseAdapter {
	private Context context;
	private List<Plan> planlist;

	public HashMap<Integer, Boolean> state = new HashMap<Integer, Boolean>();

	public PlanAdapter(Context context, List<Plan> planlist) {
		this.context = context;
		this.planlist = planlist;
	}
	
	public void setItemList(List<Plan> list) {
		planlist = list;
		//new myAsyncTask().execute(null);
    }

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return planlist.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return planlist.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	// 重写View
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		LayoutInflater mInflater = LayoutInflater.from(context);
		convertView = mInflater.inflate(R.layout.list_item2, null);

		TextView txtValue = (TextView) convertView
				.findViewById(R.id.txt_value);
		TextView txtTime = (TextView) convertView.findViewById(R.id.txt_time);

		txtValue.setText(String.valueOf(planlist.get(position).getPlanName()));
		txtTime.setText(String.valueOf(planlist.get(position).getCreateTime()));
		
		CheckBox check = (CheckBox) convertView.findViewById(R.id.cb_plan);
		check.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					state.put(position, isChecked);
				} else {
					state.remove(position);
				}
			}
		});
		check.setChecked((state.get(position) == null ? false : true));

		return convertView;
	}
	
}