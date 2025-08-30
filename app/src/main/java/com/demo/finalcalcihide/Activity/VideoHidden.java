package com.demo.finalcalcihide.Activity;

import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.finalcalcihide.Adapter.VideoHiddenAdapter;
import com.demo.finalcalcihide.FileUtils.ImgVidFHandle;
import com.demo.finalcalcihide.GridSpacingItemDecoration;
import com.demo.finalcalcihide.R;
import com.demo.finalcalcihide.Utils.ToolbarManager;
import com.demo.finalcalcihide.ViewPager.ImageandVideoViewPager;
import com.demo.finalcalcihide.Utils.AnimationManager;
import com.demo.finalcalcihide.Utils.FileUtils;

import java.util.ArrayList;
import java.util.List;

public class VideoHidden extends AppCompatActivity {

    private static final int PAGE_SIZE = 35; // Load 20 videos per page
    private static final int PRELOAD_THRESHOLD = 15; // Start loading when 5 items from end

    private ArrayList<String> allVideoPaths = new ArrayList<>();
    private ArrayList<String> currentVideoPaths = new ArrayList<>();
    private VideoHiddenAdapter videoHideAdapter;
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

    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_hidden);

        // Set navigation and status bar colors
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.FinalPrimaryColor));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.FinalPrimaryColor));

        initializeViews();
        setupRecyclerView();
        setupToolbar();
        setupClickListeners();
        handleOnBackPressed();

        // Load initial data
        loadInitialData();
    }

    private void initializeViews() {
        animationContainer = findViewById(R.id.video_animation_container);
        animationManager = new AnimationManager(this, animationContainer);

        imageRecyclerView = findViewById(R.id.video_image_gallery_recycler);
        fab_container = findViewById(R.id.video_image_gallary_fab_container);
        customToolbarContainer = findViewById(R.id.video_custom_toolbar_container);
        customBottomAppBar = findViewById(R.id.video_custom_bottom_appbar);
        customBottomAppBarVisible = findViewById(R.id.custom_btm_appbar_Visible);
        customBottomAppBarDelete = findViewById(R.id.custom_btm_appbar_delete);
        noFileIconLayout = findViewById(R.id.video_no_file_icon);

//        // Add loading progress bar (you'll need to add this to your layout)
//        loadingProgressBar = findViewById(R.id.video_loading_progress);
//        if (loadingProgressBar == null) {
//            // Create programmatically if not in layout
//            loadingProgressBar = new ProgressBar(this);
//            loadingProgressBar.setVisibility(View.GONE);
//        }
    }

    private void setupRecyclerView() {
        // Initialize adapter with pagination support
        videoHideAdapter = new VideoHiddenAdapter(this, currentVideoPaths, new VideoHiddenAdapter.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int position) {
                handleItemClick(position);
            }

            @Override
            public void onSelectionChanged(boolean isSelected) {
                onSelectandDeselect_All(isSelected);
            }
        });

        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        imageRecyclerView.setLayoutManager(layoutManager);

        int spacing = getResources().getDimensionPixelSize(R.dimen.recycler_item_spacing);
        imageRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3, spacing));
        imageRecyclerView.setAdapter(videoHideAdapter);

        // Add scroll listener for pagination
        imageRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!isLoading && !isLastPage && dy > 0) { // Only when scrolling down
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition + PRELOAD_THRESHOLD) >= totalItemCount) {
                        loadNextPage();
                    }
                }
            }
        });
    }

    private void setupToolbar() {
        toolbarManager = new ToolbarManager(this, customToolbarContainer, videoHideAdapter, currentVideoPaths, this, "Videos");
        toolbarManager.setToolbarMenu(false);
    }

    private void setupClickListeners() {
        // Show/Hide button click
        customBottomAppBarVisible.setOnClickListener(v -> {
            List<String> selectedPaths = videoHideAdapter.getSelectedImagePaths();
            final long MINIMUM_DISPLAY_TIME = 2500;

            animationManager.handleAnimationProcess(
                    AnimationManager.AnimationType.HIDE_UNHIDE,
                    selectedPaths,
                    MINIMUM_DISPLAY_TIME,
                    () -> ImgVidFHandle.moveImagesBackToOriginalLocationsWrapper(VideoHidden.this, selectedPaths),
                    (processSuccess, paths) -> stopAnimationAndUpdateUI(processSuccess, paths)
            );
        });

        // Delete button click
        customBottomAppBarDelete.setOnClickListener(v -> {
            List<String> selectedPaths = videoHideAdapter.getSelectedImagePaths();
            final long MINIMUM_DISPLAY_TIME = 2100;

            animationManager.handleAnimationProcess(
                    AnimationManager.AnimationType.DELETE,
                    selectedPaths,
                    MINIMUM_DISPLAY_TIME,
                    () -> ImgVidFHandle.moveImagesBackToRecycleLocationsWrapper(VideoHidden.this, selectedPaths, null),
                    (processSuccess, paths) -> stopAnimationAndUpdateUI(processSuccess, paths)
            );
        });

        // FAB click
        fab_container.setOnClickListener(v -> {
            Intent intent = new Intent(VideoHidden.this, ImageVideoBucket.class);
            intent.putExtra("FROM", "Videos");
            startActivity(intent);
        });
    }

    private void loadInitialData() {
//        showLoading(true);

        // Load all video paths in background
        new Thread(() -> {
            allVideoPaths = FileUtils.getVideoPaths(this);

            runOnUiThread(() -> {
                if (allVideoPaths.isEmpty()) {
                    showNoFilesLayout(true);
//                    showLoading(false);
                } else {
                    showNoFilesLayout(false);
                    loadFirstPage();
                }
            });
        }).start();
    }

    private void loadFirstPage() {
        currentPage = 0;
        isLastPage = false;
        currentVideoPaths.clear();
        loadNextPage();
    }

    private void loadNextPage() {
        if (isLoading || isLastPage) return;

        isLoading = true;
//        showLoading(true);

        // Calculate start and end indices
        int startIndex = currentPage * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, allVideoPaths.size());

        // Load next batch in background thread
        new Thread(() -> {
            List<String> newVideos = new ArrayList<>();

            for (int i = startIndex; i < endIndex; i++) {
                newVideos.add(allVideoPaths.get(i));
            }

            // Update UI on main thread
            runOnUiThread(() -> {
                int oldSize = currentVideoPaths.size();
                currentVideoPaths.addAll(newVideos);

                if (currentPage == 0) {
                    videoHideAdapter.notifyDataSetChanged();
                } else {
                    videoHideAdapter.notifyItemRangeInserted(oldSize, newVideos.size());
                }

                currentPage++;
                isLoading = false;
                isLastPage = endIndex >= allVideoPaths.size();
//                showLoading(false);
            });
        }).start();
    }

