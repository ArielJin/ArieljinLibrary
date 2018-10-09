package com.arieljin.library.zxing.capture;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.arieljin.library.R;
import com.arieljin.library.zxing.capture.camera.CameraManager;
import com.arieljin.library.zxing.capture.widget.ViewfinderView;
import com.arieljin.library.zxing.capture.widget.ZxingScanView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * @time 2018/9/29.
 * @email ariel.jin@tom.com
 */
public class ZxingManager implements SurfaceHolder.Callback {

    private static final String TAG = ZxingManager.class.getSimpleName();

    //    private static final int HISTORY_REQUEST_CODE = 0x0000bacc;
    private static final long BULK_MODE_SCAN_DELAY_MS = 500L;


    private Activity activity;
    private CaptureHandler handler;
    private ViewfinderView viewfinderView;
    private SurfaceView surfaceView;


    private boolean hasSurface = false;


    private CameraManager cameraManager;
    private InactivityTimer inactivityTimer;
    private AmbientLightManager ambientLightManager;

    private Collection<BarcodeFormat> decodeFormats;
    private Map<DecodeHintType, ?> decodeHints;

    private String characterSet;

    private Result savedResultToShow;

    private OnZxingDecodeCallback onZxingDecodeCallback;


    public ZxingManager(Activity activity, ZxingScanView zxingScanView) {
        this.activity = activity;
        this.viewfinderView = zxingScanView.getZxingViewfinder();
        this.surfaceView = zxingScanView.getZxingPreviewView();
        this.inactivityTimer = new InactivityTimer(activity);
        this.ambientLightManager = new AmbientLightManager(activity);

    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    public Handler getHandler() {
        return handler;
    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Activity getActivity() {
        return activity;
    }


    public void onResume() {

        cameraManager = new CameraManager(activity.getApplication());
        viewfinderView.setCameraManager(cameraManager);

        handler = null;
        ambientLightManager.start(cameraManager);
        inactivityTimer.onResume();

//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
//
//        if (prefs.getBoolean(PreferencesKeys.KEY_DISABLE_AUTO_ORIENTATION, true)) {
//            activity.setRequestedOrientation(getCurrentOrientation());
//        } else {
//            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR); // 旋转
//            zxingOrientationDetector.enable(); //启用监听
//        }

        Intent intent = activity.getIntent();

        if (intent != null) {

            String action = intent.getAction();
            if (Intents.Scan.ACTION.equals(action)) {

                // Scan the formats the intent requested, and return the result to the calling activity.
                decodeFormats = DecodeFormatManager.parseDecodeFormats(intent);
                decodeHints = DecodeHintManager.parseDecodeHints(intent);

                if (intent.hasExtra(Intents.Scan.WIDTH) && intent.hasExtra(Intents.Scan.HEIGHT)) {
                    int width = intent.getIntExtra(Intents.Scan.WIDTH, 0);
                    int height = intent.getIntExtra(Intents.Scan.HEIGHT, 0);
                    if (width > 0 && height > 0) {
                        cameraManager.setManualFramingRect(width, height);
                    }
                }

                if (intent.hasExtra(Intents.Scan.CAMERA_ID)) {
                    int cameraId = intent.getIntExtra(Intents.Scan.CAMERA_ID, -1);
                    if (cameraId >= 0) {
                        cameraManager.setManualCameraId(cameraId);
                    }
                }

                String customPromptMessage = intent.getStringExtra(Intents.Scan.PROMPT_MESSAGE);
                if (customPromptMessage != null) {
//                    statusView.setText(customPromptMessage);
                }

            }
//            else if (dataString != null &&
//                    dataString.contains("http://www.google") &&
//                    dataString.contains("/m/products/scan")) {
//
//                // Scan only products and send the result to mobile Product Search.
//                source = IntentSource.PRODUCT_SEARCH_LINK;
////                sourceUrl = dataString;
//                decodeFormats = DecodeFormatManager.PRODUCT_FORMATS;
//
//            }
//            else if (isZXingURL(dataString)) {
//
//                // Scan formats requested in query string (all formats if none specified).
//                // If a return URL is specified, send the results there. Otherwise, handle it ourselves.
//                source = IntentSource.ZXING_LINK;
////                sourceUrl = dataString;
//                Uri inputUri = Uri.parse(dataString);
//                scanFromWebPageManager = new ScanFromWebPageManager(inputUri);
//                decodeFormats = DecodeFormatManager.parseDecodeFormats(inputUri);
//                // Allow a sub-set of the hints to be specified by the caller.
//                decodeHints = DecodeHintManager.parseDecodeHints(inputUri);
//
//            }

            characterSet = intent.getStringExtra(Intents.Scan.CHARACTER_SET);

        }


        SurfaceHolder surfaceHolder = surfaceView.getHolder();

        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);
        } else {
            // Install the callback and wait for surfaceCreated() to init the camera.
            surfaceHolder.addCallback(this);
        }


    }

    public void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        ambientLightManager.stop();
        cameraManager.closeDriver();
//        zxingOrientationDetector.disable();


        if (!hasSurface) {
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
    }

