package com.uc.mobile.a3dtouch.service;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

//将每个菜单动作抽象为一个Action. action行为在onAction()中实现.不同的action有不同的行为,如打开网页,保存文件等.
public class Action implements Parcelable {
	public Content content;
	public String title;

	public void onAction(Context context) {
	}

	public Action(Content content, String title) {
		this.content = content;
		this.title = title;
	}

	public Action(Parcel p) {
		title = p.readString();
		content = p.readParcelable(null);

	}

	public static final Parcelable.Creator<Action> CREATOR = new Parcelable.Creator<Action>() {
		public Action createFromParcel(Parcel p) {
			return new Action(p);
		}

		public Action[] newArray(int size) {
			return new Action[size];
		}
	};

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(title);
		dest.writeParcelable(content, 0);
	}
}
