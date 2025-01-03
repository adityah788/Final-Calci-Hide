package com.example.finalcalcihide.ViewPager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.finalcalcihide.Adapter.ImageandVideoViewPagerAdapter;
import com.example.finalcalcihide.FileUtils.ImgVidFHandle;
import com.example.finalcalcihide.ImageGalleryViewModel;
import com.example.finalcalcihide.R;
import com.example.finalcalcihide.Utils.AnimationManager;

import java.util.ArrayList;
import java.util.List;

public class ImageandVideoViewPager extends AppCompatActivity {

    private ViewPager2 viewPager;
    private ImageandVideoViewPagerAdapter adapter;
    private ArrayList<String> imagePaths;
    private boolean isSystemUIVisible = true; // Initially visible
    private View toolbar;
    private ImageGalleryViewModel viewModel;

    ImageView delete, visible;
    private FrameLayout animationContainer;

    private AnimationManager animationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_image_view_pager);

        // Initialize animation container
        animationContainer = findViewById(R.id.viewpager_animation_container);
        animationManager = new AnimationManager(this, animationContainer);

        // Initialize UI components
        delete = findViewById(R.id.img_viewpager_toobar_deleter_icon);
        visible = findViewById(R.id.img_viewpager_main_toobar_unlock);



        // Handle Delete Button Click
        delete.setOnClickListener(v -> {
            List<String> selectedPaths = new ArrayList<>();
            selectedPaths.add(imagePaths.get(viewPager.getCurrentItem())); // Get the current image path

            final long MINIMUM_DISPLAY_TIME = 2100; // in milliseconds

            animationManager.handleAnimationProcess(
                    AnimationManager.AnimationType.DELETE,
                    selectedPaths,
                    MINIMUM_DISPLAY_TIME,
                    () -> {
                        // Background task: Move images back to recycle locations
                        ImgVidFHandle.moveImagesBackToRecycleLocationsWrapper(ImageandVideoViewPager.this, selectedPaths,null);
                        // Update processSuccess based on actual task outcome
                    },
                    (processSuccess, paths) -> stopAnimationAndUpdateUI(processSuccess, paths)
            );
        });

        // Handle Visible Button Click
        visible.setOnClickListener(v -> {
            List<String> selectedPaths = new ArrayList<>();
            selectedPaths.add(imagePaths.get(viewPager.getCurrentItem())); // Get the current image path

            final long MINIMUM_DISPLAY_TIME = 2500; // in milliseconds

            animationManager.handleAnimationProcess(
                    AnimationManager.AnimationType.HIDE_UNHIDE,
                    selectedPaths,
                    MINIMUM_DISPLAY_TIME,
                    () -> {
                        // Background task: Move images back to original locations
                        ImgVidFHandle.moveImagesBackToOriginalLocationsWrapper(ImageandVideoViewPager.this, selectedPaths);
                        // Update processSuccess based on actual task outcome
                    },
                    (processSuccess, paths) -> stopAnimationAndUpdateUI(processSuccess, paths)
            );
        });

        // Set system UI visibility flags
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        int semiTransparentBlack = Color.argb(128, 0, 0, 0); // alpha=128 (50% transparency), RGB=0,0,0 (black)

        // Semi-transparent black color for navigation bar
        getWindow().setNavigationBarColor(semiTransparentBlack);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ImageGalleryViewModel.class);

        toolbar = findViewById(R.id.image_view_pager_toolbar);

        // Retrieve image paths and position
        Intent intent = getIntent();
        boolean hideButtons = intent.getBooleanExtra("hideButtons", false);
        imagePaths = intent.getStringArrayListExtra("imagePaths");
        int position = intent.getIntExtra("position", 0);

        Log.d("ImageViewPager", "Loaded image paths: " + imagePaths);


        // Check if we need to hide the buttons
        if (hideButtons) {
            // Hide both buttons by setting visibility to GONE (or INVISIBLE)
            delete.setVisibility(View.GONE);
            visible.setVisibility(View.GONE);
        }


        // Setup ViewPager2
        viewPager = findViewById(R.id.imageViewpager_viewpager);
        adapter = new ImageandVideoViewPagerAdapter(this, imagePaths);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(position, false);

        // Observe LiveData in ViewModel
        viewModel.getImagePaths().observe(this, paths -> {
            adapter.updateImagePaths(paths);  // Update adapter with new data
            viewPager.setCurrentItem(position, false);  // Set the current position
        });

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

    // Helper method to stop the animation and update the UI
    private void stopAnimationAndUpdateUI(boolean processSuccess, List<String> selectedPaths) {
        if (processSuccess) {
            // Remove the moved paths from imagePaths
            imagePaths.removeAll(selectedPaths);
            adapter.notifyDataSetChanged();

            Toast.makeText(ImageandVideoViewPager.this, "Image moved successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(ImageandVideoViewPager.this, "Error moving image", Toast.LENGTH_SHORT).show();
        }
    }
}



//265