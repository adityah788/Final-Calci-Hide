package com.example.finalcalcihide.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalcalcihide.Model.Note;
import com.example.finalcalcihide.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {

    private List<Note> notesList = new ArrayList<>();
    private final OnItemSelectedListener listener;
    private final HashSet<Integer> selectedItems = new HashSet<>();

    public NotesAdapter(List<Note> notesList, OnItemSelectedListener listener) {
        if (notesList != null) {
            this.notesList = notesList;
        }
        this.listener = listener;
    }

    public interface OnItemSelectedListener {
        void onItemSelected(int position, Note note);
        void onSelectionChanged(boolean isSelected);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Note note = notesList.get(position);
        holder.bind(note);

        boolean isSelected = selectedItems.contains(position);
        holder.imageViewTick.setVisibility(isSelected ? View.VISIBLE : View.GONE);


        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemSelected(position, note);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            toggleSelection(position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return notesList != null ? notesList.size() : 0;
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

    public void updateNotes(List<Note> newNotes) {
        if (newNotes != null) {
            this.notesList = newNotes;
        } else {
            this.notesList.clear();
        }
        notifyDataSetChanged();
    }

    public boolean isSelectedAny() {
        return !selectedItems.isEmpty();
    }

    public List<Note> getSelectedNotes() {
        List<Note> selectedNotes = new ArrayList<>();
        for (Integer position : selectedItems) {
            selectedNotes.add(notesList.get(position));
        }
        return selectedNotes;
    }

    public void clearSelection(){
        selectedItems.clear();

    }



    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, bodyTextView, dateTextView;
        View imageViewTick;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.notes_Title);
            bodyTextView = itemView.findViewById(R.id.notes_body);
            dateTextView = itemView.findViewById(R.id.notes_date);
            imageViewTick = itemView.findViewById(R.id.notes_tickMarkImageView);

            // Restrict the text to one line for both title and body
            titleTextView.setMaxLines(1);
            titleTextView.setEllipsize(android.text.TextUtils.TruncateAt.END);

            bodyTextView.setMaxLines(1);
            bodyTextView.setEllipsize(android.text.TextUtils.TruncateAt.END);

        }



        public void bind(Note note) {
            // Set the title and body text
            titleTextView.setText(note.title != null ? note.title : "Untitled");
            bodyTextView.setText(note.content != null ? note.content : "");
            dateTextView.setText(note.date != null ? note.date : "");

            // Check if the title is empty and set the body text as the title if it is
            if (titleTextView.getText().toString().trim().isEmpty()) {
                titleTextView.setText(note.content != null ? note.content : ""); // Move body content to title
                titleTextView.setVisibility(View.VISIBLE);  // Ensure titleTextView is visible
                bodyTextView.setVisibility(View.GONE);  // Hide the bodyTextView
            } else {
                titleTextView.setVisibility(View.VISIBLE);  // Ensure titleTextView is visible
                bodyTextView.setVisibility(View.VISIBLE);  // Ensure bodyTextView is visible
            }
        }

    }


}
