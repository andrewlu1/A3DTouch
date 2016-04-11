package com.uc.mobile.a3dtouch.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Scroller;

public class VScrollLayout extends RelativeLayout {
	private Scroller mScroller;
	private GestureDetector mDetector;

	public VScrollLayout(Context context) {
		this(context, null, 0);
	}

	public VScrollLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public VScrollLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mScroller = new Scroller(context);
		mDetector = new GestureDetector(context,
				new GestureDetector.SimpleOnGestureListener() {
					@Override
					public boolean onDown(MotionEvent e) {
						return true;
					}

					@Override
					public boolean onScroll(MotionEvent e1, MotionEvent e2,
							float distanceX, float distanceY) {
						// 限制边界.可以增加弹性回滚效果.即抬起手指时才判断是否超出边界.
						smoothScrollBy(0, (int) distanceY);
						return false;
					}
				});
	}

	// 本控件有两种状态,要么停留顶部对齐,要么停留在底部对齐.
	public interface OnStateChangeListener {
		public void onStateChanged(boolean isAtTopOrBottom);
	}

	private OnStateChangeListener mListener;

	public void setOnStateChangeListener(OnStateChangeListener l) {
		mListener = l;
	}

	public void smoothScrollBy(int dx, int dy) {
		mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), dx,
				dy);
		postInvalidate();
	}

	public void smoothScrollTo(int fx, int fy) {
		int dx = fx - mScroller.getFinalX();
		int dy = fy - mScroller.getFinalY();
		smoothScrollBy(dx, dy);
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			int height = getChildHeight();
			int scrollY = getScrollY();
			int measureHeight = getHeight();
			boolean isAtTopOrBottom = false;
			// 滚动一半的位置.
			int scroll_2 = (height - measureHeight) / 2;
			if (scrollY < 0 || scrollY < scroll_2) {// 超出屏幕下方或者上方可滚动高度的1/2时,回原位.
				smoothScrollTo(0, 0);
				isAtTopOrBottom = true;
			} else {
				smoothScrollBy(0, height - scrollY - measureHeight);
				isAtTopOrBottom = false;
			}
			if (mListener != null) {
				mListener.onStateChanged(isAtTopOrBottom);
			}
			return false;
		}
		return mDetector.onTouchEvent(event);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return true;
	}

	private int getChildHeight() {
		if (getChildCount() == 0)
			return 0;
		View child = getChildAt(getChildCount() - 1);
		return child.getBottom();
	}

}
