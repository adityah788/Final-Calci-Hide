package com.example.finalcalcihide.Activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.finalcalcihide.GridSpacingItemDecoration;
import com.example.finalcalcihide.PermissionHandler;
import com.example.finalcalcihide.FileUtils.ImgVidFHandle;
import com.example.finalcalcihide.Utils.FileUtils;
import com.example.finalcalcihide.Utils.ToolbarManager;
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

    private ToolbarManager toolbarManager;



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

        fab_container = findViewById(R.id.image_gallary_fab_container);
        customToolbarContainer = findViewById(R.id.custom_toolbar_container);
        customBottomAppBar = findViewById(R.id.custom_bottom_appbar);
        customBottomAppBarVisible = findViewById(R.id.custom_btm_appbar_Visible);
        customBottomAppBarDelete = findViewById(R.id.custom_btm_appbar_delete);
        imagePaths = FileUtils.getImagePaths(this);



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


        toolbarManager = new ToolbarManager(this, customToolbarContainer, imageVideoHideAdapter,imagePaths);


        imageRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        int spacing = getResources().getDimensionPixelSize(R.dimen.recycler_item_spacing);
        imageRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3, spacing));


        imageRecyclerView.setAdapter(imageVideoHideAdapter);

        toolbarManager.setToolbarMenu(false);
        handleOnBackPressed();

        customBottomAppBarVisible.setOnClickListener(v -> {

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
        });

        fab_container.setOnClickListener(v -> startActivity(new Intent(ImagesHidden.this, ImageVideoBucket.class)));

        customBottomAppBarDelete.setOnClickListener(v -> Toast.makeText(ImagesHidden.this, "Delete ho gya", Toast.LENGTH_SHORT).show());

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

    private void onSelectandDeselect_All(boolean isAnyselected) {
        toolbarManager.setToolbarMenu(isAnyselected);
        setCustomBottomAppBarVisibility(isAnyselected);
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

///  350