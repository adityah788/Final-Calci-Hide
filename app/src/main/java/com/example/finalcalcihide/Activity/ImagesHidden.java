package com.example.finalcalcihide.Activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalcalcihide.Adapter.ImageVideoHideAdapter;
import com.example.finalcalcihide.R;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.finalcalcihide.GridSpacingItemDecoration;
import com.example.finalcalcihide.PermissionHandler;
import com.example.finalcalcihide.FileUtils.ImgVidFHandle;
import com.example.finalcalcihide.ViewPager.ImageandVideoViewPager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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

    private LottieAnimationView lottieHideUnhideAnimation;
    private LottieAnimationView lottieDeleteAnimation;
//    View view;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_images_hidden);

        // Set navigation bar color to black
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.navigation_bar_color));


        // Inflate the custom layout containing the Lottie animations
        LayoutInflater inflater = LayoutInflater.from(this);
        View customLayout = inflater.inflate(R.layout.animation, null);

        // Initialize the LottieAnimationView from the inflated layout
        lottieHideUnhideAnimation = customLayout.findViewById(R.id.ani_hide_unhide);
        lottieDeleteAnimation = customLayout.findViewById(R.id.ani_delete);


        PermissionHandler.requestPermissions(ImagesHidden.this);


        imageRecyclerView = findViewById(R.id.image_gallery_recycler);
//        fab_container = findViewById(R.id.fab_add);
        fab_container = findViewById(R.id.image_gallary_fab_container);
        customToolbarContainer = findViewById(R.id.custom_toolbar_container);
        customBottomAppBar = findViewById(R.id.custom_bottom_appbar);
        customBottomAppBarVisible = findViewById(R.id.custom_btm_appbar_Visible);
        customBottomAppBarDelete = findViewById(R.id.custom_btm_appbar_delete);
        imagePaths = loadImagePaths();


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


        imageRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        int spacing = getResources().getDimensionPixelSize(R.dimen.recycler_item_spacing);
        imageRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3, spacing));


        imageRecyclerView.setAdapter(imageVideoHideAdapter);

        setToolbarMenu(false);
        handleOnBackPressed();

        customBottomAppBarVisible.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<String> selectedPaths = imageVideoHideAdapter.getSelectedImagePaths();


                if (ImgVidFHandle.moveImagesBackToOriginalLocationsWrapper(ImagesHidden.this, selectedPaths)) {
                    // Remove the moved paths from imagePaths

//                    view.setVisibility(View.VISIBLE);
//                    lottieDeleteAnimation.setVisibility(View.VISIBLE);
//                    lottieDeleteAnimation.playAnimation();

                    // neche wala on krna hai

                    imagePaths.removeAll(selectedPaths);
                    imageVideoHideAdapter.notifyDataSetChanged();
                    imageVideoHideAdapter.clearSelection();

                    Toast.makeText(ImagesHidden.this, "Images moved back to original locations and deleted from app", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ImagesHidden.this, "Error moving images back", Toast.LENGTH_SHORT).show();
                }
            }
        });


        fab_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(ImagesHidden.this,ImageVideoBucket.class));

            }
        });


        customBottomAppBarDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ImagesHidden.this, "Delete ho gya", Toast.LENGTH_SHORT).show();

            }
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

    private ArrayList<String> loadImagePaths() {
        ArrayList<String> arrayList = new ArrayList<>();
        File storageDir = new File(getFilesDir(), ".dont_delete_me_by_hides/images");
        if (storageDir.exists()) {
            File[] files = storageDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        arrayList.add(file.getAbsolutePath());
                    }
                }
            }
        }
        Collections.sort(arrayList, Collections.reverseOrder());
        return arrayList;
    }


    private void onSelectandDeselect_All(boolean isAnyselected) {
        setToolbarMenu(isAnyselected);
        setCustomBottomAppBarVisibility(isAnyselected);
    }


    private void setToolbarMenu(boolean isAnyselected) {
        customToolbarContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        View customToolbar = inflater.inflate(
                isAnyselected ? R.layout.contextual_toolbar : R.layout.main_toolbar,
                customToolbarContainer,
                false
        );

        customToolbarContainer.addView(customToolbar);

        if (isAnyselected) {
            ImageView selectDeselectAll = customToolbar.findViewById(R.id.contextual_toolbar_select_and_deselect_all);
            selectDeselectAll.setOnClickListener(v -> toggleSelectDeselectAll());

            ImageView cutIcon = customToolbar.findViewById(R.id.contextual_toolbar_cutt);
            cutIcon.setOnClickListener(v -> {
                imageVideoHideAdapter.clearSelection();
                onSelectandDeselect_All(false);
            });

            TextView itemCountText = customToolbar.findViewById(R.id.item_count_text);
            itemCountText.setOnClickListener(v -> updateItemCountText());
            updateItemCountText();
        } else {
            ImageView menuIcon = customToolbar.findViewById(R.id.main_toobar_menu_icon);
            if (menuIcon != null) {
                menuIcon.setOnClickListener(v -> showPopupMenu(menuIcon));
            }
        }
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


    private void toggleSelectDeselectAll() {
        boolean selectAll = imageVideoHideAdapter.getSelectedItemCount() < imageVideoHideAdapter.getItemCount();
        imageVideoHideAdapter.selectAll(selectAll);
        updateSelectDeselectAllIcon(selectAll);
        updateItemCountText();
    }

    private void updateSelectDeselectAllIcon(boolean selectAll) {
        View customToolbar = customToolbarContainer.getChildAt(0);
        if (customToolbar != null) {
            ImageView selectDeselectAll = customToolbar.findViewById(R.id.contextual_toolbar_select_and_deselect_all);
            if (selectDeselectAll != null) {
                selectDeselectAll.setImageResource(selectAll ? R.drawable.baseline_library_add_check_24 : R.drawable.baseline_check_box_outline_blank_24);
            }
        }
    }


    private void updateItemCountText() {
        View customToolbar = customToolbarContainer.getChildAt(0);
        if (customToolbar != null) {
            TextView itemCountText = customToolbar.findViewById(R.id.item_count_text);
            if (itemCountText != null) {
                int selectedCount = imageVideoHideAdapter.getSelectedItemCount();
                int totalCount = imageVideoHideAdapter.getItemCount();
                if (selectedCount == totalCount) {
                    updateSelectDeselectAllIcon(true);
                }
                itemCountText.setText(getString(R.string.item_count, selectedCount, totalCount));
            }
        }
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.toolbar_menu);
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.dateadded) {
                Toast.makeText(ImagesHidden.this, "Date Added clicked", Toast.LENGTH_SHORT).show();
                return true;
            } else if (item.getItemId() == R.id.namee) {
                Toast.makeText(ImagesHidden.this, "Name clicked", Toast.LENGTH_SHORT).show();
                return true;
            } else {
                return false;
            }
        });
        popupMenu.show();
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

}