package com.uc.mobile.a3dtouch.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class NoTouchRoundView extends RoundView {
	public NoTouchRoundView(Context context) {
		this(context, null, 0);
	}

	public NoTouchRoundView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public NoTouchRoundView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		return true;
	}
}
