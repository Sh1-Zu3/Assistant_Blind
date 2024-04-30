package com.example.software2.ocrhy;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import android.speech.tts.TextToSpeech;

public class MainActivity9 extends AppCompatActivity {
    //RecyclerView recyclerView;
    //TextView welcomeTextView;
    //EditText messageEditText;
    //ImageButton sendButton;
    List<Message> messageList;
    private static final int REQ_CODE_SPEECH_INPUT = 100;
    MessageAdapter messageAdapter;
    private TextView mVoiceInputTv;
    float x1, x2, y1, y2;
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();
    private String tmp;
    private static TextToSpeech textToSpeech;

    private static final int CLICK_INTERVAL = 1000; // Thời gian giữa các lần nhấn (ms)
    private static final int NUM_CLICKS_TO_EXIT = 3; // Số lần nhấn để thoát ứng dụng
    private int numClicks = 0; // Biến đếm số lần nhấn
    private long lastClickTime = 0; // Thời gian của lần nhấn cuối cùng

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main9);
        messageList = new ArrayList<>();


        mVoiceInputTv = findViewById(R.id.voiceInput);

        textToSpeech = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.getDefault());
                textToSpeech.setSpeechRate(1f);
                textToSpeech.speak("Vuốt sang trái và nói câu hỏi của bạn, vuốt sang phải để nghe lại, nhấn 3 lần vào màn hình để trở về menu chính", TextToSpeech.QUEUE_FLUSH, null);
                tmp = "Vuốt sang trái và nói câu hỏi của bạn, vuốt sang phải để nghe lại, nhấn 3 lần vào màn hình để trở về menu chính";
            }
        });


        TranslatorOptions options =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.ENGLISH)
                        .setTargetLanguage(TranslateLanguage.VIETNAMESE)
                        .build();

        final Translator englishVietnameseTranslator =
                Translation.getClient(options);

        englishVietnameseTranslator.downloadModelIfNeeded()
                .addOnSuccessListener(v -> {
                    // Mô hình dịch đã được tải xong hoặc đã có sẵn
                    Log.d(TAG, "Model has been downloaded or already available");

                    // Tiếp tục với việc sử dụng Translator
                })
                .addOnFailureListener(e -> {
                    // Lỗi khi tải mô hình
                    Log.e(TAG, "Error downloading translation model: " + e.getMessage());
                });

        TranslatorOptions options2 =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.VIETNAMESE)
                        .setTargetLanguage(TranslateLanguage.ENGLISH)
                        .build();

        final Translator vietnameseEnglishTranslator =
                Translation.getClient(options2);

        vietnameseEnglishTranslator.downloadModelIfNeeded()
                .addOnSuccessListener(v -> {
                    // Mô hình dịch đã được tải xong hoặc đã có sẵn
                    Log.d(TAG, "Model has been downloaded or already available");

                    // Tiếp tục với việc sử dụng Translator
                })
                .addOnFailureListener(e -> {
                    // Lỗi khi tải mô hình
                    Log.e(TAG, "Error downloading translation model: " + e.getMessage());
                });
        /*
        recyclerView = findViewById(R.id.recycler_view);
        welcomeTextView = findViewById(R.id.welcome_text);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_btn);

        messageEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        messageEditText.setRawInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES);
        */
        // Thiết lập RecyclerView
        messageAdapter = new MessageAdapter(messageList);
        //recyclerView.setAdapter(messageAdapter);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        //recyclerView.setLayoutManager(llm);

        /* sendButton.setOnClickListener((v) -> {
            String question = messageEditText.getText().toString().trim();
            addToChat(question, Message.SENT_BY_ME);
            messageEditText.setText("");
            callAPI(question);
            welcomeTextView.setVisibility(View.GONE);
        });
        */
    }

    public boolean onTouchEvent(MotionEvent touchEvent) {
        switch (touchEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = touchEvent.getX();
                y1 = touchEvent.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2 = touchEvent.getX();
                y2 = touchEvent.getY();
                if (x1 > x2) {
                    numClicks=0;
                    startVoiceInput();
                }
                if (x1 < x2){
                    numClicks=0;
                    textToSpeech.speak(tmp, TextToSpeech.QUEUE_FLUSH, null);
                }
        }
        if (touchEvent.getAction() == MotionEvent.ACTION_DOWN) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime < CLICK_INTERVAL) {
                numClicks++;
                if (numClicks >= NUM_CLICKS_TO_EXIT) {
                    // Trở về MainActivity
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(() -> textToSpeech.speak("Bạn đang ở trong menu chính. Vuốt sang trái và nói điều bạn muốn", TextToSpeech.QUEUE_FLUSH, null), 1000);
                }
            } else {
                numClicks = 1;
            }
            lastClickTime = currentTime;
        }
        return false;
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Xin chào, bạn cần giúp gì?");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            a.printStackTrace();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                mVoiceInputTv.setText(result.get(0));
                String spokenText = result.get(0);
                translateText2(spokenText);
            }
        }
    }
    void addToChat(String message, String sentBy) {
        runOnUiThread(() -> {
            //messageList.add(new Message(message, sentBy));
            //messageAdapter.notifyDataSetChanged();
            //textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null);
            //recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
        });
    }

    void addResponse(String response) {
        runOnUiThread(() -> {
            //messageList.remove(messageList.size() - 1);
            //messageList.add(new Message(response, Message.SENT_BY_BOT));
            textToSpeech.speak(response, TextToSpeech.QUEUE_FLUSH, null);
            tmp = response;
            //addToChat(response, Message.SENT_BY_BOT);
        });
    }

    void callAPI(String question) {
        // okhttp
        //messageList.add(new Message("Typing... ", Message.SENT_BY_BOT));

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", "gpt-3.5-turbo-instruct");
            jsonBody.put("prompt", question);
            jsonBody.put("max_tokens", 4000);
            jsonBody.put("temperature", 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/completions")
                .header("Authorization", "Bearer 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")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Không thể tải phản hồi vì " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        String result = jsonArray.getJSONObject(0).getString("text");

                        // Dịch biến result sang tiếng Việt
                        translateText(result);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    addResponse("Không thể tải phản hồi vì " + response.body().string());
                }
            }
        });
    }

    void translateText(String textToTranslate) {
        TranslatorOptions options =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.ENGLISH)
                        .setTargetLanguage(TranslateLanguage.VIETNAMESE) // Chọn mã ngôn ngữ cho tiếng Việt
                        .build();

        final Translator englishVietnameseTranslator =
                Translation.getClient(options);

        englishVietnameseTranslator.translate(textToTranslate)
                .addOnSuccessListener(
                        translatedText -> {
                            // Xử lý kết quả dịch
                            addResponse(translatedText.toString());
                        })
                .addOnFailureListener(
                        e -> {
                            // Xử lý lỗi khi dịch không thành công
                            addResponse("Lỗi khi dịch: " + e.getMessage());
                        });
    }
    void translateText2(String textToTranslate) {
        TranslatorOptions options2 =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.VIETNAMESE)
                        .setTargetLanguage(TranslateLanguage.ENGLISH) // Chọn mã ngôn ngữ cho tiếng Việt
                        .build();

        final Translator vietnamEnglishTranslator =
                Translation.getClient(options2);

        vietnamEnglishTranslator.translate(textToTranslate)
                .addOnSuccessListener(
                        translatedText -> {
                            // Xử lý kết quả dịch
                            callAPI(translatedText.toString());
                        })
                .addOnFailureListener(
                        e -> {
                            // Xử lý lỗi khi dịch không thành công
                            addResponse("Lỗi khi dịch: " + e.getMessage());
                        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}