package com.zen.floatai;

import android.app.*;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.*;

public class FloatService extends Service {
    WindowManager wm;
    WindowManager.LayoutParams params;
    OverlayView view;
    boolean visible = false;

    @Override public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        buildNotification();
        TeleBot.start(this);
    }

    private void buildNotification() {
        String ch = "floatai";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel c = new NotificationChannel(ch, "System", NotificationManager.IMPORTANCE_MIN);
            c.setShowBadge(false);
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c);
        }
        Notification n = new Notification.Builder(this, ch)
                .setContentTitle("System")
                .setContentText("running")
                .setSmallIcon(android.R.drawable.ic_menu_manage)
                .setOngoing(true).build();
        startForeground(1, n);
    }

    @Override public int onStartCommand(Intent i, int f, int s) {
        if (!visible) showOverlay();
        return START_STICKY;
    }

    public void showOverlay() {
        if (visible) return;
        view = new OverlayView(this);
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 60; params.y = 200;
        wm.addView(view, params);
        visible = true;
    }

    public void hideOverlay() {
        if (!visible) return;
        try { wm.removeView(view); } catch (Exception ignored) {}
        visible = false;
    }

    public void toggleOverlay() { if (visible) hideOverlay(); else showOverlay(); }

    public void focusOn() {
        if (!visible) return;
        params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wm.updateViewLayout(view, params);
    }
    public void focusOff() {
        if (!visible) return;
        params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wm.updateViewLayout(view, params);
    }

    public void runOnUiThread(Runnable r) {
        new android.os.Handler(getMainLooper()).post(r);
    }

    @Override public void onDestroy() { hideOverlay(); TeleBot.stop(); super.onDestroy(); }
    @Override public IBinder onBind(Intent i) { return null; }
}
