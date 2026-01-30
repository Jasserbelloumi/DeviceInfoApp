package com.example.deviceinfo;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView infoTextView = findViewById(R.id.infoTextView);
        
        // جلب معلومات البطارية
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);
        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;
        float batteryPct = (level / (float)scale) * 100;

        // جلب معلومات الرام (RAM)
        ActivityManager actManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        actManager.getMemoryInfo(memInfo);
        long totalMemory = memInfo.totalMem / (1024 * 1024); // تحويل لميجابايت

        String info = "--- معلومات النظام ---\n" +
                "الشركة: " + Build.MANUFACTURER + "\n" +
                "الموديل: " + Build.MODEL + "\n" +
                "إصدار الأندرويد: " + Build.VERSION.RELEASE + "\n" +
                "المعالج (Board): " + Build.BOARD + "\n\n" +
                "--- حالة العتاد ---\n" +
                "البطارية: " + (int)batteryPct + "%\n" +
                "الرام الكلية: " + totalMemory + " MB\n" +
                "النواة: " + System.getProperty("os.arch") + "\n\n" +
                "--- هويّة الجهاز ---\n" +
                "المنتج: " + Build.PRODUCT + "\n" +
                "الهاردوير: " + Build.HARDWARE + "\n" +
                "رقم البناء: " + Build.ID;

        infoTextView.setText(info);
    }
}
