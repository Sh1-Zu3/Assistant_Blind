package com.example.software2.ocrhy;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.Nullable;

public class GetAllData extends IntentService {
    private static final String IDENTIFIER = "GetAddressIntentService";
    private ResultReceiver addressResultReceiver;
    private TextToSpeech textToSpeech;

    public GetAllData() {
        super(IDENTIFIER);
    }
    //xu ly ngoai le dâta
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String msg;
        addressResultReceiver = Objects.requireNonNull(intent).getParcelableExtra("add_receiver");
        if (addressResultReceiver == null) {
            Log.e("GetAddressIntentService", "Không nhận được giọng nói, không thể xử lý yêu cầu tiếp theo");
            return;
        }


        Location location = intent.getParcelableExtra("add_location");
        if (location == null) {
            msg = "Không có vị trí, không thể tiếp tục mà không có vị trí";
            textToSpeech.speak(msg, TextToSpeech.QUEUE_FLUSH, null);
            sendResultsToReceiver(0, msg);
            return;
        }


        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (Exception ioException) {
            Log.e("", "Lỗi khi lấy địa chỉ cho vị trí");
        }
        if (addresses == null || addresses.size() == 0) {
            msg = "Không tìm thấy địa chỉ cho vị trí";
            sendResultsToReceiver(1, msg);
        } else {
            Address address = addresses.get(0);
            String addressDetails = address.getFeatureName() + "." + "\n" +
                    "Địa phương là, " + address.getLocality() + "." + "\n" +
                    "Thành phố là, " + address.getSubAdminArea() + "." + "\n" +
                    "Tỉnh/Thành phố là, " + address.getAdminArea() + "." + "\n" +
                    "Quốc gia là, " + address.getCountryName() + "." + "\n";
            sendResultsToReceiver(2, addressDetails);
        }
    }

    private void sendResultsToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString("address_result", message);
        addressResultReceiver.send(resultCode, bundle);
    }
}
