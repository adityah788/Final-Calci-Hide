package com.example.finalcalcihide.Activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalcalcihide.Adapter.IntruderAdap;

import android.Manifest;

import com.example.finalcalcihide.R;
import com.example.finalcalcihide.Utils.AnimationManager;
import com.example.finalcalcihide.Utils.FileUtils;
import com.example.finalcalcihide.Utils.IntruderUtils;
import com.example.finalcalcihide.ViewPager.ImageandVideoViewPager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.shawnlin.numberpicker.NumberPicker;

public class Intruder extends AppCompatActivity {

    private ArrayList<String> intruderPaths = new ArrayList<>();
    private RecyclerView intruderRecyclerView;
    private IntruderAdap intruderAdap;
    private LinearLayout containerCustomBottomAppBar;
    private ImageView selectandDeselectAll, deleteIcon, settingCount, backarrow;
    private boolean isAllSelected = false; // To keep track of selection state
    private AnimationManager animationManager;
    private FrameLayout animationContainer;
    private SwitchCompat switchCompattoggle;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private SharedPreferences sharedPreferences;

    // Define a constant for your preference name
    private static final String PREFS_NAME = "MyIntruder";
    private static final String KEY_TAKE_SELFIE = "take_selfie";
    boolean hasDeniedPermission;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intruder);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.FinalPrimaryColor));


        intruderRecyclerView = findViewById(R.id.intruder_selfie_gallery_recycler);
        containerCustomBottomAppBar = findViewById(R.id.container_custom_bottom_appbar_delete);
        selectandDeselectAll = findViewById(R.id._intruder_contextual_toolbar_select_and_deselect_all);
        deleteIcon = findViewById(R.id.intruder_main_toobar_menu_icon);
        animationContainer = findViewById(R.id.real_intruder_animation_container);
        switchCompattoggle = findViewById(R.id.activity_intruder_switch);
        settingCount = findViewById(R.id.intruder_main_toobar_setting);
        backarrow = findViewById(R.id.main_toolbar_back_arrow);
        intruderPaths = FileUtils.getIntruderPaths(this);


        intruderAdap = new IntruderAdap(this, intruderPaths, new IntruderAdap.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int position) {
                handleItemClick(position);
            }

            @Override
            public void onSelectionChanged(boolean isSelected) {
                onSelectandDeselect_All(isSelected);
                // Show or hide the menu icon based on selection
                if (intruderAdap.isSelectedAny()) {
                    deleteIcon.setVisibility(View.GONE);  // Hide menu icon
                    settingCount.setVisibility(View.GONE);
                } else {
                    deleteIcon.setVisibility(View.VISIBLE);  // Show menu icon
                    settingCount.setVisibility(View.VISIBLE);
                }
            }
        });

        intruderRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        intruderRecyclerView.setAdapter(intruderAdap);

        // Initialize AnimationManager
        animationManager = new AnimationManager(this, animationContainer);

        handleOnBackPressed();

        // Handle "Select/Deselect All" click
        selectandDeselectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAllSelected) {
                    intruderAdap.selectAll(false);  // Pass 'false' to deselect all items
                    selectandDeselectAll.setImageResource(R.drawable.baseline_check_box_outline_blank_24);
                } else {
                    intruderAdap.selectAll(true);  // Pass 'true' to select all items
                    selectandDeselectAll.setImageResource(R.drawable.baseline_check_box_24);
                }
                isAllSelected = !isAllSelected;
            }
        });

        deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showdeleteiconDialog();
            }
        });

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        switchCompattoggle = findViewById(R.id.activity_intruder_switch);

        // Load the initial state from SharedPreferences
        boolean isTakeSelfieEnabled = sharedPreferences.getBoolean(KEY_TAKE_SELFIE, false);
        switchCompattoggle.setChecked(isTakeSelfieEnabled);


        containerCustomBottomAppBar.setOnClickListener(v -> {

            List<String> selectedPaths = intruderAdap.getSelectedImagePaths();
            final long MINIMUM_DISPLAY_TIME = 2100; // in milliseconds

            animationManager.handleAnimationProcess(
                    AnimationManager.AnimationType.DELETE,
                    selectedPaths,
                    MINIMUM_DISPLAY_TIME,
                    () -> {
                        // Background task: Move images back to recycle locations
//                        ImgVidFHandle.moveImagesBackToRecycleLocationsWrapper(ImagesHidden.this, selectedPaths);
                        FileUtils.deleteFiles(selectedPaths);
                        // Update processSuccess based on actual task outcome
                    },
                    (processSuccess, paths) -> stopAnimationAndUpdateUI(processSuccess, paths)
            );
        });


        switchCompattoggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Update the SharedPreferences when the switch is toggled
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_TAKE_SELFIE, isChecked);
            editor.apply(); // Save the changes

            String message = isChecked ? "Selfie will be taken" : "Selfie will not be taken";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

            // If the switch is turned on, check for camera permission
            if (isChecked) {

                // Check and request camera permission
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // If the user has denied multiple times, show a different message
                    hasDeniedPermission = sharedPreferences.getBoolean("hasDeniedPermissionn", false);
                    if (hasDeniedPermission && (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA))) {
                        showcamerapermissionDialog(true); // Show dialog with a different message
                    } else {
                        showcamerapermissionDialog(false); // Show normal dialog
                    }
                } else {
                    // Permission already granted, set up the camera
                }
            }
        });


        settingCount.setOnClickListener(v -> showObservationTimeDialog());

        backarrow.setOnClickListener(v -> finish());


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied
                boolean hasDeniedPermission = sharedPreferences.getBoolean("hasDeniedPermissionn", false);
                if (!hasDeniedPermission) {
                    // First denial, just set the flag
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("hasDeniedPermissionn", true);
                    editor.apply();
                }

                // Update switch to OFF if permission is denied
                switchCompattoggle.setChecked(false);

                // Save the denial in SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isCameraPermissionDenied", true);
                editor.apply();

                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onSelectandDeselect_All(boolean isAnySelected) {
        if (isAnySelected) {
            selectandDeselectAll.setVisibility(View.VISIBLE);
        } else {
            selectandDeselectAll.setVisibility(View.GONE);
        }

        setCustomBottomAppBarVisibility(isAnySelected);
    }

    private void handleItemClick(int position) {
        if (intruderAdap.isSelectedAny()) {
            intruderAdap.toggleSelection(position);
        } else {
            Intent intent = new Intent(this, ImageandVideoViewPager.class);
            intent.putStringArrayListExtra("imagePaths", intruderPaths);
            intent.putExtra("position", position);
            intent.putExtra("hideButtons", true); // Pass true to hide buttons
            startActivity(intent);
        }
    }

    private void setCustomBottomAppBarVisibility(boolean visible) {
        if (visible && containerCustomBottomAppBar.getVisibility() != View.VISIBLE) {
            containerCustomBottomAppBar.setTranslationY(containerCustomBottomAppBar.getHeight());
            containerCustomBottomAppBar.setVisibility(View.VISIBLE);
            containerCustomBottomAppBar.animate().translationY(0).setDuration(300).setListener(null);
        } else if (!visible && containerCustomBottomAppBar.getVisibility() == View.VISIBLE) {
            containerCustomBottomAppBar.animate()
                    .translationY(containerCustomBottomAppBar.getHeight())
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            containerCustomBottomAppBar.setVisibility(View.GONE);
                        }
                    });
        }
    }

    private void handleOnBackPressed() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (intruderAdap.isSelectedAny()) {
                    intruderAdap.clearSelection();
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
            // Remove the moved paths from imagePaths
            intruderPaths.removeAll(selectedPaths);
            intruderAdap.notifyDataSetChanged();
            intruderAdap.clearSelection();

            Toast.makeText(Intruder.this, "Images moved back to original locations and deleted from app", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(Intruder.this, "Error moving images back", Toast.LENGTH_SHORT).show();
        }
    }


    // Method to show the custom dialog with NumberPicker
    private void showObservationTimeDialog() {
        // Inflate the custom layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_no_of_intru_selfie, null);

        // Create an AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setView(dialogView);

        NumberPicker numberPicker;

        // Initialize NumberPicker and set values (1-5)
        numberPicker = dialogView.findViewById(R.id.number_picker);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(5);
        numberPicker.setWrapSelectorWheel(false); // Disable wrapping

        // Retrieve the saved value from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int savedValue = sharedPreferences.getInt("selected_number", 3); // Default to 1 if no value is found

        // Set the saved value or default to 1 if not present
        numberPicker.setValue(savedValue);

        // Set the size of the NumberPicker (if needed)
        ViewGroup.LayoutParams layoutParams = numberPicker.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;  // Adjust width
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT; // Adjust height
        numberPicker.setLayoutParams(layoutParams);

        // Find Cancel and Confirm buttons in the inflated layout
        TextView cancelTextView = dialogView.findViewById(R.id.txv_cancel);
        TextView confirmTextView = dialogView.findViewById(R.id.txv_confirm);

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Handle Cancel button click
        cancelTextView.setOnClickListener(v -> dialog.dismiss());

        // Handle Confirm button click
        confirmTextView.setOnClickListener(v -> {
            // Get the selected value from the NumberPicker
            int selectedCount = numberPicker.getValue();

            // Save the selected count in SharedPreferences
            saveLikeCount(selectedCount);

            // Dismiss the dialog
            dialog.dismiss();
        });
    }

    private void showdeleteiconDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_alert_for_all, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setView(dialogView);

        TextView cancelTextView = dialogView.findViewById(R.id.txv_cancel_alert_dialog);
        TextView title = dialogView.findViewById(R.id.custom_alert_Title);
        TextView body = dialogView.findViewById(R.id.custom_alert_body);
        TextView confirmTextView = dialogView.findViewById(R.id.txv_confirm_alert_dialog);

        AlertDialog dialog = builder.create();
        title.setText("Delete All ");
        body.setText("Delelte All Intruder Images ");
        dialog.show();

        cancelTextView.setOnClickListener(v -> dialog.dismiss());

        confirmTextView.setOnClickListener(v -> {
            Runnable deleteAllTask = () -> {
                dialog.dismiss();
                FileUtils.deleteFiles(intruderPaths);
                intruderPaths.clear();
            };

            animationManager.handleAnimationProcess(
                    AnimationManager.AnimationType.DELETE,
                    new ArrayList<>(intruderPaths), // A copy of the list
                    1000, // Minimum display time in milliseconds
                    deleteAllTask,
                    new AnimationManager.AnimationCallback() {
                        @Override
                        public void onProcessComplete(boolean success, List<String> selectedPaths) {
                            if (success) {
                                intruderAdap.notifyDataSetChanged();
                                Toast.makeText(Intruder.this, "All items deleted successfully.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Intruder.this, "Failed to delete items.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            );
        });
    }


    // Update this function to show different messages
    private void showcamerapermissionDialog(boolean isDeniedMultipleTimes) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_alert_for_all, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setView(dialogView);

        TextView cancelTextView = dialogView.findViewById(R.id.txv_cancel_alert_dialog);
        TextView title = dialogView.findViewById(R.id.custom_alert_Title);
        TextView body = dialogView.findViewById(R.id.custom_alert_body);
        TextView confirmTextView = dialogView.findViewById(R.id.txv_confirm_alert_dialog);

        AlertDialog dialog = builder.create();

        if (isDeniedMultipleTimes) {
            // If the user has denied multiple times, change the dialog message
            confirmTextView.setText("Got it");
            title.setText("Grant Permission");
            body.setText("To use camera normally, please grant camera permission. Go to Settings > Permissions > Camera > Allow.");
        } else {
            // Show normal message
            title.setText("Permission");
            body.setText("For taking selfies, please grant camera access.");
        }

        dialog.show();

        cancelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                switchCompattoggle.setChecked(false);
            }
        });

        confirmTextView.setOnClickListener(v -> {
            if (isDeniedMultipleTimes) {
                // If permission is denied multiple times, open the app settings
                String packageName = getPackageName();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
                dialog.dismiss();
            } else {
                // If not denied multiple times, request the camera permission
                dialog.dismiss();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            }
        });

    }

    // Save the selected count in SharedPreferences
    private void saveLikeCount(int count) {
        // Save the selected count in SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("selected_number", count);
        editor.apply();
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // If the user has denied multiple times, show a different message
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("hasDeniedPermission", true);
            editor.apply();
            switchCompattoggle.setChecked(false);
        }

    }
}
