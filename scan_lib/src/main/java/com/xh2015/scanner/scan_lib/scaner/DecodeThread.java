package com.xh2015.scanner.scan_lib.scaner;

import android.os.Handler;
import android.os.Looper;

import com.xh2015.scanner.scan_lib.activity.ScannerCodeActivity;

import java.util.concurrent.CountDownLatch;

/**
 * @author Vondear
 * 描述: 解码线程
 */
final class DecodeThread extends Thread {

    private final CountDownLatch handlerInitLatch;
	ScannerCodeActivity activity;
    private Handler handler;

	DecodeThread(ScannerCodeActivity activity) {
		this.activity = activity;
		handlerInitLatch = new CountDownLatch(1);
	}

	Handler getHandler() {
		try {
			handlerInitLatch.await();
		} catch (InterruptedException ie) {
			// continue?
		}
		return handler;
	}

	@Override
	public void run() {
		Looper.prepare();
		handler = new DecodeHandler(activity);
		handlerInitLatch.countDown();
		Looper.loop();
	}

}
