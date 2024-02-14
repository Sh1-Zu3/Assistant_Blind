package com.example.software2.ocrhy;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.MotionEvent;
import android.widget.TextView;
import android.content.ActivityNotFoundException;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textToSpeech = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.getDefault());
                textToSpeech.setSpeechRate(1f);
                if (firstTime == 0)
                    textToSpeech.speak("Chào mừng bạn đến với ứng dụng Blind. Vuốt sang phải để nghe các tính năng của ứng dụng và vuốt sang trái để nói điều bạn muốn.", TextToSpeech.QUEUE_FLUSH, null);
            }
        });

        mVoiceInputTv = findViewById(R.id.voiceInput);
        requestOverlayPermission();

        // Đăng ký BroadcastReceiver để lắng nghe sự kiện SCREEN_ON và SCREEN_OFF
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenReceiver, filter);
    }

    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
            } else {
                // Quyền được cấp - Hệ thống sẽ hoạt động
            }
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

                if (mVoiceInputTv.getText().toString().equalsIgnoreCase("thoát")) {
                    finishAffinity();
                    System.exit(0);
                }

                if (mVoiceInputTv.getText().toString().equalsIgnoreCase("đọc")) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity2.class);
                    startActivity(intent);
                    mVoiceInputTv.setText(null);

                } else {
                    textToSpeech.speak("Không hiểu, hãy vuốt sang phải và nói lại", TextToSpeech.QUEUE_FLUSH, null);
                }

                if (mVoiceInputTv.getText().toString().equalsIgnoreCase("máy tính")) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity3.class);
                    startActivity(intent);
                    mVoiceInputTv.setText(null);

                } else {
                    textToSpeech.speak("Không hiểu, hãy vuốt sang phải và nói lại", TextToSpeech.QUEUE_FLUSH, null);
                }

                if (mVoiceInputTv.getText().toString().equalsIgnoreCase("ngày và giờ")) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity4.class);
                    startActivity(intent);
                    mVoiceInputTv.setText(null);

                } else {
                    textToSpeech.speak("Không hiểu, hãy vuốt sang phải và nói lại", TextToSpeech.QUEUE_FLUSH, null);
                }

                if (mVoiceInputTv.getText().toString().equalsIgnoreCase("thời tiết")) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity5.class);
                    startActivity(intent);
                    mVoiceInputTv.setText(null);
                }
                else {
                    textToSpeech.speak("Không hiểu, hãy vuốt sang phải và nói lại", TextToSpeech.QUEUE_FLUSH, null);
                }

                if (mVoiceInputTv.getText().toString().equalsIgnoreCase("phần trăm pin")) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity6.class);
                    startActivity(intent);
                    mVoiceInputTv.setText(null);
                }

                else {
                    textToSpeech.speak("Không hiểu, hãy vuốt sang phải và nói lại", TextToSpeech.QUEUE_FLUSH, null);
                }

                if (mVoiceInputTv.getText().toString().equalsIgnoreCase("có")) {
                    textToSpeech.speak("  nói đọc dể đọc sách,máy tính để tính toán, ngày và giờ để biết thời gian và ngày hiện tại, thời tiết để biết thời tiết hiện nay, phần trăm pin để biết pin hiện tại, bạn có muốn nghe lại không?", TextToSpeech.QUEUE_FLUSH, null);
                    mVoiceInputTv.setText(null);
                } else if ((mVoiceInputTv.getText().toString().equalsIgnoreCase("không"))) {
                    textToSpeech.speak("Không hiểu, hãy vuốt sang phải và nói lại", TextToSpeech.QUEUE_FLUSH, null);

                } else if (mVoiceInputTv.getText().toString().equalsIgnoreCase("địa chỉ")) {

                    Intent intent = new Intent(getApplicationContext(), MainActivity8.class);
                    startActivity(intent);
                    mVoiceInputTv.setText(null);
                }
                else if (mVoiceInputTv.getText().toString().equalsIgnoreCase("hỏi đáp")) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity9.class);
                    startActivity(intent);
                    mVoiceInputTv.setText(null);
                }


                if (mVoiceInputTv.getText().toString().contains("thoát")) {
                  mVoiceInputTv.setText(null);

                    finishAffinity();
                }

            }
        } else if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            // Xử lý kết quả của yêu cầu quyền Draw Overlay
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    // Quyền đã được cấp - Tiếp tục xử lý hoặc thực hiện các hành động khác
                } else {
                    // Quyền không được cấp - Có thể thông báo cho người dùng hoặc thực hiện các hành động khác
                }
            }
        }
    }

    @Override
    protected void onPause() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
        super.onPause();
    }
}