package com.uc.mobile.a3dtouch.service;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
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
	private FrameLayout.LayoutParams mLayoutParams = new FrameLayout.LayoutParams(
			android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
			android.widget.FrameLayout.LayoutParams.MATCH_PARENT);

	public PeekWindow(Context context) {
		mInflater = LayoutInflater.from(context);
		mContext = context;
	}

	public void reset() {
		mRootView = (ViewGroup) mInflater.inflate(R.layout.peek_window, null);
		mContainerView = (FrameLayout) mRootView.findViewById(R.id.container);
		mBlurringView = (BlurringView) mRootView.findViewById(R.id.blurView);
		mContentLayout = (VScrollLayout) mRootView
				.findViewById(R.id.contentLayout);
		mMenuLayout = (ViewGroup) mRootView.findViewById(R.id.menuLayout);
		mBlurringView.setBlurWeight(0.6f);

		LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) mContainerView
				.getLayoutParams();

		params.height = Utils.getScreenHeight(mContext) - 80;
		mContainerView.setLayoutParams(params);

		mPopupWindow = new PopupWindow(mRootView,
				ViewGroup.LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,
				false);
		mContentLayout.setOnStateChangeListener(new OnStateChangeListener() {

			@Override
			public void onStateChanged(boolean isAtTopOrBottom) {
				if (isAtTopOrBottom) {
					close();
				}
			}
		});

	}

	// backView为背景View. preView 为预览View. 每次show的时候都要new一个popupwindow 真烦人.
	public void showAt(View backView, View preView) {
		reset();
		mPopupWindow.showAtLocation(backView, Gravity.CENTER, 0, 0);
		mBlurringView.setBlurredView(backView);
		mContainerView.addView(preView, mLayoutParams);
	}

	public void close() {
		if (mPopupWindow != null && mPopupWindow.isShowing()) {
			mPopupWindow.dismiss();
			mPopupWindow = null;
			mBlurringView = null;
			mRootView = null;
			mContainerView.removeAllViews();
			mContainerView = null;
		}
	}

	public boolean isShowing() {
		return mPopupWindow != null && mPopupWindow.isShowing();
	}

}
