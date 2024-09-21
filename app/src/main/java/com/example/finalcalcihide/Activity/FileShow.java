package com.example.finalcalcihide.Activity;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalcalcihide.Adapter.FileShowAdap;
import com.example.finalcalcihide.Adapter.ImageVideoHideAdapter;
import com.example.finalcalcihide.FileUtils.ImgVidFHandle;
import com.example.finalcalcihide.GridSpacingItemDecoration;
import com.example.finalcalcihide.R;
import com.example.finalcalcihide.Utils.ToolbarManager;
import com.example.finalcalcihide.Utils.AnimationManager;
import com.example.finalcalcihide.Utils.FileUtils;
import com.example.finalcalcihide.PermissionHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import abhishekti7.unicorn.filepicker.UnicornFilePicker;
import abhishekti7.unicorn.filepicker.utils.Constants;


// ... [imports]

public class FileShow extends AppCompatActivity {
    private ArrayList<String> filePaths = new ArrayList<>();
    private FileShowAdap fileAdapter;
    private RecyclerView imageRecyclerView;
    private LinearLayout customBottomAppBarDelete;
    private LinearLayout customToolbarContainer;
    private LinearLayout customBottomAppBar;
    private LinearLayout customBottomAppBarVisible;
    private FrameLayout fab_container;

    private AnimationManager animationManager;
    private FrameLayout animationContainer;

