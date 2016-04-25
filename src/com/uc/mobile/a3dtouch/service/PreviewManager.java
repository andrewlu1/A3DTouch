package com.uc.mobile.a3dtouch.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.uc.mobile.a3dtouch.service.PeekWindow.OnDismissListener;

public class PreviewManager {
	private final static String TAG = "PreviewManager";

	private static PreviewManager instance;
	private final static ImageLoader imageLoader = ImageLoader.getInstance();

	private PeekWindow mPeekWindow;

	private transient AtomicBoolean isPeeking = new AtomicBoolean(false);

	private Activity mContext;
	private LocalActivityManager mActivityManager = null;

	private PreviewManager(Activity context) {
		this.mContext = context;
		if (mPeekWindow == null) {
			// 整个生命周期只用创建一次.其他时候对象不需要依赖任何context.
			mPeekWindow = new PeekWindow(context);
			mPeekWindow.setOnDismissListener(onDismissListener);
		}
		mActivityManager = new LocalActivityManager(context, true);
		mActivityManager.dispatchCreate(new Bundle());
		if (!imageLoader.isInited()) {
			imageLoader.init(new ImageLoaderConfiguration.Builder(context).build());
		}
	}

	public static PreviewManager getInstance() {
		if (instance == null)
			throw new RuntimeException("PreviewManager must call register first!");

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
		// mActivityManager.dispatchDestroy(true);
		mPeekWindow.close();
		mPeekWindow = null;
		mActivityManager = null;
		mContext = null;
	}

	public synchronized void peek(Content content) {
		peek(content, null);
	}

	public synchronized void peek(Content content, ActionFactory factory) {

		_peekContent(content);
		if (factory != null) {
			Action[] actions = factory.produceActionsFor(content);
			if (actions != null) {
				mPeekWindow.setCustomActions(actions);
			}
		}
	}

	public synchronized void peek(String url) {
		peek(url, null);
	}

	// 预览网页.
	public void peek(String url, ActionFactory factory) {

		Content content = new Content();
		content.type = Content.TYPE_URL;
		content.url = url;
		if (factory == null) {
			factory = ActionFactory.defaultUrlActions(content);
		}
		peek(content, factory);
	}

	public synchronized void peek(Bitmap bmp) {

		peek(bmp, (ActionFactory) null);
	}

	// 预览小图.
	public synchronized void peek(Bitmap bmp, ActionFactory factory) {

		Content content = new Content();
		content.type = Content.TYPE_IMG;
		content.bmp = bmp;
		if (factory == null) {
			factory = ActionFactory.defaultImageActions(content);
		}
		peek(content, factory);
	}

	public synchronized void peek(Bitmap bmp, String url) {

		peek(bmp, url, null);
	}

	// 预览中有大图带网址.
	public synchronized void peek(Bitmap bmp, String url, ActionFactory factory) {

		Content content = new Content();
		content.type = Content.TYPE_BIG_IMG;
		content.bmp = bmp;
		content.url = url;

		if (factory == null) {
			factory = ActionFactory.defaultImageActions(content);
		}
		peek(content, factory);
	}

	public synchronized void peek(Class<? extends Activity> nextPageActivity) {

		peek(nextPageActivity, null, null);
	}

	// 预览下一屏内容.
	public void peek(Class<? extends Activity> nextPageActivity, ActionFactory factory) {

		peek(nextPageActivity, null, factory);
	}

	public synchronized void peek(Class<? extends Activity> nextPageActivity, Bundle params) {

		peek(nextPageActivity, null, null);
	}

	// 预览下一屏内容.带参数.
	public synchronized void peek(Class<? extends Activity> nextPageActivity, Bundle params, ActionFactory factory) {

		Content content = new Content();
		content.type = Content.TYPE_CUSTOM;
		content.customActivity = nextPageActivity;
		content.params = params;

		if (factory == null) {
			factory = ActionFactory.defaultAppActions(content);
		}
		peek(content, factory);
	}

	private Content mCurrentContent;

	// 重按时候打开内容.
	public synchronized void open() {
		if (mCurrentContent != null) {
			// 打开内容.
		}
	}

	private void _peekContent(final Content c) {
		if (isPeeking.compareAndSet(false, true)) {
			mCurrentContent = c;
			Message msg = Message.obtain(mHandler, 0, c);
			msg.sendToTarget();
		}
	}

	private boolean isPopWindowShow = false;
	private final Handler mHandler = new Handler(Looper.getMainLooper()) {
		public void dispatchMessage(Message msg) {
			View view = mContext.getWindow().getDecorView();
			View preView = _getPrevView((Content) msg.obj);
			mPeekWindow.showAt(view, preView);
			isPopWindowShow = true;
		};
	};

	private View _getPrevView(Content c) {
		if (c == null)
			return null;
		View view = null;
		switch (c.type) {
		case Content.TYPE_URL: {
			WebView webView = (WebView) CACHEMAP_MAP.get(c.type);
			if (webView == null) {
				webView = new WebView(mContext.getApplicationContext());
				webView.setWebViewClient(new WebViewClient());
				CACHEMAP_MAP.put(c.type, webView);
			}
			webView.loadUrl(c.url);
			view = webView;
		}
			break;
		case Content.TYPE_IMG:
		case Content.TYPE_BIG_IMG: {
			ImageView preView = (ImageView) CACHEMAP_MAP.get(c.type);
			if (preView == null) {
				preView = new ImageView(mContext.getApplicationContext());
				preView.setScaleType(ScaleType.CENTER_CROP);
				CACHEMAP_MAP.put(Content.TYPE_IMG, preView);
				CACHEMAP_MAP.put(Content.TYPE_BIG_IMG, preView);
			}
			preView.setImageBitmap(c.bmp);

			if (c.type == Content.TYPE_BIG_IMG) {
				imageLoader.displayImage(c.url, preView);
			}
			view = preView;
			break;
		}

		case Content.TYPE_CUSTOM: {
			Intent intent = new Intent(mContext, c.customActivity);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			if (c.params != null) {
				intent.putExtras(c.params);
			}
			View customView = mActivityManager.startActivity(c.customActivity.getSimpleName(), intent).getDecorView();
			view = customView;
			break;
		}
		default:
			break;
		}
		return view;
	}

	public boolean isPopWindowShow() {
		return isPopWindowShow;
	}

	// 如果 消息处理了,就返回true;
	public synchronized boolean cancel() {
		isPopWindowShow = false;
		isPeeking.compareAndSet(true, false);
		mCurrentContent = null;
		if (mPeekWindow.isShowing()) {
			mPeekWindow.close();
			return true;
		} else {
			return false;
		}
	}

	private static final Map<Integer, View> CACHEMAP_MAP = new HashMap();

	private void cleanCache() {
		WebView webView = (WebView) CACHEMAP_MAP.get(Content.TYPE_URL);
		if (webView != null)
			webView.loadData("", "text/html", "utf-8");

		ImageView img = (ImageView) CACHEMAP_MAP.get(Content.TYPE_IMG);
		if (img != null) {
			img.setImageBitmap(null);
		}
		isPeeking.compareAndSet(true, false);
		mCurrentContent = null;
	}

	private OnDismissListener onDismissListener = new OnDismissListener() {

		@Override
		public void onDismiss() {
			isPeeking.compareAndSet(true, false);
			cleanCache();

		}
	};
}
