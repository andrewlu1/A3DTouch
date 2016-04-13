package com.uc.mobile.a3dtouch.service;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class Content implements Parcelable {
	public int type; // 类型.
	public String url;// url参数. 业务不可能出现多url场景.
	public Bitmap bmp;// 图片参数.
	public Class<? extends Activity> customActivity; // 自定义预览界面.
	public Bundle params; // 可附加的参数.
	public Bundle actionBtns;// 参数表示按钮文字和按钮意图的列表. 文字不能重复.

	public final static int TYPE_UNKNOWN = -1;
	public final static int TYPE_URL = TYPE_UNKNOWN + 1;
	public final static int TYPE_IMG = TYPE_UNKNOWN + 2;
	public final static int TYPE_BIG_IMG = TYPE_UNKNOWN + 3;
	public final static int TYPE_CUSTOM = TYPE_UNKNOWN + 4;

	public Content() {
		type = TYPE_UNKNOWN;
		params = new Bundle();
		actionBtns = new Bundle();
	}

	public Content(Parcel p) {
		type = p.readInt();
		url = p.readString();
		bmp = p.readParcelable(null);
		customActivity = p.readParcelable(null);
		params = p.readBundle();
		actionBtns = p.readBundle();
	}

	public void addAction(String actionName, Intent actionIntent) {
		actionBtns.putParcelable(actionName, actionIntent);
	}

	public static final Parcelable.Creator<Content> CREATOR = new Parcelable.Creator<Content>() {
		public Content createFromParcel(Parcel p) {
			return new Content(p);
		}

		public Content[] newArray(int size) {
			return new Content[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(type);
		dest.writeString(url);
		dest.writeParcelable(bmp, 0);
		dest.writeSerializable(customActivity);
		dest.writeParcelable(params, 0);
		dest.writeParcelable(actionBtns, 0);
	}
}