    private ToolbarManager toolbarManager;

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images_hidden);

        // Set navigation bar color to black
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.navigation_bar_color));

        // Initialize animation container
        animationContainer = findViewById(R.id.animation_container);
        animationManager = new AnimationManager(this, animationContainer);

        // Request necessary permissions
        PermissionHandler.requestPermissions(FileShow.this);

        // Initialize UI components
        imageRecyclerView = findViewById(R.id.image_gallery_recycler);
        fab_container = findViewById(R.id.image_gallary_fab_container);
        customToolbarContainer = findViewById(R.id.custom_toolbar_container);
        customBottomAppBar = findViewById(R.id.custom_bottom_appbar);
        customBottomAppBarVisible = findViewById(R.id.custom_btm_appbar_Visible);
        customBottomAppBarDelete = findViewById(R.id.custom_btm_appbar_delete);
        filePaths = FileUtils.getFilePaths(this);

        // Initialize Adapter with the correct listener
        fileAdapter = new FileShowAdap(this, filePaths, new FileShowAdap.OnItemSelectedListener() {
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
        toolbarManager = new ToolbarManager(this, customToolbarContainer, fileAdapter, filePaths, this);

        // Setup RecyclerView
//        imageRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
//        int spacing = getResources().getDimensionPixelSize(R.dimen.recycler_item_spacing);
//        imageRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3, spacing));

        imageRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        imageRecyclerView.setAdapter(fileAdapter);

        // Initialize Toolbar and Back Press Handling
        toolbarManager.setToolbarMenu(false);
        handleOnBackPressed();

        // Handle Show/Hide Button Click
        customBottomAppBarVisible.setOnClickListener(v -> {
            List<String> selectedPaths = fileAdapter.getSelectedImagePaths();
            final long MINIMUM_DISPLAY_TIME = 2500; // in milliseconds

            animationManager.handleAnimationProcess(
                    AnimationManager.AnimationType.HIDE_UNHIDE,
                    selectedPaths,
                    MINIMUM_DISPLAY_TIME,
                    () -> {
                        // Background task: Move images back to original locations
                        ImgVidFHandle.moveImagesBackToOriginalLocationsWrapper(FileShow.this, selectedPaths);
                        // Update processSuccess based on actual task outcome
                        // For example, set to true if task succeeds, false otherwise
                    },
                    (processSuccess, paths) -> stopAnimationAndUpdateUI(processSuccess, paths)
            );
        });

        // Handle Delete Button Click
        customBottomAppBarDelete.setOnClickListener(v -> {
            List<String> selectedPaths = fileAdapter.getSelectedImagePaths();
            final long MINIMUM_DISPLAY_TIME = 2100; // in milliseconds

            animationManager.handleAnimationProcess(
                    AnimationManager.AnimationType.DELETE,
                    selectedPaths,
                    MINIMUM_DISPLAY_TIME,
                    () -> {
                        // Background task: Move images back to recycle locations
                        ImgVidFHandle.moveImagesBackToRecycleLocationsWrapper(FileShow.this, selectedPaths);
                        // Update processSuccess based on actual task outcome
                    },
                    (processSuccess, paths) -> stopAnimationAndUpdateUI(processSuccess, paths)
            );
        });

        // Handle FAB Click
        fab_container.setOnClickListener(v -> {
            UnicornFilePicker.from(FileShow.this) // Corrected context
                    .addConfigBuilder()
                    .selectMultipleFiles(true)
                    .showOnlyDirectory(false)
                    .setRootDirectory(Environment.getExternalStorageDirectory().getAbsolutePath())
                    .showHiddenFiles(true)
                    .setFilters(new String[]{
                            "pdf", "png", "jpg", "jpeg",       // Images
                            "mp3", "wav", "flac", "m4a",       // Audio
                            "mp4", "3gp", "mkv", "avi", "mov", // Video
                            "doc", "docx", "xls", "xlsx",      // Documents
                            "ppt", "pptx", "txt",              // Documents and Text
                            "zip", "rar"})
                    .addItemDivider(true)
                    //  .theme(R.style.UnicornFilePicker_Dracula)
                    .build()
                    .forResult(Constants.REQ_UNICORN_FILE);
        });
    }

    // Moved onActivityResult outside of onCreate
    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Constants.REQ_UNICORN_FILE && resultCode == RESULT_OK){
            if(data != null){
                ArrayList<String> files = data.getStringArrayListExtra("filePaths");

                boolean success = ImgVidFHandle.copyfilesToPrivateStorage(getApplicationContext(), files);

                if (success) {
                    // Update imagePaths after copying images
                    filePaths.clear();
                    filePaths.addAll(FileUtils.getFilePaths(this));   // Reload paths from storage

                    // Notify the adapter that data has changed
                    runOnUiThread(() -> fileAdapter.notifyDataSetChanged());

                    Toast.makeText(getApplicationContext(), "File copied to private storage", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Error copying images", Toast.LENGTH_SHORT).show();
                }

                for(String file : files){
                    Log.e(TAG, file);
                }
            }
        }
    }

    private void handleItemClick(int position) {
        if (fileAdapter.isSelectedAny()) {
            fileAdapter.toggleSelection(position);
        } else {
            String filePath = filePaths.get(position);
            File file = new File(filePath);
            boolean isAudio = FileShowAdap.isAudioFile(file);
            boolean isDocument = FileShowAdap.isDocumentFile(file);
            boolean isImage = FileShowAdap.isImageFile(file);
            boolean isVideo = FileShowAdap.isVideoFile(file);
            handleFileClick(file, isAudio, isDocument, isImage, isVideo);
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
                if (fileAdapter.isSelectedAny()) {
                    fileAdapter.clearSelection();
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
            filePaths.removeAll(selectedPaths);
            fileAdapter.notifyDataSetChanged();
            fileAdapter.clearSelection();

            Toast.makeText(FileShow.this, "Images moved back to original locations and deleted from app", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(FileShow.this, "Error moving images back", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshImageList();

        Toast.makeText(this, "onResume called", Toast.LENGTH_SHORT).show();
    }

    private void refreshImageList() {
        // Reload image paths
        ArrayList<String> updatedImagePaths = FileUtils.getFilePaths(this);

        // Update the adapter's data
        fileAdapter.updateImagePaths(updatedImagePaths);

        // Optionally, handle selection states if needed
    }

    private void handleFileClick(File file, boolean isAudio, boolean isDocument, boolean isImage, boolean isVideo) {
        // Handle click based on file type
        if (isAudio) {
            Uri uri = ImgVidFHandle.getFileUri(this, file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "audio/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent chooser = Intent.createChooser(intent, "Open with");
            if (chooser.resolveActivity(this.getPackageManager()) != null) {
                this.startActivity(chooser);
            }
        } else if (isDocument) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = ImgVidFHandle.getFileUri(this, file);
            String mimeType = getMimeType(file);
            intent.setDataAndType(uri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent chooser = Intent.createChooser(intent, "Open with");
            if (chooser.resolveActivity(this.getPackageManager()) != null) { // Changed from intent to chooser
                this.startActivity(chooser);
            }
        } else if (isImage) {
            Intent intent = new Intent(this, SinglePV.class);
            intent.putExtra("image_path", file.getAbsolutePath());
            this.startActivity(intent);
        } else if (isVideo) {
            Intent intent = new Intent(this, SinglePV.class);
            intent.putExtra("video_path", file.getAbsolutePath());
            this.startActivity(intent);
        } else {
            Toast.makeText(this, "Unsupported file type", Toast.LENGTH_SHORT).show();
        }
    }

    private String getMimeType(File file) {
        if (file.getName().endsWith(".pdf")) {
            return "application/pdf";
        } else if (file.getName().endsWith(".doc") || file.getName().endsWith(".docx")) {
            return "application/msword";
        } else if (file.getName().endsWith(".ppt") || file.getName().endsWith(".pptx")) {
            return "application/vnd.ms-powerpoint";
        } else if (file.getName().endsWith(".txt")) {
            return "text/plain";
        } else {
            return "*/*"; // Fallback MIME type
        }
    }
}
