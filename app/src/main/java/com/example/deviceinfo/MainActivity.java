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
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import org.json.JSONArray;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. تهيئة الإعلانات
        MobileAds.initialize(this, initializationStatus -> {});
        loadBannerAd();
        loadInterstitialAd();

        // 2. إعداد القائمة (Pinterest Style)
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        adapter = new WallpaperAdapter(imageUrls);
        recyclerView.setAdapter(adapter);

        // 3. جلب الصور من السيرفر
        new FetchWallpapersTask().execute();
    }

    private void loadBannerAd() {
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    // تحميل الإعلان البيني (Full Screen)
    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        // ملاحظة: هذا ID تجريبي، استبدله بـ ID الحقيقي الخاص بك للإعلان البيني
        // استخدمت "ca-app-pub-3940256099942544/1033173712" للتجربة (Test ID) لضمان عدم حظر حسابك أثناء التطوير
        // عندما تجهز، ضع الـ ID الخاص بك من AdMob هنا
        InterstitialAd.load(this,"ca-app-pub-3940256099942544/1033173712", adRequest,
            new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                    mInterstitialAd = interstitialAd;
                    mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            // عندما يغلق المستخدم الإعلان، ننفذ الأمر (تعيين الخلفية)
                            setWallpaper(selectedImageUrl);
                            loadInterstitialAd(); // تحميل إعلان جديد للمرة القادمة
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
            // إذا لم يكن الإعلان جاهزاً، نفذ الأمر مباشرة
            setWallpaper(url);
        }
    }

    private void setWallpaper(String url) {
        Toast.makeText(this, "جاري تعيين الخلفية...", Toast.LENGTH_SHORT).show();
        Glide.with(this).asBitmap().load(url).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                try {
                    WallpaperManager.getInstance(getApplicationContext()).setBitmap(resource);
                    Toast.makeText(MainActivity.this, "تم تعيين الخلفية بنجاح! ✅", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "فشل تعيين الخلفية", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {}
        });
    }

    // كلاس لجلب البيانات من API
    private class FetchWallpapersTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                // ملاحظة: بما أنني لا أملك التوثيق الدقيق للـ API الخاص بـ SourceSplash
                // سأفترض أن هذا هو الرابط للصور الشائعة (Curated).
                // إذا لم يعمل، تأكد من الـ Endpoint الصحيح من لوحة تحكم الموقع
                URL url = new URL("https://www.sourcesplash.com/api/v1/curated?per_page=20"); 
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                // إضافة الـ API Key في الهيدر
                conn.setRequestProperty("Authorization", "Bearer 90|dUvCD5IBXxQZ2CPLRZalejdVaXixrIqEQoENF93L5301f5bc");
                conn.connect();

                // إذا فشل الاتصال بالموقع الخاص، نستخدم Pexels كاحتياط لضمان عمل التطبيق الآن
                if (conn.getResponseCode() != 200) {
                     return null; 
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                return result.toString();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    // تحليل JSON (يعتمد على هيكل الرد المتوقع)
                    JSONObject jsonObject = new JSONObject(result);
                    // نتوقع مصفوفة باسم data أو photos
                    JSONArray photos = jsonObject.has("data") ? jsonObject.getJSONArray("data") : jsonObject.getJSONArray("photos");
                    
                    for (int i = 0; i < photos.length(); i++) {
                        JSONObject photo = photos.getJSONObject(i);
                        // نحاول إيجاد رابط الصورة بأكثر من صيغة محتملة
                        String imgUrl = "";
                        if (photo.has("src")) {
                            imgUrl = photo.getJSONObject("src").getString("large");
                        } else if (photo.has("url")) {
                            imgUrl = photo.getString("url");
                        } else if (photo.has("large_url")) { // صيغة SourceSplash المحتملة
                             imgUrl = photo.getString("large_url");
                        }
                        
                        if (!imgUrl.isEmpty()) imageUrls.add(imgUrl);
                    }
                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                     Log.e("API", "Error parsing JSON", e);
                }
            } else {
                // بيانات وهمية للاختبار في حال فشل الـ API
                imageUrls.add("https://images.pexels.com/photos/1266808/pexels-photo-1266808.jpeg?auto=compress&cs=tinysrgb&w=600");
                imageUrls.add("https://images.pexels.com/photos/1624496/pexels-photo-1624496.jpeg?auto=compress&cs=tinysrgb&w=600");
                imageUrls.add("https://images.pexels.com/photos/1366919/pexels-photo-1366919.jpeg?auto=compress&cs=tinysrgb&w=600");
                imageUrls.add("https://images.pexels.com/photos/1166209/pexels-photo-1166209.jpeg?auto=compress&cs=tinysrgb&w=600");
                adapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "تم تحميل صور احتياطية (تأكد من الـ API)", Toast.LENGTH_LONG).show();
            }
        }
    }

    // الأدابتير الخاص بالقائمة
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
                 .placeholder(android.R.drawable.ic_menu_gallery)
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
