package com.demo.finalcalcihide.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.finalcalcihide.R;
import com.demo.finalcalcihide.ViewModel.VideoModel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SelectVideosfromGalleryAdapter extends RecyclerView.Adapter<SelectVideosfromGalleryAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<String> imagePaths;
    private final OnItemSelectedListener listener;
    private final HashSet<Integer> hashSetselectedItems = new HashSet<>();
    private List<VideoModel> videos;

    // For pagination
    private static final int PAGE_SIZE = 35;
    private List<VideoModel> allVideos = new ArrayList<>();
    private List<VideoModel> displayedVideos = new ArrayList<>();
    private boolean isLoading = false;
    public boolean hasMoreData = true;

    // For caching durations and paths
    private Map<String, String> durationCache = new HashMap<>();
    private Map<Uri, String> pathCache = new HashMap<>();
    private ExecutorService executorService = Executors.newFixedThreadPool(3);
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    public SelectVideosfromGalleryAdapter(Context context, ArrayList<String> imagePaths, OnItemSelectedListener listener, List<VideoModel> videos) {
        this.context = context;
        this.imagePaths = imagePaths;
        this.listener = listener;
        this.allVideos = videos;
        this.videos = new ArrayList<>();

        // Load first page
        loadNextPage();
    }

    public interface OnItemSelectedListener {
        void onItemSelected(int position);
        void onSelectionChanged(boolean isSelected);
    }

    @NonNull
    @Override
    public SelectVideosfromGalleryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Load more data when approaching end
        if (position >= videos.size() - 5 && hasMoreData && !isLoading) {
            loadNextPage();
        }

        if (position >= videos.size()) return;

        VideoModel video = videos.get(position);

        holder.videoDuration.setVisibility(View.VISIBLE);
        holder.videoDuration.setText("--:--"); // Default placeholder

        // Load thumbnail with callback to sync duration loading
        Picasso.get()
                .load(video.getUri())
                .resize(300, 300)
                .centerCrop()
                .placeholder(new ColorDrawable(ContextCompat.getColor(context, R.color.status_bar)))
                .error(R.drawable.baseline_cancel_24)
                .into(holder.imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        // Load duration asynchronously after image loads
                        loadVideoDurationAsync(video.getUri(), holder.videoDuration);
                    }

                    @Override
                    public void onError(Exception e) {
                        holder.videoDuration.setText("00:00");
                    }
                });

        // Handle selection state
        boolean isSelected = hashSetselectedItems.contains(position);
        holder.imageView.setColorFilter(isSelected ? ContextCompat.getColor(context, R.color.overlayColor) : Color.TRANSPARENT, PorterDuff.Mode.SRC_ATOP);
        holder.imageViewTick.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        holder.imageViewUnTick.setVisibility(isSelected ? View.GONE : View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemSelected(position);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            toggleSelection(position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    // Load next page of videos
    private void loadNextPage() {
        if (isLoading || !hasMoreData) return;

        isLoading = true;
        int currentSize = videos.size();
        int endIndex = Math.min(currentSize + PAGE_SIZE, allVideos.size());

        if (currentSize >= allVideos.size()) {
            hasMoreData = false;
            isLoading = false;
            return;
        }

        // Add next batch
        for (int i = currentSize; i < endIndex; i++) {
            videos.add(allVideos.get(i));
        }

        mainHandler.post(() -> {
            notifyItemRangeInserted(currentSize, endIndex - currentSize);
            isLoading = false;

            if (endIndex >= allVideos.size()) {
                hasMoreData = false;
            }
        });
    }

    // Asynchronously load video duration
    private void loadVideoDurationAsync(Uri videoUri, TextView durationTextView) {
        String uriString = videoUri.toString();

        // Check cache first
        if (durationCache.containsKey(uriString)) {
            durationTextView.setText(durationCache.get(uriString));
            return;
        }

        executorService.execute(() -> {
            String duration = getVideoDurationFromUri(videoUri);
            durationCache.put(uriString, duration);

            mainHandler.post(() -> durationTextView.setText(duration));
        });
    }

    // Optimized method to get video duration directly from URI
    private String getVideoDurationFromUri(Uri videoUri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, videoUri);
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (durationStr != null) {
                long durationMs = Long.parseLong(durationStr);
                int minutes = (int) (durationMs / 1000 / 60);
                int seconds = (int) ((durationMs / 1000) % 60);
                return String.format("%02d:%02d", minutes, seconds);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "00:00";
    }

    // Optimized method to get real path (cached)
    private String getRealPathFromURI(Uri uri) {
        if (pathCache.containsKey(uri)) {
            return pathCache.get(uri);
        }

        String path = null;
        if (uri.getScheme().equalsIgnoreCase("content")) {
            String[] projection = {MediaStore.Images.Media.DATA};
            try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    path = cursor.getString(columnIndex);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (uri.getScheme().equalsIgnoreCase("file")) {
            path = uri.getPath();
        }

        String resultPath = path != null ? path : uri.getPath();
        pathCache.put(uri, resultPath);
        return resultPath;
    }




    public void toggleSelection(int position) {
        if (hashSetselectedItems.contains(position)) {
            hashSetselectedItems.remove(position);
        } else {
            hashSetselectedItems.add(position);
        }
        notifyItemChanged(position);
        listener.onSelectionChanged(!hashSetselectedItems.isEmpty());
    }

    public void clearSelection() {
        hashSetselectedItems.clear();
        notifyDataSetChanged();
        listener.onSelectionChanged(false);
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

    public HashSet<Integer> getSelectedItems() {
        return hashSetselectedItems;
    }

    public List<String> getSelectedImagePaths() {
        List<String> selectedPaths = new ArrayList<>();
        for (int position : hashSetselectedItems) {
            if (position < videos.size()) {
                // Get real path for selected video
                String path = getRealPathFromURI(videos.get(position).getUri());
                selectedPaths.add(path);
            }
        }
        return selectedPaths;
    }

    // Method to force load all remaining videos (call this when user selects "Select All")
    public void loadAllVideos() {
        if (hasMoreData) {
            int currentSize = videos.size();
            videos.clear();
            videos.addAll(allVideos);
            notifyItemRangeInserted(currentSize, allVideos.size() - currentSize);
            hasMoreData = false;
        }
    }

    // Clean up resources
    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        durationCache.clear();
        pathCache.clear();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView imageViewTick, imageViewUnTick;
        TextView videoDuration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_item_Imageview);
            imageViewTick = itemView.findViewById(R.id.tickMarkImageView);
            imageViewUnTick = itemView.findViewById(R.id.untickMarkImageView);
            videoDuration = itemView.findViewById(R.id.image_item_duration);
        }
    }
}