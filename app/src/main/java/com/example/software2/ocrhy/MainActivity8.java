package com.example.software2.ocrhy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity8 extends AppCompatActivity {
    float x1, x2;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;
    private LocationAddressResultReceiver addressResultReceiver;
    private TextView currentAddTv;
    private Location currentLocation;
    private LocationCallback locationCallback;
    private TextToSpeech textToSpeech;
    private static final int CLICK_INTERVAL = 1000;
    private static final int NUM_CLICKS_TO_EXIT = 3;
    private int numClicks = 0;
    private long lastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main8);
        addressResultReceiver = new LocationAddressResultReceiver(new Handler());
        currentAddTv = findViewById(R.id.textView);
        textToSpeech = new TextToSpeech(MainActivity8.this, status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.getDefault());
                textToSpeech.setSpeechRate(1f);
                textToSpeech.speak("Vuốt sang phải để lấy vị trí hiện tại và nhấn 3 lần vào màn hình để trở về menu chính", TextToSpeech.QUEUE_FLUSH, null);
            }
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                currentLocation = locationResult.getLocations().get(0);
                getAddress();
            }
        };
        startLocationUpdates();
    }

    @SuppressWarnings("MissingPermission")
    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new
                            String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(2000);
            locationRequest.setFastestInterval(1000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void getAddress() {
        if (!Geocoder.isPresent()) {
            Toast.makeText(MainActivity8.this, "Không thể tìm địa chỉ hiện tại, ",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentLocation == null) {
            Toast.makeText(MainActivity8.this, "Không có vị trí hiện tại.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Geocoder geocoder = new Geocoder(MainActivity8.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                StringBuilder addressInfoBuilder = new StringBuilder();

                String street = address.getThoroughfare(); // Tên đường
                if (street != null) {
                    addressInfoBuilder.append("Đường ").append(street).append(", ");
                }

                String ward = address.getSubLocality(); // Xã
                if (ward != null) {
                    addressInfoBuilder.append(ward).append(", ");
                }

                String district = address.getSubAdminArea(); // Huyện
                if (district != null) {
                    addressInfoBuilder.append(district).append(", ");
                }

                String city = address.getAdminArea(); // Tỉnh
                if (city != null) {
                    addressInfoBuilder.append(city);
                }

                String addressInfo = addressInfoBuilder.toString();

                if (!addressInfo.isEmpty()) {
                    // Hiển thị thông tin địa chỉ lên TextView
                    currentAddTv.setText(addressInfo);

                    // Đọc thông tin địa chỉ bằng TextToSpeech
                    textToSpeech = new TextToSpeech(MainActivity8.this, status -> {
                        if (status != TextToSpeech.ERROR) {
                            textToSpeech.setLanguage(Locale.getDefault());
                            textToSpeech.setSpeechRate(1f);
                            textToSpeech.speak(addressInfo, TextToSpeech.QUEUE_FLUSH, null);
                        }
                    });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull
    int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Quyền vị trí không được cấp, " +
                        "khởi động lại ứng dụng nếu bạn muốn sử dụng tính năng này", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class LocationAddressResultReceiver extends ResultReceiver {
        LocationAddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == 0) {
                Log.d("Address", "Vị trí null, đang thử lại");
                getAddress();
            }
            if (resultCode == 1) {
                Toast.makeText(MainActivity8.this, "Không tìm thấy địa chỉ, ", Toast.LENGTH_SHORT).show();
            }
            String currentAdd = resultData.getString("address_result");
            showResults(currentAdd);
        }
    }

    private void showResults(String currentAdd) {
        currentAddTv.setText(currentAdd);
    }

    public boolean onTouchEvent(MotionEvent touchEvent) {
        switch (touchEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = touchEvent.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = touchEvent.getX();
                if (x1 < x2) {
                    numClicks=0;
                    String data = currentAddTv.getText().toString();
                    if (data.isEmpty()) {
                        textToSpeech.speak("Vui lòng bật vị trí", TextToSpeech.QUEUE_FLUSH, null);
                    } else {
                        textToSpeech.speak("Vị trí hiện tại của bạn là " + data, TextToSpeech.QUEUE_FLUSH, null);
                        textToSpeech.speak("Vuốt sang phải để nghe lại hoặc nhấn 3 lần vào màn hình để trở về menu chính", TextToSpeech.QUEUE_ADD, null);
                    }
                }
                if (x1 > x2) {
                    numClicks=0;
                }
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
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
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
