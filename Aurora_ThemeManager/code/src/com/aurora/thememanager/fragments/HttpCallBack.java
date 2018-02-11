package com.aurora.thememanager.fragments;

import com.aurora.internet.InternetError;
import com.aurora.internet.Request;

public interface HttpCallBack<T> {
	/** Inform when start to handle this Request. */
	public void onPreExecute();

	/** Inform when {@link Request} execute is finish,
	 * whatever success or error or cancel, this callback
	 * method always invoke if request is done. */
	public void onFinish();

	/** Called when response success. */
	public abstract void onSuccess(T response);

	/**
	 * Callback method that an error has been occurred with the
	 * provided error code and optional user-readable message.
	 */
	public void onError(InternetError error);

	/** Inform when the {@link Request} is truly cancelled. */
	public void onCancel();

	/**
	 * Inform When the {@link Request} cache non-exist or expired,
	 * this callback method is opposite by the onUsedCache(),
	 * means the http retrieving will happen soon.
	 */
	public void onNetworking();

	/** Inform when the cache already use,
	 * it means http networking won't execute. */
	public void onUsedCache();

	/** Inform when {@link Request} execute is going to retry. */
	public void onRetry();

	/**
	 * Inform when download progress change, this callback method only available
	 * when request was {@link com.aurora.internet.request.FileDownloadRequest}.
	 */
	public void onProgressChange(long fileSize, long downloadedSize);
}
