package com.example.software2.ocrhy;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class MainActivity5 extends AppCompatActivity {

    EditText cityInput;
    final int VOICE_CODE = 100;
    TextToSpeech textToSpeech;
    Button cityBtn;
    ImageView voiceBtn;
    TextView cityTextView, timeTextView, dateTextView, weatherStatusText, temperatureText;
    ImageView weatherStatusImg;
    String currentTime, dateOutput;
    String cityEntered;

    float x1, x2, y1, y2;
    private static final int CLICK_INTERVAL = 1000; // Thời gian giữa các lần nhấn (ms)
    private static final int NUM_CLICKS_TO_EXIT = 3; // Số lần nhấn để thoát ứng dụng
    private int numClicks = 0; // Biến đếm số lần nhấn
    private long lastClickTime = 0; // Thời gian của lần nhấn cuối cùng
    private String tmp="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main5);

        cityTextView = findViewById(R.id.city_text_view);
        cityTextView.setText("");
        timeTextView = findViewById(R.id.time_text_view);
        timeTextView.setText("");
        dateTextView = findViewById(R.id.date_text_view);
        dateTextView.setText("");
        weatherStatusImg = findViewById(R.id.weather_img);
        temperatureText = findViewById(R.id.temperature_text);
        temperatureText.setText("");
        weatherStatusText = findViewById(R.id.weather_status_text);
        weatherStatusText.setText("");
        cityInput = findViewById(R.id.city_txt_input);
        cityBtn = findViewById(R.id.city_btn);
        cityInput = findViewById(R.id.city_txt_input);
        voiceBtn = findViewById(R.id.weather_img);
        textToSpeech = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.getDefault());
                Toast.makeText(MainActivity5.this, "vuốt sang trái và nói tên thành phố,vuốt sang phải để nghe lại", Toast.LENGTH_SHORT).show();
                textToSpeech.speak("Hãy nói tên thành phố.", TextToSpeech.QUEUE_FLUSH, null);
                textToSpeech.setSpeechRate(1f);
            }
            new Handler(Looper.getMainLooper()).postDelayed(() -> voice_to_text(), 2000);
        });

        //voiceBtn.setOnClickListener(view -> voice_to_text());
    }

    private void voice_to_text() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Hãy nói điều gì đó!!");
        try {
            startActivityForResult(intent, VOICE_CODE);
        } catch (ActivityNotFoundException e) {
            // Xử lý nếu thiết bị không hỗ trợ nhận giọng nói
        }
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
                    textToSpeech.speak(tmp, TextToSpeech.QUEUE_FLUSH, null);
                }
                if (x1 > x2) {
                    numClicks=0;
                    voice_to_text();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case VOICE_CODE: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    cityInput.setText(result.get(0));
                    handleVoiceInput();
                }
            }
        }
    }

    private void handleVoiceInput() {
        // Xử lý logic dựa trên giọng nói từ người dùng
        cityEntered = cityInput.getText().toString().trim();
        // Xử lý khi tên thành phố được nói ra
        api_url(cityEntered);
    }

    public void api_url(String citySearch) {
        // Tạo URL dựa trên tên thành phố
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + citySearch + "&appid=b761b4cfe64507fdd7579ab7daf29996&units=metric";

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject mainObject = response.getJSONObject("main");
                    JSONArray weatherArray = response.getJSONArray("weather");
                    JSONObject description = weatherArray.getJSONObject(0);
                    JSONObject icon = weatherArray.getJSONObject(0);
                    String iconId = icon.getString("icon");
                    String temp = (Math.round(mainObject.getDouble("temp"))) + "°C";
                    String desc = description.getString("main");
                    updateUI(temp, desc);
                    SetIcon(iconId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    Log.e("volley", Objects.requireNonNull(error.getMessage()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    public void updateUI(String temperature, String description) {
        cityEntered = cityInput.getText().toString().replaceAll("location", "");
        cityEntered = cityInput.getText().toString().replaceAll("calculator", "");
        cityEntered = cityInput.getText().toString().replaceAll("read", "");

        temperatureText.setText(temperature);
        temperatureText.getText().toString();
        cityInput.setText(cityEntered);
        cityInput.getText().toString();

        int temp = Integer.parseInt(temperature.replaceAll("°C", ""));
        if (temp < 27) {
            // Nhiệt độ dưới 27°C, có khả năng mưa
            description = ". Có khả năng mưa.";
        } else {
            // Nhiệt độ từ 27°C trở lên, không có khả năng mưa
            description = ". Không có khả năng mưa.";
        }

        textToSpeech.speak("Nhiệt độ ở thành phố " + cityEntered.replaceAll("read", "") + "là " + temperature + description, TextToSpeech.QUEUE_FLUSH, null);
        tmp = "Nhiệt độ ở thành phố " + cityEntered.replaceAll("read", "") + "là " + temperature + description;
        textToSpeech.speak("vuốt sang trái và nói tên thành phố,vuốt sang phải để nghe lại, nhấn ba lần vào màn hình để về menu chính", TextToSpeech.QUEUE_ADD, null);

        weatherStatusText.setText(description);
        timeTextView.setText(currentTime);
        dateTextView.setText(dateOutput);
        cityTextView.setText(cityEntered);
    }

    public void SetIcon(String id) {
        switch (id) {
            case "01d":
                weatherStatusImg.setImageResource(R.drawable.clear_skyd);
                break;
            case "01n":
                weatherStatusImg.setImageResource(R.drawable.clear_skyn);
                break;
            case "02d":
                weatherStatusImg.setImageResource(R.drawable.few_cloudsd);
                break;
            case "02n":
                weatherStatusImg.setImageResource(R.drawable.few_cloudn);
                break;
            case "03d":
                weatherStatusImg.setImageResource(R.drawable.few_cloudsd);
                break;
            case "03n":
                weatherStatusImg.setImageResource(R.drawable.few_cloudn);
                break;
            case "04d":
                weatherStatusImg.setImageResource(R.drawable.few_cloudsd);
                break;
            case "04n":
                weatherStatusImg.setImageResource(R.drawable.few_cloudn);
                break;
            case "09d":
                weatherStatusImg.setImageResource(R.drawable.rain);
                break;
            case "09n":
                weatherStatusImg.setImageResource(R.drawable.rain);
                break;
            case "10d":
                weatherStatusImg.setImageResource(R.drawable.rain);
                break;
            case "10n":
                weatherStatusImg.setImageResource(R.drawable.rain);
                break;
            case "11d":
                weatherStatusImg.setImageResource(R.drawable.storm);
                break;
            case "11n":
                weatherStatusImg.setImageResource(R.drawable.storm);
                break;
            case "13d":
                weatherStatusImg.setImageResource(R.drawable.snow);
                break;
            case "13n":
                weatherStatusImg.setImageResource(R.drawable.snow);
                break;
            case "50d":
                weatherStatusImg.setImageResource(R.drawable.mist);
                break;
            case "50n":
                weatherStatusImg.setImageResource(R.drawable.mistn);
                break;
        }
    }


    public void onDestroy() {
        if (cityInput.getText().toString().equalsIgnoreCase("exit")) {
            System.exit(0);
            super.onDestroy();
        }
    }

    public void onPause() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
        super.onPause();
    }
}
