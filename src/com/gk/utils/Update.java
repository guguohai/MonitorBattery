package com.gk.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gk.activity.MainActivity;
import com.gk.activity.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Update {
	private int currentV = 0, newV = 0;
	private JSONObject jo_v;
	private String newVContent = "";

	private ProgressBar pb;
	private TextView tv;
	public static int loading_process;
	private Context context;

	public Update(Context context) {
		this.context = context;
	}

	public void Start() {
		loading_process = 0;
		if (isConnect(context)) {
			// TODO Auto-generated method stub
			currentV = getVerCode(context, "com.gk.activity");
			new Thread() {
				public void run() {
					String serv = context.getResources()
							.getString(R.string.update_addr).trim();
					jo_v = getJsonObject(serv + "MonitorBattery");
					if (jo_v != null && jo_v.has("version"))
						try {
							newV = jo_v.getInt("version");
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					if (newV > currentV) {
						Message msg = BroadcastHandler.obtainMessage();
						BroadcastHandler.sendMessage(msg);
					}
				}
			}.start();
		}
	}

	private Handler BroadcastHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (jo_v.has("content")) {
				JSONArray items;
				try {
					items = jo_v.getJSONArray("content");
					for (int i = 0; i < items.length(); i++) {
						JSONObject d = items.getJSONObject(i);
						newVContent = newVContent + (i + 1) + "."
								+ d.getString("text") + "\n";
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			Dialog dialog = new AlertDialog.Builder(context)
					.setTitle("新版本更新：")
					.setMessage(newVContent)
					.setPositiveButton("更新",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Beginning();
									dialog.dismiss();
								}
							})
					.setNegativeButton("取消",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									dialog.dismiss();
								}
							}).create();
			dialog.show();
		}
	};

	public void Beginning() {
		LinearLayout ll = (LinearLayout) LayoutInflater.from(context).inflate(
				R.layout.layout_loadapk, null);
		pb = (ProgressBar) ll.findViewById(R.id.down_pb);
		tv = (TextView) ll.findViewById(R.id.tv);
		Builder builder = new Builder(context);
		builder.setView(ll);
		builder.setTitle("版本更新进度");
		// builder.setNegativeButton("后台下载",
		// new DialogInterface.OnClickListener() {
		// @Override
		// public void onClick(DialogInterface dialog, int which) {
		// Intent intent=new Intent(MainActivity.this, VersionService.class);
		// startService(intent);
		// dialog.dismiss();
		// }
		// });

		builder.show();
		new Thread() {
			public void run() {
				String serv = context.getResources()
						.getString(R.string.update_addr).trim();
				loadFile(serv+"resources/" + "MonitorBattery.apk");

				System.out.println(serv+"resources/" + "MonitorBattery.apk");
			}
		}.start();
	}

	public void loadFile(String url) {
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(url);
		HttpResponse response;
		try {
			response = client.execute(get);

			HttpEntity entity = response.getEntity();
			float length = entity.getContentLength();

			InputStream is = entity.getContent();
			FileOutputStream fileOutputStream = null;
			if (is != null) {
				File file = new File(Environment.getExternalStorageDirectory(),
						"MonitorBattery.apk");
				fileOutputStream = new FileOutputStream(file);
				byte[] buf = new byte[1024];
				int ch = -1;
				float count = 0;
				while ((ch = is.read(buf)) != -1) {
					fileOutputStream.write(buf, 0, ch);
					count += ch;
					sendMsg(1, (int) (count * 100 / length));
				}
			}
			sendMsg(2, 0);
			fileOutputStream.flush();
			if (fileOutputStream != null) {
				fileOutputStream.close();
			}
		} catch (Exception e) {
			sendMsg(-1, 0);
		}
	}

	private void sendMsg(int flag, int c) {
		Message msg = new Message();
		msg.what = flag;
		msg.arg1 = c;
		handler.sendMessage(msg);
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {// 定义一个Handler，用于处理下载线程与UI间通讯
			if (!Thread.currentThread().isInterrupted()) {
				switch (msg.what) {
				case 1:
					pb.setProgress(msg.arg1);
					loading_process = msg.arg1;
					tv.setText("已加载：" + loading_process + "%");
					break;
				case 2:
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.fromFile(new File(Environment
							.getExternalStorageDirectory(), "MonitorBattery.apk")),
							"application/vnd.android.package-archive");
					context.startActivity(intent);
					break;
				case -1:
					String error = msg.getData().getString("error");
					Toast.makeText(context, error, 1).show();
					break;
				}
			}
			super.handleMessage(msg);
		}
	};

	public JSONObject getJsonObject(String Url) {
		HttpClient client = new DefaultHttpClient();
		StringBuilder sb = new StringBuilder();
		String js = null;
		JSONObject son = null;
		HttpGet myget = new HttpGet(Url);
		try {
			HttpParams params = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(params, 8000);
			HttpResponse response = client.execute(myget);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			for (String s = reader.readLine(); s != null; s = reader.readLine()) {
				sb.append(s);
			}
			js = sb.toString();
			son = new JSONObject(js);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return null;
		}
		return son;
	}

	public boolean isConnect(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo info = connectivity.getActiveNetworkInfo();
			if (info != null && info.isConnected()) {
				if (info.getState() == NetworkInfo.State.CONNECTED) {
					return true;
				}
			}
		}
		return false;
	}

	public int getVerCode(Context _context, String _package) {
		int verCode = -1;
		try {
			verCode = _context.getPackageManager().getPackageInfo(_package, 0).versionCode;
		} catch (NameNotFoundException e) {
		}
		return verCode;
	}
}
