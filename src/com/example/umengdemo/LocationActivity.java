package com.example.umengdemo;

import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.List;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.edroid.common.utils.SdkThread;

import android.app.Activity;
import android.content.Context;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.GpsStatus.NmeaListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class LocationActivity extends Activity implements OnClickListener, Callback {
	LocationManager lm;
	TelephonyManager tm;
	
	GpsStatus gpsstatus;
	TextView mTextView, mTextView2, mTextView3, mTextView4, mTextView5;
	String mMockProviderName = LocationManager.NETWORK_PROVIDER;
	Handler h;
	int index, index2;
	FileWriter fw;
	
	
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case 1: {
			Location l = (Location) msg.obj;
			if(l == null)
				break;
			
			String s = String.format("index=%d\nlat=%f, lng=%f, acc=%f, bear=%f, speed=%f km/h, alt=%f m", 
					index++, l.getLatitude(), l.getLongitude(), l.getAccuracy(), l.getBearing(),
					l.getSpeed() * 3.6, l.getAltitude());
			mTextView.setText(s);
			
			log(s);
			
			CellLocation cl = tm.getCellLocation();
			
			if(cl instanceof GsmCellLocation) {
				GsmCellLocation gcl = (GsmCellLocation) cl;
				String s1 = String.format("gsm cid=%d, lac=%d, psc=%d", 
						gcl.getCid(), gcl.getLac(), gcl.getPsc());
				mTextView3.setText(s1);
				log(s1);
			}
			else if(cl instanceof CdmaCellLocation) {
				CdmaCellLocation ccl = (CdmaCellLocation) cl;
				
				mTextView3.setText(String.format("cdma id=%d, lat=%d, lng=%d", 
						ccl.getBaseStationId(), ccl.getBaseStationLatitude(), ccl.getBaseStationLongitude()));
			}
			
//			Wifi
			
			break;
		}
		
		case 2: {
			BDLocation l = (BDLocation) msg.obj;
			mTextView2.setText("index=" + index2++ + "\n" + bdLocation2s(l));
			break;
		}
		
		case 3: {
			break;
		}

		case 4: {
			switch (msg.arg1) {
			// 第一次定位时的事件
			case GpsStatus.GPS_EVENT_FIRST_FIX:
				mTextView4.setText("首次定位");
				break;
				
			// 开始定位的事件
			case GpsStatus.GPS_EVENT_STARTED:
				mTextView4.setText("定位开始");
				break;
			
			// 发送GPS卫星状态事件
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS: {
				GpsStatus status = lm.getGpsStatus(null); //取当前状态 
				
				int max = status.getMaxSatellites();
				int count = 0;
				
				log("gps -------------------");
				
				Iterator<GpsSatellite> it = status.getSatellites().iterator();
				while(it.hasNext()) {
					GpsSatellite gps = it.next();
					count++;
					
					log(gps.getAzimuth() + "," + gps.getElevation() 
						+ "," + gps.getPrn() + "," + gps.getSnr() 
						+ "," + gps.hasAlmanac() + "," + gps.hasEphemeris() + "," + gps.usedInFix());
				}
				
				mTextView4.setText("卫星个数：" + count);  
				break;
			}
			
			// 停止定位事件
			case GpsStatus.GPS_EVENT_STOPPED:
				mTextView4.setText("定位结束");
				break;
			}
			
			break;
		}
		
		case 5: {
			log("" + msg.obj);
			mTextView5.setText("Nmea = " + msg.obj);
			break;
		}

		default:
			break;
		}
		
		return false;
	}
	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.button1) {
			SdkThread.getDefault().postEx(new Runnable() {
				
				@Override
				public void run() {
				}
			}, new Runnable() {
				
				@Override
				public void run() {
					
				}
			});
		}
		
		if(v.getId() == R.id.button2) {
			startBaidu();
		}

		if(v.getId() == R.id.btnMockLocation) {
			setMockLocation(34.3334860000, 108.7144800000);
		}
	}
	
	void log(String s) {
		if(fw != null) {
			try {
				fw.write(s);
				fw.write("\r\n");
				fw.flush();
			} catch (Exception e) {
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location);
		
		h = new Handler(this);
		try {
			fw = new FileWriter(new File(Environment.getExternalStorageDirectory(), "gps.log"), true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		mTextView = (TextView) findViewById(R.id.textView1);
		mTextView2 = (TextView) findViewById(R.id.textView2);
		mTextView3 = (TextView) findViewById(R.id.textView3);
		mTextView4 = (TextView) findViewById(R.id.textView4);
		mTextView5 = (TextView) findViewById(R.id.textView5);
		
		findViewById(R.id.button1).setOnClickListener(this);
		findViewById(R.id.button2).setOnClickListener(this);
		findViewById(R.id.button3).setOnClickListener(this);
		findViewById(R.id.btnMockLocation).setOnClickListener(this);

		lm = (LocationManager) getSystemService(LOCATION_SERVICE);
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60, 1, gpsLocationListener);
		lm.addGpsStatusListener(gpsListener);
		lm.addNmeaListener(nmeaListener);
		
		tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		
		startBaidu();
		
//		initMockLocation();
		
		Log.i("gps", "getAllProviders----------");
		List<String> all = lm.getAllProviders();
		if(all != null) {
			for(String s : all)
				Log.i("gps", "Provider " + s);
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		lm.removeUpdates(gpsLocationListener);
		lm.removeGpsStatusListener(gpsListener);
		lm.removeNmeaListener(nmeaListener);
		
		try {
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	final NmeaListener nmeaListener = new NmeaListener() {
		
		@Override
		public void onNmeaReceived(long timestamp, String nmea) {
			h.obtainMessage(5, 0, 0, nmea).sendToTarget();
		}
	};

	private GpsStatus.Listener gpsListener = new GpsStatus.Listener() {
		
		@Override
		public void onGpsStatusChanged(int event) {
			h.obtainMessage(4, event, 0).sendToTarget();
		}
	};

	// 创建位置监听器
	private LocationListener gpsLocationListener = new LocationListener() {
		
		// 位置发生改变时调用
		@Override
		public void onLocationChanged(Location location) {
			Log.d("gpsLocationListener", "onLocationChanged " + location);
			
			h.obtainMessage(1, location).sendToTarget();
		}

		// provider失效时调用
		@Override
		public void onProviderDisabled(String provider) {
			Log.d("gpsLocationListener", "onProviderDisabled " + provider);
		}

		// provider启用时调用
		@Override
		public void onProviderEnabled(String provider) {
			Log.d("gpsLocationListener", "onProviderEnabled" + provider);
		}

		// 状态改变时调用
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.d("gpsLocationListener", "onStatusChanged " + provider);
		}
	};
	
	private LocationListener netLocationListener = new LocationListener() {
		// 位置发生改变时调用
		@Override
		public void onLocationChanged(Location location) {
			Log.d("netLocationListener", "onLocationChanged " + location);
		}
		
		// provider失效时调用
		@Override
		public void onProviderDisabled(String provider) {
			Log.d("netLocationListener", "onProviderDisabled " + provider);
		}
		
		// provider启用时调用
		@Override
		public void onProviderEnabled(String provider) {
			Log.d("netLocationListener", "onProviderEnabled" + provider);
		}
		
		// 状态改变时调用
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.d("netLocationListener", "onStatusChanged " + provider);
		}
	};

	
	public LocationClient mLocationClient = null;
	
	String bdLocation2s(BDLocation location) {
		
		
		final StringBuffer sb = new StringBuffer(256);
		sb.append("time : ");
		sb.append(location.getTime());
		sb.append("\nerror code : ");
		sb.append(location.getLocType());
		sb.append("\nlatitude : ");
		sb.append(location.getLatitude());
		sb.append("\nlontitude : ");
		sb.append(location.getLongitude());
		sb.append("\nradius : ");
		sb.append(location.getRadius());
		
		if (location.getLocType() == BDLocation.TypeGpsLocation){
			sb.append("\n来自GPS");
			sb.append("\nspeed : ");
			sb.append(location.getSpeed());
			sb.append("\nsatellite : ");
			sb.append(location.getSatelliteNumber());
			sb.append("\ndirection : ");
			sb.append("\naddr : ");
			sb.append(location.getAddrStr());
			sb.append(location.getDirection());
		} 
		else if (location.getLocType() == BDLocation.TypeNetWorkLocation){
			sb.append("\n来自网络");
			sb.append("\naddr : ");
			sb.append(location.getAddrStr());
			//运营商信息
			sb.append("\noperationers : ");
			sb.append(location.getOperators());
		}
		
		sb.append("\nAddrStr: ").append(location.getAddrStr());
		sb.append("\nProvince: ").append(location.getProvince()).append(location.getCity()).append(location.getDistrict());
		sb.append("\nDirection: ").append(location.getDirection());
		
		return sb.toString();
	}
	
	public BDLocationListener myBDLocationListener = new BDLocationListener() {
		
		@Override
		public void onReceiveLocation(BDLocation location) {
			System.out.println("tid=" + Thread.currentThread().getId());
			System.out.println(location);
			
			h.obtainMessage(2, location).sendToTarget();
		}
	};
	
	public void startBaidu() {
		if(mLocationClient == null) {
			mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
			mLocationClient.registerLocationListener( myBDLocationListener );    //注册监听函数
			
			LocationClientOption option = new LocationClientOption();
			option.setLocationMode(LocationMode.Hight_Accuracy);//设置定位模式
			option.setCoorType("bd09ll");//返回的定位结果是百度经纬度,默认值gcj02
			option.setScanSpan(5000);//设置发起定位请求的间隔时间为5000ms
			option.setIsNeedAddress(true);//返回的定位结果包含地址信息
			option.setNeedDeviceDirect(true);//返回的定位结果包含手机机头的方向
			mLocationClient.setLocOption(option);
		}

		mLocationClient.start();
	}
	
	final LocationListener mockLocationListener = new LocationListener() {

		// 位置发生改变时调用
		@Override
		public void onLocationChanged(Location location) {
			Log.d("mockLocationListener", "onLocationChanged " + location);
		}

		// provider失效时调用
		@Override
		public void onProviderDisabled(String provider) {
			Log.d("mockLocationListener", "onProviderDisabled " + provider);
		}

		// provider启用时调用
		@Override
		public void onProviderEnabled(String provider) {
			Log.d("mockLocationListener", "onProviderEnabled" + provider);
		}

		// 状态改变时调用
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.d("mockLocationListener", "onStatusChanged " + provider);
		}
	};
	
	/**
     * inilocation 初始化 位置模拟
     */
    private void initMockLocation() {
		lm.addTestProvider(mMockProviderName, false, true, false, false, true, true, true, 0, 5);
        lm.setTestProviderEnabled(mMockProviderName, true);
        lm.requestLocationUpdates(mMockProviderName, 0, 0, mockLocationListener);
    }

    /**
     * setLocation 设置GPS的位置
     */
    private void setMockLocation(double longitude, double latitude) {
        Location location = new Location(mMockProviderName);
        location.setTime(System.currentTimeMillis());
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAccuracy(1.0f); //精准度
        location.setAltitude(2.0f);
//        location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        
        lm.setTestProviderLocation(mMockProviderName, location);
    }
}
