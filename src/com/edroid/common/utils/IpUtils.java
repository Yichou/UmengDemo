package com.edroid.common.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * 通过 IP 获取省份，城市工具
 * 
 * @author Yichou 2013-11-8 11:36:58
 * 
 */
public final class IpUtils {
	
	public static class IpInfo {
		public String ip, loc;
		public int spendTime;
		

		public IpInfo(String ip, String loc, int t) {
			this.ip = ip;
			this.loc = loc;
			this.spendTime = t;
		}
		
		@Override
		public String toString() {
			return "IP = " + ip + "\n地址 = " + loc + "\n耗时 = " + spendTime;
		}
	}

	public static IpInfo getIpInfoFrom138() {
		InputStream is = null;
		long t0 = System.currentTimeMillis();
		BufferedReader r = null;
		
		try {
			URLConnection con = new URL("http://1212.ip138.com/ic.asp").openConnection();
			con.setConnectTimeout(5000);
			is = con.getInputStream();
			r = new BufferedReader(new InputStreamReader(is, "gb2312"));
			
			String l = null;
			while((l = r.readLine()) != null) {
				int  i = l.indexOf("<center>");
				if(i != -1) {
					int j = l.indexOf("</center>", i);
					String s = l.substring(i+8, j);
					
					i = s.indexOf("来自：");
					String loc = s.substring(i+3);
					
					i = s.indexOf('[');
					j = s.indexOf(']', i);
					String ip = s.substring(i+1, j);
					
					return new IpInfo(ip, loc, (int) (System.currentTimeMillis() - t0));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (Exception e) {
			}
			try {
				r.close();
			} catch (Exception e) {
			}
		}
		
		return null;
	}
	
	public static final class Location {
		public String country;
		public String city;
		public String province;
		
		@Override
		public String toString() {
			return new StringBuilder(128)
//				.append(country).append('/')
				.append(province).append('/').append(city)
				.toString();
		}
	}


}
