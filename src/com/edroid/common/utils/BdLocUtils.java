package com.edroid.common.utils;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

import android.content.Context;

public class BdLocUtils {
	
	public interface Callback {
		public void onReceiveLocation(String str, double lat, double lng);
	}

	public static void getLoc(Context context, final Callback cb) {
		final LocationClient mLocationClient = new LocationClient(context);     		 //声明LocationClient类
		
		mLocationClient.registerLocationListener(new BDLocationListener() {
			
			@Override
			public void onReceiveLocation(BDLocation location) {
				String str = "UNKNOW";
				double lat = 0, lng = 0;
				
				if(location != null) {
					str = location.getAddrStr() + " " + location.getLocType();
//					str = location.toString();
					lat = location.getLatitude();
					lng = location.getLongitude();
				}
				
				mLocationClient.stop();
				mLocationClient.stop();
				
				cb.onReceiveLocation(str, lat, lng);
			}
		});    //注册监听函数
		
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Battery_Saving);//设置定位模式
		option.setCoorType("bd09ll");//返回的定位结果是百度经纬度,默认值gcj02
		option.setScanSpan(5000);//设置发起定位请求的间隔时间为5000ms
		option.setIsNeedAddress(true);//返回的定位结果包含地址信息
//		option.setNeedDeviceDirect(false);//返回的定位结果包含手机机头的方向
		mLocationClient.setLocOption(option);
		
		mLocationClient.start();
	}
}
