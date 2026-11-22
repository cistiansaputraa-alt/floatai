package com.zen.floatai;

import android.graphics.Color;
import android.view.*;
import android.widget.*;

public class OverlayView extends LinearLayout {
    private final FloatService svc;
    private float dx, dy;
    private long lastTap = 0;

    public OverlayView(FloatService s) {
        super(s);
        this.svc = s;
        setOrientation(VERTICAL);
        setBackgroundColor(Color.argb(230, 20, 20, 20));
        setPadding(28, 28, 28, 28);

        TextView title = new TextView(s);
        title.setText("AI");
        title.setTextColor(Color.WHITE);
        title.setTextSize(15);
        title.setPadding(0, 0, 0, 12);
        addView(title);

        EditText input = new EditText(s);
        input.setHint("soal / pertanyaan...");
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.GRAY);
        input.setTextSize(13);
        addView(input);

        Button ask = new Button(s);
        ask.setText("Jawab");
        addView(ask);

        TextView out = new TextView(s);
        out.setTextColor(Color.WHITE);
        out.setTextSize(13);
        out.setPadding(0, 8, 0, 0);
        addView(out);

        ask.setOnClickListener(v -> {
            String q = input.getText().toString().trim();
            if (q.isEmpty()) return;
            out.setText("...");
            AiClient.ask(q, ans -> svc.runOnUiThread(() -> out.setText(ans)));
        });

        input.setOnFocusChangeListener((v, has) -> { if (has) svc.focusOn(); else svc.focusOff(); });

        title.setOnTouchListener((v, e) -> {
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dx = e.getRawX() - svc.params.x;
                    dy = e.getRawY() - svc.params.y;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    svc.params.x = (int)(e.getRawX() - dx);
                    svc.params.y = (int)(e.getRawY() - dy);
                    svc.wm.updateViewLayout(this, svc.params);
                    return true;
            }
            return false;
        });

        title.setOnClickListener(v -> {
            long now = System.currentTimeMillis();
            if (now - lastTap < 400) svc.hideOverlay();
            lastTap = now;
        });
    }
}
