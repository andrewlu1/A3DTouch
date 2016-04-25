package com.uc.mobile.a3dtouch;

import android.os.Bundle;
import android.webkit.WebView;

public class NextPageActivity extends BaseActivity {
	WebView webView;

	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_next_page);
		webView = (WebView) findViewById(R.id.webView);
		webView.loadUrl("file:///android_asset/index.html");
	}

	@Override
	public void onBackPressed() {
		if (webView.canGoBack()) {
			webView.goBack();
		}
	}
}
