package com.uc.mobile.a3dtouch.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.uc.mobile.a3dtouch.utils.FileDownloader;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public abstract class ActionFactory {
	public abstract Action[] produceActionsFor(Content content);

	public static ActionFactory defaultUrlActions(Content url) {
		ActionFactory factory = new ActionFactory() {

			@Override
			public Action[] produceActionsFor(Content content) {
				int size = 2;
				Action[] actions = new Action[size];

				actions[0] = new Action(content, "新页面中打开") {
					public void onAction(Context context) {
						Log.i("Action", "onClick" + this.title);
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse(this.content.url));
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(intent);
					};
				};

				actions[1] = new Action(content, "加入书签") {
					public void onAction(Context context) {
						Log.i("Action", "onClick" + this.title);
						ContentValues inputValue = new ContentValues();
						inputValue
								.put(android.provider.Browser.BookmarkColumns.BOOKMARK,
										1);
						// 添加书签Title
						inputValue.put(
								android.provider.Browser.BookmarkColumns.TITLE,
								this.content.url);
						inputValue.put(
								android.provider.Browser.BookmarkColumns.URL,
								this.content.url);
						ContentResolver cr = context.getContentResolver();
						// 向浏览器添加该书签
						Uri uri = cr.insert(
								android.provider.Browser.BOOKMARKS_URI,
								inputValue);
					};
				};
				return actions;
			}
		};
		return factory;
	}

	public static ActionFactory defaultImageActions(Content img) {
		ActionFactory factory = new ActionFactory() {

			@Override
			public Action[] produceActionsFor(Content content) {
				int size = 2;
				Action[] actions = new Action[size];

				actions[0] = new Action(content, "保存到本地") {
					public void onAction(Context context) {
						Log.i("Action", "onClick" + this.title);
						File file = Environment
								.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
						File downFile = new File(file,
								this.content.url.substring(this.content.url
										.lastIndexOf("/") + 1));
						if (this.content.url != null) {
							FileDownloader.download(this.content.url, downFile);
						} else if (this.content.bmp != null) {
							try {
								this.content.bmp.compress(CompressFormat.JPEG,
										100, new FileOutputStream(downFile));
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					};
				};

				actions[1] = new Action(content, "分享") {
					public void onAction(Context context) {
						Log.i("Action", "onClick" + this.title);

						Intent shareIntent = new Intent();
						shareIntent.setAction(Intent.ACTION_SEND);
						if (this.content.url != null) {
							shareIntent.putExtra(Intent.EXTRA_STREAM,
									Uri.parse(this.content.url));
						} else {
							shareIntent.putExtra(Intent.EXTRA_STREAM,
									this.content.bmp);
						}

						shareIntent.setType("image/*");
						shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(shareIntent);
					};
				};
				return actions;
			}
		};
		return factory;
	}

	public static ActionFactory defaultAppActions(Content nextPage) {
		ActionFactory factory = new ActionFactory() {

			@Override
			public Action[] produceActionsFor(Content content) {
				int size = 2;
				Action[] actions = new Action[size];

				actions[0] = new Action(content, "在新窗口中打开") {
					public void onAction(Context context) {
						Log.i("Action", "onClick" + this.title);
						Intent intent = new Intent(context,
								this.content.customActivity);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						if (this.content.params != null) {
							intent.putExtras(this.content.params);
						}
						context.startActivity(intent);
					};
				};

				actions[1] = new Action(content, "关闭") {
					public void onAction(Context context) {
						Log.i("Action", "onClick" + this.title);
					};
				};
				return actions;
			}
		};
		return factory;
	}
}
