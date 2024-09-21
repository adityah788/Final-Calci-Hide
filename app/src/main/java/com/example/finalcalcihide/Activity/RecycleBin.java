package com.example.finalcalcihide.Activity;

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

import com.example.finalcalcihide.Adapter.ImageVideoHideAdapter;
import com.example.finalcalcihide.FileUtils.ImgVidFHandle;
import com.example.finalcalcihide.GridSpacingItemDecoration;
import com.example.finalcalcihide.R;
import com.example.finalcalcihide.Utils.ToolbarManager;
import com.example.finalcalcihide.ViewPager.ImageandVideoViewPager;
import com.example.finalcalcihide.Utils.FileUtils;
import com.example.finalcalcihide.PermissionHandler;

import java.util.ArrayList;
import java.util.List;

public class RecycleBin extends AppCompatActivity {
    private ArrayList<String> recyclePaths = new ArrayList<>();
    private ImageVideoHideAdapter imageVideoHideAdapter;
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
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.navigation_bar_color));

        // Request necessary permissions
        PermissionHandler.requestPermissions(RecycleBin.this);

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
        imageVideoHideAdapter = new ImageVideoHideAdapter(this, recyclePaths, new ImageVideoHideAdapter.OnItemSelectedListener() {
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
        toolbarManager = new ToolbarManager(this, customToolbarContainer, imageVideoHideAdapter, recyclePaths, this);

        // Setup RecyclerView
        imageRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        int spacing = getResources().getDimensionPixelSize(R.dimen.recycler_item_spacing);
        imageRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3, spacing));
        imageRecyclerView.setAdapter(imageVideoHideAdapter);

        // Initialize Toolbar and Back Press Handling
        toolbarManager.setToolbarMenu(false);
        handleOnBackPressed();

        // Handle Show/Hide Button Click
        customBottomAppBarVisible.setOnClickListener(v -> {
            List<String> selectedPaths = imageVideoHideAdapter.getSelectedImagePaths();
            ImgVidFHandle.restoredatatoImageorVideo(RecycleBin.this, selectedPaths);
            recyclePaths.removeAll(selectedPaths);
            imageVideoHideAdapter.notifyDataSetChanged();
            imageVideoHideAdapter.clearSelection();
        });

        // Handle Delete Button Click
        customBottomAppBarDelete.setOnClickListener(v -> {
            List<String> selectedPaths = imageVideoHideAdapter.getSelectedImagePaths();
            ImgVidFHandle.deteleDataPermanentWrapper(RecycleBin.this, selectedPaths);
            recyclePaths.removeAll(selectedPaths);
            imageVideoHideAdapter.notifyDataSetChanged();
            imageVideoHideAdapter.clearSelection();
        });

    }

    private void handleItemClick(int position) {
        if (imageVideoHideAdapter.isSelectedAny()) {
            imageVideoHideAdapter.toggleSelection(position);
        } else {
            Intent intent = new Intent(this, ImageandVideoViewPager.class);
            intent.putStringArrayListExtra("imagePaths", recyclePaths);
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

    @Override
    protected void onResume() {
        super.onResume();
        refreshImageList();
    }

    private void refreshImageList() {
        ArrayList<String> updatedVideoPaths = FileUtils.getRecyclePaths(this);
        imageVideoHideAdapter.updateImagePaths(updatedVideoPaths);
    }
}
