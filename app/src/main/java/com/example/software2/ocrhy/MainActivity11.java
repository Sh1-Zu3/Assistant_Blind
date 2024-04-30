package com.example.software2.ocrhy;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.speech.tts.TextToSpeech;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity11 extends AppCompatActivity {
    float x1, x2;
    private static final int REQUEST_CALL = 1;

    private static final int CLICK_INTERVAL = 1000;
    private static final int NUM_CLICKS_TO_EXIT = 3;
    private int numClicks = 0; //
    private long lastClickTime = 0; // Thời gian của lần nhấn cuối cùng

    private static TextToSpeech textToSpeech;
    private String tmp = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main11);

        textToSpeech = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.getDefault());
                textToSpeech.setSpeechRate(1f);
                textToSpeech.speak("Vuốt sang trái và nói số điện thoại cần gọi, nhấn 3 lần vào điện thoại để trở về menu chính", TextToSpeech.QUEUE_FLUSH, null);
                tmp="Vuốt sang trái và nói số điện thoại cần gọi, nhấn 3 lần vào điện thoại để trở về menu chính";
            }
        });
    }

    public boolean onTouchEvent(MotionEvent touchEvent) {
        switch (touchEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = touchEvent.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = touchEvent.getX();
                if (x1 < x2) {
                    // Vuốt sang phải
                    numClicks=0;
                    textToSpeech.speak(tmp, TextToSpeech.QUEUE_FLUSH, null);
                }
                if (x1 > x2) {
                    // Vuốt sang trái
                    numClicks=0;
                    startSpeechRecognition();
                }
                break;
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

    private void startSpeechRecognition() {
        Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hãy nói số điện thoại bạn muốn gọi");
        tmp = "Hãy nói số điện thoại bạn muốn gọi";
        startActivityForResult(speechIntent, REQUEST_CALL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CALL && resultCode == RESULT_OK) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if(result != null && !result.isEmpty()) {
                // Lấy kết quả nhận diện giọng nói
                textToSpeech.speak("Đang gọi tới số "+ result.get(0), TextToSpeech.QUEUE_FLUSH, null);
                String phoneNumber = result.get(0);
                makePhoneCall(phoneNumber);
            } else {
                Toast.makeText(this, "Không nhận diện được giọng nói, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                textToSpeech.speak("Không nhận diện được giọng nói, vui lòng thử lại", TextToSpeech.QUEUE_FLUSH, null);
                tmp = "Không nhận diện được giọng nói, vui lòng thử lại";
            }
        }
    }

    private void makePhoneCall(String phoneNumber) {
        tmp = phoneNumber;
        if (ContextCompat.checkSelfPermission(MainActivity11.this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity11.this,
                    new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
        }else {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall(tmp); // tmpPhoneNumber là biến lưu trữ số điện thoại tạm thời
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
