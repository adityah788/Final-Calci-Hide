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
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.finalcalcihide.Adapter.FinalFileAdap;
import com.example.finalcalcihide.FileUtils.ImgVidFHandle;
import com.example.finalcalcihide.R;
import com.example.finalcalcihide.Utils.AnimationManager;
import com.example.finalcalcihide.Utils.FileUtils;
import com.example.finalcalcihide.Utils.ToolbarManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import abhishekti7.unicorn.filepicker.UnicornFilePicker;
import abhishekti7.unicorn.filepicker.utils.Constants;

public class FinalFileActivity extends AppCompatActivity {


    private ArrayList<String> filePaths = new ArrayList<>();
    private FinalFileAdap finalFileAdapter;
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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_final_file);


        // Set navigation bar color to black
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.FinalPrimaryColor));



        // Initialize animation container
        animationContainer = findViewById(R.id.final_file_animation_container);
        animationManager = new AnimationManager(this, animationContainer);

        // Initialize UI components
        imageRecyclerView = findViewById(R.id.final_file_image_gallery_recycler);
        fab_container = findViewById(R.id.final_file_gallary_fab_container);
        customToolbarContainer = findViewById(R.id.final_file_custom_toolbar_container);
        customBottomAppBar = findViewById(R.id.final_file_custom_bottom_appbar);
        customBottomAppBarVisible = findViewById(R.id.custom_btm_appbar_Visible);
        customBottomAppBarDelete = findViewById(R.id.custom_btm_appbar_delete);
        filePaths = FileUtils.getFilePaths(this);
        noFileIconLayout = findViewById(R.id.final_file_no_file_icon);


        // Initialize Adapter
        finalFileAdapter = new FinalFileAdap(this, filePaths, new FinalFileAdap.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int position) {
                handleItemClick(position);
            }

            @Override
            public void onSelectionChanged(boolean isSelected) {
                onSelectandDeselect_All(isSelected);
            }
        });

        // Initialize ToolbarManager
        toolbarManager = new ToolbarManager(this, customToolbarContainer, finalFileAdapter, filePaths, this,"Files");



        // Initialize Toolbar and Back Press Handling
        toolbarManager.setToolbarMenu(false);
        handleOnBackPressed();

        imageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        imageRecyclerView.setAdapter(finalFileAdapter);

        // Handle Show/Hide Button Click
        customBottomAppBarVisible.setOnClickListener(v -> {
            List<String> selectedPaths = finalFileAdapter.getSelectedImagePaths();
            final long MINIMUM_DISPLAY_TIME = 2500; // in milliseconds

            animationManager.handleAnimationProcess(
                    AnimationManager.AnimationType.HIDE_UNHIDE,
                    selectedPaths,
                    MINIMUM_DISPLAY_TIME,
                    () -> {
                        ImgVidFHandle.moveFilesBackToOriginalLocationsWrapper(FinalFileActivity.this, selectedPaths);
                    },
                    (processSuccess, paths) -> stopAnimationAndUpdateUI(processSuccess, paths)
            );
        });



        fab_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UnicornFilePicker.from(FinalFileActivity.this)
                        .addConfigBuilder()
                        .selectMultipleFiles(true)
                        .theme(R.style.UnicornFilePickert_Mytheme)
                        .showOnlyDirectory(false)
                        .setRootDirectory(Environment.getExternalStorageDirectory().getAbsolutePath())
                        .showHiddenFiles(true)
                        .setFilters(new String[]{
                                "pdf", "png", "jpg", "jpeg",
                                "mp3", "wav", "flac", "m4a",
                                "mp4", "3gp", "mkv", "avi", "mov",
                                "doc", "docx", "xls", "xlsx",
                                "ppt", "pptx", "txt",
                                "zip", "rar"})
                        .addItemDivider(true)
                        .build()
                        .forResult(Constants.REQ_UNICORN_FILE);
            }
        });



        // Handle Delete Button Click
        customBottomAppBarDelete.setOnClickListener(v -> {
            List<String> selectedPaths = finalFileAdapter.getSelectedImagePaths();
            final long MINIMUM_DISPLAY_TIME = 2100; // in milliseconds

            animationManager.handleAnimationProcess(
                    AnimationManager.AnimationType.DELETE,
                    selectedPaths,
                    MINIMUM_DISPLAY_TIME,
                    () -> {
                        ImgVidFHandle.moveImagesBackToRecycleLocationsWrapper(FinalFileActivity.this, selectedPaths,"FinalFileActivity");
                    },
                    (processSuccess, paths) -> stopAnimationAndUpdateUI(processSuccess, paths)
            );
        });


    }


    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQ_UNICORN_FILE && resultCode == RESULT_OK) {
            if (data != null) {
                ArrayList<String> files = data.getStringArrayListExtra("filePaths");

                // Only proceed with the animation if there are files
                if (files != null && !files.isEmpty()) {
                    // Initiate the animation process before copying files
                    final long MINIMUM_DISPLAY_TIME = 2500; // in milliseconds

                    animationManager.handleAnimationProcess(
                            AnimationManager.AnimationType.HIDE_UNHIDEE,  // Trigger hide animation
                            files,
                            MINIMUM_DISPLAY_TIME,
                            () -> {
                                // Perform file copy after animation begins
                                boolean success = ImgVidFHandle.copyfilesToPrivateStorage(getApplicationContext(), files);

                                if (success) {
                                    filePaths.clear();
                                    filePaths.addAll(FileUtils.getFilePaths(this));

                                    // Update UI after successful copy
                                    runOnUiThread(() -> {
                                        Toast.makeText(getApplicationContext(), "Files copied to private storage", Toast.LENGTH_SHORT).show();
                                        refreshRecyclerView();  // Refresh RecyclerView after file copy
                                    });
                                } else {
                                    runOnUiThread(() -> {
                                        Toast.makeText(getApplicationContext(), "Error copying files", Toast.LENGTH_SHORT).show();
                                    });
                                }

                                // Log file paths for debugging
                                for (String file : files) {
                                    Log.e(TAG, file);
                                }
                            },
                            (processSuccess, paths) -> stopAnimationAndUpdateUI(processSuccess, paths)
                    );
                } else {
                    // Optionally, handle the case when there are no files (if necessary)
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), "No files selected", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }
    }



    private void handleItemClick(int position) {
        if (finalFileAdapter.isSelectedAny()) {
            finalFileAdapter.toggleSelection(position);
        } else {
            String filePath = filePaths.get(position);
            File file = new File(filePath);
            boolean isAudio = finalFileAdapter.isAudioFile(file);
            boolean isDocument = finalFileAdapter.isDocumentFile(file);
            boolean isImage = finalFileAdapter.isImageFile(file);
            boolean isVideo = finalFileAdapter.isVideoFile(file);
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
                if (finalFileAdapter.isSelectedAny()) {
                    finalFileAdapter.clearSelection();
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
            filePaths.removeAll(selectedPaths);
//            finalFileAdapter.notifyDataSetChanged();
            refreshRecyclerView();
//            finalFileAdapter.clearSelection();
            Toast.makeText(FinalFileActivity.this, "Images moved back to original locations and deleted from app", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(FinalFileActivity.this, "Error moving images back", Toast.LENGTH_SHORT).show();
        }
    }


    private void handleFileClick(File file, boolean isAudio, boolean isDocument, boolean isImage, boolean isVideo) {
        if (isImage) {
            Uri imageUri = ImgVidFHandle.getFileUri(this, file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(imageUri, "image/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } else if (isVideo) {
            Uri videoUri = ImgVidFHandle.getFileUri(this, file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(videoUri, "video/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } else if (isAudio) {
            Uri audioUri = ImgVidFHandle.getFileUri(this, file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(audioUri, "audio/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } else if (isDocument) {
            Uri docUri = ImgVidFHandle.getFileUri(this, file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(docUri, "application/pdf"); // Set MIME type to PDF
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        ArrayList<String> updatedVideoPaths = FileUtils.getFilePaths(this);

        // Update the adapter's data
        finalFileAdapter.updatefilePaths(updatedVideoPaths);

        // Optionally, handle selection states if needed

        // Check if there are any images available
        if (updatedVideoPaths.isEmpty()) {
            // If no images are found, make the "No File" layout visible
            noFileIconLayout.setVisibility(View.VISIBLE);

        } else {
            // If images are available, hide the "No File" layout
            noFileIconLayout.setVisibility(View.GONE);

        }



    }

    private void refreshRecyclerView() {
        ArrayList<String> updatedVideoPaths = FileUtils.getFilePaths(this);

        // Update the adapter's data
        finalFileAdapter.updatefilePaths(updatedVideoPaths);

        // Optionally, handle selection states if needed

        // Check if there are any images available
        if (updatedVideoPaths.isEmpty()) {
            // If no images are found, make the "No File" layout visible
            noFileIconLayout.setVisibility(View.VISIBLE);

        } else {
            // If images are available, hide the "No File" layout
            noFileIconLayout.setVisibility(View.GONE);

        }
    }






}