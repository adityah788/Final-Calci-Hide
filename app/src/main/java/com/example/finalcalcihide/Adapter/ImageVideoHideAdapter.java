package com.example.finalcalcihide.Adapter;

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
import com.example.finalcalcihide.FileUtils.ImgVidFHandle;
import com.example.finalcalcihide.Listner.OnItemSelectedListener;
import com.example.finalcalcihide.R;
import com.example.finalcalcihide.Utils.SelectionManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ImageVideoHideAdapter extends RecyclerView.Adapter<ImageVideoHideAdapter.ViewHolder> {


    private final Context context;
    private final ArrayList<String> imagePaths;
    private final OnItemSelectedListener listener;
    private final HashSet<Integer> hashSetselectedItems = new HashSet<>();


    public ImageVideoHideAdapter(Context context, ArrayList<String> imagePaths, OnItemSelectedListener listener) {
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
        String imagePath = imagePaths.get(position);
        File file = new File(imagePath);

        Glide.with(context)
                .load(file)
                .into(holder.imageView);

        if (isVideoFile(file)) {
            holder.videoDuration.setVisibility(View.VISIBLE);
            // Here you can set the actual video duration if available
            try {
                holder.videoDuration.setText(ImgVidFHandle.getVideoDuration(file));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            holder.videoDuration.setVisibility(View.GONE);
        }

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

    @Override
    public int getItemCount() {
        return imagePaths.size();
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
