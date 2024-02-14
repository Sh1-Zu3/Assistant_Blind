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

    public boolean onKeyDown(int keyCode, @Nullable KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> textToSpeech.speak("Bạn đang ở trong menu chính. Vuốt sang trái và nói điều bạn muốn", TextToSpeech.QUEUE_FLUSH, null), 1000);
        }
        return true;
    }

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
                    textToSpeech.speak("Vuốt sang phải để nghe lại hoặc vuốt sang trái để quay lại menu chính", TextToSpeech.QUEUE_ADD, null);
                } else {
                    textToSpeech.speak("Phần trăm Pin là" + percentage + "%." +"Điện thoại không cần sạc", TextToSpeech.QUEUE_FLUSH, null);
                    textToSpeech.speak("Vuốt sang phi để nghe lại hoặc vuốt sang trái để quay lại menu chính", TextToSpeech.QUEUE_ADD, null);
                }
            }
        });

        textToSpeech.speak("bạn có thể nhấn nút tăng âm lượng để trở về menu chính", TextToSpeech.QUEUE_FLUSH, null);
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
                    BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
                    int percentage = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                    text.setText("Phần trăm Pin là " + percentage + " %");
                    text.getText().toString();
                    textToSpeech.speak("Phần trăm Pin là" + percentage + "%", TextToSpeech.QUEUE_FLUSH, null);

                }
                if (x1 > x2) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(() -> textToSpeech.speak("Bạn đang ở trong menu chính. Vuốt sang trái và nói điều bạn muốn", TextToSpeech.QUEUE_FLUSH, null), 1000);
                    Intent intent = new Intent(MainActivity6.this, MainActivity.class);
                    startActivity(intent);
                }

                break;
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
