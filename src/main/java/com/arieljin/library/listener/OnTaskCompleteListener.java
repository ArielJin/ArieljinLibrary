package com.arieljin.library.listener;

public interface OnTaskCompleteListener<T> {
	public void onTaskComplete(T result);

	public void onTaskLoadMoreComplete(T result);

	public void onTaskFailed(String error);

	public void onTaskCancel();

}