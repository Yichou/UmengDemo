package com.edroid.common.utils;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Singleton;

/**
 * 一条工作线程，可以用来执行网络等耗时操作
 * 
 * @author Yichou 2015-11-18
 *
 */
public final class WorkThread implements Callback {
	public static final Logger log = Logger.create("WorkThread");
	private HandlerThread mThread;
	private Handler mHandler;
	
	private static final Handler mUiHandler = new Handler(Looper.getMainLooper());
	
	
	public WorkThread() {
		this("WorkThread");
	}
	
	public WorkThread(String name) {
		mThread = new HandlerThread(name);
		mThread.start();
		mHandler = new Handler(mThread.getLooper(), this);
		mHandler.sendEmptyMessage(0);
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case 0:
			log.i("Hello I am worker, just give me a task.");
			break;
		
		default:
			return false;
		}

		return true;
	}
	
	/**
	 * 任务完成回调，UI线程
	 * 
	 * @author Jainbin
	 *
	 */
	public interface IFinishCallback {
		public void onFinish(Object result);
	}

	/**
	 * 执行任务回调，任务线程
	 * 
	 * @author Jainbin
	 *
	 */
	public interface IDoCallback {
		public abstract Object onDo(Object doParam);
	}
	
	private class FinishRunable implements Runnable {
		private Object mData;
		private IFinishCallback mCallback;
		
		
		private FinishRunable(IFinishCallback cb, Object data) {
			mCallback = cb;
			mData = data;
		}
		
		@Override
		public void run() {
			mCallback.onFinish(mData);
		}
	}
	
	private class DoRunable implements Runnable {
		private IDoCallback mDoCallback;
		private IFinishCallback mFinishCallback;
		private Object mDoData;

		
		private DoRunable(IDoCallback cb, Object data, IFinishCallback cb2) {
			mDoCallback = cb;
			mDoData = data;
			mFinishCallback = cb2;
		}

		@Override
		public void run() {
			Object ret = mDoCallback.onDo(mDoData);
			mUiHandler.post(new FinishRunable(mFinishCallback, ret));
		}
	}
	
	public void postCb(IDoCallback cb, Object doData, IFinishCallback cb2) {
		mHandler.post(new DoRunable(cb, doData, cb2));
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
	
	public interface ITask {
		/**
		 * 执行回调，在任务线程
		 * 
		 * @param args 参数
		 * 
		 * @return 执行结果，作为 {@link #onResult(Object)} 参数
		 */
		public Object onDo(Object... args);
		
		/**
		 * 任务完成回调，在 UI 线程
		 * 
		 * @param ret {@link #onDo(Object...)} 的返回值
		 */
		public void onResult(Object ret);
	}
	
//	public static class CommonTask implements ITask {
//		private ProgressDialog mDialog;
//
//		public CommonTask showDialog(Activity activity, String msg) {
//			mDialog = new Prgre
//			dialog = Common.createLoadingDialog(activity);
//	        dialog.show();
//	        return this;
//	    }
//		
//		@Override
//		public Object onDo(Object... args) {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public void onResult(Object ret) {
//			// TODO Auto-generated method stub
//			
//		}
//		
//	}
	
	private class ITaskRunable implements Runnable {
		private ITask mTask;
		private Object[] mArgs;
		private Object mRet;
		
		private ITaskRunable(ITask task, Object... args) {
			this.mTask = task;
			this.mArgs = args;
		}

		@Override
		public void run() {
			mRet = mTask.onDo(mArgs);
			mUiHandler.post(new Runnable() {
				
				@Override
				public void run() {
					mTask.onResult(mRet);
				}
			});
		}
	}
	
	/**
	 * 2016-1-5 by yichou
	 * 
	 * @param task 任务回调器
	 * @param args 参数
	 */
	public void postTask(ITask task, Object... args) {
		mHandler.post(new ITaskRunable(task, args));
	}

	public void postTask(ITask task) {
		mHandler.post(new ITaskRunable(task, (Object[])null));
	}
	
	public void postTaskDelay(int t, ITask task) {
		mHandler.postDelayed(new ITaskRunable(task, (Object[])null), t);
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
	
	private static final Singleton<WorkThread> gDefault = new Singleton<WorkThread>() {

		@Override
		protected WorkThread create() {
			return new WorkThread();
		}
	};
	
	public static WorkThread getDefault() {
		return gDefault.get();
	}
}
