package com.arieljin.library.manager;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class ThreadPoolManager {
	private static final ThreadPoolExecutor imageExecutorService;
	private static final ThreadPoolExecutor downloadExecutorService;
	private static final ThreadPoolExecutor httpExecutorService;

	static {
		int core = Runtime.getRuntime().availableProcessors();

		imageExecutorService = new ThreadPoolExecutor(core << 1, core << 1, 0, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy());
		downloadExecutorService = new ThreadPoolExecutor(3, 3, 0, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy());
		httpExecutorService = new ThreadPoolExecutor(core << 1, core << 1, 0, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy());
	}

	public static void imageExecute(Runnable runnable) {
		synchronized (imageExecutorService) {
			imageExecutorService.execute(runnable);
		}
	}

	public static <T> Future<T> imageSubmit(Callable<T> callable) {
		synchronized (imageExecutorService) {
			return imageExecutorService.submit(callable);
		}
	}

	public static void downloadExecute(Runnable runnable) {
		synchronized (downloadExecutorService) {
			downloadExecutorService.execute(runnable);
		}
	}

	public static <T> Future<T> downloadSubmit(Callable<T> callable) {
		synchronized (downloadExecutorService) {
			return downloadExecutorService.submit(callable);
		}
	}

	public static void httpExecute(Runnable runnable) {
		synchronized (httpExecutorService) {
			httpExecutorService.execute(runnable);
		}
	}

	public static <T> Future<T> httpSubmit(Callable<T> callable) {
		synchronized (httpExecutorService) {
			return httpExecutorService.submit(callable);
		}
	}
}