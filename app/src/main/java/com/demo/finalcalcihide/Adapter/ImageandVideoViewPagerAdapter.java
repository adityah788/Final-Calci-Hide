package com.demo.finalcalcihide.Adapter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.demo.finalcalcihide.R;
import com.demo.finalcalcihide.ViewPager.ImageandVideoViewPager;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.util.ArrayList;

public class ImageandVideoViewPagerAdapter extends RecyclerView.Adapter<ImageandVideoViewPagerAdapter.PagerViewHolder> {

    private final Context context;
    private ArrayList<String> imagePaths;
    private ExoPlayer exoPlayer;
    private int currentlyPlayingPosition = -1;


    public ImageandVideoViewPagerAdapter(Context context, ArrayList<String> imagePaths) {
        this.context = context;
        this.imagePaths = imagePaths;
        this.exoPlayer = new ExoPlayer.Builder(context).build();


    }

    @NonNull
    @Override
    public PagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.viewpager_image_item, parent, false);
        return new PagerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PagerViewHolder holder, int position) {
        String imagePath = imagePaths.get(position);
        File imageFile = new File(imagePath);

        if (!imageFile.exists()) {
            Log.e("ImageViewPagerAdapter", "Image file does not exist: " + imagePath);
            return;
        }

        if (isVideoFile(imageFile)) {
            holder.photoView.setVisibility(View.VISIBLE);
            holder.videoContainer.setVisibility(View.GONE);
            holder.playIcon.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(imageFile)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.color.status_bar)
                    .error(R.drawable.close)
                    .into(holder.photoView);

            holder.playIcon.setOnClickListener(v -> {
                holder.photoView.setVisibility(View.GONE);
                holder.playIcon.setVisibility(View.GONE);
                holder.videoContainer.setVisibility(View.VISIBLE);

                if (currentlyPlayingPosition == position) {
                    if (exoPlayer.isPlaying()) {
                        exoPlayer.pause();
                    } else {
                        exoPlayer.play();
                    }
                } else {
                    playVideo(holder, imagePath, position);
                }
            });

        } else {
            holder.photoView.setVisibility(View.VISIBLE);
            holder.videoContainer.setVisibility(View.GONE);
            holder.playIcon.setVisibility(View.GONE);

            Glide.with(context)
                    .load(imageFile)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.color.status_bar)
                    .error(R.drawable.close)
                    .into(holder.photoView);
        }


        // Preload both next and previous images to reduce lag when swiping
        preloadImage(position);


        // Set click listener to toggle system UI visibility on PhotoView
        holder.photoView.setOnClickListener(view -> ((ImageandVideoViewPager) context).toggleSystemUI());

        // Set click listener to toggle system UI visibility on PlayerView
        holder.playerView.setOnClickListener(view -> ((ImageandVideoViewPager) context).toggleSystemUI());

         }

    private void playVideo(PagerViewHolder holder, String videoPath, int position) {
        exoPlayer.setMediaItem(MediaItem.fromUri(Uri.fromFile(new File(videoPath))));
        exoPlayer.prepare();
        exoPlayer.setPlayWhenReady(true);
        holder.playerView.setPlayer(exoPlayer);
        holder.playIcon.setVisibility(View.GONE);

        // Stop the previously playing video if any
        if (currentlyPlayingPosition != -1 && currentlyPlayingPosition != position) {
            notifyItemChanged(currentlyPlayingPosition);
        }
        currentlyPlayingPosition = position;
    }

    private void preloadImage(int position) {
        if (position < getItemCount() - 1) {
            String nextImagePath = imagePaths.get(position + 1);
            File nextImageFile = new File(nextImagePath);
            if (nextImageFile.exists()) {
                Glide.with(context)
                        .load(nextImageFile)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .preload();
            }
        }

        if (position > 0) {
            String previousImagePath = imagePaths.get(position - 1);
            File previousImageFile = new File(previousImagePath);
            if (previousImageFile.exists()) {
                Glide.with(context)
                        .load(previousImageFile)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .preload();
            }
        }
    }

    private boolean isVideoFile(File file) {
        String[] videoExtensions = {".mp4", ".mkv", ".avi", ".mov"};
        for (String extension : videoExtensions) {
            if (file.getName().toLowerCase().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return imagePaths != null ? imagePaths.size() : 0;
    }

    @Override
    public void onViewRecycled(@NonNull PagerViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.getAdapterPosition() == currentlyPlayingPosition) {
            exoPlayer.stop();
            currentlyPlayingPosition = -1;
        }
    }

    public void releasePlayer() {
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    static class PagerViewHolder extends RecyclerView.ViewHolder {
        PhotoView photoView;
        PlayerView playerView;
        FrameLayout videoContainer;
        ImageView playIcon;


        PagerViewHolder(@NonNull View itemView) {
            super(itemView);
            photoView = itemView.findViewById(R.id.pager_image_ImageView);
            playerView = itemView.findViewById(R.id.video_view);
            videoContainer = itemView.findViewById(R.id.video_container);
            playIcon = itemView.findViewById(R.id.play_icon);

        }
    }


    // Update the adapter's image paths
    public void updateImagePaths(ArrayList<String> newImagePaths) {
            this.imagePaths = newImagePaths;
        notifyDataSetChanged();  // Refresh the adapter when data changes
    }

}
