package com.example.finalcalcihide.Activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalcalcihide.Adapter.ImageVideoHideAdapter;
import com.example.finalcalcihide.FileUtils.ImgVidFHandle;
import com.example.finalcalcihide.GridSpacingItemDecoration;
import com.example.finalcalcihide.R;
import com.example.finalcalcihide.Utils.ToolbarManager;
import com.example.finalcalcihide.ViewPager.ImageandVideoViewPager;
import com.example.finalcalcihide.Utils.AnimationManager;
import com.example.finalcalcihide.Utils.FileUtils;
import com.example.finalcalcihide.PermissionHandler;

import java.util.ArrayList;
import java.util.List;

public class VideoHidden extends AppCompatActivity {
    private ArrayList<String> videoPaths = new ArrayList<>();
    private ImageVideoHideAdapter imageVideoHideAdapter;
    private RecyclerView imageRecyclerView;
    private LinearLayout customBottomAppBarDelete;
    private LinearLayout customToolbarContainer;
    private LinearLayout customBottomAppBar;
    private LinearLayout customBottomAppBarVisible;
    private FrameLayout fab_container;

    private AnimationManager animationManager;
    private FrameLayout animationContainer;

    private ToolbarManager toolbarManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_hidden);

        // Set navigation bar color to black
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.navigation_bar_color));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.FinalPrimaryColor));


        // Initialize animation container
        animationContainer = findViewById(R.id.video_animation_container);
        animationManager = new AnimationManager(this, animationContainer);

        // Request necessary permissions
        PermissionHandler.requestPermissions(VideoHidden.this);

        // Initialize UI components
        imageRecyclerView = findViewById(R.id.video_image_gallery_recycler);
        fab_container = findViewById(R.id.video_image_gallary_fab_container);
        customToolbarContainer = findViewById(R.id.video_custom_toolbar_container);
        customBottomAppBar = findViewById(R.id.video_custom_bottom_appbar);
        customBottomAppBarVisible = findViewById(R.id.custom_btm_appbar_Visible);
        customBottomAppBarDelete = findViewById(R.id.custom_btm_appbar_delete);
        videoPaths = FileUtils.getVideoPaths(this);

        // Initialize Adapter
        imageVideoHideAdapter = new ImageVideoHideAdapter(this, videoPaths, new ImageVideoHideAdapter.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int position) {
                handleItemClick(position);
            }

            @Override
            public void onSelectionChanged(boolean isSelected) {
                onSelectandDeselect_All(isSelected);
            }
        });

        // Initialize ToolbarManager (assuming it's a custom class)
        toolbarManager = new ToolbarManager(this, customToolbarContainer, imageVideoHideAdapter, videoPaths, this);

        // Setup Toolbar
        toolbarManager.setToolbarMenu(false); // Inflate main_toolbar
        toolbarManager.setTitle("Videos");    // Set toolbar title

        // Setup RecyclerView
        imageRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        int spacing = getResources().getDimensionPixelSize(R.dimen.recycler_item_spacing);
        imageRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3, spacing));
        imageRecyclerView.setAdapter(imageVideoHideAdapter);

        // Initialize Toolbar and Back Press Handling
        // toolbarManager.setToolbarMenu(false); // Already called above

        handleOnBackPressed();

        // Handle Show/Hide Button Click
        customBottomAppBarVisible.setOnClickListener(v -> {
            List<String> selectedPaths = imageVideoHideAdapter.getSelectedImagePaths();
            final long MINIMUM_DISPLAY_TIME = 2500; // in milliseconds

            animationManager.handleAnimationProcess(
                    AnimationManager.AnimationType.HIDE_UNHIDE,
                    selectedPaths,
                    MINIMUM_DISPLAY_TIME,
                    () -> {
                        // Background task: Move images back to original locations
                        ImgVidFHandle.moveImagesBackToOriginalLocationsWrapper(VideoHidden.this, selectedPaths);
                        // Update processSuccess based on actual task outcome
                        // For example, set to true if task succeeds, false otherwise
                    },
                    (processSuccess, paths) -> stopAnimationAndUpdateUI(processSuccess, paths)
            );
        });

        // Handle Delete Button Click
        customBottomAppBarDelete.setOnClickListener(v -> {
            List<String> selectedPaths = imageVideoHideAdapter.getSelectedImagePaths();
            final long MINIMUM_DISPLAY_TIME = 2100; // in milliseconds

            animationManager.handleAnimationProcess(
                    AnimationManager.AnimationType.DELETE,
                    selectedPaths,
                    MINIMUM_DISPLAY_TIME,
                    () -> {
                        // Background task: Move images back to recycle locations
                        ImgVidFHandle.moveImagesBackToRecycleLocationsWrapper(VideoHidden.this, selectedPaths,null);
                        // Update processSuccess based on actual task outcome
                    },
                    (processSuccess, paths) -> stopAnimationAndUpdateUI(processSuccess, paths)
            );
        });

        // Handle FAB Click
        fab_container.setOnClickListener(v -> {
            Intent intent = new Intent(VideoHidden.this, ImageVideoBucket.class);
            intent.putExtra("FROM", "Videos"); // Specify that we're handling images
            startActivity(intent);
        });

    }

    private void handleItemClick(int position) {
        if (imageVideoHideAdapter.isSelectedAny()) {
            imageVideoHideAdapter.toggleSelection(position);
        } else {
            Intent intent = new Intent(this, ImageandVideoViewPager.class);
            intent.putStringArrayListExtra("imagePaths", videoPaths);
            intent.putExtra("position", position);
            startActivity(intent);
        }
    }

    private void onSelectandDeselect_All(boolean isAnySelected) {
        toolbarManager.setToolbarMenu(isAnySelected);
        setCustomBottomAppBarVisibility(isAnySelected);

            toolbarManager.setTitle("Videos");

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
            videoPaths.removeAll(selectedPaths);
            imageVideoHideAdapter.notifyDataSetChanged();
            imageVideoHideAdapter.clearSelection();
            toolbarManager.setTitle("Videos"); // Reset toolbar title

            Toast.makeText(VideoHidden.this, "Images moved back to original locations and deleted from app", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(VideoHidden.this, "Error moving images back", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshImageList();

        Toast.makeText(this, "onResume called", Toast.LENGTH_SHORT).show();
    }

    private void refreshImageList() {
        // Reload video paths
        ArrayList<String> updatedVideoPaths = FileUtils.getVideoPaths(this);

        // Update the adapter's data
        imageVideoHideAdapter.updateImagePaths(updatedVideoPaths);

        // Optionally, handle selection states if needed
    }
}
