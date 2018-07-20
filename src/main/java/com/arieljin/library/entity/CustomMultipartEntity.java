package com.arieljin.library.entity;

import com.arieljin.library.abs.AbsTask;
import com.arieljin.library.manager.FileUploadManager;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("deprecation")
public final class CustomMultipartEntity extends MultipartEntity {
	private volatile Set<OnFileUploadProgressListener> listeners;
	private long transferred, totalSize;
	public int percent;

	private WeakReference<AbsTask<?>> task;

	public CustomMultipartEntity(OnFileUploadProgressListener listener, AbsTask<?> task) {
		this.task = new WeakReference<AbsTask<?>>(task);
		addListener(listener);
	}

	public void addListener(OnFileUploadProgressListener listener) {
		if (listener != null) {
			if (listeners == null) {
				listeners = new HashSet<OnFileUploadProgressListener>();
			}

			listeners.add(listener);
		}
	}

	public AbsTask<?> getTask() {
		if (task != null) {
			return task.get();
		}
		return null;
	}

	public void addPart(String name, FileBody fileBody) {
		super.addPart(name, fileBody);
		FileUploadManager.addEntity(fileBody.getFile().getPath(), this);
	}

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		totalSize = getContentLength();
		super.writeTo(new CountingOutputStream(outstream));
	}

	public interface OnFileUploadProgressListener {
		void transferred(long total, long num, int percent);
	}

	public final class CountingOutputStream extends FilterOutputStream {

		public CountingOutputStream(OutputStream out) {
			super(out);
		}

		public void write(byte[] b, int off, int len) throws IOException {
			out.write(b, off, len);
			transferred += len;

			sendPercent();
		}

		public void write(int b) throws IOException {
			out.write(b);
			transferred++;

			sendPercent();
		}

		public void sendPercent() {
			int newPercent = (int) ((double) transferred / totalSize * 100);
			if (newPercent != percent) {
				percent = newPercent;

				if (listeners != null) {
					for (OnFileUploadProgressListener listener : listeners) {
						listener.transferred(totalSize, transferred, percent);
					}
				}
			}
		}
	}
}