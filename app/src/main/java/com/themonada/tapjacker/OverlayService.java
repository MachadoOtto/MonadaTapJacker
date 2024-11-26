package com.themonada.tapjacker;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class OverlayService extends Service {
    public static final String ACTION_UPDATE_TEXT = "com.themonada.tapjacker.ACTION_UPDATE_TEXT";
    public static final String ACTION_UPDATE_OPACITY = "com.themonada.tapjacker.ACTION_UPDATE_OPACITY";
    public static final String EXTRA_TEXT = "com.themonada.tapjacker.EXTRA_TEXT";
    public static final String EXTRA_OPACITY = "com.themonada.tapjacker.EXTRA_OPACITY";

    private WindowManager mWindowManager;
    private View mOverlayView;
    private TextView mOverlayTextView;

    public OverlayService() {}

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ACTION_UPDATE_TEXT:
                        String text = intent.getStringExtra(EXTRA_TEXT);
                        updateOverlayText(text);
                        break;
                    case ACTION_UPDATE_OPACITY:
                        int opacity = intent.getIntExtra(EXTRA_OPACITY, 255);
                        updateOverlayOpacity(opacity);
                        break;
                }
            }
        }
        return START_STICKY;
    }

    private void updateOverlayText(String text) {
        if (mOverlayTextView != null) {
            mOverlayTextView.setText(text);
        }
    }

    private void updateOverlayOpacity(int opacity) {
        if (mOverlayView != null) {
            WindowManager.LayoutParams params = (WindowManager.LayoutParams) mOverlayView.getLayoutParams();
            params.alpha = (float) opacity / 255;
            mWindowManager.updateViewLayout(mOverlayView, params);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Inicializar el servicio y mostrar el overlay
        showOverlay();
    }

    private void showOverlay() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.CENTER;

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater inflater = LayoutInflater.from(this);
        mOverlayView = inflater.inflate(R.layout.overlay_layout, null);
        mOverlayTextView = mOverlayView.findViewById(R.id.overlayTextView);

        mWindowManager.addView(mOverlayView, params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOverlayView != null) {
            mWindowManager.removeView(mOverlayView);
        }
    }
}
