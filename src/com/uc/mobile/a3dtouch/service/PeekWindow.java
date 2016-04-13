package com.uc.mobile.a3dtouch.service;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.R.integer;
import android.app.Instrumentation;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.uc.mobile.a3dtouch.R;
import com.uc.mobile.a3dtouch.utils.Utils;
import com.uc.mobile.a3dtouch.widget.BlurringView;
import com.uc.mobile.a3dtouch.widget.VScrollLayout;
import com.uc.mobile.a3dtouch.widget.VScrollLayout.OnStateChangeListener;

/**
 * 只是一个显示窗口.
 * 
 * @author Administrator
 * 
 */
public class PeekWindow {
	private final static String TAG = "PeekWindow";

	private PopupWindow mPopupWindow;
	private LayoutInflater mInflater;
	private ViewGroup mRootView;
	private FrameLayout mContainerView;
	private BlurringView mBlurringView;
	private VScrollLayout mContentLayout;
	private ViewGroup mMenuLayout;
	private Context mContext;
	Instrumentation mInstrumentation = new Instrumentation();
	private final static ExecutorService threadPool = Executors
			.newSingleThreadExecutor();
	private FrameLayout.LayoutParams mLayoutParams = new FrameLayout.LayoutParams(
			android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
			android.widget.FrameLayout.LayoutParams.MATCH_PARENT);

	public PeekWindow(Context context) {
		mInflater = LayoutInflater.from(context);
		mContext = context.getApplicationContext();
	}

	public void reset() {
		mRootView = (ViewGroup) mInflater.inflate(R.layout.peek_window, null);
		mContainerView = (FrameLayout) mRootView.findViewById(R.id.container);
		mBlurringView = (BlurringView) mRootView.findViewById(R.id.blurView);
		mContentLayout = (VScrollLayout) mRootView
				.findViewById(R.id.contentLayout);
		mMenuLayout = (ViewGroup) mRootView.findViewById(R.id.menuLayout);
		mBlurringView.setBlurWeight(0.8f);

		LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) mContainerView
				.getLayoutParams();

		params.height = Utils.getScreenHeight(mContext) - 80;
		mContainerView.setLayoutParams(params);

		mPopupWindow = new PopupWindow(mRootView,
				ViewGroup.LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,
				false);
		mPopupWindow.setAnimationStyle(R.style.popwin_anim_style);
		mContentLayout.setOnStateChangeListener(new OnStateChangeListener() {

			@Override
			public void onStateChanged(boolean isAtTopOrBottom) {
				if (isAtTopOrBottom) {
					close();
				}
			}
		});

	}

	private void sendMotionDown() {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				mInstrumentation.sendPointerSync(MotionEvent.obtain(
						SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
						MotionEvent.ACTION_DOWN, 800, 800, 0));

			}
		});
	}

	// backView为背景View. preView 为预览View. 每次show的时候都要new一个popupwindow 真烦人.
	public void showAt(View backView, View preView) {
		reset();
		mPopupWindow.showAtLocation(backView, Gravity.CENTER, 0, 0);
		mBlurringView.setBlurredView(backView);
		mContainerView.addView(preView, mLayoutParams);
		sendMotionDown();
	}

	public void close() {
		if (mPopupWindow != null && mPopupWindow.isShowing()) {
			mPopupWindow.dismiss();
			mPopupWindow = null;
			mBlurringView = null;
			mRootView = null;
			mContainerView.removeAllViews();
			mContainerView = null;
			if (onDismissListener != null)
				onDismissListener.onDismiss();
		}
	}

	private final int BTN_TAG = 0x7f090000;

	public void setCustomActions(Action[] actions) {
		mMenuLayout.removeAllViews();
		int size = 5;
		if (actions.length <= 5)
			size = actions.length;
		else {
			throw new RuntimeException("actions can be more than 5.");
		}

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
				android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
		params.topMargin = params.bottomMargin = 10;

		for (int i = 0; i < size; i++) {
			Button btnButton = new Button(mContext);
			btnButton.setBackgroundResource(R.drawable.drawable_btn);
			String key = actions[i].title;
			btnButton.setText(key);
			btnButton.setTag(BTN_TAG, actions[i]);
			btnButton.setOnClickListener(onClickListener);

			mMenuLayout.addView(btnButton, params);
		}
	}

	public boolean isShowing() {
		return mPopupWindow != null && mPopupWindow.isShowing();
	}

	public interface OnDismissListener {
		public void onDismiss();
	}

	private OnDismissListener onDismissListener;

	public void setOnDismissListener(OnDismissListener l) {
		this.onDismissListener = l;
	}

	// private static Set<View> MENU_CACHE = new HashSet<View>();
	private View.OnClickListener onClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Object tag = v.getTag(BTN_TAG);

			if (tag != null && tag instanceof Action) {
				((Action) tag).onAction(v.getContext());
			}
			close();
		}
	};
}
