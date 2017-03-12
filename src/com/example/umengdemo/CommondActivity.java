package com.example.umengdemo;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.edroid.common.utils.SdkThread;

public class CommondActivity extends Activity implements OnClickListener {
	TextView tvOutput;
	EditText editCmd;
	OutputStream cmdSender;
	InputStream cmdReader;
	SdkThread cmdThread;
	Process cmdProcess;
	
	StringBuilder sb = new StringBuilder(2048);

	
	void init() {
		try {
			Process p = Runtime.getRuntime().exec("su");
			cmdProcess = p;

			cmdSender = p.getOutputStream();
			cmdReader = p.getInputStream();
			synchronized (p) {
//				p.wait();
			}
			p.getErrorStream().close();
			
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_cmd);
	
		tvOutput = (TextView) findViewById(R.id.tvOutput);
		editCmd = (EditText) findViewById(R.id.editCmd);
		findViewById(R.id.btnSend).setOnClickListener(this);
		
		cmdThread = new SdkThread("cmd");
		cmdThread.post(new Runnable() {
			
			@Override
			public void run() {
//				init();
			}
		});
	}
	
	void send(String cmd) {
		OutputStream os = null;
		InputStream is = null;;
		
		try {
			Process p = Runtime.getRuntime().exec("su");
			System.out.println("----");
			
			p.getErrorStream().close();
			
			cmdSender = p.getOutputStream();
			cmdReader = p.getInputStream();
			
			cmdSender.write(cmd.getBytes());
			cmdSender.write('\n');
			cmdSender.flush();
			
			p.waitFor();
			
			if(sb.length() > 0)
				sb.delete(0, sb.length()-1);
			
			//读返回值
			char[] buf = new char[128];
			int read = -1;
			
			InputStreamReader reader = new InputStreamReader(cmdReader);
			while((read = reader.read(buf)) != -1) {
				sb.append(buf, 0, read);
			}
			
			System.out.println(sb.toString());
			
			cmdThread.postToUiThread(new Runnable() {
				
				@Override
				public void run() {
					tvOutput.setText(sb.toString());
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(cmdReader != null) cmdReader.close();
			} catch (Exception e2) {
			}
			try {
				if(cmdSender != null) cmdSender.close();
			} catch (Exception e2) {
			}
		}
	}
	
	@Override
	public void onClick(View v) {
		final String cmd = editCmd.getText().toString();
		if(cmd.length() > 0) {
//			cmdThread.notifyThread();
			
			cmdThread.post(new Runnable() {
				
				@Override
				public void run() {
					send(cmd);
				}
			});
		}
	}
}
