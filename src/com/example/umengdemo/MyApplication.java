package com.example.umengdemo;

import java.lang.reflect.Method;

import android.app.Application;
import android.content.Context;
import dalvik.system.DexClassLoader;

public class MyApplication extends Application {

	

	@Override
	public void onCreate() {
		super.onCreate();
		
		
//		try {
//			DexClassLoader cl = new DexClassLoader(new File(Environment.getExternalStorageDirectory(), "update.jar").getPath(), 
//					getDir("dexopt", 0).getPath(),
//					null, 
//					ClassLoader.getSystemClassLoader());
//			Class<?> cls = cl.loadClass("com.nongxin.usersdk.UserSDK");
//			Method m = cls.getDeclaredMethod("active", Context.class, String.class);
//			m.setAccessible(true);
//			JSONObject jb = new JSONObject();
//			
//			m.invoke(null, this, jb.toString());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		try {
			DexClassLoader cl = new DexClassLoader("/data/local/tmp/vroot.jar", 
					getDir("dexopt", 0).getPath(), 
					null, getClassLoader());
			Class<?> cls = cl.loadClass("krsdk.XSolution");
			Object obj = cls.newInstance();
			
			// init(Context paramContext, String paramString, ClassLoader paramClassLoader)
			Method m = cls.getDeclaredMethod("init", Context.class, String.class, ClassLoader.class);
			m.setAccessible(true);
			m.invoke(obj, this, "/data/local/tmp/libs", getClassLoader());
			
			//int root(Context paramContext)
			m = cls.getDeclaredMethod("root", Context.class);
			m.setAccessible(true);
			System.err.println("root ret=" + m.invoke(obj, this));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
