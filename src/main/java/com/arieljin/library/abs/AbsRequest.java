package com.arieljin.library.abs;

import android.content.Context;

import com.arieljin.library.entity.CustomMultipartEntity;
import com.arieljin.library.entity.CustomMultipartEntity.OnFileUploadProgressListener;
import com.arieljin.library.manager.FileUploadManager;
import com.arieljin.library.utils.DigestUtil;

import org.apache.http.NameValuePair;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings({ "deprecation", "serial" })
public abstract class AbsRequest implements Serializable {
	public OnFileUploadProgressListener listener;
	public UploadType uploadType;

	public AbsRequest() {
	}

	public AbsRequest(UploadType uploadType) {
		this.uploadType = uploadType;
	}

	public List<NameValuePair> getBody(Context context) {
		init();

		ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();

//		 addParams(list, getClass().getDeclaredFields());
		addParams(context, list, getClass().getFields());

		return list;
	}

	protected void addParams(Context context, ArrayList<NameValuePair> list, Field[] fields) {
		for (Field field : fields) {
			Object obj = null;
			try {
				field.setAccessible(true);
				obj = field.get(this);
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			}

			if (obj != null && !field.getName().equals("serialVersionUID")) {
				if (obj instanceof OnFileUploadProgressListener) {
					continue;
				} else if (obj instanceof Boolean) {
					list.add(new BasicNameValuePair(field.getName(), (Boolean) obj ? "1" : "0"));
				} else if (obj instanceof File) {
					uploadFile(context, list, field, (File) obj);
				} else {
					list.add(new BasicNameValuePair(field.getName(), obj.toString()));
				}
			}
		}
	}

	protected void uploadFile(Context context, final ArrayList<NameValuePair> list, final Field field, final File file) {
	}

	public CustomMultipartEntity getMultipartEntity(Context context, AbsTask<?> task) {
		init();

		CustomMultipartEntity entity = new CustomMultipartEntity(listener, task);

		for (Field field : getClass().getFields()) {
			try {
				field.setAccessible(true);
				Object obj = field.get(this);

				if (obj instanceof OnFileUploadProgressListener) {
					continue;
				}

				putBody(context, entity, field.getName(), obj);
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			} catch (UnsupportedEncodingException e) {
			}
		}
		return entity;
	}

	protected void putBody(Context context, CustomMultipartEntity entity, Object name, Object obj) throws UnsupportedEncodingException {
		if (obj != null) {
			if (obj instanceof File) {
				File file = (File) obj;
				entity.addPart(name.toString(), new FileBody(file));
				String md5 = DigestUtil.getMd5ByFile(file);
				entity.addPart(name.toString() + "_md5", new StringBody(md5, Charset.defaultCharset()));
			} else if (obj instanceof Boolean) {
				entity.addPart(name.toString(), new StringBody((Boolean) obj ? "1" : "0", Charset.defaultCharset()));
			} else if (obj instanceof Map) {
				for (Entry<?, ?> entry : ((Map<?, ?>) obj).entrySet()) {
					putBody(context,entity, entry.getKey(), entry.getValue());
				}
			} else {
				entity.addPart(name.toString(), new StringBody(obj.toString(), Charset.defaultCharset()));
			}
		}
	}

	protected void init() {
	}

	public enum UploadType {
		chat, photo, composition, head;
	}

	public void finish() {
		for (Field field : getClass().getFields()) {
			try {
				field.setAccessible(true);
				Object obj = field.get(this);
				removeMultipartEntity(obj);
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			}
		}
	}

	private void removeMultipartEntity(Object obj) {
		if (obj != null) {
			if (obj instanceof File) {
				FileUploadManager.removeEntity(((File) obj).getPath());
			} else if (obj instanceof Map) {
				for (Entry<?, ?> entry : ((Map<?, ?>) obj).entrySet()) {
					removeMultipartEntity(entry.getValue());
				}
			}
		}
	}
}