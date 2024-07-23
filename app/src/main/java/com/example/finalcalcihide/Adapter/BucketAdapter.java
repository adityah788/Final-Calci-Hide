package com.example.finalcalcihide.Adapter;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.finalcalcihide.Activity.ImageVideoBucket;
import com.example.finalcalcihide.Activity.SelectImagesorVideos;
import com.example.finalcalcihide.R;

import java.util.List;

public class BucketAdapter extends RecyclerView.Adapter<BucketAdapter.MyViewHolder> {
    private List<String> bucketNames;
    private List<String> bitmapList;
    private Context context;
    private OnBucketClickListener listener; // Add listener

    public interface OnBucketClickListener {
        void onBucketClick(String bucketName);
    }

    public BucketAdapter(List<String> bucketNames, List<String> bitmapList, Context context, OnBucketClickListener listener) {
        this.bucketNames = bucketNames;
        this.bitmapList = bitmapList;
        this.context = context;
        this.listener = listener; // Initialize listener
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.bucket_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.title.setText(bucketNames.get(position));
        Glide.with(context)
                .load("file://" + bitmapList.get(position))
//                .apply(new RequestOptions().override(300, 300).centerCrop())
                .into(holder.thumbnail);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getBindingAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION && listener != null) {
                    listener.onBucketClick(bucketNames.get(adapterPosition));
                }
            }
        });


    }
    @Override
    public int getItemCount() {
        return bucketNames.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public ImageView thumbnail;

        public MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            thumbnail = view.findViewById(R.id.image);
        }
    }
}
