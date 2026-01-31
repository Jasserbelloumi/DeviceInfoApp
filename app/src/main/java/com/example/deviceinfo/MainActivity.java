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
    
    // معرفات الإعلانات الحقيقية الخاصة بك
    private final String BANNER_ID = "ca-app-pub-7500537470112334/4696609974";
    // ملاحظة: يفضل إنشاء وحدة Interstitial خاصة بك ووضعها هنا
    private final String INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, status -> {});
        
        loadBanner();
        loadInterstitial(); // تحميل أول إعلان شاشة كاملة

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, 1));
        
        loadPhotos("wallpaper"); 

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                showInterstitialThenAction(() -> loadPhotos(query)); // إعلان قبل البحث
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
        InterstitialAd.load(this, INTERSTITIAL_ID, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                mInterstitialAd = interstitialAd;
            }
        });
    }

    // وظيفة سحرية: تعرض إعلان وإذا انتهى تنفذ الأمر المطلوب
    private void showInterstitialThenAction(Runnable action) {
        if (mInterstitialAd != null) {
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    action.run();
                    loadInterstitial(); // تحميل الإعلان التالي فوراً
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
        for (int i = 0; i < 40; i++) { // زيادة عدد الصور المعروضة
            imageUrls.add("https://source.unsplash.com/featured/?" + query + "," + r.nextInt(5000));
        }
        recyclerView.setAdapter(new WallpaperAdapter(imageUrls));
    }

    private void setWallpaper(String url) {
        showInterstitialThenAction(() -> {
            Toast.makeText(this, "جاري معالجة الصورة... ✨", Toast.LENGTH_SHORT).show();
            Glide.with(this).asBitmap().load(url).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                    try {
                        WallpaperManager.getInstance(MainActivity.this).setBitmap(resource);
                        Toast.makeText(MainActivity.this, "تم التعيين بنجاح! ✅", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) { e.printStackTrace(); }
                }
            });
        });
    }

    private class WallpaperAdapter extends RecyclerView.Adapter<WallpaperAdapter.VH> {
        List<String> list;
        WallpaperAdapter(List<String> list) { this.list = list; }
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_wallpaper, p, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int p) {
            Glide.with(h.img).load(list.get(p)).thumbnail(0.1f).centerCrop().into(h.img);
            h.itemView.setOnClickListener(v -> setWallpaper(list.get(p)));
        }
        @Override public int getItemCount() { return list.size(); }
        class VH extends RecyclerView.ViewHolder {
            ImageView img;
            VH(View v) { super(v); img = v.findViewById(R.id.wallpaper_image); }
        }
    }
}
