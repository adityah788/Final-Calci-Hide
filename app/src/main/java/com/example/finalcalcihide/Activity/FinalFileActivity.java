package com.example.finalcalcihide.Activity;

import static android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ActivityNotFoundException;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.balysv.materialripple.BuildConfig;
import com.example.finalcalcihide.Adapter.FinalFileAdap;
import com.example.finalcalcihide.FileUtils.ImgVidFHandle;
import com.example.finalcalcihide.R;
import com.example.finalcalcihide.Utils.AnimationManager;
import com.example.finalcalcihide.Utils.FileUtils;
import com.example.finalcalcihide.Utils.ToolbarManager;
import com.example.finalcalcihide.ViewPager.ImageandVideoViewPager;

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


    private final static int APP_STORAGE_ACCESS_REQUEST_CODE = 501;
    private static final int REQUEST_STORAGE_PERMISSIONS = 123;
    private static final int REQUEST_MEDIA_PERMISSIONS = 456;
    private final String readPermission = android.Manifest.permission.READ_EXTERNAL_STORAGE;
    private final String writePermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_final_file);

        checkPermissions();

        // Set navigation bar color to black
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.navigation_bar_color));
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
        toolbarManager = new ToolbarManager(this, customToolbarContainer, finalFileAdapter, filePaths, this);

        imageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        imageRecyclerView.setAdapter(finalFileAdapter);

        // Initialize Toolbar and Back Press Handling
        toolbarManager.setToolbarMenu(false);
        handleOnBackPressed();


        // Handle Show/Hide Button Click
        customBottomAppBarVisible.setOnClickListener(v -> {
            List<String> selectedPaths = finalFileAdapter.getSelectedImagePaths();
            final long MINIMUM_DISPLAY_TIME = 2500; // in milliseconds

            animationManager.handleAnimationProcess(
                    AnimationManager.AnimationType.HIDE_UNHIDE,
                    selectedPaths,
                    MINIMUM_DISPLAY_TIME,
                    () -> {
                        ImgVidFHandle.moveImagesBackToOriginalLocationsWrapper(FinalFileActivity.this, selectedPaths);
                    },
                    (processSuccess, paths) -> stopAnimationAndUpdateUI(processSuccess, paths)
            );
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
                        ImgVidFHandle.moveImagesBackToRecycleLocationsWrapper(FinalFileActivity.this, selectedPaths);
                    },
                    (processSuccess, paths) -> stopAnimationAndUpdateUI(processSuccess, paths)
            );
        });

        // Fab click handler for file picker
        fab_container.setOnClickListener(v -> {
            // Proceed with the file picker only if permissions are granted
            UnicornFilePicker.from(FinalFileActivity.this)
                    .addConfigBuilder()
                    .selectMultipleFiles(true)
                    .setRootDirectory(Environment.getExternalStorageDirectory().getAbsolutePath())
                    .showHiddenFiles(false)
                    .setFilters(new String[]{"pdf", "png", "jpg", "jpeg"})
                    .addItemDivider(true)
                    .build()
                    .forResult(Constants.REQ_UNICORN_FILE);

        });


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions were granted. You can proceed with your file operations.
                //Showing dialog when Show Dialog button is clicked.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    //Android version is 11 and above so to access all types of files we have to give
                    //special permission so show user a dialog..
                    accessAllFilesPermissionDialog();
                } else {
                    //Android version is 10 and below so need of special permission...
//                    filePickerDialog.show();
                    Toast.makeText(this, "Android version is 10 and below so need of special permission", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Permissions were denied. Show a rationale dialog or inform the user about the importance of these permissions.
                showRationaleDialog();
            }
        }

        //This conditions only works on Android 13 and above versions
        if (requestCode == REQUEST_MEDIA_PERMISSIONS) {
            if (grantResults.length > 0 && areAllPermissionsGranted(grantResults)) {
                // Permissions were granted. You can proceed with your media file operations.
                //Showing dialog when Show Dialog button is clicked.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    //Android version is 11 and above so to access all types of files we have to give
                    //special permission so show user a dialog..
                    accessAllFilesPermissionDialog();
                }
            } else {
                // Permissions were denied. Show a rationale dialog or inform the user about the importance of these permissions.
                showRationaleDialog();
            }
        }
    }


    private boolean areAllPermissionsGranted(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void showRationaleDialog() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, readPermission) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this, writePermission)) {
            // Show a rationale dialog explaining why the permissions are necessary.
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("This app needs storage permissions to read and write files.")
                    .setPositiveButton("OK", (dialog, which) -> {
                        // Request permissions when the user clicks OK.
                        ActivityCompat.requestPermissions(FinalFileActivity.this, new String[]{readPermission, writePermission}, REQUEST_STORAGE_PERMISSIONS);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                        // Handle the case where the user cancels the permission request.
                    })
                    .show();
        } else {
            // Request permissions directly if no rationale is needed.
            ActivityCompat.requestPermissions(this, new String[]{readPermission, writePermission}, REQUEST_STORAGE_PERMISSIONS);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void accessAllFilesPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permission Needed")
                .setMessage("This app needs all files access permissions to view files from your storage. Clicking on OK will redirect you to new window were you have to enable the option.")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Request permissions when the user clicks OK.
                    Intent intent = new Intent(ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                    startActivityForResult(intent, APP_STORAGE_ACCESS_REQUEST_CODE);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                    // Handle the case where the user cancels the permission request.
//                    filePickerDialog.show();
                    Toast.makeText(this, "Handle the case where the user cancels the permission request.", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_STORAGE_ACCESS_REQUEST_CODE) {
            // Permission granted. Now resume your workflow.
//            filePickerDialog.show();
            Toast.makeText(this, "Permission granted. Now resume your workflow", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleItemClick(int position) {
        if (finalFileAdapter.isSelectedAny()) {
            finalFileAdapter.toggleSelection(position);
        } else {
            Intent intent = new Intent(this, ImageandVideoViewPager.class);
            intent.putStringArrayListExtra("imagePaths", filePaths);
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
            finalFileAdapter.notifyDataSetChanged();
            finalFileAdapter.clearSelection();
            Toast.makeText(FinalFileActivity.this, "Images moved back to original locations and deleted from app", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(FinalFileActivity.this, "Error moving images back", Toast.LENGTH_SHORT).show();
        }
    }


    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            //As the device is Android 13 and above so I want the permission of accessing Audio, Images, Videos
            //You can ask permission according to your requirements what you want to access.
            String audioPermission = android.Manifest.permission.READ_MEDIA_AUDIO;
            String imagesPermission = android.Manifest.permission.READ_MEDIA_IMAGES;
            String videoPermission = android.Manifest.permission.READ_MEDIA_VIDEO;
            // Check for permissions and request them if needed
            if (ContextCompat.checkSelfPermission(FinalFileActivity.this, audioPermission) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(FinalFileActivity.this, imagesPermission) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(FinalFileActivity.this, videoPermission) == PackageManager.PERMISSION_GRANTED) {
                // You have the permissions, you can proceed with your media file operations.
                //Showing dialog when Show Dialog button is clicked.

                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    // Handle the error, perhaps notify the user
                    Toast.makeText(this, "Permission settings not found", Toast.LENGTH_SHORT).show();
                }

//                filePickerDialog.show();
                Toast.makeText(this, " You have the permissions, you can proceed with your media file operations", Toast.LENGTH_SHORT).show();
            } else {
                // You don't have the permissions. Request them.
                ActivityCompat.requestPermissions(FinalFileActivity.this, new String[]{audioPermission, imagesPermission, videoPermission}, REQUEST_MEDIA_PERMISSIONS);
            }
        } else {
            //Android version is below 13 so we are asking normal read and write storage permissions
            // Check for permissions and request them if needed
            if (ContextCompat.checkSelfPermission(FinalFileActivity.this, readPermission) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(FinalFileActivity.this, writePermission) == PackageManager.PERMISSION_GRANTED) {
                // You have the permissions, you can proceed with your file operations.
                // Show the file picker dialog when needed
//                filePickerDialog.show();
                Toast.makeText(this, "You have the permissions, you can proceed with your file operations. Below 13", Toast.LENGTH_SHORT).show();
            } else {
                // You don't have the permissions. Request them.
                ActivityCompat.requestPermissions(FinalFileActivity.this, new String[]{readPermission, writePermission}, REQUEST_STORAGE_PERMISSIONS);
            }
        }
    }


}