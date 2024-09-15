package com.example.finalcalcihide.Activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalcalcihide.Adapter.ImageVideoHideAdapter;

import com.example.finalcalcihide.R;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieDrawable; // Add this import for LottieDrawable

import com.airbnb.lottie.LottieAnimationView;
import com.example.finalcalcihide.GridSpacingItemDecoration;
import com.example.finalcalcihide.PermissionHandler;
import com.example.finalcalcihide.FileUtils.ImgVidFHandle;
import com.example.finalcalcihide.Utils.FileUtils;
import com.example.finalcalcihide.Utils.ToolbarManager;
import com.example.finalcalcihide.ViewPager.ImageandVideoViewPager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImagesHidden extends AppCompatActivity {
    private ArrayList<String> imagePaths = new ArrayList<>();
    private ImageVideoHideAdapter imageVideoHideAdapter;
    private RecyclerView imageRecyclerView;
    private LinearLayout customBottomAppBarDelete;
    private LinearLayout customToolbarContainer;
    private LinearLayout customBottomAppBar;
    private LinearLayout customBottomAppBarVisible;
    private FrameLayout fab_container;

    private LottieAnimationView lottieHideUnhideAnimation;
    private LottieAnimationView lottieDeleteAnimation;
    private ToolbarManager toolbarManager;
    private FrameLayout animationContainer; // Added to manage animation visibility




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_images_hidden);

        // Set navigation bar color to black
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.navigation_bar_color));


        // Inflate the custom layout containing the Lottie animations
        LayoutInflater inflater = LayoutInflater.from(this);
        View customLayout = inflater.inflate(R.layout.animation, null);

        // Initialize the LottieAnimationView from the inflated layout
        lottieHideUnhideAnimation = customLayout.findViewById(R.id.ani_hide_unhide);
        lottieDeleteAnimation = customLayout.findViewById(R.id.ani_delete);
        animationContainer = findViewById(R.id.animation_container);


        // Add the inflated custom layout to the animation container
        animationContainer.addView(customLayout);

        PermissionHandler.requestPermissions(ImagesHidden.this);


        imageRecyclerView = findViewById(R.id.image_gallery_recycler);

        fab_container = findViewById(R.id.image_gallary_fab_container);
        customToolbarContainer = findViewById(R.id.custom_toolbar_container);
        customBottomAppBar = findViewById(R.id.custom_bottom_appbar);
        customBottomAppBarVisible = findViewById(R.id.custom_btm_appbar_Visible);
        customBottomAppBarDelete = findViewById(R.id.custom_btm_appbar_delete);
        imagePaths = FileUtils.getImagePaths(this);



        imageVideoHideAdapter = new ImageVideoHideAdapter(this, imagePaths, new ImageVideoHideAdapter.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int position) {
                handleItemClick(position);
            }

            @Override
            public void onSelectionChanged(boolean isSelected) {
                onSelectandDeselect_All(isSelected);
            }
        });


        toolbarManager = new ToolbarManager(this, customToolbarContainer, imageVideoHideAdapter,imagePaths);


        imageRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        int spacing = getResources().getDimensionPixelSize(R.dimen.recycler_item_spacing);
        imageRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3, spacing));


        imageRecyclerView.setAdapter(imageVideoHideAdapter);

        toolbarManager.setToolbarMenu(false);
        handleOnBackPressed();


        customBottomAppBarVisible.setOnClickListener(v -> {

            List<String> selectedPaths = imageVideoHideAdapter.getSelectedImagePaths();

            // Define minimum display time (e.g., 2 seconds)
            final long MINIMUM_DISPLAY_TIME = 2500; // in milliseconds
            final long animationStartTime = System.currentTimeMillis();

            // Show the animation when the button is clicked
            animationContainer.setVisibility(View.VISIBLE);
            lottieHideUnhideAnimation.setVisibility(View.VISIBLE);

            // Set the animation to loop infinitely
            lottieHideUnhideAnimation.setRepeatCount(LottieDrawable.INFINITE);
            lottieHideUnhideAnimation.playAnimation();

            // Run the process in a background thread (like using AsyncTask or Kotlin Coroutines)
            new Thread(() -> {
                boolean processSuccess = ImgVidFHandle.moveImagesBackToOriginalLocationsWrapper(ImagesHidden.this, selectedPaths);

                // Now update the UI on the main thread when the process is done
                runOnUiThread(() -> {
                    long elapsedTime = System.currentTimeMillis() - animationStartTime;

                    if (elapsedTime < MINIMUM_DISPLAY_TIME) {
                        // If the process finished too fast, wait for the remaining time
                        long remainingTime = MINIMUM_DISPLAY_TIME - elapsedTime;
                        imageRecyclerView.postDelayed(() -> stopAnimationAndUpdateUI(processSuccess, selectedPaths), remainingTime);
                    } else {
                        // Process took enough time, stop the animation and update the UI immediately
                        stopAnimationAndUpdateUI(processSuccess, selectedPaths);
                    }
                });
            }).start();
        });



     customBottomAppBarDelete.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             List<String> selectedPaths = imageVideoHideAdapter.getSelectedImagePaths();

             // Define minimum display time (e.g., 2 seconds)
             final long MINIMUM_DISPLAY_TIME = 2100; // in milliseconds
             final long animationStartTime = System.currentTimeMillis();

             // Show the animation when the button is clicked
             animationContainer.setVisibility(View.VISIBLE);
             lottieDeleteAnimation.setVisibility(View.VISIBLE);

             // Set the animation to loop infinitely
             lottieDeleteAnimation.setRepeatCount(LottieDrawable.INFINITE);
             lottieDeleteAnimation.playAnimation();

             // Run the process in a background thread (like using AsyncTask or Kotlin Coroutines)
             new Thread(() -> {
                 boolean processSuccess = ImgVidFHandle.moveImagesBackToRecycleLocationsWrapper(ImagesHidden.this, selectedPaths);

                 // Now update the UI on the main thread when the process is done
                 runOnUiThread(() -> {
                     long elapsedTime = System.currentTimeMillis() - animationStartTime;

                     if (elapsedTime < MINIMUM_DISPLAY_TIME) {
                         // If the process finished too fast, wait for the remaining time
                         long remainingTime = MINIMUM_DISPLAY_TIME - elapsedTime;
                         imageRecyclerView.postDelayed(() -> stopAnimationAndUpdateUI(processSuccess, selectedPaths), remainingTime);
                     } else {
                         // Process took enough time, stop the animation and update the UI immediately
                         stopAnimationAndUpdateUI(processSuccess, selectedPaths);
                     }
                 });
             }).start();
         }
     });

        fab_container.setOnClickListener(v -> startActivity(new Intent(ImagesHidden.this, ImageVideoBucket.class)));

    }


    private void handleItemClick(int position) {
        if (imageVideoHideAdapter.isSelectedAny()) {
            imageVideoHideAdapter.toggleSelection(position);
        } else {
            Intent intent = new Intent(this, ImageandVideoViewPager.class);
            intent.putStringArrayListExtra("imagePaths", imagePaths);
            intent.putExtra("position", position);
            startActivity(intent);
        }
    }

    private void onSelectandDeselect_All(boolean isAnyselected) {
        toolbarManager.setToolbarMenu(isAnyselected);
        setCustomBottomAppBarVisibility(isAnyselected);
    }



    private void setCustomBottomAppBarVisibility(boolean visible) {
        if (visible && customBottomAppBar.getVisibility() != View.VISIBLE) {
            fab_container.setVisibility(View.GONE);
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
            fab_container.setVisibility(View.VISIBLE);

        }
    }



    private void handleOnBackPressed() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (imageVideoHideAdapter.isSelectedAny()) {
                    imageVideoHideAdapter.clearSelection();
                    onSelectandDeselect_All(false);
                } else {
                    finish();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }


    // Helper method to stop the animation and update the UI
    private void stopAnimationAndUpdateUI(boolean processSuccess, List<String> selectedPaths) {
        if (processSuccess) {
            // Remove the moved paths from imagePaths
            imagePaths.removeAll(selectedPaths);
            imageVideoHideAdapter.notifyDataSetChanged();
            imageVideoHideAdapter.clearSelection();

            Toast.makeText(ImagesHidden.this, "Images moved back to original locations and deleted from app", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(ImagesHidden.this, "Error moving images back", Toast.LENGTH_SHORT).show();
        }

        // Stop and hide the animation after the process completes
        lottieHideUnhideAnimation.cancelAnimation();
        lottieHideUnhideAnimation.setVisibility(View.GONE);
        animationContainer.setVisibility(View.GONE);
    }


}

///  350