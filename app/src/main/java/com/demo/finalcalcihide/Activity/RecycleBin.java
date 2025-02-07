package com.demo.finalcalcihide.Activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.finalcalcihide.Adapter.RecyclebinAdapter;
import com.demo.finalcalcihide.FileUtils.ImgVidFHandle;
import com.demo.finalcalcihide.GridSpacingItemDecoration;
import com.demo.finalcalcihide.R;
import com.demo.finalcalcihide.Utils.ToolbarManager;
import com.demo.finalcalcihide.Utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RecycleBin extends AppCompatActivity {
    private ArrayList<String> recyclePaths = new ArrayList<>();
    private RecyclebinAdapter recyclebinAdapter;
    private RecyclerView imageRecyclerView;
    private LinearLayout customBottomAppBarDelete;
    private LinearLayout customToolbarContainer;
    private LinearLayout customBottomAppBar;
    private LinearLayout customBottomAppBarVisible;
    TextView restoretxt;
    private ToolbarManager toolbarManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycle_bin);

        // Set navigation bar color to black
//        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.FinalPrimaryColor));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.FinalPrimaryColor));


        // Initialize UI components
        imageRecyclerView = findViewById(R.id.recycle_image_gallery_recycler);
        customToolbarContainer = findViewById(R.id.recycle_custom_toolbar_container);
        customBottomAppBar = findViewById(R.id.recycle_custom_bottom_appbar);
        customBottomAppBarVisible = findViewById(R.id.custom_btm_appbar_Visible);
        customBottomAppBarDelete = findViewById(R.id.custom_btm_appbar_delete);
        restoretxt = findViewById(R.id.custom_btm_appbar_ori_txtV); // Ensure this ID is correct

        // Check if restoretxt is not null
        if (restoretxt != null) {
            restoretxt.setText("Restore");
        } else {
            Toast.makeText(this, "TextView not found!", Toast.LENGTH_SHORT).show();
        }

        recyclePaths = FileUtils.getRecyclePaths(this);

        // Initialize Adapter
        recyclebinAdapter = new RecyclebinAdapter(this, recyclePaths, new RecyclebinAdapter.OnItemSelectedListener() {
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
        toolbarManager = new ToolbarManager(this, customToolbarContainer, recyclebinAdapter, recyclePaths, this,"Recyclebin");

        // Initialize Toolbar and Back Press Handling
        toolbarManager.setToolbarMenu(false);


//        toolbarManager.setTitle("Recyclebin");

        // Setup RecyclerView
        imageRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        int spacing = getResources().getDimensionPixelSize(R.dimen.recycler_item_spacing);
        imageRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3, spacing));
        imageRecyclerView.setAdapter(recyclebinAdapter);


        handleOnBackPressed();

        // Handle Show/Hide Button Click
        customBottomAppBarVisible.setOnClickListener(v -> {
            List<String> selectedPaths = recyclebinAdapter.getSelectedImagePaths();
            ImgVidFHandle.restoredatatoImageorVideo(RecycleBin.this, selectedPaths);
            recyclePaths.removeAll(selectedPaths);
            recyclebinAdapter.notifyDataSetChanged();
            recyclebinAdapter.clearSelection();
        });

        // Handle Delete Button Click
        customBottomAppBarDelete.setOnClickListener(v -> {
            List<String> selectedPaths = recyclebinAdapter.getSelectedImagePaths();
            ImgVidFHandle.deteleDataPermanentWrapper(RecycleBin.this, selectedPaths);
            recyclePaths.removeAll(selectedPaths);
            recyclebinAdapter.notifyDataSetChanged();
            recyclebinAdapter.clearSelection();
        });

    }

    private void handleItemClick(int position) {
        if (recyclebinAdapter.isSelectedAny()) {
            recyclebinAdapter.toggleSelection(position);
        } else {
            Intent intent = new Intent(this, SinglePV.class);
            String currentPath = recyclePaths.get(position); // Get the current path
            if (recyclebinAdapter.isVideoFile(new File(currentPath))) {

                intent.putExtra("videopath", currentPath); // Pass only the current path
                startActivity(intent);

            } else if (recyclebinAdapter.isImageFile(new File(currentPath))) {
                intent.putExtra("imagePath", currentPath); // Pass only the current path
                startActivity(intent);


            }

//            intent.putStringArrayListExtra("imagePath", recyclePaths);
//            intent.putExtra("position", position);
//            startActivity(intent);
        }
    }

    private void onSelectandDeselect_All(boolean isAnySelected) {
        toolbarManager.setToolbarMenu(isAnySelected);
        setCustomBottomAppBarVisibility(isAnySelected);
    }

    private void setCustomBottomAppBarVisibility(boolean visible) {
        if (visible && customBottomAppBar.getVisibility() != View.VISIBLE) {
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
        }
    }

    private void handleOnBackPressed() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (recyclebinAdapter.isSelectedAny()) {
                    recyclebinAdapter.clearSelection();
                    onSelectandDeselect_All(false);
                } else {
                    finish();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshImageList();
    }

    private void refreshImageList() {
        ArrayList<String> updatedVideoPaths = FileUtils.getRecyclePaths(this);
        recyclebinAdapter.updateImagePaths(updatedVideoPaths);
    }
}
