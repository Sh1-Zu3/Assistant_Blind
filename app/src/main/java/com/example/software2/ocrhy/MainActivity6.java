package com.example.software2.ocrhy;

import android.content.Intent;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class MainActivity6 extends AppCompatActivity {
    TextView text;
    private static final int REQ_CODE_SPEECH_INPUT = 100;
    float x1, x2, y1, y2;
    TextToSpeech textToSpeech;

    private static final int CLICK_INTERVAL = 1000; // Thời gian giữa các lần nhấn (ms)
    private static final int NUM_CLICKS_TO_EXIT = 3; // Số lần nhấn để thoát ứng dụng
    private int numClicks = 0; // Biến đếm số lần nhấn
    private long lastClickTime = 0; // Thời gian của lần nhấn cuối cùng


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main6);
        text = findViewById(R.id.text);
        textToSpeech = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.getDefault());
                textToSpeech.setSpeechRate(1f);

                BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
                int percentage = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                text.setText("Phần trăm Pin là " + percentage + " %");
                text.getText().toString();
                if (percentage < 50) {
                    textToSpeech.speak("Phần trăm Pin là" + percentage + " %"+ ". Vui lòng sạc điện thoại", TextToSpeech.QUEUE_FLUSH, null);
                    textToSpeech.speak("Vuốt sang phải để nghe lại hoặc nhấn 3 lần vào màn hình để trở về menu chính", TextToSpeech.QUEUE_ADD, null);
                } else {
                    textToSpeech.speak("Phần trăm Pin là" + percentage + "%." +"Điện thoại không cần sạc", TextToSpeech.QUEUE_FLUSH, null);
                    textToSpeech.speak("Vuốt sang phải để nghe lại hoặc nhấn 3 lần vào màn hình để trở về menu chính", TextToSpeech.QUEUE_ADD, null);
                }
            }
        });

        //textToSpeech.speak("bạn có thể nhấn nút tăng âm lượng để trở về menu chính", TextToSpeech.QUEUE_FLUSH, null);
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
                if (x1 < x2) {
                    numClicks=0;
                    BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
                    int percentage = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                    text.setText("Phần trăm Pin là " + percentage + " %");
                    text.getText().toString();
                    textToSpeech.speak("Phần trăm Pin là" + percentage + "%", TextToSpeech.QUEUE_FLUSH, null);

                }
                if (x1 > x2) {
                    numClicks=0;
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

    public void onDestroy() {
        if (text.getText().toString().equalsIgnoreCase("thoát")) {
            finish();
        }
        super.onDestroy();
    }

    public void onPause() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
        super.onPause();
    }
}
