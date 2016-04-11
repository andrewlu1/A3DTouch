package com.uc.mobile.a3dtouch;

import com.uc.mobile.a3dtouch.service.PreviewManager;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public abstract class BaseActivity extends FragmentActivity {
	protected void onCreate(Bundle b) {
		super.onCreate(b);
	}

	protected void onStart() {
		super.onStart();
		PreviewManager.register(this);
	}

	protected void onStop() {
		super.onStop();
		PreviewManager.unregister(this);
	}

	public void onBackPressed() {
		if (!PreviewManager.getInstance().cancel()) {
			super.onBackPressed();
		}
	}
}
