package com.zen.floatai;

import java.util.concurrent.*;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class TeleBot {
    private static final String TOKEN = "8863561550:AAF5Yyfwoq4x2BYUyrXh3_PT5EBl2IGthfk";
    private static final String ALLOWED_CHAT = "5612600300";
    private static volatile boolean running = false;
    private static long lastUpdate = 0;
    private static FloatService svc;
    private static final ScheduledExecutorService pool = Executors.newSingleThreadScheduledExecutor();

    public static void start(FloatService s) {
        svc = s;
        if (running) return;
        running = true;
        pool.scheduleWithFixedDelay(TeleBot::poll, 0, 3, TimeUnit.SECONDS);
    }

    public static void stop() { running = false; }

    private static void poll() {
        if (!running) return;
        try {
            HttpUrl url = HttpUrl.parse("https://api.telegram.org/bot" + TOKEN
                    + "/getUpdates?offset=" + (lastUpdate + 1) + "&timeout=1");
            Response r = new OkHttpClient().newCall(new Request.Builder().url(url).build()).execute();
            JSONObject j = new JSONObject(r.body().string());
            if (!j.optBoolean("ok")) return;
            JSONArray arr = j.getJSONArray("result");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject u = arr.getJSONObject(i);
                lastUpdate = u.getLong("update_id");
                JSONObject msg = u.optJSONObject("message");
                if (msg == null) continue;
                String chat = msg.getJSONObject("chat").getString("id");
                if (!chat.equals(ALLOWED_CHAT)) continue;
                handle(msg.optString("text", "").trim());
            }
        } catch (Exception ignored) {}
    }

    private static void handle(String cmd) {
        switch (cmd) {
            case "/show": svc.showOverlay(); break;
            case "/hide": svc.hideOverlay(); break;
            case "/toggle": svc.toggleOverlay(); break;
        }
    }
}
