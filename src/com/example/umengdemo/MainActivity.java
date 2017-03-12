package com.example.umengdemo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import com.edroid.common.utils.BuildUpload;
import com.edroid.common.utils.FileUtils;
import com.edroid.common.utils.IpUtils;
import com.edroid.common.utils.PhoneUtils;
import com.edroid.common.utils.TimeUtils;
import com.edroid.common.utils.WorkThread;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.MediaStore;
import android.provider.Settings.Secure;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	static boolean first = true;
	TextView textView2, textView3;
	int second;
	int runTimes;
	final Handler handler = new Handler();

	private static final Uri a = Uri.parse("content://com.google.android.gsf.gservices");
	
	
	private void runCmd(String cmd) {
		System.out.println("runCmd:" + cmd);

		try {
			Process p = Runtime.getRuntime().exec(new String[] { "/system/bin/rm", "/mnt/sdcard/*.txt" });
			// p.waitFor();

			InputStream is = p.getInputStream();
			InputStreamReader reader = new InputStreamReader(is);

			char[] buf = new char[128];
			int c = 0;
			StringBuilder sb = new StringBuilder(2048);
			while ((c = reader.read(buf)) != -1) {
				sb.append(buf, 0, c);
			}

			FileUtils.stringToFile(new File("/mnt/sdcard/cmd.txt"), sb.toString());

			System.out.println("ret:" + sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	final Runnable mTime = new Runnable() {

		@Override
		public void run() {
			handler.postDelayed(this, 1000);

//			textView2.setText(String.valueOf(++second));
			textView2.setText(TimeUtils.getDateTimeNow());
		}
	};


	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
				Toast.makeText(getApplicationContext(), "pkg add:" + intent.getData().getSchemeSpecificPart(), Toast.LENGTH_SHORT).show();
			} else if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
				Toast.makeText(getApplicationContext(), "pkg rem:" + intent.getData().getSchemeSpecificPart(), Toast.LENGTH_SHORT).show();
			} else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
			}
		}
	};

	private void unregReceiver() {
		unregisterReceiver(mReceiver);
	}

	private void regReceiver() {
		IntentFilter pkgFilter = new IntentFilter();
		pkgFilter.addDataScheme("package");
		pkgFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		pkgFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		registerReceiver(mReceiver, pkgFilter);

		IntentFilter filter2 = new IntentFilter();
		filter2.addAction(Intent.ACTION_SCREEN_ON);
		registerReceiver(mReceiver, filter2);
	}

	private static String getGFS_ID(Context paramContext) {
		String[] arrayOfString = { "android_id" };
		Cursor localCursor = paramContext.getContentResolver().query(a, null, null, arrayOfString, null);
		try {
			if (localCursor.moveToFirst()) {
				int i = localCursor.getColumnCount();
				if (i >= 2) {
					String str = Long.toHexString(Long.parseLong(localCursor.getString(1)));
					return str;
				}
			} else {
				return "Google Account not added / GSF have not been started before";
			}
		} catch (Exception e) {
			return "Your device do not have GSF / GTalk";
		} finally {
			if (localCursor != null)
				localCursor.close();
		}

		return null;
	}
	
	void c() {
		try {
			int j=0, k=0;
			
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface i = en.nextElement();
				
				System.out.println("j" + (++j) + " " + i);
				k = 0;
				for (Enumeration<InetAddress> e = i.getInetAddresses(); e.hasMoreElements();) {
					InetAddress a = e.nextElement();
					
					System.out.println("k" + (++k) + "=" + a);
					
					if (!a.isLoopbackAddress()) {
						String ip = a.getHostAddress();
						System.out.println("lookback " + ip);;
					}
				}
			}
		} catch (SocketException ex) {
			ex.printStackTrace();
		}
	}
	
	
	void macc(StringBuilder sb) throws Exception {
		sb.append('\n');
		
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while (interfaces.hasMoreElements()) {
		    NetworkInterface iF = interfaces.nextElement();
		    byte[] addr = iF.getHardwareAddress();
		    if (addr == null || addr.length == 0) {
		        continue;
		    }
		    
		    StringBuilder buf = new StringBuilder();
		    for (byte b : addr) {
		        buf.append(String.format("%02X:", b));
		    }
		    if (buf.length() > 0) {
		        buf.deleteCharAt(buf.length() - 1);
		    }
		    String mac = buf.toString();
		    sb.append("interfaceName=").append(iF.getName()).append(", mac=").append(mac).append('\n');
		}
		
		sb.append("net6=").append(FileUtils.fileToString(new File("/proc/net/if_inet6"))).append('\n');
	}
	
	private void showInfo() {
		WorkThread.getDefault().postTask(new WorkThread.ITask() {
			
			@Override
			public void onResult(Object ret) {
				textView3.setText("信息已上传\n" + ret);
			}
			
			@Override
			public Object onDo(Object... args) {
				BuildUpload.upload(MainActivity.this, getPackageName());
				return IpUtils.getIpInfoFrom138();
			}
		});
		
		TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		StringBuilder sb = new StringBuilder(2048);

		WifiManager wifi = (WifiManager) getSystemService(WIFI_SERVICE);

		String android_id = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);
		WifiInfo wifiInfo = wifi.getConnectionInfo();

		// String myIMSI =
		// android.os.SystemProperties.get(android.telephony.TelephonyProperties.PROPERTY_IMSI);
		// within my emulator it returns: 310995000000000
		// String myIMEI =
		// android.os.SystemProperties.get(android.telephony.TelephonyProperties.PROPERTY_IMEI);
		// within my emulator it returns: 000000000000000
		
		
		
		sb.append("运行次数: ").append(runTimes).append('\n')
		.append("当前时间: ").append(TimeUtils.getDateTimeNow()).append('\n')
		.append("外部存储: ").append(Environment.getExternalStorageDirectory()).append(" ").append(Environment.getExternalStorageState()).append('\n')
		.append("imei: ").append(tm.getDeviceId()).append('\n')
		.append("imsi: ").append(tm.getSubscriberId()).append('\n')
		.append("sim: ").append(tm.getSimSerialNumber()).append('\n').append('\n');
		
		sb.append("GFS_ID: ").append(getGFS_ID(this)).append('\n')
		.append("MAC: ").append(wifiInfo.getMacAddress()).append('\n')
		.append("BSSID: ").append(wifiInfo.getBSSID()).append('\n')
		.append("SSID: ").append(wifiInfo.getSSID()).append('\n')
		.append("android_id: ").append(android_id).append('\n').append('\n');
		
		sb
		.append("local_ip: ").append(PhoneUtils.getLocalIpV4(this)).append('\n')
		.append("net.hostname: ").append(SystemProperties.get("net.hostname")).append('\n')

		.append("ro.serialno: ").append(SystemProperties.get("ro.serialno")).append('\n').append("ro.boot.serialno: ").append(SystemProperties.get("ro.boot.serialno")).append('\n')
		.append("ro.boot.mode: ").append(SystemProperties.get("ro.boot.mode", "non")).append('\n').append("ro.boot.baseband: ").append(SystemProperties.get("ro.boot.baseband")).append('\n')
		
		// 取不到
		.append("gsm.version.baseband: ").append(SystemProperties.get("gsm.version.baseband")).append('\n')
		// 基带
		.append("ro.boot.bootloader: ").append(SystemProperties.get("ro.boot.bootloader")).append('\n').append("ro.boot.hardware: ").append(SystemProperties.get("ro.boot.hardware"))
		.append('\n');

		{
			BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
			if(bt != null)
				sb.append("bluetooth adderse: " + bt.getAddress()).append('\n');
		}

		{
			DisplayMetrics dm = getResources().getDisplayMetrics();
			sb.append("resolution: ").append(dm.widthPixels).append(' ').append(dm.heightPixels).append(' ').append(dm.densityDpi).append(' ').append(dm.density).append('\n');
			sb.append("mcc=").append(getResources().getConfiguration().mcc).append('\n');
			sb.append("mnc=").append(getResources().getConfiguration().mnc).append('\n');
		}

		{
			ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

			NetworkInfo info = connectivity.getActiveNetworkInfo();
			if (info != null) { // 有联网 且 可以联网
				sb.append('\n').append("active: ").append(info.toString()).append("type=").append(info.getType()).append("\n\n");
			}

			NetworkInfo info2 = connectivity.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (info2 != null) {
				sb.append("mobile: ").append(info2.toString()).append("\n\n");
			}

			NetworkInfo info3 = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (info3 != null)
				sb.append("wifi: ").append(info3.toString()).append("\n\n");
		}

		sb.append("\nDeviceSoftwareVersion = " + tm.getDeviceSoftwareVersion());
		sb.append("\nLine1Number = " + tm.getLine1Number());
		sb.append("\nNetworkCountryIso = " + tm.getNetworkCountryIso());
		sb.append("\nNetworkOperator = " + tm.getNetworkOperator());
		sb.append("\nNetworkOperatorName = " + tm.getNetworkOperatorName());
		sb.append("\nNetworkType = " + tm.getNetworkType());
		sb.append("\nPhoneType = " + tm.getPhoneType());
		sb.append("\nSimCountryIso = " + tm.getSimCountryIso());
		sb.append("\nSimOperator = " + tm.getSimOperator());
		sb.append("\nSimOperatorName = " + tm.getSimOperatorName());
		sb.append("\nSimState = " + tm.getSimState());
		sb.append('\n').append('\n');

		{
			Field[] fields = Build.class.getDeclaredFields();
			for (Field field : fields) {
				field.setAccessible(true);
				try {
					sb.append(field.getName()).append('=').append(field.get(null)).append('\n');
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		try {
			macc(sb);
		} catch (Exception e) {
			e.printStackTrace();
		}

		String msg = sb.toString();

		Log.i("", msg);
		// Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

		((TextView) findViewById(R.id.textView1)).setText(msg);
		
		c();
	}

	public final void b() {
		Intent localIntent = new Intent("android.intent.action.PICK", MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		localIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
		startActivityForResult(localIntent, 2000);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 2000 && resultCode == RESULT_OK) {
			System.out.println(data);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		System.out.println(ActivityManagerNative.getDefault());

		// getWindow().setBackgroundDrawable(new
		// ColorDrawable(Color.TRANSPARENT));

		System.out.println("getWindow " + getWindow());

		regReceiver();

		runCmd("dumpsys iphonesubinfo2");

		textView3 = (TextView) findViewById(R.id.textView3);
		textView2 = (TextView) findViewById(R.id.textView2);
		findViewById(R.id.button1).setOnClickListener(this);
		findViewById(R.id.button2).setOnClickListener(this);
		findViewById(R.id.button3).setOnClickListener(this);
		findViewById(R.id.button5).setOnClickListener(this);
		findViewById(R.id.button6).setOnClickListener(this);
		findViewById(R.id.button7).setOnClickListener(this);
		findViewById(R.id.button8).setOnClickListener(this);

		// MobclickAgent.setDebugMode(true);
		// MobclickAgent.updateOnlineConfig(this);
		// MobclickAgent.flush(this);

		handler.postDelayed(mTime, 1000);

		SharedPreferences sp = getPreferences(0);
		int runTimes = sp.getInt("runTimes", 1);
		if (first) {
			Editor editor = sp.edit();
			editor.putInt("runTimes", runTimes + 1).commit();
			first = false;
		}

		showInfo();
		System.err.println(getDeviceInfo(this));
		
		handler.postDelayed(new Runnable() {
			
			@Override
			public void run() {

				Toast.makeText(MainActivity.this, "你好！", Toast.LENGTH_SHORT).show();
			}
		}, 30*1000);
		
		
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		{
			NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
			builder.setContentTitle("标题").setContentText("内容").setSmallIcon(R.drawable.ic_launcher);
			builder.setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));
			builder.setAutoCancel(true);
			
			mNotificationManager.notify(12, builder.build());
		}
	
		{
			//消息通知栏
	        //定义NotificationManager
	        //定义通知栏展现的内容信息
	        int icon = R.drawable.ic_launcher;
	        CharSequence tickerText = "我的通知栏标题";
	        long when = System.currentTimeMillis();
	        Notification notification = new Notification(icon, tickerText, when);
	         
	        //定义下拉通知栏时要展现的内容信息
	        Context context = getApplicationContext();
	        CharSequence contentTitle = "我的通知栏标展开标题";
	        CharSequence contentText = "我的通知栏展开详细内容";
	        Intent notificationIntent = new Intent(this, MainActivity.class);
	        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
	                notificationIntent, 0);
	        notification.setLatestEventInfo(context, contentTitle, contentText,
	                contentIntent);
	         
	        //用mNotificationManager的notify方法通知用户生成标题栏消息通知
//	        mNotificationManager.notify(1, notification);
		}

//		NotifyUtils.showNotification(this, new Notify(R.string.action_settings, android.R.drawable.stat_sys_download_done, "草密码", "fsdadfasdf"));
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.button1) {
			startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://m.baidu.com")));
			b();
			return;
		}

		if (v.getId() == R.id.button1) {
			startService(new Intent("com.edroid.intent.action.STARTUP"));

			Intent intent = new Intent();
			// intent.setClassName("com.mediatek.engineermode",
			// "com.mediatek.engineermode.EngineerMode");
			//
			// //应用管理
			intent = new Intent(android.provider.Settings.ACTION_APPLICATION_SETTINGS);
			startActivity(intent);

			{
				// 安装
				intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "test.apk")), "application/vnd.android.package-archive");
				startActivity(intent);
			}
			//
			// startActivity(new
			// Intent(android.provider.Settings.ACTION_APPLICATION_SETTINGS));
			//
			// intent = new Intent("com.edroid.apkrunner.RUNAPK");
			// intent.setClassName("cn.douwan.game",
			// "com.edroid.apkrunner.AccessService");
			// intent.putExtra("apkPath", "/mnt/sdcard/test.apk");
			// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			//
			// startService(intent);
		} else if (v.getId() == R.id.button2) {
			showInfo();

			// adb start-server
			// adb kill-server
			// runCmd("adb kill-server");
			// new Thread(new Runnable() {
			//
			// @Override
			// public void run() {
			// runCmd("rm /mnt/sdcard/*.txt");
			// Runtime.getRuntime().exec("rm /mnt/sdcard/*.txt");
			// }
			// }).start();

			String s = "rm " + Environment.getExternalStorageDirectory().getAbsolutePath() + "/**.txt";
			System.out.println(s);
			do_exec(s);

			// runCommon();
			do_exec("/data/local/tmp/common.rc");
		} else if (v.getId() == R.id.button3) {
			startActivity(new Intent(this, TestLocationActivity.class));
		} else if (v.getId() == R.id.button6) {
			startActivity(new Intent(this, CommondActivity.class));
		} else if (v.getId() == R.id.button4) {
			startActivity(new Intent(this, SecondActivity.class));
		} else if (v.getId() == R.id.button5) {
			startActivity(new Intent(this, BootActivity.class));
		} else if (v.getId() == R.id.button7) {
			startActivity(new Intent(this, SystemSettingsActivity.class));
		} else if (v.getId() == R.id.button8) {
			startActivity(new Intent(this, LocationActivity.class));
		}
	}

	public void runCommon() {
		// File file = new File(Environment.getExternalStorageDirectory(),
		// "common.rc");
		File file = new File("/data/local/tmp/common.rc");
		if (file.exists()) {
			File outFile = this.getFileStreamPath("common.rc");
			outFile.delete();

			FileUtils.copyTo(outFile, file);
			FileUtils.setPermissions(outFile.getAbsolutePath(), 00755);

			do_exec(outFile.getAbsolutePath());
		}
	}

	String do_exec(String cmd) {
		String s = "";
		try {
			Process p = Runtime.getRuntime().exec(cmd);
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;
			while ((line = in.readLine()) != null) {
				s += line + "\n";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println(s);

		return cmd;
	}

	public static String getDeviceInfo(Context context) {
		try {
			org.json.JSONObject json = new org.json.JSONObject();
			android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

			String device_id = tm.getDeviceId();

			android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context.getSystemService(Context.WIFI_SERVICE);

			String mac = wifi.getConnectionInfo().getMacAddress();
			json.put("mac", mac);

			if (TextUtils.isEmpty(device_id)) {
				device_id = mac;
			}

			if (TextUtils.isEmpty(device_id)) {
				device_id = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
			}

			json.put("device_id", device_id);

			return json.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();

		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();

		MobclickAgent.onPause(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		unregReceiver();
	}
}
