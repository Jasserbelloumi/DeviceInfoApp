package com.example.deviceinfo;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // تهيئة الإعلانات
        MobileAds.initialize(this, initializationStatus -> {});

        // تحميل الإعلان في الـ View
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // الكود السابق لعرض المعلومات
        TextView systemTV = findViewById(R.id.system_info);
        String sysInfo = "الشركة: " + android.os.Build.MANUFACTURER + "\n" +
                         "الموديل: " + android.os.Build.MODEL + "\n" +
                         "إصدار الأندرويد: " + android.os.Build.VERSION.RELEASE;
        systemTV.setText(sysInfo);
    }
}
