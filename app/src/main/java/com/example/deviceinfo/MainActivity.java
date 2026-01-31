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
    private String currentSearch = "wallpaper";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, status -> {});
        loadBanner();
        loadInterstitial();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, 1));
        
        loadPhotos("nature"); // تحميل صور افتراضية عند الفتح

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                loadPhotos(query);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) { return false; }
        });
    }

    private void loadPhotos(String query) {
        imageUrls.clear();
        Random r = new Random();
        // خدعة جلب 30 رابط بحث مباشر بدون تخزين
        for (int i = 0; i < 30; i++) {
            imageUrls.add("https://source.unsplash.com/featured/?" + query + "," + r.nextInt(1000));
        }
        recyclerView.setAdapter(new WallpaperAdapter(imageUrls));
    }

    private void loadBanner() {
        AdView adView = findViewById(R.id.adView);
        adView.loadAd(new AdRequest.Builder().build());
    }

    private void loadInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();
        // تأكد من وضع ID الإعلان البيني الحقيقي هنا للربح
        InterstitialAd.load(this, "ca-app-pub-3940256099942544/1033173712", adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) { mInterstitialAd = interstitialAd; }
        });
    }

    private void setWallpaper(String url) {
        if (mInterstitialAd != null) {
            mInterstitialAd.show(this);
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    downloadAndSet(url);
                    loadInterstitial();
                }
            });
        } else {
            downloadAndSet(url);
        }
    }

    private void downloadAndSet(String url) {
        Toast.makeText(this, "جاري التحميل والتعيين... ⏳", Toast.LENGTH_SHORT).show();
        Glide.with(this).asBitmap().load(url).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                try {
                    WallpaperManager.getInstance(MainActivity.this).setBitmap(resource);
                    Toast.makeText(MainActivity.this, "تم بنجاح! ✅", Toast.LENGTH_SHORT).show();
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }

    private class WallpaperAdapter extends RecyclerView.Adapter<WallpaperAdapter.VH> {
        List<String> list;
        WallpaperAdapter(List<String> list) { this.list = list; }
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_wallpaper, p, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int p) {
            Glide.with(h.img).load(list.get(p)).centerCrop().into(h.img);
            h.itemView.setOnClickListener(v -> setWallpaper(list.get(p)));
        }
        @Override public int getItemCount() { return list.size(); }
        class VH extends RecyclerView.ViewHolder {
            ImageView img;
            VH(View v) { super(v); img = v.findViewById(R.id.wallpaper_image); }
        }
    }
}
