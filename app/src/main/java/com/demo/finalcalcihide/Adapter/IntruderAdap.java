package com.demo.finalcalcihide.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.demo.finalcalcihide.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class IntruderAdap extends RecyclerView.Adapter<IntruderAdap.ViewHolder> {

    private final Context context;
    private final ArrayList<String> imagePaths;
    private final OnItemSelectedListener listener;
    private final HashSet<Integer> selectedItems = new HashSet<>();

    public IntruderAdap(Context context, ArrayList<String> imagePaths, OnItemSelectedListener listener) {
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.intruder_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imagePath = imagePaths.get(position);
        File file = new File(imagePath);

        if (file.exists()) {
            Glide.with(context)
                    .load(file)
                    .into(holder.imageView);
        } else {
            // Handle the case where the file does not exist
            holder.imageView.setImageDrawable(null); // Or set a placeholder/error image
        }
        // Format and display date and time
        // Assuming that the file has a corresponding date and time for simplicity
        long lastModified = file.lastModified();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        holder.txtDate.setText(dateFormat.format(lastModified));
        holder.txtTime.setText(timeFormat.format(lastModified));

        boolean isSelected = selectedItems.contains(position);
//        holder.imageView.setColorFilter(isSelected ? ContextCompat.getColor(context, R.color.overlayColor) : Color.TRANSPARENT, PorterDuff.Mode.SRC_ATOP);
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

    public void toggleSelection(int position) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position);
        } else {
            selectedItems.add(position);
        }
        notifyItemChanged(position);
        listener.onSelectionChanged(!selectedItems.isEmpty());
    }

    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
        listener.onSelectionChanged(false);
    }

    public void selectAll(boolean selectAll) {
        selectedItems.clear();
        if (selectAll) {
            for (int i = 0; i < getItemCount(); i++) {
                selectedItems.add(i);
            }
        }
        notifyDataSetChanged();
        listener.onSelectionChanged(!selectedItems.isEmpty());
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public boolean isSelectedAny() {
        return !selectedItems.isEmpty();
    }

    public List<String> getSelectedImagePaths() {
        List<String> selectedPaths = new ArrayList<>();
        for (int position : selectedItems) {
            selectedPaths.add(imagePaths.get(position));
        }
        return selectedPaths;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView, imageViewTick;
        TextView txtDate, txtTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.intruder_selfiee);
            imageViewTick = itemView.findViewById(R.id.intruder_tickMarkImageView);
            txtDate = itemView.findViewById(R.id.intruder_date);
            txtTime = itemView.findViewById(R.id.intruder_time);
        }
    }
}
