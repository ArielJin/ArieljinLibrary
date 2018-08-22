package com.arieljin.library.listener;

public interface OnTaskCompleteListener<T> {
	void onTaskComplete(T result);

//	void onTaskComplete();

	void onTaskLoadMoreComplete(T result);

	void onTaskFailed(String error);

	void onTaskCancel();

}