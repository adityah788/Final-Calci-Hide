package com.example.finalcalcihide.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.finalcalcihide.R;

import java.util.List;

public class BucketAdapter extends RecyclerView.Adapter<BucketAdapter.MyViewHolder> {
    private List<String> bucketNames;
    private List<String> bitmapList;
    private List<Integer> imageCounts; // Add a list of image counts
    private Context context;
    private OnBucketClickListener listener;

    public interface OnBucketClickListener {
        void onBucketClick(String bucketName);
    }

    public BucketAdapter(List<String> bucketNames, List<String> bitmapList, List<Integer> imageCounts, Context context, OnBucketClickListener listener) {
        this.bucketNames = bucketNames;
        this.bitmapList = bitmapList;
        this.imageCounts = imageCounts; // Initialize image counts
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.bucket_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.title.setText( bucketNames.get(position));

        // Load the thumbnail image using Glide
        Glide.with(context)
                .load("file://" + bitmapList.get(position))
                .into(holder.thumbnail);

        // Set the image count
        holder.titleSize.setText("("+String.valueOf(imageCounts.get(position))+")");


        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && listener != null) {
                listener.onBucketClick(bucketNames.get(adapterPosition));
            }
        });
    }

    @Override
    public int getItemCount() {
        return bucketNames.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, titleSize;
        public ImageView thumbnail;

        public MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            titleSize = view.findViewById(R.id.title_no);
            thumbnail = view.findViewById(R.id.image);
        }
    }
}
