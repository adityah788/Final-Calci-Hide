package com.example.finalcalcihide.Activity;


import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.bumptech.glide.Glide;
import com.example.finalcalcihide.R;
import com.github.chrisbanes.photoview.PhotoView;

public class SinglePV extends AppCompatActivity {

    PhotoView photoView;
    PlayerView playerView;
    ImageView playIcon;
    ExoPlayer exoPlayer;
    FrameLayout videocontainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewpage_image);

        photoView = findViewById(R.id.pager_image_ImageView_thoda_dikkat);
        playerView = findViewById(R.id.viewPage_image_viewPagevideo_view);
        playIcon = findViewById(R.id.viewPage_image_play_icon);
        videocontainer = findViewById(R.id.viewPage_image_video_container);

        // Retrieve intent extras
        String imagePath = getIntent().getStringExtra("image_path");
        String videoPath = getIntent().getStringExtra("video_path");

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


