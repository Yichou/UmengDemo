package com.example.umengdemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class SystemSettingsActivity2 extends Activity implements OnClickListener {
	TextView mTextView;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.avtivity_settings);
		
		mTextView = (TextView) findViewById(R.id.textView1);
		findViewById(R.id.button1).setOnClickListener(this);
		findViewById(R.id.button2).setOnClickListener(this);
		findViewById(R.id.button3).setOnClickListener(this);
		
		Configuration c = new Configuration();
		
		android.provider.Settings.System.getConfiguration(getContentResolver(), c);
	}

	@SuppressLint("NewApi")
	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.button1) {
//			android.provider.Settings.System.putString(getContentResolver(), "bd_setting_i2", "demo");
			
		} else if(v.getId() == R.id.button2) {
			String s = "bd_setting_i2=" + android.provider.Settings.System.getString(getContentResolver(), "bd_setting_i2") + "\n";
			s += "bd_setting_i=" + android.provider.Settings.System.getString(getContentResolver(), "bd_setting_i") + "\n";
			s += "5F9583053A92EBF41DCE0B372D759FA2=" + android.provider.Settings.System.getString(getContentResolver(), "5F9583053A92EBF41DCE0B372D759FA2") + "\n";
			s += "com.baidu.deviceid=" + android.provider.Settings.System.getString(getContentResolver(), "com.baidu.deviceid") + "\n";
			s += "__MTA_DEVICE_INFO__=" + android.provider.Settings.System.getString(getContentResolver(), "__MTA_DEVICE_INFO__") + "\n";
			s += "bd_setting_i2_g=" + android.provider.Settings.Global.getString(getContentResolver(), "bd_setting_i2_g") + "\n";
			
			mTextView.setText(s);
		} else if(v.getId() == R.id.button3) {
			android.provider.Settings.System.putString(getContentResolver(), "bd_setting_i2_g", null);
			android.provider.Settings.System.putString(getContentResolver(), "5F9583053A92EBF41DCE0B372D759FA2", null);
			android.provider.Settings.System.putString(getContentResolver(), "com.baidu.deviceid", null);
			android.provider.Settings.System.putString(getContentResolver(), "__MTA_DEVICE_INFO__", null);
			android.provider.Settings.System.putString(getContentResolver(), "bd_setting_i2", null);
			android.provider.Settings.System.putString(getContentResolver(), "bd_setting_i", null);
		}
	}
	
}
