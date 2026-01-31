package com.example.deviceinfo;

import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.ads.*;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<String> imageUrls = new ArrayList<>();
    private InterstitialAd mInterstitialAd;
    private WallpaperAdapter adapter;
    
    // إعلاناتك الحقيقية (تأكد من مطابقة الـ ID)
    private final String BANNER_ID = "ca-app-pub-7500537470112334/4696609974";
    // ملاحظة: ضع هنا كود الـ Interstitial الخاص بك عندما يكون جاهزاً
    private final String INTER_ID = "ca-app-pub-3940256099942544/1033173712"; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // تشغيل الإعلانات
        MobileAds.initialize(this, status -> {});
        loadBanner();
        loadInterstitial();

        // إعداد القائمة بتصميم شبكي (2 أعمدة)
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, 1));
        adapter = new WallpaperAdapter(imageUrls);
        recyclerView.setAdapter(adapter);
        
        // صور البداية
        loadPhotos("Dark Amoled Wallpaper");

        // محرك البحث الذكي
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // عرض إعلان عند كل عملية بحث لزيادة الأرباح
                showAdsAndRun(() -> loadPhotos(query));
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) { return false; }
        });
    }

    private void loadBanner() {
        AdView adView = findViewById(R.id.adView);
        adView.setAdUnitId(BANNER_ID);
        adView.loadAd(new AdRequest.Builder().build());
    }

    private void loadInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, INTER_ID, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                mInterstitialAd = interstitialAd;
            }
        });
    }

    private void showAdsAndRun(Runnable action) {
        if (mInterstitialAd != null) {
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    action.run();
                    loadInterstitial(); // تجهيز الإعلان التالي
                }
            });
            mInterstitialAd.show(this);
        } else {
            action.run();
            loadInterstitial();
        }
    }

    private void loadPhotos(String query) {
        imageUrls.clear();
        Random r = new Random();
        // جلب 60 صورة جديدة تماماً في كل مرة لضمان عدم التكرار
        for (int i = 0; i < 60; i++) {
            imageUrls.add("https://loremflickr.com/600/1000/" + query.trim().replace(" ","") + "?lock=" + r.nextInt(100000));
        }
        runOnUiThread(() -> {
            adapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(0);
        });
        Toast.makeText(this, "تم تحديث الصور ✨", Toast.LENGTH_SHORT).show();
    }

    private void setWallpaper(String url) {
        Toast.makeText(this, "جاري تعيين الخلفية... ⏳", Toast.LENGTH_SHORT).show();
        Glide.with(this).asBitmap().load(url).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                try {
                    WallpaperManager.getInstance(MainActivity.this).setBitmap(resource);
                    Toast.makeText(MainActivity.this, "تم التعيين بنجاح! ✅", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "فشل في التعيين", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class WallpaperAdapter extends RecyclerView.Adapter<WallpaperAdapter.VH> {
        List<String> list;
        WallpaperAdapter(List<String> list) { this.list = list; }
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            View view = LayoutInflater.from(p.getContext()).inflate(R.layout.item_wallpaper, p, false);
            return new VH(view);
        }
        @Override public void onBindViewHolder(@NonNull VH h, int p) {
            Glide.with(h.img.getContext())
                .load(list.get(p))
                .centerCrop()
                .placeholder(android.R.drawable.progress_horizontal)
                .into(h.img);
            
            h.itemView.setOnClickListener(v -> setWallpaper(list.get(p)));
        }
        @Override public int getItemCount() { return list.size(); }
        class VH extends RecyclerView.ViewHolder {
            ImageView img;
            VH(View v) { super(v); img = v.findViewById(R.id.wallpaper_image); }
        }
    }
}
