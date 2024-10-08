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

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import com.bumptech.glide.request.RequestOptions;
import com.example.finalcalcihide.FileUtils.ImgVidFHandle;
import com.example.finalcalcihide.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SelectImageVideosAdapter extends RecyclerView.Adapter<SelectImageVideosAdapter.ViewHolder> {

//    private List<String> bitmapList;
//    private List<Boolean> selected;
//    private Context context;
//
//    public SelectImageVideosAdapter(List<String> bitmapList, List<Boolean> selected, Context context) {
//        this.bitmapList = bitmapList;
//        this.selected = selected;
//        this.context = context;
//    }
//
//
//    @NonNull
//    @Override
//    public SelectImageVideosAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.select_img_vid_adap_item, parent, false);
//
//        return new MyViewHolder(itemView);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull SelectImageVideosAdapter.MyViewHolder holder, int position) {
//        Glide.with(context).load("file://" + bitmapList.get(position)).apply(new RequestOptions().override(153, 160).centerCrop().dontAnimate().skipMemoryCache(true)).transition(withCrossFade()).into(holder.thumbnail);
//        if (selected.get(position).equals(true)) {
//            holder.check.setVisibility(View.VISIBLE);
//            holder.check.setAlpha(150);
//        } else {
//            holder.check.setVisibility(View.GONE);
//        }
//
//    }
//
//    @Override
//    public int getItemCount() {
//        return 0;
//    }
//
//
//    public class MyViewHolder extends RecyclerView.ViewHolder {
//        public ImageView thumbnail, check;
//
//        public MyViewHolder(View view) {
//            super(view);
//            thumbnail = (ImageView) view.findViewById(R.id.image);
//            check = (ImageView) view.findViewById(R.id.image2);
//        }
//    }


    private final Context context;
    private final ArrayList<String> imagePaths;
    private final OnItemSelectedListener listener;
    private final HashSet<Integer> hashSetselectedItems = new HashSet<>();


    public SelectImageVideosAdapter(Context context, ArrayList<String> imagePaths, OnItemSelectedListener listener) {
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
    public SelectImageVideosAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item, parent, false);
        return new SelectImageVideosAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imagePath = imagePaths.get(position);
        File file = new File(imagePath);

        Glide.with(context)
                .load(file)
                .into(holder.imageView);

        if (isVideoFile(file)) {
            holder.videoIcon.setVisibility(View.VISIBLE);
            holder.videoDuration.setVisibility(View.VISIBLE);
            // Here you can set the actual video duration if available
            try {
                holder.videoDuration.setText(ImgVidFHandle.getVideoDuration(file));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            holder.videoIcon.setVisibility(View.GONE);
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

    private String getVideoDuration(File file) {
        // Placeholder method to get video duration
        // Replace this with actual implementation to get video duration
        return "00:00"; // Default placeholder duration
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
        ImageView imageViewTick, videoIcon;
        TextView videoDuration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_item_Imageview);
            imageViewTick = itemView.findViewById(R.id.tickMarkImageView);
            videoIcon = itemView.findViewById(R.id.image_item_video);
            videoDuration = itemView.findViewById(R.id.image_item_duration);
        }
    }

}
