package com.example.software2.ocrhy;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
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

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import android.speech.tts.TextToSpeech;

public class MainActivity9 extends AppCompatActivity {
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

    private static final int CLICK_INTERVAL = 1000;
    private static final int NUM_CLICKS_TO_EXIT = 3;
    private int numClicks = 0;
    private long lastClickTime = 0;

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
                .addOnSuccessListener(v -> Log.d(TAG, "done"))
                .addOnFailureListener(e -> Log.e(TAG, "Error down: " + e.getMessage()));

        messageAdapter = new MessageAdapter(messageList);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
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
                    numClicks = 0;
                    startVoiceInput();
                }
                if (x1 < x2){
                    numClicks = 0;
                    textToSpeech.speak(tmp, TextToSpeech.QUEUE_FLUSH, null);
                }
        }
        if (touchEvent.getAction() == MotionEvent.ACTION_DOWN) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime < CLICK_INTERVAL) {
                numClicks++;
                if (numClicks >= NUM_CLICKS_TO_EXIT) {
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
                String spokenText = result.get(0);
                mVoiceInputTv.setText(spokenText);
                translateText(spokenText);
            }
        }
    }


    void addToChat(String message, String sentBy) {

        runOnUiThread(() -> {});
    }

    void addResponse(String response) {
        runOnUiThread(() -> {
            textToSpeech.speak(response, TextToSpeech.QUEUE_FLUSH, null);
            tmp = response;
        });
    }

    public void callGeminiAPI(String spokenText){
        new GenerateContentTask().execute(spokenText);
    }

    private class GenerateContentTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String spokenText = strings[0];
            GenerativeModel gm = new GenerativeModel("gemini-pro", "AIzaSyCw0qxIiG64AfbY8AZJL6idROjGnlqde4s");
            GenerativeModelFutures model = GenerativeModelFutures.from(gm);

            Content content = new Content.Builder()
                    .addText(spokenText) // Use the recognized text
                    .build();

            try {
                GenerateContentResponse result = model.generateContent(content).get();
                return result.getText();
            } catch (Exception e) {
                Log.e(TAG, "chiiuuu: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String resultText) {
            if (resultText != null) {
                translateText2(resultText);
            } else {
                addResponse("error gemini");
            }
        }
    }

    void translateText(String textToTranslate) {
        TranslatorOptions options =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.VIETNAMESE)
                        .setTargetLanguage(TranslateLanguage.ENGLISH)
                        .build();

        final Translator vietnamEnglishTranslator =
                Translation.getClient(options);

        vietnamEnglishTranslator.translate(textToTranslate)
                .addOnSuccessListener(
                        translatedText -> {
                            callGeminiAPI(translatedText); // Gọi hàm Gemni với văn bản đã dịch sang tiếng Anh
                        })
                .addOnFailureListener(
                        e -> {
                            addResponse("Error translating: " + e.getMessage());
                        });
    }


    void translateText2(String textToTranslate) {
        TranslatorOptions options2 =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.ENGLISH)
                        .setTargetLanguage(TranslateLanguage.VIETNAMESE)
                        .build();

        final Translator englishVietnameseTranslator =
                Translation.getClient(options2);

        englishVietnameseTranslator.translate(textToTranslate)
                .addOnSuccessListener(
                        translatedText -> {
                            addResponse(translatedText.toString());
                        })
                .addOnFailureListener(
                        e -> {
                            addResponse("Error translating: " + e.getMessage());
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
