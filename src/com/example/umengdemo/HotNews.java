package com.example.umengdemo;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * 获取"http://top.sogou.com/"的每日搜索热词
 * 
 * @author Dr.zm 20141104
 *
 */
public class HotNews {

	public static void main(String[] args) {

		List<TodayHotItem> list = new HotNews().getTodayHotNews();
		for (TodayHotItem i : list) {
			System.out.println(i);
		}
		System.out.println("over");
	}

	/**
	 * 获取某网站地址的html内容
	 * 
	 * @param url
	 * @param charset
	 *            原始html内容的字符集
	 * @param timeout
	 *            超时设置，单位毫秒
	 * @return 仅当http响应码为200时返回html内容，其它返回null
	 */
	public String getHtml(String url, String charset, int timeout) throws SocketTimeoutException, MalformedURLException, IOException {
		URL murl = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) murl.openConnection();
		// 超时设置
		conn.setConnectTimeout(timeout);
		conn.setReadTimeout(timeout);
		conn.connect();
		if (conn.getResponseCode() == 200) {
			StringWriter sw = new StringWriter();
			Reader r = new InputStreamReader(conn.getInputStream(), charset);
			char[] buf = new char[1024 * 4];
			int readLen = 0;
			while ((readLen = r.read(buf)) != -1) {
				sw.write(buf, 0, readLen);
			}
			return sw.toString();
		}
		return null;
	}

	public ArrayList<TodayHotItem> getTodayHotNews() {
		try {
			String str = getHtml("http://top.sogou.com/", "gb2312", 3000);
			// System.out.println(str);
			// 很遗憾，这个网站并不符合xml语法，无法直接用DOM解析
			Pattern p = Pattern.compile("(?<=main_1\\\">)\\s*<table.*?</table>");// 提取表格
			Matcher m = p.matcher(str);
			m.find();
			str = m.group().trim();
			// System.out.println(str);

			// 用dom来解析更方便
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			StringReader sr = new StringReader(str);
			Document doc = db.parse(new InputSource(sr));

			ArrayList<TodayHotItem> ret = new ArrayList<TodayHotItem>();
			NodeList nl = doc.getElementsByTagName("tr");
			for (int i = 0, length = nl.getLength(); i < length; i++) {
				Element tr = (Element) nl.item(i);
				Element a = (Element) tr.getElementsByTagName("a").item(0);
				Element span = (Element) tr.getElementsByTagName("span").item(1);
				TodayHotItem item = new TodayHotItem(a.getAttribute("title"), a.getAttribute("href"), Integer.parseInt(span.getTextContent()));
				ret.add(item);
			}
			return ret;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static class TodayHotItem {
		private String title;
		private String href;
		private int count;// 热度

		public TodayHotItem() {

		}

		public TodayHotItem(String title, String href, int count) {
			this.title = title;
			this.href = href;
			this.count = count;
		}

		public int getCount() {
			return count;
		}

		public void setCount(int count) {
			this.count = count;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getHref() {
			return href;
		}

		public void setHref(String href) {
			this.href = href;
		}

		@Override
		public String toString() {
			return "TodayHotItem [title=" + title + ", href=" + href + ", count=" + count + "]";
		}

	}

}
