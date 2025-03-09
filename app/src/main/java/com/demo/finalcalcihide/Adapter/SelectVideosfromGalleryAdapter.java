package com.demo.finalcalcihide.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SelectVideosfromGalleryAdapter extends RecyclerView.Adapter<SelectVideosfromGalleryAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<String> imagePaths;
    private final OnItemSelectedListener listener;
    private final HashSet<Integer> hashSetselectedItems = new HashSet<>();
    private List<VideoModel> videos;

    public SelectVideosfromGalleryAdapter(Context context, ArrayList<String> imagePaths, OnItemSelectedListener listener, List<VideoModel> videos) {
        this.context = context;
        this.imagePaths = imagePaths;
        this.listener = listener;
        this.videos = videos;
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
        VideoModel video = videos.get(position);

        holder.videoDuration.setVisibility(View.VISIBLE);

        // Load thumbnail using Picasso (as per your request to not change this part)
        Picasso.get()
                .load(video.getUri())
                .resize(300, 300)
                .centerCrop()
                .placeholder(R.drawable.reel)
                .error(R.drawable.baseline_cancel_24)
                .into(holder.imageView);

        // Get and set video duration
        String videoPath = getRealPathFromURI(video.getUri()); // Convert URI to file path string
        String videoDuration = getVideoDuration(videoPath);
        holder.videoDuration.setText(videoDuration);

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

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
    }

    // Method to get video duration using MediaMetadataRetriever
    private String getVideoDuration(String videoUri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(videoUri);
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long durationMs = Long.parseLong(durationStr);
            int minutes = (int) (durationMs / 1000 / 60);
            int seconds = (int) ((durationMs / 1000) % 60);
            retriever.release();
            return String.format("%02d:%02d", minutes, seconds);
        } catch (Exception e) {
            e.printStackTrace();
            return "00:00"; // Return "00:00" if duration cannot be retrieved
        }
    }

    private String getRealPathFromURI(Uri uri) {
        String path = null;

        if (uri.getScheme().equalsIgnoreCase("content")) {
            // If URI is a content URI
            String[] projection = {android.provider.MediaStore.Images.Media.DATA};
            try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.DATA);
                    path = cursor.getString(columnIndex);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (uri.getScheme().equalsIgnoreCase("file")) {
            // If URI is a file URI
            path = uri.getPath();
        }

        return path != null ? path : uri.getPath(); // Return the file path or the URI path
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

    // In your SelectImageVideosAdapter
    public HashSet<Integer> getSelectedItems() {
        return hashSetselectedItems;
    }

    public List<String> getSelectedImagePaths() {
        List<String> selectedPaths = new ArrayList<>();
        for (int position : hashSetselectedItems) {
            selectedPaths.add(imagePaths.get(position));
        }
        return selectedPaths;
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
