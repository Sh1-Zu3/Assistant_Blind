package com.example.software2.ocrhy;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity7 extends AppCompatActivity {
    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private static int firstTime = 0;
    private TextView mVoiceInputTv;
    float x1, x2, y1, y2;
    private String tmp = "";

    private String[] instructionArray;
    private int currentIndex = 0;

    private static TextToSpeech textToSpeech;
    private static final int CLICK_INTERVAL = 1000; // oăkfpaokmfpawf
    private static final int NUM_CLICKS_TO_EXIT = 3; // olawjfoa
    private int numClicks = 0;
    private long lastClickTime = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main7);
        //loi thoai
        instructionArray = new String[]{
                "1.Tôi có thể đọc báo cho bạn nghe, có một số chủ đề như du lịch, giáo dục, khoa học,thế giới,giải trí..",
                "2.Tôi có thể mở nhạc cho bạn nghe",
                "3.Tôi có thể cho bạn biết dung lượng pin hiện tại, chỉ cần nói phần trăm pin",
                "4.Tôi có thể cho bạn biết ngày và giờ",
                "5.Tôi có thể đọc được chữ thông qua camera qua cho bạn, chỉ cần nói bật camera",
                "6.Tôi có thể tính toán các phép tính, chỉ cần nói máy tính",
                "7.Tôi có thể cho bạn biết vị trí hiện tại của mình",
                "8.Tôi có thể cho bạn biết thời tiết hiện tại",
                "9.Tôi có thể gọi điện thoại khi bạn cần",
                "10.Tôi có thể trả lời câu hỏi của bạn, chỉ cần nói tôi có một số câu hỏi",
                "Tôi đã trình bày cho bạn những chức năng mà tôi có thể thực hiện, bây giờ chỉ cần ấn 3 lần vào màn hình để về menu chính"
        };

        textToSpeech = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.getDefault());
                textToSpeech.setSpeechRate(1f);
                textToSpeech.speak("Bạn đang ở trong chế độ hướng dẫn, hãy vuốt qua trái để nghe hướng dẫn, vuốt qua phải để nghe lại ", TextToSpeech.QUEUE_FLUSH, null);
                tmp = "Bạn đang ở trong chế độ hướng dẫn, hãy vuốt qua trái để nghe hướng dẫn, vuốt qua phải để nghe lại";
            }
        });

        mVoiceInputTv = findViewById(R.id.voiceInput);

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
                    // sieu cap thuat toan
                    textToSpeech.speak(instructionArray[currentIndex], TextToSpeech.QUEUE_FLUSH, null);
                    tmp = instructionArray[currentIndex];
                    currentIndex = (currentIndex + 1) % instructionArray.length;
                }
                if (x1 < x2) {
                    numClicks=0;
                    textToSpeech.speak(tmp, TextToSpeech.QUEUE_FLUSH, null);
                }
                break;
        }
        if (touchEvent.getAction() == MotionEvent.ACTION_DOWN) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime < CLICK_INTERVAL) {
                numClicks++;
                if (numClicks >= NUM_CLICKS_TO_EXIT) {
                    //return ne
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

    @Override
    public void onDestroy() {
        //huy tts
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
