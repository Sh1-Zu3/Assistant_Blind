package com.example.software2.ocrhy;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity12 extends AppCompatActivity {

    private float x1, x2;
    private int currentIndex = 0; // Index starts from 0
    private static TextToSpeech textToSpeech;

    private static final int CLICK_INTERVAL = 1000;
    private static final int NUM_CLICKS_TO_EXIT = 3;
    private int numClicks = 0;
    private long lastClickTime = 0;
    private MediaPlayer mediaPlayer;
    private ArrayList<String> songUrls = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main12);
        textToSpeech = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.getDefault());
                textToSpeech.setSpeechRate(1f);
            }
        });

        // Load song URLs from Firebase Storage
        loadSongUrls();
    }

    // Method to load song URLs from Firebase Storage
    private void loadSongUrls() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("songs");

        // List all items under "songs" directory
        storageRef.listAll()
                .addOnSuccessListener(listResult -> {
                    // Loop through each item to get the download URL
                    for (StorageReference item : listResult.getItems()) {
                        item.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                            // Add the download URL to the list
                            songUrls.add(downloadUrl.toString());
                            // If it's the last URL, start playing the first song
                            if (songUrls.size() == listResult.getItems().size()) {
                                playSong(songUrls.get(currentIndex));
                            }
                        }).addOnFailureListener(exception -> {
                            // Handle any errors
                            Toast.makeText(MainActivity12.this, "Error loading song URLs", Toast.LENGTH_SHORT).show();
                        });
                    }
                })
                .addOnFailureListener(exception -> {
                    // Handle any errors
                    Toast.makeText(MainActivity12.this, "Error loading song URLs", Toast.LENGTH_SHORT).show();
                });
    }

    // Method to play a song from a given URL
    private void playSong(String url) {
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(url);
            mediaPlayer.setOnPreparedListener(mp -> mp.start());
            mediaPlayer.setOnCompletionListener(mp -> {
                currentIndex = (currentIndex + 1) % songUrls.size(); // Move to the next song
                playSong(songUrls.get(currentIndex));
            });
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Toast.makeText(this, "Failed to play song", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean onTouchEvent(MotionEvent touchEvent) {
        switch (touchEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = touchEvent.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = touchEvent.getX();
                if (x1 < x2) {
                    // Swipe right
                    numClicks=1;
                    currentIndex = (currentIndex + 1) % songUrls.size();
                    playSong(songUrls.get(currentIndex) );
                } else if (x1 > x2) {
                    // Swipe left
                    numClicks=1;
                    currentIndex = (currentIndex - 1 + songUrls.size()) % songUrls.size();
                    playSong(songUrls.get(currentIndex));
                }
                break;
        }
        if (touchEvent.getAction() == MotionEvent.ACTION_DOWN) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime < CLICK_INTERVAL) {
                numClicks++;
                if (numClicks >= NUM_CLICKS_TO_EXIT) {
                    // Nếu số lần nhấn đạt đến NUM_CLICKS_TO_EXIT, thoát ứng dụng
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                    }

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
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
