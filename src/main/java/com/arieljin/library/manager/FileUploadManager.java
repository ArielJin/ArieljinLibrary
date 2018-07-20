package com.arieljin.library.manager;

import android.util.Log;

import com.arieljin.library.entity.CustomMultipartEntity;
import com.arieljin.library.utils.MyWeakHashMap;

public final class FileUploadManager {
	public static final MyWeakHashMap<String, CustomMultipartEntity> uploadEntities = new MyWeakHashMap<String, CustomMultipartEntity>();

	public static void addEntity(String filePath, CustomMultipartEntity entity) {
		Log.e("tyler", filePath);
		uploadEntities.put(filePath, entity);
	}

	public static CustomMultipartEntity getEntity(String filePath) {
		Log.e("tyler", filePath);
		return uploadEntities.get(filePath);
	}

	public static void removeEntity(String path) {
		uploadEntities.remove(path);
	}
}