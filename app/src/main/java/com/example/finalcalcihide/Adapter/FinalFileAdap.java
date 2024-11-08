package com.example.finalcalcihide.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalcalcihide.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public
class FinalFileAdap extends RecyclerView.Adapter<FinalFileAdap.ViewHolder> {

    private final Context context;
    private final ArrayList<String> filePaths;
    private final FinalFileAdap.OnItemSelectedListener listener;
    private final HashSet<Integer> hashSetselectedItems = new HashSet<>();

    public FinalFileAdap(Context context, ArrayList<String> filePaths, FinalFileAdap.OnItemSelectedListener listener) {
        this.context = context;
        this.filePaths = filePaths;
        this.listener = listener;
    }


    public interface OnItemSelectedListener {
        void onItemSelected(int position);
        void onSelectionChanged(boolean isSelected);
    }


    @NonNull
    @Override
    public FinalFileAdap.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return filePaths.size();
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
            selectedPaths.add(filePaths.get(position));
        }
        return selectedPaths;
    }





    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }


    public void updateImagePaths(ArrayList<String> newImagePaths) {
//        filePaths.clear();
//        filePaths.addAll(newImagePaths);
//        // Clear selections as the data has changed
//        hashSetselectedItems.clear();
//        notifyDataSetChanged();
//        listener.onSelectionChanged(false);
    }
}
