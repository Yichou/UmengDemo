package com.example.umengdemo;

import java.util.HashMap;

import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.AnyRes;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class SecondActivity extends Activity implements OnClickListener {
	TextView textView;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity2);
		
		textView = (TextView) findViewById(R.id.textView1);
		findViewById(R.id.button1).setOnClickListener(this);
		
//		MobclickAgent.onEvent(this, "event0");
//		/**
//		 * 批量发送
//		 */
//		MobclickAgent.onEvent(this, "event1", 2);
//		MobclickAgent.onEvent(this, "event2", "params0");
//		
//		HashMap<String, String> map = new HashMap<String, String>();
//		map.put("p0", "a");
//		map.put("p1", "b");
//		map.put("p2", "c");
//		MobclickAgent.onEvent(this, "event2", map);
//		
//		MobclickAgent.updateOnlineConfig(this);
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
	public void onClick(View v) {
//		AnalyticsConfig.
//		MobclickAgent.
//		textView.setText(MobclickAgent.getConfigParams(this, "test"));
//		MobclickAgent.onEvent(this, "name", "hello ");
	}
}
