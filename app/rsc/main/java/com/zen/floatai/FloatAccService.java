package com.zen.floatai;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;

public class FloatAccService extends AccessibilityService {
    @Override public void onAccessibilityEvent(AccessibilityEvent e) {}
    @Override public void onInterrupt() {}
    @Override protected void onServiceConnected() {
        startService(new Intent(this, FloatService.class));
    }
}
