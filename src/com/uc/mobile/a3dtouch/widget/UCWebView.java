package com.uc.mobile.a3dtouch.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.uc.mobile.a3dtouch.web.ITouchInterface;
import com.uc.mobile.a3dtouch.web.TouchInterfaceImpl;

public class UCWebView extends WebView {
	private GestureDetector mGestureDetector = null;
	private ITouchInterface mTouchInterface = new TouchInterfaceImpl();

	public UCWebView(Context context) {
		this(context, null, 0);
	}

	public UCWebView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public UCWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		WebSettings settings = getSettings();
		settings.setJavaScriptEnabled(true);
		setWebViewClient(new JSWebViewClient());
		mGestureDetector = new GestureDetector(getContext(), onGestureListener);
	}

	@Override
	public final boolean onTouchEvent(MotionEvent event) {
		mGestureDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}

	public void setTouchInterface(ITouchInterface touchInterface) {
		this.mTouchInterface = touchInterface;
	}

	private GestureDetector.SimpleOnGestureListener onGestureListener = new GestureDetector.SimpleOnGestureListener() {
		public void onLongPress(MotionEvent e) {
			HitTestResult result = getHitTestResult();

			Log.i("HitTestResult", result.getExtra() == null ? "null" : result.getExtra());

			switch (result.getType()) {
			case HitTestResult.IMAGE_TYPE: {
				mTouchInterface.onPreviewRequired(result.getExtra(), ITouchInterface.TYPE_IMG);
			}
				break;
			case HitTestResult.SRC_ANCHOR_TYPE: {
				mTouchInterface.onPreviewRequired(result.getExtra(), ITouchInterface.TYPE_URL);
			}
				break;
			case HitTestResult.UNKNOWN_TYPE: {
				System.out.println(result);
				break;
			}
			default:
				break;
			}
		};
	};
}

class JSWebViewClient extends WebViewClient {

	@Override
	public void onPageFinished(WebView view, String url) {
		super.onPageFinished(view, url);
		view.loadUrl(doInject());
	}

	private String doInject() {
		return "javascript:" + "var body = document.getElementsByTagName(\"body\")[0];" + "body.onselectstart = function(){return false;};" +
		// "document.oncontextmenu= function(){return false;};" +
				"console.log(\"doInject success.\");";
	}
}
