package com.uc.mobile.a3dtouch.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class DefaultPreviewActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(layoutResID)
	}

	public void onStart() {
		super.onStart();
		Log.i("DefaultPreviewActivity", "onStart");
	}

	public void onStop() {
		super.onStop();
		Log.i("DefaultPreviewActivity", "onStop");
	}

	public void onDestroy() {
		super.onDestroy();
		Log.i("DefaultPreviewActivity", "onDestroy");
	}
}
