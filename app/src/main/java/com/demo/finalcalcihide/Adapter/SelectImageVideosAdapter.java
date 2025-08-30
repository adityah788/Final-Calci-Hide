package com.demo.finalcalcihide.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import com.demo.finalcalcihide.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SelectImageVideosAdapter extends RecyclerView.Adapter<SelectImageVideosAdapter.ViewHolder> {


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
                .placeholder(new ColorDrawable(ContextCompat.getColor(context, R.color.status_bar)))
                .into(holder.imageView);


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
        return imagePaths.size();
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
        ImageView imageViewTick,imageViewUnTick;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_item_Imageview);
            imageViewTick = itemView.findViewById(R.id.tickMarkImageView);
            imageViewUnTick = itemView.findViewById(R.id.untickMarkImageView);

        }
    }

}
