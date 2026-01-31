package com.example.deviceinfo;

import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
    private final String INTER_ID = "ca-app-pub-3940256099942544/1033173712"; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, status -> {});
        loadBanner();
        loadInterstitial();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, 1));
        adapter = new WallpaperAdapter(imageUrls);
        recyclerView.setAdapter(adapter);

        // برمجة أزرار الأقسام
        findViewById(R.id.btnCars).setOnClickListener(v -> loadPhotos("Luxury Cars"));
        findViewById(R.id.btnAnime).setOnClickListener(v -> loadPhotos("Anime Art"));
        findViewById(R.id.btnNature).setOnClickListener(v -> loadPhotos("Nature 4K"));
        findViewById(R.id.btnDark).setOnClickListener(v -> loadPhotos("Amoled Dark"));

        loadPhotos("Random Wallpaper");

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                showAdsAndRun(() -> loadPhotos(query));
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) { return false; }
        });
    }

    private void loadBanner() {
        AdView adView = findViewById(R.id.adView);
        adView.loadAd(new AdRequest.Builder().build());
    }

    private void loadInterstitial() {
        InterstitialAd.load(this, INTER_ID, new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) { mInterstitialAd = interstitialAd; }
        });
    }

    private void showAdsAndRun(Runnable action) {
        if (mInterstitialAd != null) {
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() { action.run(); loadInterstitial(); }
            });
            mInterstitialAd.show(this);
        } else { action.run(); loadInterstitial(); }
    }

    private void loadPhotos(String query) {
        imageUrls.clear();
        Random r = new Random();
        for (int i = 0; i < 60; i++) {
            imageUrls.add("https://loremflickr.com/600/1000/" + query.trim().replace(" ","") + "?lock=" + r.nextInt(100000));
        }
        adapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(0);
    }

    private void shareImage(String url) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "شوف هذه الخلفية الرائعة من تطبيقنا: " + url);
        startActivity(Intent.createChooser(intent, "مشاركة عبر:"));
    }

    private void setWallpaper(String url) {
        showAdsAndRun(() -> {
            Toast.makeText(this, "جاري التثبيت... ✅", Toast.LENGTH_SHORT).show();
            Glide.with(this).asBitmap().load(url).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                    try {
                        WallpaperManager.getInstance(MainActivity.this).setBitmap(resource);
                        Toast.makeText(MainActivity.this, "تم تغيير الخلفية!", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {}
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
            Glide.with(h.img).load(list.get(p)).centerCrop().into(h.img);
            h.itemView.setOnClickListener(v -> setWallpaper(list.get(p)));
            // مشاركة عند الضغط المطول
            h.itemView.setOnLongClickListener(v -> { shareImage(list.get(p)); return true; });
        }
        @Override public int getItemCount() { return list.size(); }
        class VH extends RecyclerView.ViewHolder {
            ImageView img;
            VH(View v) { super(v); img = v.findViewById(R.id.wallpaper_image); }
        }
    }
}
