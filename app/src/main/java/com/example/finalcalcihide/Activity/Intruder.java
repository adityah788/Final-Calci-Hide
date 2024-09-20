package com.example.finalcalcihide.Activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalcalcihide.Adapter.IntruderAdap;
import com.example.finalcalcihide.FileUtils.ImgVidFHandle;
import com.example.finalcalcihide.R;
import com.example.finalcalcihide.Utils.AnimationManager;
import com.example.finalcalcihide.Utils.FileUtils;
import com.example.finalcalcihide.ViewPager.ImageandVideoViewPager;

import java.util.ArrayList;
import java.util.List;

public class Intruder extends AppCompatActivity {

    private ArrayList<String> intruderPaths = new ArrayList<>();
    private RecyclerView intruderRecyclerView;
    private LinearLayout customToolbarContainer;
    private IntruderAdap intruderAdap;
    private LinearLayout containerCustomBottomAppBar;
    private ImageView selectandDeselectAll, deleteIcon;
    private boolean isAllSelected = false; // To keep track of selection state

    private AnimationManager animationManager;
    private FrameLayout animationContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intruder);

        intruderRecyclerView = findViewById(R.id.intruder_selfie_gallery_recycler);
        customToolbarContainer = findViewById(R.id.intruder_custom_toolbar_container);
        containerCustomBottomAppBar = findViewById(R.id.container_custom_bottom_appbar_delete);
        selectandDeselectAll = findViewById(R.id._intruder_contextual_toolbar_select_and_deselect_all);
        deleteIcon = findViewById(R.id.intruder_main_toobar_menu_icon);
        animationContainer = findViewById(R.id.intruder_animation_container);

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
                } else {
                    deleteIcon.setVisibility(View.VISIBLE);  // Show menu icon
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

                // Show confirmation dialog
                new AlertDialog.Builder(Intruder.this)
                        .setTitle("Delete All")
                        .setMessage("Do you want to delete all items?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // User confirmed deletion of all items
                            // Define the process task: delete all items
                            Runnable deleteAllTask = () -> {
                                // Delete all files from storage
                                FileUtils.deleteFiles(intruderPaths);
                                // Clear the intruderPaths list
                                intruderPaths.clear();
                            };

                            // Handle the animation and deletion process
                            animationManager.handleAnimationProcess(
                                    AnimationManager.AnimationType.DELETE,
                                    new ArrayList<>(intruderPaths), // Pass a copy of the list
                                    1000, // Minimum display time in milliseconds
                                    deleteAllTask,
                                    new AnimationManager.AnimationCallback() {
                                        @Override
                                        public void onProcessComplete(boolean success, List<String> selectedPaths) {
                                            if (success) {
                                                // Notify the adapter about data changes
                                                intruderAdap.notifyDataSetChanged();
                                                // Optionally, show a success message
                                                Toast.makeText(Intruder.this, "All items deleted successfully.", Toast.LENGTH_SHORT).show();
                                            } else {
                                                // Handle failure (optional)
                                                Toast.makeText(Intruder.this, "Failed to delete items.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                            );
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

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
}
