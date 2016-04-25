package com.uc.mobile.a3dtouch.web;

import android.webkit.JavascriptInterface;

public interface ITouchInterface {
	int TYPE_URL = 0;
	int TYPE_IMG = 1;

	@JavascriptInterface
	public void onPreviewRequired(String url, int resType);
}
