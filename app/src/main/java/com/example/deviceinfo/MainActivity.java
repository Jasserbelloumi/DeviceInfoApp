package com.example.deviceinfo;

import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WallpaperAdapter adapter;
    private List<String> imageUrls = new ArrayList<>();
    private InterstitialAd mInterstitialAd;
    private String selectedImageUrl = "";
    
    // مفتاح الـ API الخاص بك
    private static final String API_KEY = "90|dUvCD5IBXxQZ2CPLRZalejdVaXixrIqEQoENF93L5301f5bc";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // تهيئة الإعلانات
        MobileAds.initialize(this, initializationStatus -> {});
        
        // تحميل البنر
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // تحميل الإعلان البيني
        loadInterstitialAd();

        // إعداد القائمة
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        adapter = new WallpaperAdapter(imageUrls);
        recyclerView.setAdapter(adapter);

        // بدء جلب الصور
        new FetchWallpapersTask().execute();
    }

    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        
        // هام جداً: استبدل النص أدناه بكود الوحدة الإعلانية البينية (Interstitial) من حساب AdMob الخاص بك
        // الكود الذي في الصورة عندك هو للبنر فقط، يجب أن تنشئ واحد جديد للـ Interstitial
        String myInterstitialId = "ca-app-pub-7500537470112334/YOUR_INTERSTITIAL_ID_HERE"; 
        
        // ملاحظة: إذا لم تضع الكود الصحيح، سيتم استخدام كود اختباري مؤقتاً لكي لا يتوقف التطبيق
        // بمجرد أن تنشئ الوحدة، احذف هذا السطر واستخدم الكود الخاص بك
        if (myInterstitialId.contains("YOUR_INTERSTITIAL")) {
             myInterstitialId = "ca-app-pub-3940256099942544/1033173712"; // كود جوجل الاحتياطي
        }

        InterstitialAd.load(this, myInterstitialId, adRequest,
            new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                    mInterstitialAd = interstitialAd;
                    mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            setWallpaper(selectedImageUrl);
                            loadInterstitialAd(); // تحميل إعلان جديد
                        }
                    });
                }
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    mInterstitialAd = null;
                }
            });
    }

    private void onImageClicked(String url) {
        selectedImageUrl = url;
        if (mInterstitialAd != null) {
            mInterstitialAd.show(MainActivity.this);
        } else {
            setWallpaper(url);
        }
    }

    private void setWallpaper(String url) {
        Toast.makeText(this, "جاري ضبط الخلفية... 🎨", Toast.LENGTH_SHORT).show();
        Glide.with(this).asBitmap().load(url).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                try {
                    WallpaperManager.getInstance(getApplicationContext()).setBitmap(resource);
                    Toast.makeText(MainActivity.this, "تم تغيير الخلفية بنجاح! ✅", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "حدث خطأ!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {}
        });
    }

    // جلب الصور من SourceSplash API
    private class FetchWallpapersTask extends AsyncTask<Void, String, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                // سنجلب 15 صورة عشوائية لملء الشبكة
                for (int i = 0; i < 15; i++) {
                    URL url = new URL("https://www.sourcesplash.com/api/random");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
                    
                    if (conn.getResponseCode() == 200) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder result = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) result.append(line);
                        
                        // تحليل JSON حسب شرح الموقع: { "url": "..." }
                        JSONObject jsonObject = new JSONObject(result.toString());
                        if (jsonObject.has("url")) {
                            publishProgress(jsonObject.getString("url"));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            imageUrls.add(values[0]);
            adapter.notifyItemInserted(imageUrls.size() - 1);
        }
    }

    // RecyclerView Adapter
    private class WallpaperAdapter extends RecyclerView.Adapter<WallpaperAdapter.ViewHolder> {
        private List<String> urls;
        public WallpaperAdapter(List<String> urls) { this.urls = urls; }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wallpaper, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String url = urls.get(position);
            Glide.with(holder.imageView.getContext())
                 .load(url)
                 .placeholder(android.R.drawable.ic_menu_gallery) // صورة مؤقتة أثناء التحميل
                 .into(holder.imageView);
            
            holder.itemView.setOnClickListener(v -> onImageClicked(url));
        }

        @Override
        public int getItemCount() { return urls.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            ViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.wallpaper_image);
            }
        }
    }
}
