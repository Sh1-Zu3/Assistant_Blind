package com.example.software2.ocrhy;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity3 extends AppCompatActivity {
    private Button button;
    public TextView txtScreen;
    public Button button2;
    public TextToSpeech textToSpeech;
    public TextView txtInput;
    private boolean lastNumeric;

    // Biểu thức hiện tại có lỗi hay không
    private boolean stateError;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private static final int CLICK_INTERVAL = 1000;
    private int numClicks = 0; // Biến đếm số lần nhấn
    private long lastClickTime = 0; // Thời gian của lần nhấn cuối cùng
    private float startX,startY;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        setNumericOnClickListener();
        setOperatorOnClickListener();
        txtScreen = findViewById(R.id.txtScreen);
        txtInput = findViewById(R.id.txtInput);

        textToSpeech = new TextToSpeech(getApplicationContext(), status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.getDefault());
                textToSpeech.setSpeechRate(1f);
                Toast.makeText(MainActivity3.this, "vuốt sang trái và nói phép tính. Nhấn 3 lần vào màn hình để quay lại menu chính", Toast.LENGTH_SHORT).show();
                textToSpeech.speak("vuốt sang trái và nói phép tính. Nhấn 3 lần vào màn hình để quay lại menu chính", TextToSpeech.QUEUE_FLUSH, null);
            }
        });
    }

    private void setNumericOnClickListener() {
        new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button button = (Button) v;
                if (stateError) {
                    txtScreen.setText(button.getText());
                    stateError = false;
                } else {
                    txtScreen.append(button.getText());
                }
                lastNumeric = true;
            }
        };
    }

    private void setOperatorOnClickListener() {
        new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastNumeric && !stateError) {
                    Button button = (Button) v;
                    txtScreen.append(button.getText());
                    lastNumeric = false;
                }
            }
        };

        findViewById(R.id.btnClear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtScreen.setText("");
                txtInput.setText("");
                lastNumeric = false;
                stateError = false;
            }
        });


    }

    @Override
    public boolean onTouchEvent(MotionEvent touchEvent) {
        switch (touchEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                startX = touchEvent.getX();
                startY = touchEvent.getY();
                break;
            case MotionEvent.ACTION_UP:
                float endX = touchEvent.getX();
                float endY = touchEvent.getY();

                if (startX > endX && Math.abs(startX - endX) > Math.abs(startY - endY)) {
                    numClicks=0;
                    promptSpeechInput();
                } else {
                    long now = System.currentTimeMillis();
                    if (now - lastClickTime < CLICK_INTERVAL) {
                        numClicks++;
                    } else {
                        numClicks = 1;
                    }
                    lastClickTime = now;

                    // Nếu nhấn 3 lần, chuyển về MainActivity
                    if (numClicks >= 3) {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        numClicks = 0;
                    }
                }
                lastNumeric = true;
                break;
        }
        return super.onTouchEvent(touchEvent);
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), getString(R.string.speech_not_supported), Toast.LENGTH_SHORT).show();
        }
    }

    private void onEqual() {
        if (lastNumeric && !stateError) {
            final String inputNumber = txtInput.getText().toString();
            txtScreen.setText(inputNumber);

            Expression expression;
            try {
                expression = new ExpressionBuilder(inputNumber).build();
                double result = expression.evaluate();
                txtScreen.setText(Double.toString(result).replaceAll("\\.0*$", ""));
                Toast.makeText(MainActivity3.this, "Kết quả là", Toast.LENGTH_SHORT).show();
                textToSpeech.speak("Kết quả là " + txtScreen.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                textToSpeech.speak("vuốt sang trái và nói điều bạn muốn", TextToSpeech.QUEUE_ADD,null);
                textToSpeech.setSpeechRate(1f);
            } catch (Exception e) {
                txtScreen.setText("Lỗi, chạm vào màn hình và nói lại");
                textToSpeech.speak("Lỗi, chạm vào màn hình và nói lại", TextToSpeech.QUEUE_FLUSH, null);
                onPause();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT:
                if (resultCode == RESULT_OK && null != data) {
                    final ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String change = result.toString();
                    txtInput.setText(result.get(0));

                    if(txtInput.getText().toString().equalsIgnoreCase("thoát")) {
                        finishAffinity();
                        super.onPause();
                    }
                    else {
                        textToSpeech.speak("Không hiểu, vuốt sang trái và nói lại", TextToSpeech.QUEUE_FLUSH, null);
                    }

                    // Chuyển đổi tiếng Anh sang toán tử và số
                    change = change.replace("x", "*");
                    change = change.replace("X", "*");
                    change = change.replace("add", "+");
                    change = change.replace("sub", "-");
                    change = change.replace("to", "2");
                    change = change.replace(" plus ", "+");
                    change = change.replace("two", "2");
                    change = change.replace(" minus ", "-");
                    change = change.replace(" times ", "*");
                    change = change.replace(" into ", "*");
                    change = change.replace(" in2 ", "*");
                    change = change.replace(" multiply by ", "*");
                    change = change.replace(" divide by ", "/");
                    change = change.replace("divide", "/");
                    change = change.replace("equal", "=");
                    change = change.replace("equals", "=");

                    if (change.contains("=")) {
                        change = change.replace("=", "");
                        txtInput.setText(change);
                        onEqual();
                    } else {
                        txtInput.setText(change);
                        onEqual();
                    }
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (txtInput.getText().toString().equals("exit")){
            finish();
        }
        super.onDestroy();
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
    @Override
    protected void onPause() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
        super.onPause();
    }
}