//    private void showLoading(boolean show) {
//        if (loadingProgressBar != null) {
//            loadingProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
//        }
//    }

    private void showNoFilesLayout(boolean show) {
        noFileIconLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void handleItemClick(int position) {
        if (videoHideAdapter.isSelectedAny()) {
            videoHideAdapter.toggleSelection(position);
        } else {
            Intent intent = new Intent(this, ImageandVideoViewPager.class);
            // Pass all video paths for proper navigation
            intent.putStringArrayListExtra("imagePaths", allVideoPaths);
            // Calculate the actual position in the full list
            String currentVideoPath = currentVideoPaths.get(position);
            int actualPosition = allVideoPaths.indexOf(currentVideoPath);
            intent.putExtra("position", actualPosition);
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
                if (videoHideAdapter.isSelectedAny()) {
                    videoHideAdapter.clearSelection();
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
            // Remove from both lists
            allVideoPaths.removeAll(selectedPaths);
            currentVideoPaths.removeAll(selectedPaths);

            videoHideAdapter.notifyDataSetChanged();
            videoHideAdapter.clearSelection();

            // Check if we need to show no files layout
            if (allVideoPaths.isEmpty()) {
                showNoFilesLayout(true);
            }

//            Toast.makeText(VideoHidden.this, "Videos moved back to original locations and deleted from app", Toast.LENGTH_SHORT).show();
        } else {
//            Toast.makeText(VideoHidden.this, "Error moving videos back", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshVideoList();
    }

    private void refreshVideoList() {
        // Reload all data when resuming
        loadInitialData();
    }

    // Method to manually refresh data
    public void refreshData() {
        currentVideoPaths.clear();
        videoHideAdapter.notifyDataSetChanged();
        loadInitialData();
    }
}