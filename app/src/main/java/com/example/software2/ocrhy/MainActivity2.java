package com.example.software2.ocrhy;

import static android.Manifest.permission.CAMERA;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity2 extends AppCompatActivity {
    private static final int REQUEST_SPEECH = 101;
    private static final int REQ_CODE_SPEECH_INPUT = 100;
    Button buttonCamera;
    private Button button;
    private TextView mVoiceInputTv;
    private static int firstTime = 0;
    float x1, x2, y1, y2;
    private TextView textView;
    private SurfaceView surfaceView;
    private CameraSource cameraSource;
    private TextRecognizer textRecognizer;
    private static TextToSpeech textToSpeech;
    private String stringResult = null;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        mVoiceInputTv = (TextView) findViewById(R.id.textView);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, PackageManager.PERMISSION_GRANTED);

        textToSpeech = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.getDefault());
                textToSpeech.setSpeechRate(1f);
                Toast.makeText(MainActivity2.this, "Vuốt sang trái và nói có để đọc, nói không để quay lại menu chính", Toast.LENGTH_SHORT).show();
                textToSpeech.speak("Vuốt sang trái và nói có để đọc, nói không để quay lại menu chính", TextToSpeech.QUEUE_ADD, null);
            }
        });

        mVoiceInputTv = (TextView) findViewById(R.id.textView);
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
                    textToSpeech.speak(stringResult, TextToSpeech.QUEUE_FLUSH, null);
                    textToSpeech.speak("Vuốt sang phải để nghe lại. Hoặc vuốt sang trái và nói điều bạn muốn", TextToSpeech.QUEUE_ADD, null);
                } else if (x1 > x2) {
                    startVoiceInput();
                }
                break;
        }
        return false;
    }

    private void textRecognizer() {
        Toast.makeText(MainActivity2.this, "Chạm vào màn hình để nghe", Toast.LENGTH_SHORT).show();
        textToSpeech.speak("Chạm vào màn hình để chụp ảnh văn bản và nghe", TextToSpeech.QUEUE_FLUSH, null);
        textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                .setRequestedPreviewSize(1280, 1024)
                .setAutoFocusEnabled(true)
                .build();
        surfaceView = findViewById(R.id.surfaceView);
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> textToSpeech.speak("chạm vào màn hình để đọc", TextToSpeech.QUEUE_FLUSH, null), 5000);

        Context context = this;
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    cameraSource.start(surfaceView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });
    }

    private void capture() {
        textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<TextBlock> detections) {
                SparseArray<TextBlock> sparseArray = detections.getDetectedItems();
                StringBuilder stringBuilder = new StringBuilder();

                for (int i = 0; i < sparseArray.size(); ++i) {
                    TextBlock textBlock = sparseArray.valueAt(i);
                    if (textBlock != null && textBlock.getValue() != null) {
                        stringBuilder.append(textBlock.getValue()).append(" ");
                    }
                }

                final String stringText = stringBuilder.toString();

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> {
                    stringResult = stringText;
                    resultObtained();
                });
            }
        });
    }

    private void resultObtained() {
        setContentView(R.layout.activity_main2);
        textView = findViewById(R.id.textView);
        textView.setText(stringResult);
        textToSpeech.speak(stringResult, TextToSpeech.QUEUE_FLUSH, null);
        textToSpeech.speak("Vuốt sang phải để nghe lại. Hoặc vuốt sang trái và nói điều bạn muốn", TextToSpeech.QUEUE_ADD, null);
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString());
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
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT:
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    mVoiceInputTv.setText(result.get(0));
                }
                if (mVoiceInputTv.getText().toString().equalsIgnoreCase("đọc")) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity2.class);
                    startActivity(intent);
                }
                if (mVoiceInputTv.getText().toString().equalsIgnoreCase("ngày và giờ")) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity4.class);
                    startActivity(intent);
                }
                if (mVoiceInputTv.getText().toString().equalsIgnoreCase("phần trăm pin")) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity6.class);
                    startActivity(intent);
                    mVoiceInputTv.setText(null);
                }
                if (mVoiceInputTv.getText().toString().equalsIgnoreCase("vị trí")) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity8.class);
                    startActivity(intent);
                    mVoiceInputTv.setText(null);
                }
                if (mVoiceInputTv.getText().toString().equalsIgnoreCase("thời tiết")) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity5.class);
                    startActivity(intent);
                    mVoiceInputTv.setText(null);
                } else {
                    textToSpeech.speak("Không hiểu, hãy vuốt sang phải và nói lại", TextToSpeech.QUEUE_FLUSH, null);
                }
                if (mVoiceInputTv.getText().toString().equalsIgnoreCase("máy tính")) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity3.class);
                    startActivity(intent);
                    mVoiceInputTv.setText(null);
                } else if (mVoiceInputTv.getText().toString().equalsIgnoreCase("thoát")) {
                    finish();
                } else {
                    textToSpeech.speak("Không hiểu, hãy vuốt sang phải và nói lại", TextToSpeech.QUEUE_FLUSH, null);
                }

                if (mVoiceInputTv.getText().toString().equalsIgnoreCase("có")) {
                    setContentView(R.layout.surface);
                    surfaceView = findViewById(R.id.surfaceView);
                    surfaceView.setOnClickListener(v -> capture());
                    textRecognizer();
                    mVoiceInputTv.setText(null);
                } else if (mVoiceInputTv.getText().toString().equalsIgnoreCase("không")) {
                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(() -> textToSpeech.speak("Bạn đang ở trong menu chính. Hãy vuốt sang trái và nói điều bạn muốn", TextToSpeech.QUEUE_FLUSH, null), 1000);

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
                break;
        }
    }

    public boolean onKeyDown(int keyCode, @Nullable KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            textToSpeech.speak("Bạn đang ở trong menu chính. Hãy vuốt sang trái và nói điều bạn muốn", TextToSpeech.QUEUE_FLUSH, null);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> textToSpeech.speak("Bạn đang ở trong menu chính. Hãy vuốt sang trái và nói điều bạn muốn", TextToSpeech.QUEUE_FLUSH, null), 1000);
        }
        return true;
    }

    public void onPause() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
        super.onPause();
    }
}
