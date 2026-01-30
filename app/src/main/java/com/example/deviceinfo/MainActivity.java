package com.example.deviceinfo;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView systemTV = findViewById(R.id.system_info);
        TextView hardwareTV = findViewById(R.id.hardware_info);

        // جلب معلومات النظام
        String sysInfo = "الشركة: " + android.os.Build.MANUFACTURER + "\n" +
                         "الموديل: " + android.os.Build.MODEL + "\n" +
                         "إصدار الأندرويد: " + android.os.Build.VERSION.RELEASE + "\n" +
                         "المعالج: " + android.os.Build.BOARD;

        // جلب البطارية والرام
        Intent batteryStatus = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : 0;

        ActivityManager actManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        actManager.getMemoryInfo(memInfo);
        long totalRam = memInfo.totalMem / (1024 * 1024);

        String hardInfo = "نسبة البطارية: " + level + "%\n" +
                          "الرام الكلية: " + totalRam + " MB\n" +
                          "النواة: " + System.getProperty("os.arch");

        systemTV.setText(sysInfo);
        hardwareTV.setText(hardInfo);
    }
}
