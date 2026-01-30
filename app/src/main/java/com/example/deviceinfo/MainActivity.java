package com.example.deviceinfo;

import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends android.app.Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView infoTextView = findViewById(R.id.infoTextView);
        
        String info = "معلومات الجهاز:\n\n" +
                "Model: " + Build.MODEL + "\n" +
                "Manufacturer: " + Build.MANUFACTURER + "\n" +
                "Brand: " + Build.BRAND + "\n" +
                "Device: " + Build.DEVICE + "\n" +
                "Android Version: " + Build.VERSION.RELEASE + "\n" +
                "SDK Level: " + Build.VERSION.SDK_INT + "\n" +
                "Hardware: " + Build.HARDWARE + "\n" +
                "Board: " + Build.BOARD + "\n" +
                "Display: " + Build.DISPLAY;

        infoTextView.setText(info);
    }
}
