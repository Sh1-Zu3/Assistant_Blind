package com.example.software2.ocrhy;

import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainActivity4 extends AppCompatActivity {
    private TextToSpeech textToSpeech;
    private TextView format7;
    float x1, x2, y1, y2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        String dateTime = null;
        Calendar calendar = null;
        SimpleDateFormat simpleDateFormat;
        format7 = findViewById(R.id.format7);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            calendar = Calendar.getInstance();
        }
        simpleDateFormat = new SimpleDateFormat("'Ngày' dd-LLLL-yyyy 'và giờ' KK:mm aaa ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            dateTime = simpleDateFormat.format(calendar.getTime()).toString();
        }
        format7.setText(dateTime);
        format7.getText().toString();

        String finalDateTime = dateTime;
        textToSpeech = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.getDefault());
                textToSpeech.setSpeechRate(1f);
                textToSpeech.speak(finalDateTime, TextToSpeech.QUEUE_FLUSH, null);
                textToSpeech.speak("Chạm vào màn hình và vuốt sang phải để nghe lại, vuốt sang trái để quay lại menu chính", TextToSpeech.QUEUE_ADD, null);
            }
        });
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
                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(() -> textToSpeech.speak("Bạn đang ở trong menu chính. Vuốt sang trái và nói điều bạn muốn", TextToSpeech.QUEUE_FLUSH, null), 1000);

                    Intent intent = new Intent(MainActivity4.this, MainActivity.class);
                    startActivity(intent);
                }

                if (x1 < x2) {
                    String dateTime = null;
                    Calendar calendar = null;
                    SimpleDateFormat simpleDateFormat;
                    format7 = findViewById(R.id.format7);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        calendar = Calendar.getInstance();
                    }
                    simpleDateFormat = new SimpleDateFormat("'Ngày' dd-LLLL-yyyy 'và giờ' KK:mm aaa ");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        dateTime = simpleDateFormat.format(calendar.getTime()).toString();
                    }
                    format7.setText(dateTime);
                    format7.getText().toString();

                    String finalDateTime = dateTime;
                    textToSpeech.speak(finalDateTime, TextToSpeech.QUEUE_FLUSH, null);
                    textToSpeech.speak("Chạm vào màn hình và vuốt sang phải để nghe lại, vuốt sang trái để quay lại menu chính", TextToSpeech.QUEUE_ADD, null);
                }
        }

        return false;
    }

    public void onPause() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
        super.onPause();

    }
}
