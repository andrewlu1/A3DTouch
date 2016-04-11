package com.uc.mobile.a3dtouch.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.WindowManager;

/**
 * Created by Administrator on 2016/4/7.
 */
public class Utils {
	public static int getStatusBarHeight(Activity context) {
		Rect rect = new Rect();
		context.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
		return rect.top;
	}

	public static Rect getScreenSize(Context context) {
		Rect rect = new Rect();
		WindowManager manager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		manager.getDefaultDisplay().getRectSize(rect);
		return rect;
	}

	public static int getScreenWidth(Context context) {
		Rect rect = new Rect();
		WindowManager manager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		manager.getDefaultDisplay().getRectSize(rect);
		return rect.width();
	}

	public static int getScreenHeight(Context context) {
		Rect rect = new Rect();
		WindowManager manager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		manager.getDefaultDisplay().getRectSize(rect);
		return rect.height();
	}
}
