package com.uc.mobile.a3dtouch;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.uc.mobile.a3dtouch.service.PreviewManager;

public class MainActivity extends BaseActivity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViewById(R.id.peekUrlBtn).setOnLongClickListener(
				onLongClickListener);
		findViewById(R.id.peekLittleImgBtn).setOnLongClickListener(
				onLongClickListener);

		findViewById(R.id.peekBigImgBtn).setOnLongClickListener(
				onLongClickListener);

		findViewById(R.id.peekNextPageBtn).setOnLongClickListener(
				onLongClickListener);
	}

	public final View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			v.cancelLongPress();
			onClick(v);
			return true;
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.peekUrlBtn: {
			PreviewManager.getInstance().peek("http://baidu.com");
			break;
		}
		case R.id.peekLittleImgBtn: {
			Bitmap bmp = BitmapFactory.decodeResource(getResources(),
					R.drawable.p1);
			PreviewManager.getInstance().peek(bmp);
			break;
		}
		case R.id.peekBigImgBtn: {
			Bitmap bmp = BitmapFactory.decodeResource(getResources(),
					R.drawable.p1);
			PreviewManager.getInstance().peek(bmp,
					"http://www.pp3.cn/uploads/allimg/111110/114J0L31-5.jpg");
			break;
		}
		case R.id.peekNextPageBtn: {
			PreviewManager.getInstance().peek(DetialInfoActivity.class);
			break;
		}
		case R.id.nextPageBtn: {
			Intent intent = new Intent(this, NextPageActivity.class);
			startActivity(intent);
			break;
		}
		default:
			break;
		}
	}
}
