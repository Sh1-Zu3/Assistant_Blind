package com.example.software2.ocrhy;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainActivity10 extends AppCompatActivity {

    float x1, x2;
    int i = 0;
    TextView textView;
    public TextToSpeech textToSpeech;
    private List<String[]> articles;

    private static final int CLICK_INTERVAL = 1000;
    private static final int NUM_CLICKS_TO_EXIT = 3; //
    private int numClicks = 0; //
    private long lastClickTime = 0;

    private boolean isdone = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main10);

        textView = findViewById(R.id.textView);

        String[] topics = {
                "du lịch","travel","travels","tourism","tourisms",
                "thế giới","world","worlds",
                "kinh doanh","business","businesses",
                "khoa học","scientific","scientifics",
                "giải trí","entertainment","entertainments",
                "thể thao","sport","sports",
                "pháp luật","law","laws","legal","legals",
                "giáo dục","education","educations","educational",
                "sức khỏe","health","healths",
                "đời sống","life","lifes",
        };
        Map<String, String> keywordMap = new HashMap<>();

        // Thêm các từ khóa và topic tương ứng vào bản đồ
        keywordMap.put("du lịch", "du-lich");
        keywordMap.put("travel", "du-lich");
        keywordMap.put("travels", "du-lich");
        keywordMap.put("tourism", "du-lich");
        keywordMap.put("tourisms", "du-lich");

        keywordMap.put("thế giới", "the-gioi");
        keywordMap.put("world", "the-gioi");
        keywordMap.put("worlds", "the-gioi");

        keywordMap.put("kinh doanh", "kinh-doanh");
        keywordMap.put("business", "kinh-doanh");
        keywordMap.put("businesses", "kinh-doanh");

        keywordMap.put("khoa học", "khoa-hoc");
        keywordMap.put("scientific", "khoa-hoc");
        keywordMap.put("scientifics", "khoa-hoc");

        keywordMap.put("giải trí", "giai-tri");
        keywordMap.put("entertainment", "giai-tri");
        keywordMap.put("entertainments", "giai-tri");

        keywordMap.put("thể thao", "the-thao");
        keywordMap.put("sport", "the-thao");
        keywordMap.put("sports", "the-thao");

        keywordMap.put("pháp luật", "phap-luat");
        keywordMap.put("law", "phap-luat");
        keywordMap.put("laws", "phap-luat");
        keywordMap.put("legal", "phap-luat");
        keywordMap.put("legals", "phap-luat");

        keywordMap.put("giáo dục", "giao-duc");
        keywordMap.put("education", "giao-duc");
        keywordMap.put("educations", "giao-duc");
        keywordMap.put("educational", "giao-duc");

        keywordMap.put("sức khỏe", "suc-khoe");
        keywordMap.put("health", "suc-khoe");
        keywordMap.put("healths", "suc-khoe");

        keywordMap.put("đời sống", "doi-song");
        keywordMap.put("life", "doi-song");
        keywordMap.put("lifes", "doi-song");


        Intent intent = getIntent();
        String topic_vi = intent.getStringExtra("topic-vi");
        String topic_en = intent.getStringExtra("topic-en");
        String selectedTopic = "scientific";//set tieng anh chu tieng viet no bi loi
        //String normalizedTopic = keywordMap.get(selectedTopic);
        //Log.d("CC", normalizedTopic);
        if (topic_vi != null && topic_en != null) {
            //Toast.makeText(MainActivity10.this, "tiếng việt:"+topic_vi, Toast.LENGTH_SHORT).show();
            //Toast.makeText(MainActivity10.this, "tiếng anh:"+topic_en, Toast.LENGTH_SHORT).show();
            for (String keyword : topics) {
                if ((topic_vi.contains(keyword)) || topic_en.contains(keyword)) {
                    selectedTopic = keyword;
                    //Toast.makeText(MainActivity10.this, "CÓ NÈ", Toast.LENGTH_SHORT).show();
                    //Toast.makeText(MainActivity10.this, selectedTopic, Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        }
        String normalizedTopic = keywordMap.get(selectedTopic);
        Log.d("cccc", normalizedTopic);
        //Toast.makeText(MainActivity10.this, normalizedTopic, Toast.LENGTH_SHORT).show();
        new PythonTask().execute(normalizedTopic, "1");
    }

    private class PythonTask extends AsyncTask<String, Void, List<PyObject>> {
        @Override
        protected List<PyObject> doInBackground(String... params) {
            List<PyObject> result = new ArrayList<>();
            String articleType = params[0];
            int totalPages = 1;

            if (!Python.isStarted()) {
                Python.start(new AndroidPlatform(MainActivity10.this));
            }
            Python py = Python.getInstance();
            PyObject pyobj = py.getModule("script");
            PyObject obj = pyobj.callAttr("crawl_type", articleType, totalPages);
            isdone=true;
            return obj.asList();
        }

        @Override
        protected void onPostExecute(List<PyObject> pythonList) {
            articles = new ArrayList<>();

            for (PyObject articleObj : pythonList) {
                String title = articleObj.get("title").toString();
                String description = articleObj.get("description").toString();
                String paragraphs = articleObj.get("paragraphs").toString();


                articles.add(new String[]{title, description, paragraphs});
            }

            //e
            displayArticle(i);

            textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        int result = textToSpeech.setLanguage(Locale.getDefault());
                        if (result == TextToSpeech.LANG_MISSING_DATA ||
                                result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Log.e("TextToSpeech", "Language not supported");
                        } else {
                            // TextToSpeech is ready
                            speakArticleChunks(i);
                            i += 1;
                        }
                    } else {
                        Log.e("TextToSpeech", "Initialization failed");
                    }
                }
            });

        }
    }


    private void speakArticleChunks(final int articleIndex) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String[] titleAndDescription = {articles.get(articleIndex)[0], articles.get(articleIndex)[1]};
                for (String text : titleAndDescription) {
                    textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                    // Wait for the speech to finish
                    while (textToSpeech.isSpeaking()) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                int MAX_WORDS = 20;
                String[] words = articles.get(articleIndex)[2].split("\\s+");

                for (int start = 0; start < words.length; start += MAX_WORDS) {
                    int end = Math.min(start + MAX_WORDS, words.length);
                    String[] chunkArray = Arrays.copyOfRange(words, start, end);
                    String chunk = TextUtils.join(" ", chunkArray);


                    textToSpeech.speak(chunk, TextToSpeech.QUEUE_ADD, null);


                    while (textToSpeech.isSpeaking()) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }




    private void displayArticle(int index) {
        String resultString = "Chủ đề: " + articles.get(index)[0] + "\n"
                + "Mô tả: " + articles.get(index)[1] + "\n"
                + "Nội dung: " + articles.get(index)[2] + "\n";
        textView.setText(resultString);
    }

    @Override
    public boolean onTouchEvent(MotionEvent touchEvent) {
        switch (touchEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = touchEvent.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = touchEvent.getX();
                if (isdone) {
                    if (x1 < x2) {
                        numClicks=0;

                        i = Math.max(i - 2, 0);
                        speakArticleChunks(i);
                        displayArticle(i);
                        i = i + 1;
                    } else if (x1 > x2) {
                        numClicks=0;
                        speakArticleChunks(i);
                        displayArticle(i);
                        i = i + 1;

                    }
                }
        }

        if (touchEvent.getAction() == MotionEvent.ACTION_DOWN) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime < CLICK_INTERVAL) {
                numClicks++;
                if (numClicks >= NUM_CLICKS_TO_EXIT) {
                    textToSpeech.stop();
                    textToSpeech.shutdown();
                    //out
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
    protected void onDestroy() {
        // Dừng TextToSpeech khi Activity bị hủy
        textToSpeech.stop();
        textToSpeech.shutdown();
        super.onDestroy();
    }

}
