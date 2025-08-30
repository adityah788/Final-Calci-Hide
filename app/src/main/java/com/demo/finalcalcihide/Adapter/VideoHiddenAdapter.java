package com.demo.finalcalcihide.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.demo.finalcalcihide.FileUtils.ImgVidFHandle;
import com.demo.finalcalcihide.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VideoHiddenAdapter extends RecyclerView.Adapter<VideoHiddenAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<String> imagePaths;
    private final OnItemSelectedListener listener;
    private final HashSet<Integer> hashSetselectedItems = new HashSet<>();

    // For efficient video duration loading
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);
    private final ConcurrentHashMap<String, String> durationCache = new ConcurrentHashMap<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Glide request options for better performance
    private final RequestOptions glideOptions;

    public VideoHiddenAdapter(Context context, ArrayList<String> imagePaths, OnItemSelectedListener listener) {
        this.context = context;
        this.imagePaths = imagePaths;
        this.listener = listener;

        // Setup optimized Glide options
        glideOptions = new RequestOptions()
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .skipMemoryCache(false)
                .placeholder(new ColorDrawable(ContextCompat.getColor(context, R.color.status_bar)))
                .error(new ColorDrawable(ContextCompat.getColor(context, R.color.status_bar)));
    }

    public interface OnItemSelectedListener {
        void onItemSelected(int position);
        void onSelectionChanged(boolean isSelected);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup holder, int viewType) {
        View view = LayoutInflater.from(holder.getContext()).inflate(R.layout.image_item, holder, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imagePath = imagePaths.get(position);
        File file = new File(imagePath);

        holder.videoDuration.setVisibility(View.VISIBLE);

        // Load thumbnail with optimized settings
        Glide.with(context)
                .load(file)
                .apply(glideOptions)
                .into(holder.imageView);

        // Load video duration efficiently
        loadVideoDuration(holder, imagePath, file);

        // Handle selection state
        boolean isSelected = hashSetselectedItems.contains(position);
        holder.imageView.setColorFilter(
                isSelected ? ContextCompat.getColor(context, R.color.overlayColor) : Color.TRANSPARENT,
                PorterDuff.Mode.SRC_ATOP
        );
        holder.imageViewTick.setVisibility(isSelected ? View.VISIBLE : View.GONE);

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemSelected(holder.getAdapterPosition());
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                toggleSelection(currentPosition);
            }
            return true;
        });
    }

    private void loadVideoDuration(@NonNull ViewHolder holder, String imagePath, File file) {
        // Check cache first
        if (durationCache.containsKey(imagePath)) {
            holder.videoDuration.setText(durationCache.get(imagePath));
            return;
        }

        // Show loading state
        holder.videoDuration.setText("--:--");

        // Load duration in background
        executorService.execute(() -> {
            try {
                String duration = ImgVidFHandle.getVideoDuration(file);

                // Cache the result
                durationCache.put(imagePath, duration);

                // Update UI on main thread
                mainHandler.post(() -> {
                    // Make sure the view is still bound to the same item
                    int currentPosition = holder.getAdapterPosition();
                    if (currentPosition != RecyclerView.NO_POSITION &&
                            currentPosition < imagePaths.size() &&
                            imagePaths.get(currentPosition).equals(imagePath)) {
                        holder.videoDuration.setText(duration);
                    }
                });

            } catch (IOException e) {
                // Handle error - show default duration
                mainHandler.post(() -> {
                    int currentPosition = holder.getAdapterPosition();
                    if (currentPosition != RecyclerView.NO_POSITION &&
                            currentPosition < imagePaths.size() &&
                            imagePaths.get(currentPosition).equals(imagePath)) {
                        holder.videoDuration.setText("00:00");
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        // Clear Glide to prevent loading issues
        Glide.with(context).clear(holder.imageView);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        // Clean up executor service
        executorService.shutdown();
    }

    public void toggleSelection(int position) {
        if (position < 0 || position >= imagePaths.size()) return;

        if (hashSetselectedItems.contains(position)) {
            hashSetselectedItems.remove(position);
        } else {
            hashSetselectedItems.add(position);
        }
        notifyItemChanged(position);
        listener.onSelectionChanged(!hashSetselectedItems.isEmpty());
    }

    public void clearSelection() {
        if (!hashSetselectedItems.isEmpty()) {
            hashSetselectedItems.clear();
            notifyDataSetChanged();
            listener.onSelectionChanged(false);
        }
    }

    public void selectAll(boolean selectAll) {
        hashSetselectedItems.clear();
        if (selectAll) {
            for (int i = 0; i < getItemCount(); i++) {
                hashSetselectedItems.add(i);
            }
        }
        notifyDataSetChanged();
        listener.onSelectionChanged(!hashSetselectedItems.isEmpty());
    }

    public int getSelectedItemCount() {
        return hashSetselectedItems.size();
    }

    public boolean isSelectedAny() {
        return !hashSetselectedItems.isEmpty();
    }

    public List<String> getSelectedImagePaths() {
        List<String> selectedPaths = new ArrayList<>();
        for (int position : hashSetselectedItems) {
            if (position < imagePaths.size()) {
                selectedPaths.add(imagePaths.get(position));
            }
        }
        return selectedPaths;
    }

    public void updateImagePaths(ArrayList<String> newImagePaths) {
        imagePaths.clear();
        imagePaths.addAll(newImagePaths);
        // Clear selections and cache as the data has changed
        hashSetselectedItems.clear();
        durationCache.clear();
        notifyDataSetChanged();
        listener.onSelectionChanged(false);
    }

    // Method to add new items (for pagination)
    public void addImagePaths(List<String> newPaths) {
        int oldSize = imagePaths.size();
        imagePaths.addAll(newPaths);
        notifyItemRangeInserted(oldSize, newPaths.size());
    }

    // Method to clear cache manually if needed
    public void clearDurationCache() {
        durationCache.clear();
    }

    // Get cache size for debugging
    public int getDurationCacheSize() {
        return durationCache.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView imageViewTick;
        TextView videoDuration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_item_Imageview);
            imageViewTick = itemView.findViewById(R.id.tickMarkImageView);
            videoDuration = itemView.findViewById(R.id.image_item_duration);
        }
    }
}