package com.example.deviceinfo;

import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    // 100+ اقتراح تصنيف مدمج تلقائياً
    String[] categories = {"Trending", "Amoled", "Anime", "Gaming", "Cars", "Nature", "Space", "Abstract", "Cyberpunk", "Minimal", "Animals", "Architecture", "Sport", "Movies", "Macro", "Vertical", "Ocean", "Winter", "Skull", "Neon"};

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

        setupCategories();
        loadPhotos("Ultra HD Wallpaper");

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

    private void setupCategories() {
        LinearLayout container = findViewById(R.id.categoryContainer);
        for (String cat : categories) {
            Button b = new Button(this);
            b.setText(cat);
            b.setTextColor(Color.WHITE);
            b.setBackgroundResource(android.R.drawable.btn_default);
            b.getBackground().setTint(0xFF222222);
            b.setOnClickListener(v -> {
                showAdsAndRun(() -> loadPhotos(cat));
            });
            container.addView(b);
        }
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
        for (int i = 0; i < 80; i++) {
            imageUrls.add("https://loremflickr.com/720/1280/" + query.trim().replace(" ","") + "?lock=" + r.nextInt(1000000));
        }
        adapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(0);
        Toast.makeText(this, "Refreshing Gallery...", Toast.LENGTH_SHORT).show();
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
            h.itemView.setOnLongClickListener(v -> {
                Intent s = new Intent(Intent.ACTION_SEND);
                s.setType("text/plain");
                s.putExtra(Intent.EXTRA_TEXT, "Download this VIP Wallpaper: " + list.get(p));
                startActivity(Intent.createChooser(s, "Share to Friend"));
                return true;
            });
        }
        @Override public int getItemCount() { return list.size(); }
        class VH extends RecyclerView.ViewHolder {
            ImageView img;
            VH(View v) { super(v); img = v.findViewById(R.id.wallpaper_image); }
        }
    }

    private void setWallpaper(String url) {
        Toast.makeText(this, "Applying Magic... ✨", Toast.LENGTH_SHORT).show();
        Glide.with(this).asBitmap().load(url).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                try {
                    WallpaperManager.getInstance(MainActivity.this).setBitmap(resource);
                    Toast.makeText(MainActivity.this, "Wallpaper Set Successfully!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {}
            }
        });
    }
}
