package com.example.capturescreen;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.Format;
import java.text.SimpleDateFormat;

public class CaptureScreenService extends Service implements ImageReader.OnImageAvailableListener {
    private LinearLayout mFloatLayout = null;
    private WindowManager.LayoutParams wmParams = null;
    private WindowManager mWindowManager = null;
    private LayoutInflater inflater = null;
    private ImageButton mFloatView = null;

    private static final String TAG = "MainActivity";

    private SimpleDateFormat dateFormat = null;
    private String strDate = null;
    private String pathImage = null;
    private String nameImage = null;

    private MediaProjection mMediaProjection = null;
    private VirtualDisplay mVirtualDisplay = null;

    public static int mResultCode = 0;
    public static Intent mResultData = null;
    public static MediaProjectionManager mMediaProjectionManager = null;

    private WindowManager mWindowManager1 = null;
    private int windowWidth = 0;
    private int windowHeight = 0;
    private ImageReader mImageReader = null;
    private DisplayMetrics metrics = null;
    private int mScreenDensity = 0;
    private Handler handler = new Handler();

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        createFloatView();

        createVirtualEnvironment();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    private void createFloatView() {
        wmParams = new WindowManager.LayoutParams();
        mWindowManager = (WindowManager) getApplication().getSystemService(getApplication().WINDOW_SERVICE);
        wmParams.type = LayoutParams.TYPE_PHONE;
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        wmParams.x = 0;
        wmParams.y = 0;
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        inflater = LayoutInflater.from(getApplication());
        mFloatLayout = (LinearLayout) inflater.inflate(R.layout.float_layout, null);
        mWindowManager.addView(mFloatLayout, wmParams);
        mFloatView = (ImageButton) mFloatLayout.findViewById(R.id.float_id);

        mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        mFloatView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                wmParams.x = (int) event.getRawX() - mFloatView.getMeasuredWidth() / 2;
                wmParams.y = (int) event.getRawY() - mFloatView.getMeasuredHeight() / 2 - 25;
                mWindowManager.updateViewLayout(mFloatLayout, wmParams);
                return false;
            }
        });

        mFloatView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // hide the button
                mFloatView.setVisibility(View.INVISIBLE);
                Handler handler = new Handler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "Thread name:" + Thread.currentThread().getName());
                        startVirtual();
                    }
                });
            }
        });

        Log.e(TAG, "created the float sphere view");
    }

    private void createVirtualEnvironment() {
        Log.e(TAG, "Ready to prepared the virtual environment");
        dateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
        strDate = dateFormat.format(new java.util.Date());
        pathImage = Environment.getExternalStorageDirectory().getPath() + "/Pictures/";
        nameImage = pathImage + strDate + ".png";
        mMediaProjectionManager = (MediaProjectionManager) getApplication().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mWindowManager1 = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        windowWidth = mWindowManager1.getDefaultDisplay().getWidth();
        windowHeight = mWindowManager1.getDefaultDisplay().getHeight();
        metrics = new DisplayMetrics();
        mWindowManager1.getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        Log.e(TAG, "Finish to prepared the virtual environment");
    }

    public void startVirtual() {
        mImageReader = ImageReader.newInstance(windowWidth, windowHeight, 1, 2);
        mImageReader.setOnImageAvailableListener(this, null);
        if (mMediaProjection != null) {
            createVirtualDisplay();
        } else {
            Log.e(TAG, "MediaProjection instance is null");
            createMediaProjectionInstance();
            createVirtualDisplay();
        }
    }

    public void createMediaProjectionInstance() {
        Log.e(TAG, "Ready to create MediaProjection instance");
        mResultData = ((CaptureScreenApplication) getApplication()).getIntent();
        mResultCode = ((CaptureScreenApplication) getApplication()).getResult();
        mMediaProjectionManager = ((CaptureScreenApplication) getApplication()).getMediaProjectionManager();
        mMediaProjection = mMediaProjectionManager.getMediaProjection(mResultCode, mResultData);
        Log.e(TAG, "Finish to create MediaProjection instance is:" + mMediaProjection);
    }

    private void createVirtualDisplay() {
        Log.e(TAG, "Ready to createVirtualDisplay");
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("screencap",
                windowWidth, windowHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), new VirtualDisplay.Callback() {
                    @Override
                    public void onPaused() {
                        super.onPaused();
                        Log.e(TAG, "VirtualDisplay.Callback->onPaused");
                    }

                    @Override
                    public void onResumed() {
                        super.onResumed();
                        Log.e(TAG, "VirtualDisplay.Callback->onResumed");
                    }

                    @Override
                    public void onStopped() {
                        super.onStopped();
                        Log.e(TAG, "VirtualDisplay.Callback->onStopped");
                    }
                }, handler);
        Log.e(TAG, "Finish to createVirtualDisplay");
    }

    private void startCapture(ImageReader reader) {
        Bitmap bitmap = null;
        Image image = null;
        strDate = dateFormat.format(new java.util.Date());
        nameImage = pathImage + strDate + ".png";
        image = reader.acquireLatestImage();
        bitmap = createBitmapFromImage(image);
        createFileFromBitmap(bitmap, nameImage);
    }

    private void tearDownMediaProjection(ImageReader reader) {
        if (reader != null) {
            reader.close();
            reader = null;
        }
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        Log.e(TAG, "reader closed");
        Log.e(TAG, "mMediaProjection is stop");
    }

    private Bitmap createBitmapFromImage(Image image) {
        if (image == null) {
            Log.e(TAG, "image is null");
            return null;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        image.close();
        Log.e(TAG, "image data captured");
        return bitmap;
    }

    private void createFileFromBitmap(Bitmap bitmap, String desFilePath) {
        if (bitmap == null) {
            return;
        } else {
            try {
                File fileImage = new File(desFilePath);
                if (!fileImage.exists()) {
                    fileImage.createNewFile();
                    Log.e(TAG, "image file created");
                }
                FileOutputStream out = new FileOutputStream(fileImage);
                if (out != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                    Intent media = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri contentUri = Uri.fromFile(fileImage);
                    media.setData(contentUri);
                    this.sendBroadcast(media);
                    Log.e(TAG, "screen image saved");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void releaseVirtualDisplay() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        mVirtualDisplay = null;
        Log.e(TAG, "virtual display stopped");
    }

    @Override
    public void onDestroy() {
        // to remove mFloatLayout from windowManager
        super.onDestroy();
        if (mFloatLayout != null) {
            mWindowManager.removeView(mFloatLayout);
        }
        tearDownMediaProjection(mImageReader);
        Log.e(TAG, "application destroy");
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        Log.e(TAG, "MaxImages:" + reader.getMaxImages());
        startCapture(reader);
        tearDownMediaProjection(reader);
        mFloatView.setVisibility(View.VISIBLE);

    }
}