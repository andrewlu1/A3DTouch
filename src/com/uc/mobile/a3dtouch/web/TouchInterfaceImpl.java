package com.uc.mobile.a3dtouch.web;

import android.util.Log;
import android.webkit.JavascriptInterface;

import com.uc.mobile.a3dtouch.service.PreviewManager;

public class TouchInterfaceImpl implements ITouchInterface {

	@JavascriptInterface
	public void onPreviewRequired(String url, int resType) {
		Log.i("JsTouchInterface", url);
		switch (resType) {
		case TYPE_URL:
			PreviewManager.getInstance().peek(url);
			break;
		case TYPE_IMG: {
			PreviewManager.getInstance().peek(null, url);
			break;
		}
		default:
			break;
		}
	}
}
