package com.example.umengdemo;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class TestLocationActivity extends Activity implements Callback {

	private LocationManager locationManager;
	private GpsStatus gpsstatus;
	private Handler h = new Handler(this);
	private int count;
	
	@Override
	public boolean handleMessage(Message msg) {
		if(msg.what == 1000) {
			getLocation();
			count++;
			h.sendEmptyMessageDelayed(1000, 1000);
			
			return true;
		}
		
		return false;
	}
	
	/**
     * 强制帮用户打开GPS
     * @param context
     */ 
	public static final void openGPS(Context context) {
		Intent GPSIntent = new Intent();
		GPSIntent.setClassName("com.android.settings", 
				"com.android.settings.widget.SettingsAppWidgetProvider");
		GPSIntent.addCategory("android.intent.category.ALTERNATIVE");
		GPSIntent.setData(Uri.parse("custom:3"));
		try {
			PendingIntent.getBroadcast(context, 0, GPSIntent, 0).send();
		} catch (CanceledException e) {
			e.printStackTrace();
		}
	}
	
	public void turnGPSOn() {
		Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
		intent.putExtra("enabled", true);
//		sendBroadcast(intent);

		String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		if (!provider.contains("gps")) { // if gps is disabled
			final Intent poke = new Intent();
			poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
			poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
			poke.setData(Uri.parse("3"));
			
//			sendBroadcast(poke);
		}
	}
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		
//		openGPS(getApplicationContext());
		turnGPSOn();

//		// 获取到LocationManager对象
//		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//		// 增加GPS状态监听器
//		locationManager.addGpsStatusListener(gpsListener);
//
//		// 根据设置的Criteria对象，获取最符合此标准的provider对象
//		final String currentProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER).getName();
//		
//		// 根据当前provider对象获取最后一次位置信息
//		Location currentLocation = locationManager.getLastKnownLocation(currentProvider);
//		
//		// 如果位置信息为null，则请求更新位置信息
//		if (currentLocation == null) {
////			locationManager.requestLocationUpdates(currentProvider, 0, 0, locationListener);
//		}

		getLocation();
		
//		h.sendEmptyMessageDelayed(1000, 1000);
		
		
		
//		gettude(this);

		// 直到获得最后一次位置信息为止，如果未获得最后一次位置信息，则显示默认经纬度
		// 每隔10秒获取一次位置信息
//		new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//
//				while (true) {
//					Location currentLocation = locationManager.getLastKnownLocation(currentProvider);
//
//					if (currentLocation != null) {
//						Log.d("Location", "Latitude: " + currentLocation.getLatitude());
//						Log.d("Location", "location: " + currentLocation.getLongitude());
//						break;
//					} else {
//						Log.d("Location", "current location null");
//					}
//
//					try {
//						Thread.sleep(3000);
//					} catch (InterruptedException e) {
//						Log.e("Location", e.getMessage());
//					}
//				}
//			}
//		});
		// .start();

	}

	private void getLocation() {
		// 获取位置管理服务
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		// 查找到服务信息
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE); // 高精度
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_HIGH); // 低功耗

		String provider = locationManager.getBestProvider(criteria, true); // 获取GPS信息
		Location location = locationManager.getLastKnownLocation(provider); // 通过GPS获取位置
		updateToNewLocation(location);
		
		// 设置监听器，自动更新的最小时间为间隔N秒(1秒为1*1000，这样写主要为了方便)或最小位移变化超过N米
		locationManager.requestLocationUpdates(provider, 1 * 1000, 500, locationListener);
	}

	private void updateToNewLocation(Location location) {
		TextView tv1;
		tv1 = (TextView) this.findViewById(R.id.textView1);
		
		if (location != null) {
			double latitude = location.getLatitude();
			double longitude = location.getLongitude();
			tv1.setText("维度：" + latitude + "\n经度" + longitude + " " + count);
		} else {
			tv1.setText("无法获取地理信息" + " " + count);
		}

	}

	public static String gettude(Context context) {
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if (location != null) {
			double latitude = location.getLatitude(); // 经度
			double longitude = location.getLongitude(); // 纬度
			double altitude = location.getAltitude(); // 海拔

			Log.e("tag", "" + latitude + "," + longitude + "," + altitude);

			return latitude + "," + longitude;
		}

		return "0,0";
	}
	
	public static Address getAddress(Context context) {
		LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(false);
		criteria.setPowerRequirement(Criteria.POWER_HIGH);
		
		// 取得效果最好的criteria
		String provider = manager.getBestProvider(criteria, true);
		if (provider == null) return null;
		
		// 得到坐标相关的信息
		Location location = manager.getLastKnownLocation(provider);
		if (location == null) return null;

		// 更具地理环境来确定编码
		Geocoder gc = new Geocoder(context, Locale.getDefault());
		try {
			double latitude = location.getLatitude();
			double longitude = location.getLongitude();
			// 取得地址相关的一些信息\经度、纬度
			List<Address> addresses = gc.getFromLocation(latitude, longitude, 1);
			if (addresses.size() > 0) {
				for(Address a : addresses) {
					System.out.println(a);
				}
				
				return addresses.get(0);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	private GpsStatus.Listener gpsListener = new GpsStatus.Listener() {

		// GPS状态发生变化时触发
		@Override
		public void onGpsStatusChanged(int event) {
			// 获取当前状态
			gpsstatus = locationManager.getGpsStatus(null);
			switch (event) {
			// 第一次定位时的事件
			case GpsStatus.GPS_EVENT_FIRST_FIX:
				break;

			// 开始定位的事件
			case GpsStatus.GPS_EVENT_STARTED:
				break;

			// 发送GPS卫星状态事件
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				Toast.makeText(TestLocationActivity.this, "GPS_EVENT_SATELLITE_STATUS", Toast.LENGTH_SHORT).show();
				
				Iterable<GpsSatellite> allSatellites = gpsstatus.getSatellites();
				Iterator<GpsSatellite> it = allSatellites.iterator();
				int count = 0;
				while (it.hasNext()) {
					count++;
				}
				
				Toast.makeText(TestLocationActivity.this, "Satellite Count:" + count, Toast.LENGTH_SHORT).show();
				break;

			// 停止定位事件
			case GpsStatus.GPS_EVENT_STOPPED:
				Log.d("Location", "GPS_EVENT_STOPPED");
				break;
			}
		}
	};

	// 创建位置监听器
	private LocationListener locationListener = new LocationListener() {

		// 位置发生改变时调用
		@Override
		public void onLocationChanged(Location location) {
			Log.d("Location", "onLocationChanged");
		}

		// provider失效时调用
		@Override
		public void onProviderDisabled(String provider) {
			Log.d("Location", "onProviderDisabled");
		}

		// provider启用时调用
		@Override
		public void onProviderEnabled(String provider) {
			Log.d("Location", "onProviderEnabled");
		}

		// 状态改变时调用
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.d("Location", "onStatusChanged");
		}
	};
	
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
}
