package com.example.finalcalcihide.ViewPager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.finalcalcihide.Adapter.ImageandVideoViewPagerAdapter;
import com.example.finalcalcihide.R;

import java.util.ArrayList;

public class ImageandVideoViewPager extends AppCompatActivity {

    private ViewPager2 viewPager;
    private ImageandVideoViewPagerAdapter adapter;
    private ArrayList<String> imagePaths;
    private boolean isSystemUIVisible = true; // Initially visible
    private View toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_image_view_pager);

        // Set system UI visibility flags
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        // Set status bar color to transparent
        getWindow().setStatusBarColor(Color.TRANSPARENT);


        toolbar = findViewById(R.id.image_view_pager_toolbar);

        // Retrieve image paths and position
        Intent intent = getIntent();
        imagePaths = intent.getStringArrayListExtra("imagePaths");
        int position = intent.getIntExtra("position", 0);

        Log.d("ImageViewPager", "Loaded image paths: " + imagePaths);

        // Setup ViewPager2
        viewPager = findViewById(R.id.imageViewpager_viewpager);
        adapter = new ImageandVideoViewPagerAdapter(this, imagePaths);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(position, false);

        // Back button functionality
        ImageView backArrow = findViewById(R.id.img_viewpager_toolbar_back_arrow);
        backArrow.setOnClickListener(v -> finish());

        // Handle back press callback
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        // Add a page change callback to the ViewPager
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager2.SCROLL_STATE_IDLE && !isSystemUIVisible) {
                    getWindow().getDecorView().setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_IMMERSIVE
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                }
            }
        });
    }

    public void toggleSystemUI() {
        View decorView = getWindow().getDecorView();
        if (isSystemUIVisible) {
            // Hide system UI
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            toolbar.setVisibility(View.GONE);

        } else {
            // Show system UI
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            toolbar.setVisibility(View.VISIBLE);

        }
        isSystemUIVisible = !isSystemUIVisible;
    }
}
