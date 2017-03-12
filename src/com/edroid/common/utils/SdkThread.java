package com.edroid.common.utils;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Singleton;

/**
 * SDK线程，执行所有 SDK 异步业务逻辑
 * 
 * @author Yichou 2013-12-23
 *
 */
public final class SdkThread implements Callback {
	public static final Logger log = Logger.create("SdkThread");
	private HandlerThread mThread;
	private Handler mHandler;
	private Handler mUiHandler;
	
	
	public SdkThread(String name) {
		mThread = new HandlerThread(name);
		mThread.start();
		mHandler = new Handler(mThread.getLooper(), this);
		mHandler.sendEmptyMessage(0);
		
		mUiHandler = new Handler(Looper.getMainLooper());
	}
	
	public HandlerThread getThread() {
		return mThread;
	}
	
	public void notifyThread() {
		synchronized (mThread) {
			mThread.notify();
		}
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case 0:
			log.i("hello I am SdkThread");
			break;
		
		default:
			return false;
		}

		return true;
	}
	
	
	private class IRunable implements Runnable {
		Runnable taskRunnable;
		Runnable callbackRunnable;
		
		public IRunable(Runnable task, Runnable callback) {
			this.taskRunnable = task;
			this.callbackRunnable = callback;
		}
		
		@Override
		public void run() {
			taskRunnable.run();
			mUiHandler.post(callbackRunnable);
		}
	}
	
	public void postDelay(Runnable r, long delayMillis) {
		mHandler.postDelayed(r, delayMillis);
	}
	
	/**
	 * 发送一个带回调的任务
	 * 
	 * @param task 主任务，在 sdk 线程执行
	 * @param callback 回调器，主任务在 sdk 线程执行完毕后，在UI线程调此回调
	 */
	public void postEx(Runnable task, Runnable callback) {
		mHandler.post(new IRunable(task, callback));
	}
	
	public void post(Runnable r) {
		mHandler.post(r);
	}
	
	/**
	 * 发送一个任务到UI线程
	 * 
	 * @param r 任务
	 */
	public void postToUiThread(Runnable r) {
		mUiHandler.post(r);
	}
	
	public Handler getHandler() {
		return mHandler;
	}
	
	public Handler getUiHandler() {
		return mUiHandler;
	}
	
	public void init() {
	}
	
	public void exit() {
		mThread.quit();
		try {
			mThread.join(3000);
		} catch (InterruptedException e) {
		}
		log.i("join finish!");
		log.d("exited");
	}
	
	private static final Singleton<SdkThread> gDefault = new Singleton<SdkThread>() {

		@Override
		protected SdkThread create() {
			return new SdkThread("default");
		}
	};
	
	public static SdkThread getDefault() {
		return gDefault.get();
	}
}
