package com.uc.mobile.a3dtouch.widget;

import android.R.integer;
import android.content.Context;
import android.graphics.Point;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.RelativeLayout;
import android.widget.Scroller;

public class VScrollLayout extends RelativeLayout {
	private Scroller mScroller;
	private OnScrollListener mDetector;
	private final int MIN_TOUCH_SLOP;

	public VScrollLayout(Context context) {
		this(context, null, 0);
	}

	public VScrollLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public VScrollLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mScroller = new Scroller(context);
		mDetector = new OnScrollListener();
		MIN_TOUCH_SLOP = ViewConfiguration.get(context).getScaledTouchSlop();
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
		return mDetector.onTouchEvent(event);
	}

	private float mDownPointY = 0;

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		Log.i("VScrollLayout", ev.toString());

		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			mDownPointY = ev.getY();
		}
		// 防止子控件接收滚动事件.不允许子控件滚动.但可以点击...
		if (ev.getAction() == MotionEvent.ACTION_MOVE) {
			if (Math.abs(ev.getY() - mDownPointY) < MIN_TOUCH_SLOP) {
				return false;
			}
			return true;
		}
		return false;
	}

	private int getChildHeight() {
		if (getChildCount() == 0)
			return 0;
		View child = getChildAt(getChildCount() - 1);
		return child.getBottom();
	}

	private class OnScrollListener {
		private final int POINT_INVALIDE = -1;

		private Point mDownPoint = new Point(0, 0);
		private Point mLastPoint = new Point(0, 0);
		private int mDownPointId = POINT_INVALIDE;

		public boolean onTouchEvent(MotionEvent ev) {
			Log.i("VScrollLayout-T", ev.toString());

			boolean ret = true;
			switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				mDownPointId = MotionEventCompat.getPointerId(ev, 0);
				mDownPoint.x = (int) ev.getX();
				mDownPoint.y = (int) ev.getY();
				Log.e("VScrollLayout", "ACTION_DOWN");
			}
				break;
			case MotionEvent.ACTION_MOVE: {
				// 当子控件拦截了Down事件后,本控件可能收不到Down事件,不能继续操作.直接返回.
				if (POINT_INVALIDE == mDownPointId) {
					ret = false;
					break;
				}

				int index = MotionEventCompat
						.findPointerIndex(ev, mDownPointId);
				if (index < 0)
					break;

				mLastPoint.x = (int) MotionEventCompat.getX(ev, index);
				mLastPoint.y = (int) MotionEventCompat.getY(ev, index);
				onActionScroll(-mLastPoint.x + mDownPoint.x, -mLastPoint.y
						+ mDownPoint.y);

				mDownPoint.set(mLastPoint.x, mLastPoint.y);

				break;
			}
			case MotionEvent.ACTION_UP: {
				ret = onActionUp(ev);
				mDownPointId = POINT_INVALIDE;
				Log.e("VScrollLayout", "ACTION_UP");
				break;
			}
			case MotionEvent.ACTION_CANCEL: {
				Log.e("VScrollLayout", "ACTION_CANCEL");
				break;
			}
			default:
				break;
			}
			return ret;
		}
	}

	private boolean onActionUp(MotionEvent e) {
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
		return true;
	}

	private boolean onActionScroll(int dx, int dy) {
		smoothScrollBy(0, dy);
		return false;
	}
}
