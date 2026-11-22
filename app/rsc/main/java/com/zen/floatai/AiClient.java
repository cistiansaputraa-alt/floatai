package com.zen.floatai;

import android.os.Handler;
import android.os.Looper;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.concurrent.TimeUnit;

public class AiClient {
    private static final String API_KEY = "AIza_GANTI_KEY_GEMINI_KAMU";
    private static final String MODEL = "gemini-1.5-flash";
    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/"
            + MODEL + ":generateContent?key=" + API_KEY;

    private static final OkHttpClient http = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS).build();
    private static final Handler ui = new Handler(Looper.getMainLooper());

    public interface Cb { void onResult(String s); }

    public static void ask(String question, Cb cb) {
        String prompt = "Kamu asisten jawab soal. Kalau PG, balas: huruf + teks opsi, lalu alasan 1 baris. "
                + "Kalau esai, jawaban lengkap padat, bahasa Indonesia, tanpa pembuka. Soal: " + question;
        try {
            JSONObject body = new JSONObject();
            body.put("contents", new JSONArray()
                    .put(new JSONObject()
                            .put("parts", new JSONArray()
                                    .put(new JSONObject().put("text", prompt)))));
            body.put("generationConfig", new JSONObject()
                    .put("temperature", 0.3));

            Request req = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                    .build();

            http.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(Call c, java.io.IOException e) {
                    ui.post(() -> cb.onResult("[err] " + e.getMessage()));
                }
                @Override public void onResponse(Call c, Response r) throws java.io.IOException {
                    try {
                        String raw = r.body().string();
                        JSONObject j = new JSONObject(raw);
                        if (j.has("error")) {
                            ui.post(() -> cb.onResult("[api] " + j.optJSONObject("error").optString("message")));
                            return;
                        }
                        String ans = j.getJSONArray("candidates")
                                .getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts")
                                .getJSONObject(0)
                                .getString("text");
                        ui.post(() -> cb.onResult(ans.trim()));
                    } catch (Exception e) {
                        ui.post(() -> cb.onResult("[parse] " + e.getMessage()));
                    }
                }
            });
        } catch (Exception e) { cb.onResult("[err] " + e.getMessage()); }
    }
}
