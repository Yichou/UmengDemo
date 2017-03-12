package com.edroid.common.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

import org.json.JSONObject;

import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;


/**
 * 收集机型信息
 * 	添加 cpuinfo
 * 
 * @author yichou 2017-3-12
 *
 */
public class BuildUpload {
	static final String HOST = "http://apkhooker.com/hook";

	public static boolean upload(Context ctx, String app) {
		String url = HOST + "/collect.php?action=build";
		try {
			String info = get(ctx);
			System.out.println(info);
			String ret = HttpUtils.post(url, 
					"pkg=" + app + "&info=" + info);
			JSONObject jsonObject = new JSONObject(ret);
            System.out.println(ret);
            int code = jsonObject.getInt("code");
			return code == 200;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
	
	private static String read(String path) {
		try {
			Process p = Runtime.getRuntime().exec("cat " + path);
			BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
			StringBuilder sb = new StringBuilder(512);
			String l = null;
			while((l = r.readLine()) != null)
				sb.append(l);
			r.close();
			
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	private static String getMemTotal() {
		try {
			Process p = Runtime.getRuntime().exec("cat /proc/meminfo");
			BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
			StringBuilder sb = new StringBuilder(512);
			String l = null;
			while((l = r.readLine()) != null) {
				if(l.startsWith("MemTotal:")) {
					String[] ss = l.split("\\s+");
					r.close();
					return ss[1];
				}
			}
			r.close();
			
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static String get(Context ctx) {
		JSONObject root = new JSONObject();
		try {
			JSONObject jsonObject = new JSONObject();
			
			Field[] fields = Build.class.getDeclaredFields();
			for(Field f : fields) {
				f.setAccessible(true);
				
				if("SERIAL".equals(f.getName())) continue;
				if("HOST".equals(f.getName())) continue;
				
				if(f.getGenericType() == String.class) {
					jsonObject.put(f.getName(), f.get(null));
				}
			}
			
			DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
			jsonObject.put("WIDTH", dm.widthPixels);
			jsonObject.put("HEIGHT", dm.heightPixels);
			jsonObject.put("DPI", dm.densityDpi);
			
			fields = Build.VERSION.class.getDeclaredFields();
			for(Field f : fields) {
				f.setAccessible(true);
				if(f.getGenericType() == String.class) {
					jsonObject.put(f.getName(), f.get(null));
				}
			}
			root.put("build", jsonObject);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		try {
			String s = read("/proc/version");
			root.put("version", s);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			String s = read("/proc/sys/kernel/osrelease");
			root.put("osrelease", s);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			String s = read("/proc/cpuinfo");
			root.put("cpuinfo", s);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			root.put("memsize", getMemTotal());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return root.toString();
	}
}
