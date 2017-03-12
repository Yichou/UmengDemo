package com.example.umengdemo;

import com.chrisplus.rootmanager.RootManager;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class BootActivity extends Activity implements OnClickListener {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_boot);
		
		findViewById(R.id.button1).setOnClickListener(this);
		findViewById(R.id.button2).setOnClickListener(this);
		findViewById(R.id.button3).setOnClickListener(this);
		findViewById(R.id.button4).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button1:
			RootManager.getInstance().runCommand("reboot");
			break;
		case R.id.button2:
			RootManager.getInstance().runCommand("reboot");
			break;
		case R.id.button3:
			RootManager.getInstance().runCommand("reboot recovery");
			break;
		case R.id.button4:
			RootManager.getInstance().runCommand("reboot bootloader");
			break;

		default:
			break;
		}
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
}
