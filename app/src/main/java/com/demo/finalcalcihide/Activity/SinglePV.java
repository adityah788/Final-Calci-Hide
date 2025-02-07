package com.demo.finalcalcihide.Activity;


import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.bumptech.glide.Glide;
import com.demo.finalcalcihide.FileUtils.ImgVidFHandle;
import com.demo.finalcalcihide.R;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.Collections;
import java.util.List;

public class SinglePV extends AppCompatActivity {

    PhotoView photoView;
    PlayerView playerView;
    ImageView playIcon;
    ExoPlayer exoPlayer;
    FrameLayout videocontainer;
    ImageView delete, visible,back;
    private boolean isSystemUIVisible = true; // Initially visible
    private View toolbar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_p_v_image);

        photoView = findViewById(R.id.p_v_image_ImageView_thoda_dikkat);
        playerView = findViewById(R.id.p_v_viewPage_image_viewPagevideo_view);
        playIcon = findViewById(R.id.p_v_viewPage_image_play_icon);
        videocontainer = findViewById(R.id.p_v_viewPage_image_video_container);
        delete = findViewById(R.id.p_v_img_viewpager_toobar_deleter_icon);
        visible = findViewById(R.id.p_v_img_viewpager_main_toobar_unlock);
        back = findViewById(R.id.p_v_img_viewpager_toolbar_back_arrow);
        toolbar = findViewById(R.id.p_v_image_view_pager_toolbar);

        // Retrieve intent extras
        String imagePath = getIntent().getStringExtra("imagePath");
        String videoPath = getIntent().getStringExtra("videopath");


        // Set system UI visibility flags
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        int semiTransparentBlack = Color.argb(128, 0, 0, 0); // alpha=128 (50% transparency), RGB=0,0,0 (black)

        // Semi-transparent black color for navigation bar
        getWindow().setNavigationBarColor(semiTransparentBlack);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.stausbarsinglepv));



        if (imagePath != null) {
            // Load image using Glide into PhotoView
            videocontainer.setVisibility(View.GONE);
            photoView.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(imagePath)
                    .into(photoView);
            playIcon.setVisibility(View.GONE); // Hide play icon for images
        } else if (videoPath != null) {
            // Initialize ExoPlayer
            photoView.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(videoPath)
                    .into(photoView);
            exoPlayer = new ExoPlayer.Builder(this).build();
            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoPath));
            exoPlayer.setMediaItem(mediaItem);
            exoPlayer.prepare();
            exoPlayer.setPlayWhenReady(false); // Don't autoplay, wait for click on play icon
            playerView.setPlayer(exoPlayer);

            // Show play icon and set click listener to start playback
            playIcon.setVisibility(View.VISIBLE);
            playIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    photoView.setVisibility(View.GONE);
                    videocontainer.setVisibility(View.VISIBLE);
                    exoPlayer.setPlayWhenReady(true); // Start video playback
                    playIcon.setVisibility(View.GONE); // Hide play icon once playback starts
                }
            });
        }


        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imagePath != null) {
                    List<String> selectedPaths = Collections.singletonList(imagePath);
                    ImgVidFHandle.deteleDataPermanentWrapper(SinglePV.this, selectedPaths);
                }
                else {
                    List<String> selectedPaths = Collections.singletonList(videoPath);
                    ImgVidFHandle.deteleDataPermanentWrapper(SinglePV.this, selectedPaths);


                }
                finish();

            }
        });

        visible.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imagePath != null) {
                    List<String> selectedPaths = Collections.singletonList(imagePath);
                    ImgVidFHandle.restoredatatoImageorVideo(SinglePV.this, selectedPaths);
                    Toast.makeText(SinglePV.this, "click hua hai", Toast.LENGTH_SHORT).show();


                }
                else {
                    List<String> selectedPaths = Collections.singletonList(videoPath);
                    ImgVidFHandle.restoredatatoImageorVideo(SinglePV.this, selectedPaths);
                    Toast.makeText(SinglePV.this, "click hua hai visibel", Toast.LENGTH_SHORT).show();

                }
                finish();

            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSystemUI();
            }
        });

        playerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSystemUI();

            }
        });




    }

    public void toggleSystemUI() {
        View decorView = getWindow().getDecorView();
        if (isSystemUIVisible) {
            // Hide system UI
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            toolbar.setVisibility(View.GONE);

        } else {
            // Show system UI
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            toolbar.setVisibility(View.VISIBLE);
        }
        isSystemUIVisible = !isSystemUIVisible;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release ExoPlayer when activity is destroyed
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }
}


