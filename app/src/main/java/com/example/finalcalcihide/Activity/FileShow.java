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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalcalcihide.Adapter.FileShowAdap;
import com.example.finalcalcihide.FileUtils.ImgVidFHandle;
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
        setContentView(R.layout.file_show_acitivity);

        // Set navigation bar color
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.navigation_bar_color));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.FinalPrimaryColor));

        // Initialize components
        animationContainer = findViewById(R.id.animation_container);
        animationManager = new AnimationManager(this, animationContainer);
        imageRecyclerView = findViewById(R.id.file_gallery_recycler);
        fab_container = findViewById(R.id.file_gallary_fab_container);
        customToolbarContainer = findViewById(R.id.file_custom_toolbar_container);
        customBottomAppBar = findViewById(R.id.file_custom_bottom_appbar);
        customBottomAppBarVisible = findViewById(R.id.custom_btm_appbar_Visible);
        customBottomAppBarDelete = findViewById(R.id.custom_btm_appbar_delete);
        filePaths = FileUtils.getFilePaths(this);

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

        toolbarManager = new ToolbarManager(this, customToolbarContainer, fileAdapter, filePaths, this);
        toolbarManager.setToolbarMenu(false);
        toolbarManager.setTitle("Files");

        imageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        imageRecyclerView.setAdapter(fileAdapter);

        handleOnBackPressed();

        PermissionHandler.requestPermissions(FileShow.this);

        // Show/Hide Button Handler
        customBottomAppBarVisible.setOnClickListener(v -> {
            List<String> selectedPaths = fileAdapter.getSelectedImagePaths();
            final long MINIMUM_DISPLAY_TIME = 2500;

            animationManager.handleAnimationProcess(
                    AnimationManager.AnimationType.HIDE_UNHIDE,
                    selectedPaths,
                    MINIMUM_DISPLAY_TIME,
                    () -> ImgVidFHandle.moveImagesBackToOriginalLocationsWrapper(FileShow.this, selectedPaths),
                    (processSuccess, paths) -> stopAnimationAndUpdateUI(processSuccess, paths)
            );
        });

        // Delete Button Handler
        customBottomAppBarDelete.setOnClickListener(v -> {
            List<String> selectedPaths = fileAdapter.getSelectedImagePaths();
            final long MINIMUM_DISPLAY_TIME = 2100;

            animationManager.handleAnimationProcess(
                    AnimationManager.AnimationType.DELETE,
                    selectedPaths,
                    MINIMUM_DISPLAY_TIME,
                    () -> ImgVidFHandle.moveImagesBackToRecycleLocationsWrapper(FileShow.this, selectedPaths),
                    (processSuccess, paths) -> stopAnimationAndUpdateUI(processSuccess, paths)
            );
        });

        // FAB Click Handler
        fab_container.setOnClickListener(v -> {
            Log.d("FileShow", "Checking permissions before file picker");
            if (PermissionHandler.checkPermissions(this)) {
                Log.d("FileShow", "Launching file picker");
                // Proceed with launching the file picker
                UnicornFilePicker.from(FileShow.this)
                        .addConfigBuilder()
                        .selectMultipleFiles(true)
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
            } else {
                // Request permissions if not granted
                Log.d("FileShow", "Requesting permissions");
                PermissionHandler.requestPermissions(this);
            }
        });


    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQ_UNICORN_FILE && resultCode == RESULT_OK) {
            if (data != null) {
                ArrayList<String> files = data.getStringArrayListExtra("filePaths");

                boolean success = ImgVidFHandle.copyfilesToPrivateStorage(getApplicationContext(), files);

                if (success) {
                    filePaths.clear();
                    filePaths.addAll(FileUtils.getFilePaths(this));

                    runOnUiThread(() -> fileAdapter.notifyDataSetChanged());
                    Toast.makeText(getApplicationContext(), "Files copied to private storage", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Error copying files", Toast.LENGTH_SHORT).show();
                }

                for (String file : files) {
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
        toolbarManager.setTitle("Files");
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

    private void stopAnimationAndUpdateUI(boolean processSuccess, List<String> selectedPaths) {
        if (processSuccess) {
            filePaths.removeAll(selectedPaths);
            fileAdapter.notifyDataSetChanged();
            fileAdapter.clearSelection();
            Toast.makeText(FileShow.this, "Images moved back to original locations", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(FileShow.this, "Error moving images", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshImageList();
    }

    private void refreshImageList() {
        ArrayList<String> updatedImageList = FileUtils.getFilePaths(this);
        filePaths.clear();
        filePaths.addAll(updatedImageList);
        fileAdapter.notifyDataSetChanged();
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
            intent.setDataAndType(docUri, "application/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        }
    }

}
