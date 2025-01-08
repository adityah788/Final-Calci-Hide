package com.example.finalcalcihide.Activity;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.example.finalcalcihide.Adapter.SelectImageVideosAdapter;
import com.example.finalcalcihide.FileUtils.ImgVidFHandle;
import com.example.finalcalcihide.GridSpacingItemDecoration;
import com.example.finalcalcihide.R;
import com.example.finalcalcihide.ViewPager.ImageandVideoViewPager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SelectImagesorVideos extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayout customToolbarContainer;
    private LinearLayout customBottomAppBar;
    private SelectImageVideosAdapter selectImagesorVideosAdapter;
    private ArrayList<String> mediaList = new ArrayList<>();
    public static List<Boolean> selected = new ArrayList<>();
    //    public static ArrayList<String> imagesSelected = new ArrayList<>();
    public static String parent;
    private LottieAnimationView lottieHideUnhideAnimation;
    private FrameLayout animationContainer; // Added to manage animation visibility


    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_select_images);

        // Set navigation bar color to black
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.FinalPrimaryColor));


        // Inflate the custom layout containing the Lottie animations
        LayoutInflater inflater = LayoutInflater.from(this);
        View customLayout = inflater.inflate(R.layout.animation, null);

        // Initialize the LottieAnimationView from the inflated layout
        lottieHideUnhideAnimation = customLayout.findViewById(R.id.ani_hide_unhide);
        animationContainer = findViewById(R.id.animation_container);


        // Add the inflated custom layout to the animation container
        animationContainer.addView(customLayout);


        customToolbarContainer = findViewById(R.id.select_img_custom_toolbar_container);
        customBottomAppBar = findViewById(R.id.custom_btm_appbar_hide);

        selectImagesorVideosAdapter = new SelectImageVideosAdapter(this, mediaList, new SelectImageVideosAdapter.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int position) {
                handleItemClick(position);
            }

            @Override
            public void onSelectionChanged(boolean isSelected) {
                onSelectandDeselect_All(isSelected);
            }
        });

        recyclerView = findViewById(R.id.select_image_gallery_recycler);
        parent = Objects.requireNonNull(getIntent().getExtras()).getString("FROM");
        mediaList.clear();
        selected.clear();

        if ("Images".equals(parent)) {
            mediaList.addAll(ImageVideoBucket.mediaList);
        } else if ("Videos".equals(parent)) {
            mediaList.addAll(ImageVideoBucket.mediaList);
        } else {
            Log.w(TAG, "Unknown 'FROM' value: " + parent);
        }


        setupRecyclerView();

        setToolbarMenu(false);


        customBottomAppBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<String> selectedPaths = getSelectedImagePaths();

                if (!selectedPaths.isEmpty()) {
                    // Show the animation container and start the Lottie animation
                    animationContainer.setVisibility(View.VISIBLE);
                    lottieHideUnhideAnimation.setVisibility(View.VISIBLE);

                    // Start the animation and set it to loop
                    lottieHideUnhideAnimation.setRepeatCount(LottieDrawable.INFINITE);
                    lottieHideUnhideAnimation.playAnimation();

                    // Minimum display time for the animation
                    final long MINIMUM_DISPLAY_TIME = 2000; // 2.5 seconds
                    final long animationStartTime = System.currentTimeMillis();

                    // Perform the hide operation in a background thread
                    new Thread(() -> {
                        boolean result = ImgVidFHandle.copyImagesToPrivateStorageWrapper(SelectImagesorVideos.this, new ArrayList<>(selectedPaths));

                        // Now update the UI on the main thread after the process completes
                        runOnUiThread(() -> {
                            long elapsedTime = System.currentTimeMillis() - animationStartTime;

                            if (elapsedTime < MINIMUM_DISPLAY_TIME) {
                                // Ensure the animation runs for at least the minimum time
                                long remainingTime = MINIMUM_DISPLAY_TIME - elapsedTime;
                                recyclerView.postDelayed(() -> stopAnimationAndComplete(result), remainingTime);
                            } else {
                                // Stop animation immediately if the process took longer than minimum time
                                stopAnimationAndComplete(result);
                            }
                        });
                    }).start();
                } else {
                    Toast.makeText(SelectImagesorVideos.this, "No images selected to hide.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    private List<String> getSelectedImagePaths() {
        List<String> selectedPaths = new ArrayList<>();
        for (int position : selectImagesorVideosAdapter.getSelectedItems()) {
            selectedPaths.add(mediaList.get(position));
        }
        return selectedPaths;
    }

    private void setupRecyclerView() {
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 3);
        int spacing = getResources().getDimensionPixelSize(R.dimen.recycler_item_spacing);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(3, spacing));

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(selectImagesorVideosAdapter);
    }

    private void handleItemClick(int position) {
//        if (selectImagesorVideosAdapter.isSelectedAny()) {
        selectImagesorVideosAdapter.toggleSelection(position);
//        } else {
//            Intent intent = new Intent(this, ImageandVideoViewPager.class);
//            intent.putStringArrayListExtra("imagePaths", mediaList);
//            intent.putExtra("position", position);
//            startActivity(intent);
//        }
    }

    private void onSelectandDeselect_All(boolean isAnySelected) {
        setToolbarMenu(isAnySelected);
        setCustomBottomAppBarVisibility(isAnySelected);
    }

    private void setToolbarMenu(boolean isAnySelected) {
        customToolbarContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        View customToolbar = inflater.inflate(
                isAnySelected ? R.layout.contextual_toolbar : R.layout.main_toolbar,
                customToolbarContainer,
                false
        );

        customToolbarContainer.addView(customToolbar);

        if (isAnySelected) {
            ImageView selectDeselectAll = customToolbar.findViewById(R.id.contextual_toolbar_select_and_deselect_all);
            if (selectDeselectAll != null) {
                selectDeselectAll.setOnClickListener(v -> toggleSelectDeselectAll());
            }

            ImageView cutIcon = customToolbar.findViewById(R.id.contextual_toolbar_cutt);
            if (cutIcon != null) {
                cutIcon.setOnClickListener(v -> {
                    selectImagesorVideosAdapter.clearSelection();
                    onSelectandDeselect_All(false);
                });
            }

            TextView itemCountText = customToolbar.findViewById(R.id.item_count_text);
            if (itemCountText != null) {
                itemCountText.setOnClickListener(v -> updateItemCountText());
                updateItemCountText();
            }
        } else {
            ImageView menuIcon = customToolbar.findViewById(R.id.main_toobar_menu_icon);
            menuIcon.setVisibility(View.GONE);


            ImageView backArrow = customToolbar.findViewById(R.id.main_toolbar_back_arrow);
            if (backArrow != null) {
                backArrow.setOnClickListener(v -> finish());
            }

            TextView titleTextView = customToolbar.findViewById(R.id.main_toolbar_title);

            if ("Videos".equals(parent)) {
                mediaList.addAll(ImageVideoBucket.mediaList);
                titleTextView.setText("Select Videos");
            } else {
                titleTextView.setText("Select Images");
            }

        }
    }

    private void setCustomBottomAppBarVisibility(boolean visible) {
        if (visible && customBottomAppBar.getVisibility() != View.VISIBLE) {
            customBottomAppBar.setTranslationY(customBottomAppBar.getHeight());
            customBottomAppBar.setVisibility(View.VISIBLE);
            customBottomAppBar.animate().translationY(0).setDuration(300).setListener(null);
        } else if (!visible && customBottomAppBar.getVisibility() == View.VISIBLE) {
            customBottomAppBar.animate()
                    .translationY(customBottomAppBar.getHeight())
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            customBottomAppBar.setVisibility(View.GONE);
                        }
                    });
        }
    }

    private void toggleSelectDeselectAll() {
        boolean selectAll = selectImagesorVideosAdapter.getSelectedItemCount() < selectImagesorVideosAdapter.getItemCount();
        selectImagesorVideosAdapter.selectAll(selectAll);
        updateSelectDeselectAllIcon(selectAll);
        updateItemCountText();
    }

    private void updateSelectDeselectAllIcon(boolean selectAll) {
        View customToolbar = customToolbarContainer.getChildAt(0);
        if (customToolbar != null) {
            ImageView selectDeselectAll = customToolbar.findViewById(R.id.contextual_toolbar_select_and_deselect_all);
            if (selectDeselectAll != null) {
                selectDeselectAll.setImageResource(selectAll ? R.drawable.baseline_library_add_check_24 : R.drawable.baseline_check_box_outline_blank_24);
            }
        }
    }

    private void updateItemCountText() {
        View customToolbar = customToolbarContainer.getChildAt(0);
        if (customToolbar != null) {
            TextView itemCountText = customToolbar.findViewById(R.id.item_count_text);
            if (itemCountText != null) {
                int selectedCount = selectImagesorVideosAdapter.getSelectedItemCount();
                int totalCount = selectImagesorVideosAdapter.getItemCount();
                itemCountText.setText(getString(R.string.item_count, selectedCount, totalCount));
            }
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void stopAnimationAndComplete(boolean result) {

        // Handle success or failure of the process
        if (result) {
            Toast.makeText(SelectImagesorVideos.this,
                    parent.equals("Images") ? "Images hidden successfully!" : "Videos hidden successfully!",
                    Toast.LENGTH_SHORT).show();

            // Redirect based on whether it's Images or Videos
            Intent intent;
            if ("Images".equals(parent)) {
                intent = new Intent(SelectImagesorVideos.this, ImagesHidden.class);
            } else if ("Videos".equals(parent)) {
                intent = new Intent(SelectImagesorVideos.this, VideoHidden.class);
            } else {
                // Fallback case, could log or handle unknown types
                Log.w(TAG, "Unknown 'FROM' value in stopAnimationAndComplete: " + parent);
                return;
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            finish(); // Close the SelectImagesorVideos activity
            startActivity(intent);
        } else {
            Toast.makeText(SelectImagesorVideos.this, "Failed to hide " + (parent.equals("Images") ? "images." : "videos."), Toast.LENGTH_SHORT).show();
        }

        // Stop the animation
        lottieHideUnhideAnimation.cancelAnimation();
        lottieHideUnhideAnimation.setVisibility(View.GONE);
        animationContainer.setVisibility(View.GONE);
    }

}
