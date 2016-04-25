package com.uc.mobile.a3dtouch.widget;

import com.uc.mobile.a3dtouch.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

/**
 * Created by Administrator on 2016/4/6. 
 * CardView 在低版本上内容与边框之间会有空隙.并非真正的圆角布局.
 * RoundView用来构造一个真正的圆角布局.可选形状有: 圆角矩形, 圆,椭圆.矩形.矩形实际上是弧度为0的圆角矩形.
 */
public class RoundView extends FrameLayout {
	private float mRadius = 0;
	private float mStrokeWidth = 0;
	private int mStrokeColor = Color.TRANSPARENT;
	private Path mBoundPath = null;
	private Type mType = Type.Rect;// 默认为 圆角矩形,即未声明type属性时,默认当成圆角矩形.

	public RoundView(Context context) {
		this(context, null);
	}

	public RoundView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RoundView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setWillNotDraw(false);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.RoundView);
		mRadius = a.getDimension(R.styleable.RoundView_radius, mRadius);
		mStrokeWidth = a.getDimension(R.styleable.RoundView_strokeWidth,
				mStrokeWidth);
		mStrokeColor = a.getColor(R.styleable.RoundView_strokeColor,
				mStrokeColor);
		int shape = a.getInt(R.styleable.RoundView_shape, mType.getType());
		mType = Type.from(shape);
		a.recycle();
	}

	public void setRadius(float radius) {
		if (mRadius == radius)
			return;
		this.mRadius = radius;
		postInvalidate();
	}

	public float getRadius() {
		return mRadius;
	}

	public void setStrokeWidth(float strok) {
		this.mStrokeWidth = strok;
		postInvalidate();
	}

	public float getStrokeWidth() {
		return mStrokeWidth;
	}

	public void setStrokeColor(int strokeColor) {
		this.mStrokeColor = strokeColor;
	}

	public int getStrokeColor() {
		return mStrokeColor;
	}

	public final void draw(Canvas canvas) {
		beforeDraw(canvas);
		super.draw(canvas);
	}

	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		afterDraw(canvas);
	}

	private void beforeDraw(Canvas canvas) {
		Rect rect = new Rect();
		getLocalVisibleRect(rect);
		mBoundPath = onCaculatePath(rect);
		canvas.clipPath(mBoundPath);

		Log.i("RoundView", "beforeDraw");
	}

	private void afterDraw(Canvas canvas) {
		Rect rect = new Rect();
		getLocalVisibleRect(rect);
		// 进行描边操作.
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
		p.setStyle(Paint.Style.STROKE);
		p.setColor(mStrokeColor);
		p.setStrokeWidth(mStrokeWidth);
		Path path = onGetPathStroke(rect, mBoundPath);
		if (path == null)
			return;
		canvas.drawPath(path, p);
	}

	protected Path onCaculatePath(Rect r) {
		switch (mType) {
		case Rect:
			return caculateRoundRectPath(r);
		case Circle:
			return caculateCirclePath(r);
		case Oval:
			return caculateOvalPath(r);
		}
		return caculateRoundRectPath(r);
	}

	protected Path onGetPathStroke(Rect r, Path boundPath) {
		switch (mType) {
		case Circle:
			return getCirclePathWithinStroke(r, boundPath);
		default:
			return getPathWithinStroke(r, boundPath);
		}
	}

	// 将path 进行变换,以容纳描边线宽.
	private Path getPathWithinStroke(Rect r, Path path) {
		if (mStrokeWidth <= 0)
			return path;

		// 防止边过宽,完全遮挡内容.
		int minWidth = r.width() > r.height() ? r.height() : r.width();
		if (minWidth <= 0)
			return null;

		if (mStrokeWidth >= minWidth / 2)
			mStrokeWidth = minWidth / 2.5f;

		Path p = new Path();
		Matrix matrix = new Matrix();
		float scaleX = (r.width() - mStrokeWidth / 2) / r.width();
		float scaleY = (r.height() - mStrokeWidth / 2) / r.height();

		matrix.setScale(scaleX, scaleY, r.centerX(), r.centerY());
		path.transform(matrix, p);
		return p;
	}

	private Path getCirclePathWithinStroke(Rect r, Path path) {
		if (mStrokeWidth <= 0)
			return path;
		// 防止边过宽,完全遮挡内容.
		int minWidth = r.width() > r.height() ? r.height() : r.width();
		if (minWidth <= 0)
			return null;

		if (mStrokeWidth >= minWidth / 2)
			mStrokeWidth = minWidth / 2.5f;

		Path p = new Path();
		Matrix matrix = new Matrix();
		float scale = (minWidth - mStrokeWidth / 2) / minWidth;

		matrix.setScale(scale, scale, r.centerX(), r.centerY());
		path.transform(matrix, p);
		return p;
	}

	// 以下方法留做备用,可用于生产各种外形的边框.仅供参考.
	private Path caculateRoundRectPath(Rect r) {
		Path path = new Path();
		float radius = getRadius();
		float elevation = 0;
		path.addRoundRect(new RectF(r.left + elevation, r.top + elevation,
				r.right - elevation, r.bottom - elevation), radius, radius,
				Path.Direction.CW);
		return path;
	}

	private Path caculateCirclePath(Rect r) {
		Path path = new Path();
		int radius = r.width() > r.height() ? r.height() / 2 : r.width() / 2;
		path.addCircle(r.left + radius, r.top + radius, radius,
				Path.Direction.CW);
		return path;
	}

	private Path caculateOvalPath(Rect r) {
		Path path = new Path();
		path.addOval(new RectF(r), Path.Direction.CW);
		return path;
	}

	public enum Type {
		Rect(0), Circle(1), Oval(2);
		private int type;

		Type(int type) {
			this.type = type;
		}

		public int getType() {
			return this.type;
		}

		public static Type from(int type) {
			switch (type) {
			case 0:
				return Rect;
			case 1:
				return Circle;
			case 2:
				return Oval;
			default:
				return Rect;
			}
		}
	}
}
