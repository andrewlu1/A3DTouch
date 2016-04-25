package com.uc.mobile.a3dtouch.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.RenderScript.ContextType;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.lang.ref.WeakReference;

import com.uc.mobile.a3dtouch.R;

/**
 * A custom view for presenting a dynamically blurred version of another view's
 * content.
 * <p/>
 * Use {@link #setBlurredView(View)} to set up the reference to the view to be
 * blurred. After that, call {@link #invalidate()} to trigger blurring whenever
 * necessary. <integer name="default_blur_radius">15</integer> <integer
 * name="default_downsample_factor">8</integer> <color
 * name="default_overlay_color">#AAFFFFFF</color>
 */
public class BlurringView extends View {

	public BlurringView(Context context) {
		this(context, null);
	}

	public BlurringView(Context context, AttributeSet attrs) {
		super(context, attrs);

		initializeRenderScript(context);

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.PxBlurringView);
		setBlurRadius(a.getInt(R.styleable.PxBlurringView_blurRadius, mRadius));
		setDownsampleFactor(a.getInt(
				R.styleable.PxBlurringView_downsampleFactor, mDownsampleFactor));
		setOverlayColor(a.getColor(R.styleable.PxBlurringView_overlayColor,
				mOverlayColor));

		a.recycle();
	}

	public void setBlurredView(View blurredView) {
		mBlurredViewRef = new WeakReference<View>(blurredView);
	}

	@Override
	protected final void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mBlurredViewRef == null)
			return;
		View mBlurredView = mBlurredViewRef.get();
		if (mBlurredView != null) {
			if (prepare()) {
				// If the background of the blurred view is a color drawable, we
				// use it to clear
				// the blurring canvas, which ensures that edges of the child
				// views are blurred
				// as well; otherwise we clear the blurring canvas with a
				// transparent color.
				if (mBlurredView.getBackground() != null
						&& mBlurredView.getBackground() instanceof ColorDrawable) {
					mBitmapToBlur.eraseColor(((ColorDrawable) mBlurredView
							.getBackground()).getColor());
				} else {
					mBitmapToBlur.eraseColor(Color.TRANSPARENT);
				}

				mBlurredView.draw(mBlurringCanvas);
				blur();

				canvas.save();
				canvas.translate(mBlurredView.getX() - getX(),
						mBlurredView.getY() - getY());
				canvas.scale(mDownsampleFactor, mDownsampleFactor);
				canvas.drawBitmap(mBlurredBitmap, 0, 0, null);
				canvas.restore();
			}
			canvas.drawColor(mOverlayColor);
		}
	}

	public void setBlurRadius(int radius) {

		if (radius > 25)
			radius = 25;
		if (radius > 0) {
			mBlurScript.setRadius(radius);
			mRadius = radius;
		} else {
			mRadius = 0;
		}
	}

	public void setDownsampleFactor(int factor) {
		if (factor <= 0) {
			throw new IllegalArgumentException(
					"Downsample factor must be greater than 0.");
		}

		if (mDownsampleFactor != factor) {
			mDownsampleFactor = factor;
			mDownsampleFactorChanged = true;
		}
	}

	public void setOverlayColor(int color) {
		mOverlayColor = color;
	}

	/**
	 * add. for blur weight gradually change. async process than means you can
	 * call it at any thread any time.
	 * 
	 * @param weight
	 *            0-100. 0 is no blur. 100 is max blur.
	 */
	private float mWeight = 0;
	private int mRadius = 0;

	public void setBlurWeight(float weight) {
		if (mWeight == weight) {
			return;
		}

		if (weight < 0 || weight > 1) {
			throw new IllegalArgumentException(
					"BlureWeight  must be rang 0-100");
		}
		mWeight = weight;

		final int radius = (int) (weight * 25);
		// final int factor = (int) (weight * 5 / 100 + 1);
		final int color = Color.argb((int) (weight * 100f), 50, 50, 50);
		setDownsampleFactor(4);
		setOverlayColor(color);
		setBlurRadius(radius);
		postInvalidate();
	}

	private void initializeRenderScript(Context context) {
		mRenderScript = RenderScript.create(context);
		mBlurScript = ScriptIntrinsicBlur.create(mRenderScript,
				Element.U8_4(mRenderScript));
	}

	protected boolean prepare() {
		if (mBlurredViewRef == null)
			return false;
		View mBlurredView = mBlurredViewRef.get();
		if (mRadius <= 0 || mBlurredView == null)
			return false;
		final int width = mBlurredView.getWidth();
		final int height = mBlurredView.getHeight();

		if (mBlurringCanvas == null || mDownsampleFactorChanged
				|| mBlurredViewWidth != width || mBlurredViewHeight != height) {
			mDownsampleFactorChanged = false;

			mBlurredViewWidth = width;
			mBlurredViewHeight = height;

			int scaledWidth = width / mDownsampleFactor;
			int scaledHeight = height / mDownsampleFactor;

			// The following manipulation is to avoid some RenderScript
			// artifacts at the edge.
			// scaledWidth = scaledWidth - scaledWidth % 4 + 4;
			// scaledHeight = scaledHeight - scaledHeight % 4 + 4;

			if (mBlurredBitmap == null
					|| mBlurredBitmap.getWidth() != scaledWidth
					|| mBlurredBitmap.getHeight() != scaledHeight) {
				mBitmapToBlur = Bitmap.createBitmap(scaledWidth, scaledHeight,
						Bitmap.Config.ARGB_8888);
				if (mBitmapToBlur == null) {
					return false;
				}

				mBlurredBitmap = Bitmap.createBitmap(scaledWidth, scaledHeight,
						Bitmap.Config.ARGB_8888);
				if (mBlurredBitmap == null) {
					return false;
				}
			}

			mBlurringCanvas = new Canvas(mBitmapToBlur);
			mBlurringCanvas.scale(1f / mDownsampleFactor,
					1f / mDownsampleFactor);
			mBlurInput = Allocation.createFromBitmap(mRenderScript,
					mBitmapToBlur, Allocation.MipmapControl.MIPMAP_NONE,
					Allocation.USAGE_SCRIPT);
			mBlurOutput = Allocation.createTyped(mRenderScript,
					mBlurInput.getType());
		}
		return true;
	}

	protected void blur() {

		mBlurInput.copyFrom(mBitmapToBlur);
		mBlurScript.setInput(mBlurInput);
		mBlurScript.forEach(mBlurOutput);
		mBlurOutput.copyTo(mBlurredBitmap);
	}

	@Override
	protected void onDetachedFromWindow() {
		Log.i("BlurringView", "onDetachedFromWindow");
		if (mRenderScript != null) {
			mRenderScript.destroy();
			mRenderScript = null;
		}
		if (mBlurScript != null) {
			mBlurScript.destroy();
			mBlurScript = null;
		}
		super.onDetachedFromWindow();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		Log.i("BlurringView", "onAttachedToWindow");
		initializeRenderScript(getContext().getApplicationContext());
	}

	private int mDownsampleFactor = 1;
	private int mOverlayColor = 0x00ffffff;

	private WeakReference<View> mBlurredViewRef;

	private int mBlurredViewWidth, mBlurredViewHeight;

	private boolean mDownsampleFactorChanged;
	private Bitmap mBitmapToBlur, mBlurredBitmap;
	private Canvas mBlurringCanvas;
	private RenderScript mRenderScript;
	private ScriptIntrinsicBlur mBlurScript;
	private Allocation mBlurInput, mBlurOutput;

}
