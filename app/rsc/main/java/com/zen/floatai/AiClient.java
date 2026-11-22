package com.zen.floatai;

import android.os.Handler;
import android.os.Looper;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.concurrent.TimeUnit;

public class AiClient {
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String API_KEY = "GROQ_KEY_DI_SINI";
    private static final String MODEL = "llama-3.1-70b-versatile";
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
            body.put("model", MODEL);
            body.put("temperature", 0.3);
            body.put("messages", new JSONArray()
                    .put(new JSONObject().put("role", "user").put("content", prompt)));

            Request req = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                    .build();

            http.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(Call c, java.io.IOException e) {
                    ui.post(() -> cb.onResult("[err] " + e.getMessage()));
                }
                @Override public void onResponse(Call c, Response r) throws java.io.IOException {
                    try {
                        JSONObject j = new JSONObject(r.body().string());
                        String ans = j.getJSONArray("choices").getJSONObject(0)
                                .getJSONObject("message").getString("content");
                        ui.post(() -> cb.onResult(ans.trim()));
                    } catch (Exception e) {
                        ui.post(() -> cb.onResult("[parse] " + e.getMessage()));
                    }
                }
            });
        } catch (Exception e) { cb.onResult("[err] " + e.getMessage()); }
    }
}
