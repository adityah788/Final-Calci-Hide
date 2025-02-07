package com.demo.finalcalcihide.Adapter;
;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.demo.finalcalcihide.FileUtils.ImgVidFHandle;
import com.demo.finalcalcihide.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class RecyclebinAdapter extends RecyclerView.Adapter<RecyclebinAdapter.ViewHolder> {


    private final Context context;
    private final ArrayList<String> imagePaths;
    private final OnItemSelectedListener listener;
    private final HashSet<Integer> hashSetselectedItems = new HashSet<>();


    public RecyclebinAdapter(Context context, ArrayList<String> imagePaths, OnItemSelectedListener listener) {
        this.context = context;
        this.imagePaths = imagePaths;
        this.listener = listener;
    }

    public interface OnItemSelectedListener {
        void onItemSelected(int position);
        void onSelectionChanged(boolean isSelected);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String filePath = imagePaths.get(position);
        File file = new File(filePath);

        // Use Glide only for image or video files.
        if (isVideoFile(file)) {
            Glide.with(context)
                    .load(file)
                    .into(holder.imageView);
            holder.videoDuration.setVisibility(View.VISIBLE);
            try {
                holder.videoDuration.setText(ImgVidFHandle.getVideoDuration(file));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            holder.imageView.setPadding(0, 0, 0, 0); // Remove padding for video files
        } else if (isDocumentFile(file)) {
            // Set the document icon and hide video duration
            holder.imageView.setImageResource(R.drawable.pdf_fla);
            holder.videoDuration.setVisibility(View.GONE);

            // Add padding of 25dp to the ImageView
            int paddingInPx = (int) (25 * context.getResources().getDisplayMetrics().density); // Convert 20dp to pixels
            holder.imageView.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx);
            // Change the background color to @color/TertiaryPrimarycolor
            int backgroundColor = ContextCompat.getColor(context, R.color.TertiaryPrimarycolor);
            holder.imageView.setBackgroundColor(backgroundColor);
        } else if (isAudioFile(file)) {
            // Set the audio icon and hide video duration
            holder.imageView.setImageResource(R.drawable.audio_file);
            holder.videoDuration.setVisibility(View.GONE);
            int paddingInPx = (int) (25 * context.getResources().getDisplayMetrics().density); // Convert 20dp to pixels
            holder.imageView.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx);
            // Change the background color to @color/TertiaryPrimarycolor
            int backgroundColor = ContextCompat.getColor(context, R.color.TertiaryPrimarycolor);
            holder.imageView.setBackgroundColor(backgroundColor);

        } else {
            // For other file types, you can load a default image or handle accordingly
            Glide.with(context)
                    .load(file)
                    .into(holder.imageView);
            holder.videoDuration.setVisibility(View.GONE);
            holder.imageView.setPadding(0, 0, 0, 0); // Remove padding for other file types
        }

        // Handle item selection
        boolean isSelected = hashSetselectedItems.contains(position);
        holder.imageView.setColorFilter(isSelected ? ContextCompat.getColor(context, R.color.overlayColor) : Color.TRANSPARENT, PorterDuff.Mode.SRC_ATOP);
        holder.imageViewTick.setVisibility(isSelected ? View.VISIBLE : View.GONE);

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


    public boolean isVideoFile(File file) {
        String[] videoExtensions = {".mp4", ".mkv", ".avi", ".mov"};
        return hasExtension(file, videoExtensions);
    }

    public boolean isImageFile(File file) {
        String[] imageExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".tiff", ".webp"};
        return hasExtension(file, imageExtensions);
    }

    private boolean isDocumentFile(File file) {
        String[] documentExtensions = {".pdf", ".doc", ".docx", ".xls", ".xlsx"};
        return hasExtension(file, documentExtensions);
    }

    private boolean isAudioFile(File file) {
        String[] audioExtensions = {".mp3", ".wav", ".aac", ".flac"};
        return hasExtension(file, audioExtensions);
    }

    private boolean hasExtension(File file, String[] extensions) {
        String fileName = file.getName().toLowerCase();
        for (String extension : extensions) {
            if (fileName.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

//    private boolean isVideoFile(File file) {
//        String[] videoExtensions = {".mp4", ".mkv", ".avi", ".mov"};
//        for (String extension : videoExtensions) {
//            if (file.getName().toLowerCase().endsWith(extension)) {
//                return true;
//            }
//        }
//        return false;
//    }


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

    public List<String> getSelectedImagePaths() {
        List<String> selectedPaths = new ArrayList<>();
        for (int position : hashSetselectedItems) {
            selectedPaths.add(imagePaths.get(position));
        }
        return selectedPaths;
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



    public void updateImagePaths(ArrayList<String> newImagePaths) {
        imagePaths.clear();
        imagePaths.addAll(newImagePaths);
        // Clear selections as the data has changed
        hashSetselectedItems.clear();
        notifyDataSetChanged();
        listener.onSelectionChanged(false);
    }

}