    public void onDestroy() {
        inactivityTimer.shutdown();
    }

//    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
//        if (resultCode == Activity.RESULT_OK && requestCode == HISTORY_REQUEST_CODE && historyManager != null) {
//            int itemNumber = intent.getIntExtra(Intents.History.ITEM_NUMBER, -1);
//            if (itemNumber >= 0) {
//                HistoryItem historyItem = historyManager.buildHistoryItem(itemNumber);
//                decodeOrStoreSavedBitmap(null, historyItem.getResult());
//            }
//        }
//    }

    public void onKeyDown(int keyCode) {
        switch (keyCode) {
//            case KeyEvent.KEYCODE_BACK:
//                if (source == IntentSource.NATIVE_APP_INTENT) {
//                    activity.setResult(Activity.RESULT_CANCELED);
//                    activity.finish();
//                    return true;
//                }
//                if ((source == IntentSource.NONE || source == IntentSource.ZXING_LINK) /*&& lastResult != null*/) {
//                    restartPreviewAfterDelay(0L);
//                    return true;
//                }
//                break;
//            case KeyEvent.KEYCODE_FOCUS:
//            case KeyEvent.KEYCODE_CAMERA:
                // Handle these events so they don't launch the Camera app
//                return true;
            // Use volume up/down to turn on light
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                cameraManager.setTorch(false);
                break;
//                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                cameraManager.setTorch(true);
                break;
//                return true;
        }
//        return false;

    }

    public void restartPreviewAfterDelay(long delayMS) {
        if (handler != null) {
            handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
        }
//        resetStatusView();
    }

    public void restartPreviewAfterDelay() {
        if (handler != null) {
            handler.sendEmptyMessageDelayed(R.id.restart_preview, BULK_MODE_SCAN_DELAY_MS);
        }
//        resetStatusView();
    }


    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a RuntimeException.
            if (handler == null) {
                handler = new CaptureHandler(this, decodeFormats, decodeHints, characterSet, cameraManager);
            }
            decodeOrStoreSavedBitmap(null, null);
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.w(TAG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }
    }

    private void displayFrameworkBugMessageAndExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("错误");
        builder.setMessage("很遗憾，Android 相机出现问题。你可能需要重启设备。");
        builder.setPositiveButton("确定", new FinishListener(activity));
        builder.setOnCancelListener(new FinishListener(activity));
        builder.show();
    }

    private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result) {
        // Bitmap isn't used yet -- will be used soon
        if (handler == null) {
            savedResultToShow = result;
        } else {
            if (result != null) {
                savedResultToShow = result;
            }
            if (savedResultToShow != null) {
                Message message = Message.obtain(handler, R.id.decode_succeeded, savedResultToShow);
                handler.sendMessage(message);
            }
            savedResultToShow = null;
        }
    }


    /**
     * A valid barcode has been found, so give an indication of success and show the results.
     *
     * @param rawResult   The contents of the barcode.
     * @param scaleFactor amount by which thumbnail was scaled
     * @param barcode     A greyscale bitmap of the camera data which was decoded.
     */
    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        inactivityTimer.onActivity();
//        lastResult = rawResult;
//        ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(this, rawResult);
        if (onZxingDecodeCallback != null)
            if (rawResult != null) {
                if (onZxingDecodeCallback.zxingDecodeComplete(rawResult.getText(), com.arieljin.library.zxing.BarcodeFormat.valueOf(rawResult.getBarcodeFormat().name())))
                    restartPreviewAfterDelay(BULK_MODE_SCAN_DELAY_MS);
            } else {
                onZxingDecodeCallback.zxingDecodeFailed("扫码失败！");

            }
    }
//        restartPreviewAfterDelay(BULK_MODE_SCAN_DELAY_MS);


    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    public boolean decodeBmpFromFile(Bitmap bmp) {
        if (bmp == null) {
            return false;
        } else {
            int width = bmp.getWidth();
            int height = bmp.getHeight();
            int[] pixs = new int[width * height];
            bmp.getPixels(pixs, 0, width, 0, 0, width, height);
            Reader reader = new MultiFormatReader();
            boolean isSuccess = false;

            try {
                Result rst = reader.decode(this.getBinaryBitmap(width, height, pixs));
                if (rst != null) {
                    decodeOrStoreSavedBitmap(bmp, rst);
                    isSuccess = true;
                }

            } catch (NotFoundException var14) {
                var14.printStackTrace();
            } catch (ChecksumException var15) {
                var15.printStackTrace();
            } catch (FormatException var16) {
                var16.printStackTrace();
            } catch (IllegalArgumentException var17) {
                var17.printStackTrace();
            }

            return isSuccess;

        }
    }

    private BinaryBitmap getBinaryBitmap(int width, int height, int[] pixs) {
        return new BinaryBitmap(new HybridBinarizer(new RGBLuminanceSource(width, height, pixs)));
    }



    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;

    }

    public void setOnZxingDecodeCallback(OnZxingDecodeCallback onZxingDecodeCallback) {
        this.onZxingDecodeCallback = onZxingDecodeCallback;
    }

    public interface OnZxingDecodeCallback {

        boolean zxingDecodeComplete(String result, com.arieljin.library.zxing.BarcodeFormat format);

        void zxingDecodeFailed(String errorMsg);
    }
}
