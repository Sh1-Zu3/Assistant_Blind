package com.example.software2.ocrhy;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private static int firstTime = 0;
    private TextView mVoiceInputTv;
    private float x1, x2, y1, y2;

    private static TextToSpeech textToSpeech;
    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 232;
    private BroadcastReceiver screenReceiver = new ScreenReceiver();
    private static final long SPEECH_TIMEOUT = 1250;
    private boolean check=false; // xet activity
    private Handler speechTimeoutHandler;
    private boolean speechTimeoutReached = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        textToSpeech = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.getDefault());
                textToSpeech.setSpeechRate(1f);
                if (firstTime == 0)
                    textToSpeech.speak("Chào mừng bạn đến với ứng dụng Blind. Vuốt sang phải để nghe các tính năng của ứng dụng và vuốt sang trái để nói điều bạn muốn.", TextToSpeech.QUEUE_FLUSH, null);
            }
        });
        //tieng viet qua anh
        TranslatorOptions options2 =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.VIETNAMESE)
                        .setTargetLanguage(TranslateLanguage.ENGLISH)
                        .build();

        final Translator vietnameseEnglishTranslator =
                Translation.getClient(options2);

        vietnameseEnglishTranslator.downloadModelIfNeeded()
                .addOnSuccessListener(v -> {

                    Log.d(TAG, "Model has been downloaded or already available");

                })
                .addOnFailureListener(e -> {

                    Log.e(TAG, "Error downloading translation model: " + e.getMessage());
                });

        mVoiceInputTv = findViewById(R.id.voiceInput);
        requestOverlayPermission();

        // bat tat man hinh
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenReceiver, filter);

        //hen gio ghi am tu out
        speechTimeoutHandler = new Handler();
    }

    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
            } else {}
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent touchEvent) {
        firstTime = 1;
        switch (touchEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = touchEvent.getX();
                y1 = touchEvent.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2 = touchEvent.getX();
                y2 = touchEvent.getY();
                if (x1 < x2) {
                    firstTime = 1;
                    Intent intent = new Intent(MainActivity.this, MainActivity7.class);
                    startActivity(intent);
                    //chay bth
                }
                if (x1 > x2) {
                    startVoiceInput();
                    break;
                }
                break;
        }
        return false;
    }

    private void startVoiceInput() {

        speechTimeoutReached = false;

        speechTimeoutHandler.postDelayed(speechTimeoutRunnable, SPEECH_TIMEOUT);

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
            speechTimeoutHandler.removeCallbacks(speechTimeoutRunnable);

            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                mVoiceInputTv.setText(result.get(0));

                if (mVoiceInputTv.getText().toString().equalsIgnoreCase("thoát")) {
                    finishAffinity();
                    System.exit(0);
                }

                String recognizedText = mVoiceInputTv.getText().toString();

                //main code
                translateText2(recognizedText);
            }
        } else if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            //cap quyen
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {

                } else {

                }
            }
        }
    }

    private void stopVoiceInput() {
        speechTimeoutHandler.removeCallbacks(speechTimeoutRunnable);
    }

    private Runnable speechTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            speechTimeoutReached = true;

            stopVoiceInput();
        }
    };

    void translateText2(String textToTranslate) {
        TranslatorOptions options2 =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.VIETNAMESE)
                        .setTargetLanguage(TranslateLanguage.ENGLISH)
                        .build();

        final Translator vietnamEnglishTranslator =
                Translation.getClient(options2);

        vietnamEnglishTranslator.translate(textToTranslate)
                .addOnSuccessListener(
                        translatedText -> {

                            //xu ly
                            String translatedTextString = translatedText.toString();
                            //Toast.makeText(getApplicationContext(),translatedTextString, Toast.LENGTH_SHORT).show();

                            Log.d(TAG, "Translated text: " + translatedTextString);


                            handleTranslatedText(translatedTextString,textToTranslate);
                        })
                .addOnFailureListener(
                        e -> {

                            Log.e(TAG, "Translation failed: " + e.getMessage());
                        });
    }

    void handleTranslatedText(String translatedText,String textorigin) {
        // tu khoa, co the tieng viet java xu ly khong tot nen xai tieng anh nua cho chac:c
        String[] keywords = {
                "camera",
                "computer","máy tính",
                "date","ngày","time","hour","giờ","thời gian","date and time","ngày và giờ",
                "weather","thời tiết",
                "batery","pin","phần trăm pin","batery percent",
                "location","vị trí","position",
                "question","câu hỏi",
                "read","đọc", "paper","báo","read","read the paper","đọc báo",
                "call","gọi","make a call","gọi điện thoại",
                "music","nhạc","listen to music","nghe nhạc",
        };
        //String[] keywords = {"camera", "máy tính", "ngày và giờ", "thời tiết", "phần trăm pin", "vị trí", "câu hỏi", "đọc báo", "gọi điện thoại", "thời gian", "ngày giờ", "giờ", "ngày","gọi","điện thoại","nghe nhạc"};
        // Kiểm tra xem chuỗi có chứa từ khóa nào đó hay không
        for (String keyword : keywords) {
            if ((translatedText.toLowerCase().contains(keyword.toLowerCase())) || (textorigin.toLowerCase().contains(keyword.toLowerCase()))){                // Chuỗi chứa từ khóa, thực hiện xử lý tương ứng
                switch (keyword) {
                    case "camera":
                        if (check==false) {
                            check = true;
                            Intent intentRead = new Intent(getApplicationContext(), MainActivity2.class);
                            startActivity(intentRead);
                            mVoiceInputTv.setText(null);
                            break;
                        }
                    case "computer":
                    case "máy tính":
                        if (check==false) {
                            check = true;
                            Intent intentCalculator = new Intent(getApplicationContext(), MainActivity3.class);
                            startActivity(intentCalculator);
                            mVoiceInputTv.setText(null);
                            break;
                        }
                    case "date":
                    case "time":
                    case "hour":
                    case "date and time":
                    case "ngày":
                    case "giờ":
                    case "thời gian":
                    case "ngày và giờ":
                        if (check==false) {
                            check=true;
                            //xu ly cac truong hop co cung tu khoa
                            Intent intentDateTime = new Intent(getApplicationContext(), MainActivity4.class);
                            startActivity(intentDateTime);
                            mVoiceInputTv.setText(null);
                            break;
                        }
                    case "weather":
                    case "thời tiết":
                        if (check==false) {
                            check = true;
                            Intent intentWeather = new Intent(getApplicationContext(), MainActivity5.class);
                            startActivity(intentWeather);
                            mVoiceInputTv.setText(null);
                            break;
                        }
                    case "batery":
                    case "batery percent":
                    case "pin":
                    case "phần trăm pin":
                        if (check==false) {
                            check = true;
                            Intent intentBattery = new Intent(getApplicationContext(), MainActivity6.class);
                            startActivity(intentBattery);
                            mVoiceInputTv.setText(null);
                            break;
                        }
                    case "location":
                    case "vị trí":
                    case "position":
                        if (check==false) {
                            check = true;
                            Intent intentAddress = new Intent(getApplicationContext(), MainActivity8.class);
                            startActivity(intentAddress);
                            mVoiceInputTv.setText(null);
                            break;
                        }
                    case "question":
                    case "câu hỏi":
                        if (check==false) {
                            check = true;
                            Intent intentQA = new Intent(getApplicationContext(), MainActivity9.class);
                            startActivity(intentQA);
                            mVoiceInputTv.setText(null);
                            break;
                        }
                    case "read":
                    case "paper":
                    case "đọc":
                    case "báo":
                    case "đọc báo":
                    case "read the paper":
                        if (check==false) {
                            check=true;
                            textToSpeech.speak("Xin đợi tải báo trong ít phút", TextToSpeech.QUEUE_FLUSH, null);
                            Intent intentNews = new Intent(getApplicationContext(), MainActivity10.class);
                            intentNews.putExtra("topic-vi", mVoiceInputTv.getText().toString());
                            intentNews.putExtra("topic-en", translatedText.toLowerCase());
                            startActivity(intentNews);
                            mVoiceInputTv.setText(null);
                            break;
                        }
                    case "make a call":
                    case "phone":
                    case "call":
                    case "gọi điện thoại":
                    case "gọi":
                    case "điện thoại":
                        if (check==false) {
                            check = true;
                            Intent intentCallPhone = new Intent(getApplicationContext(), MainActivity11.class);
                            startActivity(intentCallPhone);
                            mVoiceInputTv.setText(null);
                            break;
                        }
                    case "listen to music":
                    case "music":
                    case "nghe nhạc":
                    case "nhạc":
                        if (check==false) {
                            //if else cho chac
                            check = true;
                            Intent intentMusic = new Intent(getApplicationContext(), MainActivity12.class);
                            startActivity(intentMusic);
                            mVoiceInputTv.setText(null);
                            break;
                        }
                }
            }
        }
        check=false;
    }

    @Override
    protected void onPause() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
        super.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
}
