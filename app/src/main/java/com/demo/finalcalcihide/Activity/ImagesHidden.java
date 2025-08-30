package com.demo.finalcalcihide.Activity;

import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.finalcalcihide.Adapter.ImageVideoHideAdapter;
import com.demo.finalcalcihide.FileUtils.ImgVidFHandle;
import com.demo.finalcalcihide.GridSpacingItemDecoration;
import com.demo.finalcalcihide.R;
import com.demo.finalcalcihide.Utils.ToolbarManager;
import com.demo.finalcalcihide.ViewPager.ImageandVideoViewPager;
import com.demo.finalcalcihide.Utils.AnimationManager;
import com.demo.finalcalcihide.Utils.FileUtils;

import java.util.ArrayList;
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
    private AnimationManager animationManager;
    private FrameLayout animationContainer;

    private ToolbarManager toolbarManager;
    private RelativeLayout noFileIconLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images_hidden);

        // Set navigation and status bar colors
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.FinalPrimaryColor));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.FinalPrimaryColor));

        // Initialize animation container
        animationContainer = findViewById(R.id.animation_container);
        animationManager = new AnimationManager(this, animationContainer);

        // Initialize UI components
        imageRecyclerView = findViewById(R.id.image_gallery_recycler);
        fab_container = findViewById(R.id.image_gallary_fab_container);
        customToolbarContainer = findViewById(R.id.custom_toolbar_container);
        customBottomAppBar = findViewById(R.id.custom_bottom_appbar);
        customBottomAppBarVisible = findViewById(R.id.custom_btm_appbar_Visible);
        customBottomAppBarDelete = findViewById(R.id.custom_btm_appbar_delete);
        noFileIconLayout = findViewById(R.id.no_file_icon);

        imagePaths = FileUtils.getImagePaths(this);

        // Initialize adapter for images
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

        // Toolbar manager setup
        toolbarManager = new ToolbarManager(this, customToolbarContainer, imageVideoHideAdapter, imagePaths, this, "Images");

        // Setup RecyclerView
        imageRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        int spacing = getResources().getDimensionPixelSize(R.dimen.recycler_item_spacing);
        imageRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3, spacing));
        imageRecyclerView.setAdapter(imageVideoHideAdapter);

        // Initialize toolbar and back press handling
        toolbarManager.setToolbarMenu(false);
        handleOnBackPressed();

        // Show/Hide button click
        customBottomAppBarVisible.setOnClickListener(v -> {
            List<String> selectedPaths = imageVideoHideAdapter.getSelectedImagePaths();
            final long MINIMUM_DISPLAY_TIME = 2500;

            animationManager.handleAnimationProcess(
                    AnimationManager.AnimationType.HIDE_UNHIDE,
                    selectedPaths,
                    MINIMUM_DISPLAY_TIME,
                    () -> ImgVidFHandle.moveImagesBackToOriginalLocationsWrapper(ImagesHidden.this, selectedPaths),
                    (processSuccess, paths) -> stopAnimationAndUpdateUI(processSuccess, paths)
            );
        });

        // Delete button click
        customBottomAppBarDelete.setOnClickListener(v -> {
            List<String> selectedPaths = imageVideoHideAdapter.getSelectedImagePaths();
            final long MINIMUM_DISPLAY_TIME = 2100;

            animationManager.handleAnimationProcess(
                    AnimationManager.AnimationType.DELETE,
                    selectedPaths,
                    MINIMUM_DISPLAY_TIME,
                    () -> ImgVidFHandle.moveImagesBackToRecycleLocationsWrapper(ImagesHidden.this, selectedPaths, null),
                    (processSuccess, paths) -> stopAnimationAndUpdateUI(processSuccess, paths)
            );
        });

        // FAB click
        fab_container.setOnClickListener(v -> {
            Intent intent = new Intent(ImagesHidden.this, ImageVideoBucket.class);
            intent.putExtra("FROM", "Images");
            startActivity(intent);
        });
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

    private void onSelectandDeselect_All(boolean isAnySelected) {
        toolbarManager.setToolbarMenu(isAnySelected);
        setCustomBottomAppBarVisibility(isAnySelected);
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
                        public void onAnimationEnd(android.animation.Animator animation) {
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

    private void stopAnimationAndUpdateUI(boolean processSuccess, List<String> selectedPaths) {
        if (processSuccess) {
            imagePaths.removeAll(selectedPaths);
            imageVideoHideAdapter.notifyDataSetChanged();
            imageVideoHideAdapter.clearSelection();
            refreshImageList();

            Log.d("ImagesHidden", "Images moved back to original locations and deleted from app");

//            Toast.makeText(ImagesHidden.this, "Images moved back to original locations and deleted from app", Toast.LENGTH_SHORT).show();
        } else {
            Log.e(String.valueOf(ImagesHidden.this), "Error moving images back");
//            Toast.makeText(ImagesHidden.this, "Error moving images back", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
            refreshImageList();
    }

    private void refreshImageList() {
        ArrayList<String> updatedImagePaths = FileUtils.getImagePaths(this);
        imageVideoHideAdapter.updateImagePaths(updatedImagePaths);

        if (updatedImagePaths.isEmpty()) {
            noFileIconLayout.setVisibility(View.VISIBLE);
        } else {
            noFileIconLayout.setVisibility(View.GONE);
        }
    }
}
